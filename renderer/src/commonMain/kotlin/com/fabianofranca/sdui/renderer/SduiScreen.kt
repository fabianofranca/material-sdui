package com.fabianofranca.sdui.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.fabianofranca.sdui.contract.Screen
import com.fabianofranca.sdui.contract.SnackbarLength
import com.fabianofranca.sdui.contract.Trigger
import com.fabianofranca.sdui.engine.ScreenController
import com.fabianofranca.sdui.engine.SduiHost
import com.fabianofranca.sdui.engine.StateStore
import com.fabianofranca.sdui.engine.UiState

/**
 * Desenha uma tela do SDUI: `topBar`, `content`, `bottomBar` e `floating` num [Scaffold] do
 * material3, as camadas abertas por cima, o Snackbar e o carregamento de tela inteira.
 *
 * Serve sozinho (por exemplo, para uma tela que veio de outro lugar) ou dentro de [SduiApp],
 * que cuida de pedir as telas ao BFF e da navegação.
 *
 * @param store o estado da tela; passe um que sobreviva à tela para manter o que a pessoa digitou.
 * @param onRefresh o que a ação `refreshScreen` faz (pedir a tela de novo).
 * @param restoreUi o [UiState] guardado por [onLeave] na última vez (itens acrescentados e erros).
 * @param onLeave chamado quando a tela sai da composição, com o [UiState] dela.
 */
@Composable
fun SduiScreen(
    screen: Screen,
    host: SduiHost,
    modifier: Modifier = Modifier,
    store: StateStore = remember(screen) { SnapshotStateStore(screen.state) },
    icons: SduiIcons = SduiIcons.Default,
    onRefresh: suspend () -> Unit = {},
    restoreUi: UiState? = null,
    onLeave: (UiState) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val currentOnRefresh by rememberUpdatedState(onRefresh)
    val currentOnLeave by rememberUpdatedState(onLeave)
    val controller = remember(screen, store, host) {
        ScreenController(screen, host, store, scope, refreshScreen = { currentOnRefresh() }, restore = restoreUi)
    }
    DisposableEffect(controller) {
        onDispose { currentOnLeave(controller.ui.value) }
    }
    val ui by controller.ui.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(controller) { controller.onScreenAppear() }
    LaunchedEffect(controller) {
        controller.snackbars.collect { request ->
            val result = snackbarHostState.showSnackbar(
                message = request.message,
                actionLabel = request.actionLabel,
                withDismissAction = request.duration == SnackbarLength.Indefinite,
                duration = request.duration.toCompose(),
            )
            controller.onSnackbarResult(request, actionPerformed = result == SnackbarResult.ActionPerformed)
        }
    }
    SduiBackHandler(enabled = Trigger.OnBack in screen.events) { controller.onBack() }

    CompositionLocalProvider(
        LocalSduiController provides controller,
        LocalSduiUi provides ui,
        LocalSduiIcons provides icons,
    ) {
        Box(modifier.fillMaxSize()) {
            Scaffold(
                topBar = { screen.topBar?.let { SduiComponent(it) } },
                bottomBar = { screen.bottomBar?.let { SduiComponent(it) } },
                floatingActionButton = { screen.floating?.let { SduiComponent(it) } },
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { padding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .consumeWindowInsets(padding)
                        .imePadding(),
                ) {
                    if (Trigger.OnRefresh in screen.events) {
                        RefreshBox(onRefresh = { controller.run(Trigger.OnRefresh) }) {
                            SduiComponent(screen.content, Modifier.fillMaxSize())
                        }
                    } else {
                        SduiComponent(screen.content, Modifier.fillMaxSize())
                    }
                }
            }

            for (id in ui.openModals) {
                val modal = controller.component(id) ?: continue
                key(id) { SduiModal(modal) }
            }

            if (ui.isScreenLoading) ScreenLoading()
        }
    }
}

/** Cobre a tela e bloqueia o toque enquanto uma requisição com `loading: screen` roda. */
@Composable
private fun ScreenLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {}),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
