#!/usr/bin/env bash
# Prepara uma máquina Linux (ambiente cloud do Claude Code, CI) para o projeto:
#   1. Android SDK (cmdline-tools, plataforma, build-tools, platform-tools) e local.properties;
#   2. Android CLI (android preview render, emulator, ...);
#   3. espelho do Maven Central para o Gradle e o Robolectric (o repo.maven.apache.org
#      costuma devolver 429 para IPs compartilhados de nuvem);
#   4. ANDROID_HOME e PATH no perfil do shell.
#
# No ambiente cloud, use no "Setup script" (que começa um diretório acima do repositório):
#   cd material-sdui && bash scripts/setup-cloud-env.sh
#
# Pode rodar quantas vezes quiser: o que já existe é mantido.
#
# Variáveis (opcionais):
#   ANDROID_HOME        onde instalar o SDK (padrão: /opt/android-sdk)
#   SDUI_MAVEN_MIRROR   espelho do Maven Central; vazio desliga (padrão: espelho do Google Cloud)
#   SDUI_WARMUP=1       baixa as dependências e compila tudo no fim (sessões começam mais rápidas)
#
# Hosts usados: dl.google.com, services.gradle.org e o espelho do Maven.
set -euo pipefail

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
MAVEN_MIRROR="${SDUI_MAVEN_MIRROR-https://maven-central.storage-download.googleapis.com/maven2}"
GRADLE_HOME_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}"

# Mantenha alinhado com gradle/libs.versions.toml (android-compileSdk).
SDK_PACKAGES=("platforms;android-37.0" "build-tools;37.0.0" "platform-tools")
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-9862592_latest.zip"
ANDROID_CLI_URL="https://dl.google.com/android/cli/latest/linux_x86_64/android"

log() { echo "[setup] $*"; }

# 1. Android SDK
SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
if [ ! -x "$SDKMANAGER" ]; then
    # O zip publicado é antigo; serve de ponte para o sdkmanager instalar o cmdline-tools atual.
    log "Instalando o Android command-line tools em $ANDROID_HOME"
    tmp="$(mktemp -d)"
    curl -fsSL -o "$tmp/cmdline-tools.zip" "$CMDLINE_TOOLS_URL"
    unzip -q -o "$tmp/cmdline-tools.zip" -d "$tmp"
    (set +o pipefail; yes | "$tmp/cmdline-tools/bin/sdkmanager" --sdk_root="$ANDROID_HOME" --licenses > /dev/null 2>&1 || true)
    "$tmp/cmdline-tools/bin/sdkmanager" --sdk_root="$ANDROID_HOME" --install "cmdline-tools;latest" > /dev/null 2>&1
    rm -rf "$tmp"
fi
log "Aceitando licenças e instalando ${SDK_PACKAGES[*]}"
(set +o pipefail; yes | "$SDKMANAGER" --sdk_root="$ANDROID_HOME" --licenses > /dev/null 2>&1 || true)
"$SDKMANAGER" --sdk_root="$ANDROID_HOME" --install "${SDK_PACKAGES[@]}" > /dev/null
echo "sdk.dir=$ANDROID_HOME" > "$REPO_DIR/local.properties"

# 2. Android CLI
CLI_DIR="$ANDROID_HOME/android-cli"
if [ ! -x "$CLI_DIR/android" ]; then
    log "Instalando o Android CLI"
    mkdir -p "$CLI_DIR"
    curl -fsSL -o "$CLI_DIR/android" "$ANDROID_CLI_URL"
    chmod +x "$CLI_DIR/android"
fi

# 3. Espelho do Maven Central
mkdir -p "$GRADLE_HOME_DIR/init.d"
INIT_SCRIPT="$GRADLE_HOME_DIR/init.d/sdui-maven-mirror.init.gradle.kts"
PROPS="$GRADLE_HOME_DIR/gradle.properties"
touch "$PROPS"
sed -i '/^robolectric\.repo=/d' "$PROPS"
if [ -n "$MAVEN_MIRROR" ]; then
    log "Espelho do Maven Central: $MAVEN_MIRROR"
    cat > "$INIT_SCRIPT" <<GRADLE
// Gerado por scripts/setup-cloud-env.sh. Põe um espelho do Maven Central na frente dos
// repositórios dos builds desta máquina (o repo.maven.apache.org devolve 429 aqui).
fun RepositoryHandler.mirrorFirst() {
    val repo = maven { name = "sduiMavenMirror"; url = uri("$MAVEN_MIRROR") }
    remove(repo)
    add(0, repo)
}
settingsEvaluated {
    pluginManagement.repositories.mirrorFirst()
    dependencyResolutionManagement.repositories.mirrorFirst()
}
GRADLE
    # Lido por renderer/build.gradle.kts: o Robolectric baixa o android-all na hora do teste.
    echo "robolectric.repo=$MAVEN_MIRROR" >> "$PROPS"
else
    rm -f "$INIT_SCRIPT"
fi

# 4. Perfil do shell
PROFILE="$HOME/.bashrc"
if ! grep -q "# sdui-env" "$PROFILE" 2>/dev/null; then
    log "Exportando ANDROID_HOME e PATH em $PROFILE"
    {
        echo ""
        echo "# sdui-env (scripts/setup-cloud-env.sh)"
        echo "export ANDROID_HOME=\"$ANDROID_HOME\""
        echo "export PATH=\"\$PATH:$CLI_DIR:$ANDROID_HOME/platform-tools\""
    } >> "$PROFILE"
fi

# 5. Aquecimento (opcional)
if [ "${SDUI_WARMUP:-0}" = "1" ]; then
    log "Baixando dependências e compilando (SDUI_WARMUP=1)"
    (cd "$REPO_DIR" && ANDROID_HOME="$ANDROID_HOME" ./gradlew --no-daemon -q \
        :contract:jvmTestClasses :engine:jvmTestClasses :bff:testClasses :androidApp:assembleDebug)
fi

log "Pronto. SDK em $ANDROID_HOME; Android CLI em $CLI_DIR/android."
