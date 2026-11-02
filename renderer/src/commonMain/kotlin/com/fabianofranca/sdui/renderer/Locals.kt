package com.fabianofranca.sdui.renderer

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.fabianofranca.sdui.engine.ScreenController
import com.fabianofranca.sdui.engine.UiState

internal val LocalSduiController = staticCompositionLocalOf<ScreenController> {
    error("SduiComponent só pode ser usado dentro de SduiScreen")
}

internal val LocalSduiUi = compositionLocalOf { UiState() }

internal val LocalSduiIcons = staticCompositionLocalOf { SduiIcons.Default }
