# App iOS

Casca fina em SwiftUI que embute o renderer (`SduiRenderer.framework`, gerado pelo Kotlin/Native). O projeto do Xcode sai do `project.yml` com o [XcodeGen](https://github.com/yonaskolb/XcodeGen).

- Endereço do BFF: `baseUrl` em `iosApp/ContentView.swift` (hoje `https://bff.fabianofranca.com`).
- Primeira tela: `home`, ou a variável `SDUI_START_ROUTE` (o CI usa para as capturas).

## Sem Mac: GitHub Actions

O workflow [`.github/workflows/ios.yml`](../.github/workflows/ios.yml) roda num macOS do GitHub a cada push no branch e também manualmente (**Actions → iOS → Run workflow**). Ele publica estes artefatos na página da execução:

| Artefato             | O que é                                                                    |
| -------------------- | -------------------------------------------------------------------------- |
| `ios-screenshots`    | Capturas do app no Simulador (início, catálogo, ajustes; claro e escuro).  |
| `iosApp-simulator`   | O `.app` para Simulador.                                                   |
| `iosApp-device-ipa`  | O `.ipa` para iPhone, **sem assinatura** (para o Sideloadly).              |
| `xcodebuild-log`     | Os logs do `xcodebuild`, para investigar falhas.                           |

## Instalar no iPhone com o Sideloadly (Windows, Apple ID gratuito)

Funciona sem a conta paga da Apple. Limites do Apple ID gratuito: o app vale por **7 dias** (depois, reinstale) e até 3 apps instalados assim ao mesmo tempo. O app pede iOS 15 ou mais novo.

### No computador (uma vez)

1. Instale o **iTunes** e o **iCloud** baixados do site da Apple, **não** as versões da Microsoft Store (o Sideloadly não funciona com elas):
   - iTunes: <https://www.apple.com/itunes/download/win64>
   - iCloud: <https://support.apple.com/pt-br/103232>
2. Instale o **Sideloadly**: <https://sideloadly.io>.

### Baixar o `.ipa`

1. Abra a última execução verde do workflow **iOS** em <https://github.com/fabianofranca/material-sdui/actions/workflows/ios.yml>.
2. Em **Artifacts**, baixe `iosApp-device-ipa` (é preciso estar logado no GitHub).
3. Descompacte o `.zip`: dentro está o `iosApp-unsigned.ipa`.

### Instalar

1. Ligue o iPhone no computador pelo cabo, desbloqueie e toque em **Confiar** no aviso "Confiar neste computador?".
2. Abra o Sideloadly. O iPhone deve aparecer em **iDevice**.
3. Arraste o `iosApp-unsigned.ipa` para a janela (ou clique no ícone do `.ipa`).
4. Em **Apple account**, digite o seu Apple ID.
5. Clique em **Start**. Digite a senha do Apple ID e, se pedir, o código de verificação de dois fatores.
6. Espere a mensagem **Done**.

Se aparecer um erro sobre o identificador do app (bundle ID) já em uso, abra **Advanced options** e marque **Change bundle ID**; o Sideloadly cria um identificador só seu.

### No iPhone (uma vez)

1. **Modo Desenvolvedor** (iOS 16 ou mais novo): **Ajustes → Privacidade e Segurança → Modo Desenvolvedor** → ligue → **Reiniciar**. Depois que o iPhone ligar, toque em **Ativar** e digite o código do aparelho.
   A opção só aparece depois que um app instalado assim existe no aparelho; se ela não estiver lá, instale primeiro (passo anterior) e volte aqui.
2. **Confiar no desenvolvedor**: **Ajustes → Geral → VPN e Gerenciamento de Dispositivo** → em "App de Desenvolvedor", toque no seu Apple ID → **Confiar em "…"** → **Confiar**.
3. Abra o app **Material SDUI**.

### Depois de 7 dias

O app para de abrir quando o certificado gratuito vence. Repita a seção "Instalar" com o mesmo `.ipa` (ou um mais novo); os dados do app continuam.

## TestFlight (quando houver conta paga)

O workflow já tem o job `testflight`. Ele roda só manualmente: **Actions → iOS → Run workflow**, com a opção **testflight** marcada. Antes:

1. Entre no [Apple Developer Program](https://developer.apple.com/programs/).
2. No portal da Apple, registre o App ID `com.fabianofranca.sdui.ios`, crie um certificado **Apple Distribution** (exporte o `.p12` com senha) e um perfil de provisionamento **App Store** para esse App ID.
3. No [App Store Connect](https://appstoreconnect.apple.com), crie o app com esse bundle ID e uma chave de API em **Users and Access → Integrations** (papel **App Manager**; baixe o `.p8`).
4. No GitHub, em **Settings → Secrets and variables → Actions**, crie os segredos (os arquivos vão em base64: no PowerShell, `[Convert]::ToBase64String([IO.File]::ReadAllBytes("arquivo"))`):

| Segredo                    | Valor                                           |
| -------------------------- | ----------------------------------------------- |
| `APPLE_TEAM_ID`            | o Team ID (10 caracteres, em Membership)        |
| `IOS_DIST_CERT_P12_BASE64` | o `.p12` do certificado Apple Distribution      |
| `IOS_DIST_CERT_PASSWORD`   | a senha do `.p12`                               |
| `IOS_PROFILE_BASE64`       | o `.mobileprovision` (App Store)                |
| `ASC_KEY_ID`               | o Key ID da chave da API                        |
| `ASC_ISSUER_ID`            | o Issuer ID (no topo da página de chaves)       |
| `ASC_KEY_P8_BASE64`        | o arquivo `.p8` da chave                        |

O número do build enviado é o número da execução do workflow, então cada envio é aceito como um build novo.
