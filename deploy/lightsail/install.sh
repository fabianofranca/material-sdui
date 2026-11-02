#!/usr/bin/env bash
# Instala ou atualiza o BFF numa instância Ubuntu (Lightsail, EC2, VPS).
#
#   sudo bash install.sh bff-0.1.0.zip bff.seudominio.com
#
# - instala o Java 21 (JRE) e, com domínio, o Caddy (HTTPS automático, Let's Encrypt);
# - põe o BFF em /opt/bff, rodando como o usuário de sistema "bff" num serviço do systemd;
# - o BFF escuta só em 127.0.0.1:8080; quem fica exposto é o Caddy, nas portas 80 e 443;
# - se o Caddy já existe (servindo outros sites), só ACRESCENTA o bloco do BFF ao Caddyfile,
#   depois de guardar uma cópia e validar; os outros sites não mudam;
# - em máquinas com menos de 2 GB e sem swap, cria 1 GB de swap (BFF_SWAP=0 desliga).
#
# Se as portas 80/443 são de outro servidor (nginx, Apache...), rode sem o domínio e
# configure esse servidor como proxy (veja deploy/lightsail/README.md):
#
#   sudo BFF_PORT=8080 bash install.sh bff-0.1.0.zip
#
# Variáveis: BFF_PORT (8080), BFF_HEAP (256m), BFF_SWAP (1).
# Pode rodar de novo com um zip novo para atualizar.
set -euo pipefail

ZIP="${1:?Uso: sudo bash install.sh <bff-x.y.z.zip> [domínio]}"
DOMAIN="${2:-}"
PORT="${BFF_PORT:-8080}"
HEAP="${BFF_HEAP:-256m}"
CADDYFILE=/etc/caddy/Caddyfile
MARK_BEGIN="# >>> material-sdui bff (deploy/lightsail/install.sh)"
MARK_END="# <<< material-sdui bff"

[ "$(id -u)" = "0" ] || { echo "Rode com sudo."; exit 1; }
[ -f "$ZIP" ] || { echo "Não achei $ZIP."; exit 1; }
log() { echo; echo "==> $*"; }

# Outro servidor (que não o Caddy) nas portas 80/443 impede o HTTPS por aqui: para antes de mexer.
if [ -n "$DOMAIN" ]; then
    others="$(ss -ltnpH '( sport = :80 or sport = :443 )' 2>/dev/null | grep -v '"caddy"' || true)"
    if [ -n "$others" ]; then
        echo "As portas 80/443 já estão em uso por outro serviço:"
        echo "$others"
        echo "Rode sem o domínio e configure esse serviço como proxy (deploy/lightsail/README.md)."
        exit 1
    fi
fi
if ss -ltnpH "( sport = :$PORT )" 2>/dev/null | grep -qv '"java"'; then
    echo "A porta $PORT já está em uso. Escolha outra: sudo BFF_PORT=8081 bash install.sh ..."
    exit 1
fi

# Pouca memória e nenhuma swap: 1 GB de swap evita que a JVM seja morta por falta de memória.
if [ "${BFF_SWAP:-1}" = "1" ] && [ -z "$(swapon --show --noheadings)" ] \
    && [ "$(awk '/MemTotal/ {print $2}' /proc/meminfo)" -lt 2000000 ]; then
    log "Criando 1 GB de swap em /swapfile"
    fallocate -l 1G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile > /dev/null
    swapon /swapfile
    grep -q '^/swapfile ' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
fi

log "Instalando Java 21 e unzip"
export DEBIAN_FRONTEND=noninteractive
apt-get update -q
apt-get install -y -q openjdk-21-jre-headless unzip curl

