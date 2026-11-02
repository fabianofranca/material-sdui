package com.fabianofranca.sdui.renderer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.fabianofranca.sdui.contract.ScreenResponse
import com.fabianofranca.sdui.contract.SduiJson
import com.fabianofranca.sdui.engine.HttpOutcome
import com.fabianofranca.sdui.engine.SduiHost
import com.fabianofranca.sdui.engine.SduiNavigator
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Desenha as telas de `docs/examples/` (o JSON real do BFF) e grava PNGs em
 * `renderer/build/outputs/roborazzi/`. Serve para conferir o renderer sem aparelho.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h860dp-xhdpi")
class ScreenshotTest {

    @Test fun home() = capture("home")

    @Test fun homeFollowing() = capture("home", "home-following", "tab" to JsonPrimitive("following"))

    @Test fun article() = capture("article")

    @Test fun settings() = capture("settings")

    @Test fun settingsDark() = capture("settings", "settings-dark", dark = true)

    @Test fun catalogContent() = capture("catalog", "catalog-content")

    @Test fun catalogActions() = capture("catalog", "catalog-actions", "section" to JsonPrimitive("actions"))

    @Test fun catalogForms() = capture("catalog", "catalog-forms", "section" to JsonPrimitive("forms"))

    private fun capture(
        example: String,
        name: String = example,
        vararg state: Pair<String, JsonElement>,
        dark: Boolean = false,
    ) {
        val dir = System.getProperty("sdui.examples") ?: "../docs/examples"
        val screen = SduiJson.decodeFromString(ScreenResponse.serializer(), File(dir, "$example.json").readText()).screen
        val store = SnapshotStateStore(screen.state + state)
        captureRoboImage("build/outputs/roborazzi/$name.png") {
            Theme(dark) { SduiScreen(screen = screen, host = host, store = store) }
        }
    }

    @Composable
    private fun Theme(dark: Boolean, content: @Composable () -> Unit) {
        MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme(), content = content)
    }

    private val host = SduiHost(
        navigator = object : SduiNavigator {
            override fun navigate(route: String, params: Map<String, JsonElement>, replace: Boolean) = Unit
            override fun back() = Unit
            override fun openDeeplink(uri: String) = Unit
        },
        http = { HttpOutcome(200) },
        logger = { println("[sdui] $it") },
    )
}
