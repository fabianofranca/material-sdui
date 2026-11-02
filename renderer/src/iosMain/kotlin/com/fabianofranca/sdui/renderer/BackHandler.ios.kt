package com.fabianofranca.sdui.renderer

import androidx.compose.runtime.Composable

@Composable
internal actual fun SduiBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // O iOS não tem voltar do sistema; as telas usam o navigationIcon da TopAppBar.
}
