#!/usr/bin/env bash
# Instala ou atualiza o BFF numa instância Ubuntu (Lightsail, EC2, VPS).
#
#   sudo bash install.sh bff-0.1.0.zip bff.seudominio.com
#
# Se as portas 80/443 já são de outro serviço (nginx, Apache...), rode sem o domínio e
# configure esse serviço como proxy (veja deploy/lightsail/README.md):
#
#   sudo BFF_PORT=8080 bash install.sh bff-0.1.0.zip
#
# - instala o Java 21 (JRE) e, com domínio, o Caddy (HTTPS automático, Let's Encrypt);
# - põe o BFF em /opt/bff, rodando como o usuário de sistema "bff" num serviço do systemd;
# - o BFF escuta só em 127.0.0.1:8080; quem fica exposto é o Caddy, nas portas 80 e 443.
#
# Pode rodar de novo com um zip novo para atualizar.
set -euo pipefail

ZIP="${1:?Uso: sudo bash install.sh <bff-x.y.z.zip> [domínio]}"
DOMAIN="${2:-}"
PORT="${BFF_PORT:-8080}"

[ "$(id -u)" = "0" ] || { echo "Rode com sudo."; exit 1; }
[ -f "$ZIP" ] || { echo "Não achei $ZIP."; exit 1; }
log() { echo; echo "==> $*"; }

# Outro serviço nas portas 80/443 impede o Caddy; melhor parar antes de mexer em algo.
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
Environment=JAVA_OPTS=-Xmx384m
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
    if ! command -v caddy > /dev/null; then
        log "Instalando o Caddy"
        apt-get install -y -q debian-keyring debian-archive-keyring apt-transport-https gnupg
        curl -1sLf https://dl.cloudsmith.io/public/caddy/stable/gpg.key \
            | gpg --dearmor --yes -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
        curl -1sLf https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt \
            > /etc/apt/sources.list.d/caddy-stable.list
        apt-get update -q
        apt-get install -y -q caddy
    fi
    log "Configurando HTTPS para $DOMAIN"
    cat > /etc/caddy/Caddyfile <<CADDY
$DOMAIN {
    encode gzip
    reverse_proxy 127.0.0.1:$PORT
}
CADDY
    systemctl enable caddy > /dev/null
    systemctl restart caddy
    echo "Teste: curl https://$DOMAIN/health (o certificado pode levar 1 minuto na primeira vez)"
else
    echo "Sem domínio: o BFF só é acessível de dentro da instância."
fi

log "Pronto"
