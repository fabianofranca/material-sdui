package com.fabianofranca.sdui.android

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.fabianofranca.sdui.contract.ScreenResponse
import com.fabianofranca.sdui.contract.SduiJson
import com.fabianofranca.sdui.engine.HttpOutcome
import com.fabianofranca.sdui.engine.SduiHost
import com.fabianofranca.sdui.engine.SduiNavigator
import com.fabianofranca.sdui.renderer.SduiScreen
import com.fabianofranca.sdui.renderer.SnapshotStateStore
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

// Previews das telas de docs/examples (o JSON real do BFF), no Android Studio ou no terminal:
//   android preview render androidApp --output docs/previews

@Preview(name = "Início", widthDp = 400, heightDp = 860)
@Composable
fun HomePreview() = ExamplePreview("home")

@Preview(name = "Início: Seguindo", widthDp = 400, heightDp = 860)
@Composable
fun HomeFollowingPreview() = ExamplePreview("home", "tab" to JsonPrimitive("following"))

@Preview(name = "Artigo", widthDp = 400, heightDp = 860)
@Composable
fun ArticlePreview() = ExamplePreview("article")

@Preview(name = "Configurações", widthDp = 400, heightDp = 860)
@Composable
fun SettingsPreview() = ExamplePreview("settings")

@Preview(name = "Configurações: escuro", widthDp = 400, heightDp = 860)
@Composable
fun SettingsDarkPreview() = ExamplePreview("settings", dark = true)

@Preview(name = "Catálogo: conteúdo", widthDp = 400, heightDp = 860)
@Composable
fun CatalogContentPreview() = ExamplePreview("catalog")

@Preview(name = "Catálogo: ações", widthDp = 400, heightDp = 860)
@Composable
fun CatalogActionsPreview() = ExamplePreview("catalog", "section" to JsonPrimitive("actions"))

@Preview(name = "Catálogo: formulário", widthDp = 400, heightDp = 860)
@Composable
fun CatalogFormsPreview() = ExamplePreview("catalog", "section" to JsonPrimitive("forms"))

/** Lê `assets/<example>.json` e desenha a tela, com [state] por cima do estado inicial. */
@Composable
private fun ExamplePreview(example: String, vararg state: Pair<String, JsonElement>, dark: Boolean = false) {
    val context = LocalContext.current
    val screen = remember(example) {
        val json = context.assets.open("$example.json").bufferedReader().use { it.readText() }
        SduiJson.decodeFromString(ScreenResponse.serializer(), json).screen
    }
    val store = remember(screen) { SnapshotStateStore(screen.state + state) }
    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        SduiScreen(screen = screen, host = PreviewHost, store = store)
    }
}

private val PreviewHost = SduiHost(
    navigator = object : SduiNavigator {
        override fun navigate(route: String, params: Map<String, JsonElement>, replace: Boolean) = Unit
        override fun back() = Unit
        override fun openDeeplink(uri: String) = Unit
    },
    http = { HttpOutcome(200) },
)
