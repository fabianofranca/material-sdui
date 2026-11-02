package com.fabianofranca.sdui.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.fabianofranca.sdui.renderer.SduiApp
import com.fabianofranca.sdui.renderer.SduiConfig

/** A casca do app: o tema e o SduiApp. Tela nova não muda este código; só o BFF. */
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val config = remember {
                    SduiConfig(
                        baseUrl = BuildConfig.BFF_URL,
                        analytics = { event -> Log.d("sdui-analytics", event.toString()) },
                        logger = { message -> Log.w("sdui", message) },
                        onExit = { finish() },
                    )
                }
                // testTag vira resource-id, para UiAutomator/Maestro/Appium.
                SduiApp(config, Modifier.semantics { testTagsAsResourceId = true })
            }
        }
    }
}
