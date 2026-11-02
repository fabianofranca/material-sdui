package com.fabianofranca.sdui.renderer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * O ponto de entrada do iOS: o app SwiftUI/UIKit embute este UIViewController.
 * Em Swift: `SduiViewControllerKt.SduiViewController(baseUrl: "...", startRoute: "home")`.
 *
 * O tema aqui é o padrão do Material 3. Um app com design system próprio troca o
 * `MaterialTheme` por um módulo Kotlin com as cores, a tipografia e as formas dele.
 */
fun SduiViewController(baseUrl: String, startRoute: String): UIViewController = ComposeUIViewController {
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme) {
        val config = remember { SduiConfig(baseUrl = baseUrl, startRoute = startRoute) }
        SduiApp(config)
    }
}
