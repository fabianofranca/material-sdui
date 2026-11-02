package com.fabianofranca.sdui.renderer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.ProgressKind
import androidx.compose.runtime.staticCompositionLocalOf
import com.fabianofranca.sdui.engine.ScreenController
import com.fabianofranca.sdui.engine.UiState

internal val LocalSduiController = staticCompositionLocalOf<ScreenController> {
    error("SduiComponent só pode ser usado dentro de SduiScreen")
}

internal val LocalSduiUi = compositionLocalOf { UiState() }

internal val LocalSduiIcons = staticCompositionLocalOf { SduiIcons.Default }

/**
 * Se o componente pode ocupar a largura toda. Dentro de uma Row, não: lá quem quer a
 * largura ganha `weight(1f)` da própria Row e divide o espaço com os vizinhos.
 */
internal val LocalFillWidth = compositionLocalOf { true }

/** Dentro de uma Column com rolagem, uma LazyColumn vira uma Column (as duas rolando quebram). */
internal val LocalInVerticalScroll = compositionLocalOf { false }

/** `fillMaxWidth()` quando o pai deixa (veja [LocalFillWidth]). */
@Composable
@ReadOnlyComposable
internal fun fillWidth(): Modifier = if (LocalFillWidth.current) Modifier.fillMaxWidth() else Modifier

/** Os componentes que ocupam a largura disponível; numa Row, eles recebem `weight(1f)`. */
internal fun Component.fillsWidth(): Boolean = when (this) {
    is Component.Column, is Component.Row, is Component.Card, is Component.TextField, is Component.RadioGroup -> true
    is Component.Checkbox -> props.label != null
    is Component.Switch -> props.label != null
    is Component.Button -> props.fullWidth
    is Component.ProgressIndicator -> props.kind == ProgressKind.Linear
    is Component.Image -> props.size == null && props.aspectRatio != null
    is Component.Text -> props.align != null
    else -> false
}
