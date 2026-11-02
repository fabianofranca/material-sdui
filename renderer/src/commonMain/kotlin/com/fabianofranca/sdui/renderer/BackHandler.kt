package com.fabianofranca.sdui.renderer

import androidx.compose.runtime.Composable

/** O voltar do sistema. No Android, o botão/gesto de voltar; no iOS, não há. */
@Composable
internal expect fun SduiBackHandler(enabled: Boolean, onBack: () -> Unit)
