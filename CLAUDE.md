# CLAUDE.md

Server-Driven UI com Material 3 em Kotlin Multiplatform. Visão geral, módulos e decisões: [README.md](README.md). Contrato do JSON: [docs/CONTRACT.md](docs/CONTRACT.md).

## Ambiente

- Linux novo (ambiente cloud, CI): `bash scripts/setup-cloud-env.sh`. O script instala o Android SDK (API 37) e o Android CLI, e configura um espelho do Maven Central, porque o `repo.maven.apache.org` devolve 429 para IPs de nuvem. Pode rodar mais de uma vez.
- Precisa de acesso a `dl.google.com` (Google Maven e SDK). Sem ele, só `contract`, `engine` e `bff` compilam.
- JDK 17 ou mais novo. O Gradle vem pelo wrapper (9.8.2).
- O app iOS só monta num Mac (Xcode + XcodeGen). No Linux dá para compilar o Kotlin do iOS: `:renderer:compileKotlinIosSimulatorArm64`.

## Comandos

```bash
./gradlew :contract:jvmTest :engine:jvmTest :bff:test     # testes rápidos (JVM)
./gradlew :renderer:compileKotlinIosSimulatorArm64        # o renderer compila para iOS
./gradlew :androidApp:assembleDebug                       # APK
./gradlew :renderer:testAndroidHostTest                   # capturas: renderer/build/outputs/roborazzi/*.png
./gradlew :bff:run                                        # BFF em localhost:8080 (PORT muda a porta)

# Previews do Compose (androidApp/src/debug/.../SduiPreviews.kt) pelo Android CLI:
android preview list androidApp
android preview render androidApp --fqn=com.fabianofranca.sdui.android.SduiPreviewsKt.HomePreview --output build/previews
```

Antes de um commit, rode pelo menos os testes JVM e o `assembleDebug`. Se mexeu no renderer, gere as capturas e olhe as imagens.

## Mapa do código

- `contract/`: os tipos do JSON. Em `Component.kt`, todo componente segue o mesmo molde e repete `id`, `props`, `events`, `visible`, `testTag`, `a11y` e `analytics`. As props ficam em `Props.kt`, as ações em `Action.kt` e a configuração do `Json` (compatibilidade) em `SduiJson.kt`.
- `engine/`: `ScreenController` (ações, camadas, validação, `RequestResult`), `Bindings.kt` (ligações `$state`), `ScreenRepository` (cache, ETag, versão). Não depende do Compose; teste aqui o que for lógica.
- `renderer/`: um arquivo por grupo (`Layout`, `Content`, `Inputs`, `Navigation`, `Modals`). `SduiComponent.kt` despacha por tipo; `SduiApp.kt` cuida da pilha de telas e da carga no BFF.
- `bff/`: `dsl/` (um construtor por componente e por ação), `screens/` (as telas de exemplo), `Application.kt` (rotas).

## Convenções

- Documentação, KDoc, mensagens ao usuário e mensagens de commit em português, frases curtas. Nomes de código em inglês.
- Compatibilidade do contrato: prop nova é sempre opcional e tem padrão; nunca remova nem mude o sentido de algo sem subir `CONTRACT_VERSION`. Tipo, ação, gatilho e valor de enum desconhecidos não podem quebrar a leitura (há testes para isso).
- Prop nullable no contrato com padrão diferente de `null` não sobrevive à ida e volta do JSON (`explicitNulls = false`). Use um tipo não nulo com padrão.
- No renderer, as props do contrato vêm de outro módulo: copie para um `val` local antes de checar `null` (smart cast não funciona).
- Ids únicos na tela: o DSL do BFF falha com id repetido e os testes do BFF conferem todas as telas.

## Para acrescentar um componente

1. `contract`: a classe em `Component.kt`, as props em `Props.kt` e, se tiver filhos ou componentes nas props, `Tree.kt`.
2. `engine`: se for um campo, `fieldBinding`/`rulesOf` em `ScreenController`.
3. `renderer`: o composable e o ramo em `SduiComponent`.
4. `bff`: o construtor em `dsl/Components.kt` e um uso em `screens/CatalogScreen.kt`. O teste `catalogoTemTodosOsComponentesDoMvp` falha se faltar.
5. `docs/CONTRACT.md` e os exemplos: `./gradlew :bff:run` e salve `curl localhost:8080/screens/catalog` em `docs/examples/catalog.json`.