log "Copiando o BFF para /opt/bff"
id bff > /dev/null 2>&1 || useradd --system --no-create-home --shell /usr/sbin/nologin bff
tmp="$(mktemp -d)"
unzip -q "$ZIP" -d "$tmp"
dist="$(find "$tmp" -mindepth 1 -maxdepth 1 -type d | head -n 1)"
[ -x "$dist/bin/bff" ] || { echo "O zip não tem bin/bff. É o zip de ./gradlew :bff:distZip?"; exit 1; }
rm -rf /opt/bff.old
[ -d /opt/bff ] && mv /opt/bff /opt/bff.old
mv "$dist" /opt/bff
rm -rf "$tmp"

log "Configurando o serviço bff (systemd)"
cat > /etc/systemd/system/bff.service <<UNIT
[Unit]
Description=Material SDUI BFF
After=network.target

[Service]
User=bff
Environment=PORT=$PORT
Environment=HOST=127.0.0.1
Environment="JAVA_OPTS=-Xmx$HEAP -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m -Xss512k"
ExecStart=/opt/bff/bin/bff
Restart=always
RestartSec=3
NoNewPrivileges=true
ProtectSystem=strict
ProtectHome=true
PrivateTmp=true

[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable bff > /dev/null
systemctl restart bff

log "Esperando o BFF responder"
for _ in $(seq 1 60); do
    if curl -fsS "http://127.0.0.1:$PORT/health" > /dev/null 2>&1; then break; fi
    sleep 1
done
curl -fsS "http://127.0.0.1:$PORT/health" > /dev/null || {
    echo "O BFF não respondeu. Veja: sudo journalctl -u bff -n 50"; exit 1;
}
echo "BFF no ar em 127.0.0.1:$PORT"

if [ -n "$DOMAIN" ]; then
    fresh_caddy=0
    if ! command -v caddy > /dev/null; then
        log "Instalando o Caddy"
        apt-get install -y -q debian-keyring debian-archive-keyring apt-transport-https gnupg
        curl -1sLf https://dl.cloudsmith.io/public/caddy/stable/gpg.key \
            | gpg --dearmor --yes -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
        curl -1sLf https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt \
            > /etc/apt/sources.list.d/caddy-stable.list
        apt-get update -q
        apt-get install -y -q caddy
        fresh_caddy=1
    fi
    [ -f "$CADDYFILE" ] || { echo "Não achei $CADDYFILE. O Caddy daqui usa outro arquivo de configuração?"; exit 1; }

    log "Configurando HTTPS para $DOMAIN no Caddy"
    backup="$CADDYFILE.bak-$(date +%Y%m%d-%H%M%S)"
    cp "$CADDYFILE" "$backup"
    echo "Cópia do Caddyfile atual: $backup"
    block="$MARK_BEGIN
$DOMAIN {
    encode gzip
    reverse_proxy 127.0.0.1:$PORT
}
$MARK_END"
    if [ "$fresh_caddy" = "1" ]; then
        # Caddy recém-instalado: troca a página padrão pelo BFF.
        printf '%s\n' "$block" > "$CADDYFILE"
    else
        # Caddy que já existia: tira o bloco do BFF de uma instalação anterior e acrescenta o novo.
        sed -i '/^# >>> material-sdui bff/,/^# <<< material-sdui bff/d' "$CADDYFILE"
        printf '\n%s\n' "$block" >> "$CADDYFILE"
    fi
    if ! caddy validate --config "$CADDYFILE" --adapter caddyfile > /tmp/caddy-validate.log 2>&1; then
        cp "$backup" "$CADDYFILE"
        echo "O Caddyfile novo não passou na validação; o original foi restaurado. Detalhes:"
        tail -n 20 /tmp/caddy-validate.log
        exit 1
    fi
    systemctl enable caddy > /dev/null
    if systemctl is-active --quiet caddy; then systemctl reload caddy; else systemctl start caddy; fi
    echo "Teste: curl https://$DOMAIN/health (o certificado pode levar 1 minuto na primeira vez)"
    echo "Para desfazer: sudo cp $backup $CADDYFILE && sudo systemctl reload caddy"
else
    echo "Sem domínio: o BFF só é acessível de dentro da instância."
fi

log "Pronto"
