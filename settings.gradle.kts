rootProject.name = "material-sdui"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

// Contrato do JSON (KMP, sem Compose): usado pelo BFF e pelos apps.
include(":contract")
// Motor do renderer (KMP, sem Compose): estado da tela, ligações, ações, carga das telas.
include(":engine")
// Renderer (Compose Multiplatform + material3): desenha cada componente.
include(":renderer")
// BFF (Ktor/JVM): monta o JSON de cada tela.
include(":bff")
// App Android de exemplo (casca fina). O app iOS fica em iosApp/ (Xcode).
include(":androidApp")
