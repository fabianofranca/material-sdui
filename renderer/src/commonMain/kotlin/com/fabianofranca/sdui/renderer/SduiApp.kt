package com.fabianofranca.sdui.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fabianofranca.sdui.contract.ScreenResponse
import com.fabianofranca.sdui.contract.UrlMode
import com.fabianofranca.sdui.engine.InMemoryScreenCache
import com.fabianofranca.sdui.engine.KtorSduiHttp
import com.fabianofranca.sdui.engine.ScreenCache
import com.fabianofranca.sdui.engine.ScreenRepository
import com.fabianofranca.sdui.engine.SduiAnalytics
import com.fabianofranca.sdui.engine.SduiHost
import com.fabianofranca.sdui.engine.SduiLogger
import com.fabianofranca.sdui.engine.SduiNavigator
import com.fabianofranca.sdui.engine.UiState
import com.fabianofranca.sdui.engine.UrlOpener
import io.ktor.client.HttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.JsonElement

/** Uma tela nativa registrada no app, com os parâmetros da rota e o navegador. */
class NativeRoute(val params: Map<String, JsonElement>, val navigator: SduiNavigator)

/**
 * O que o app entrega ao SDUI. Só [baseUrl] é obrigatório.
 *
 * @param httpClient o cliente do app: autenticação e cabeçalhos ficam nele, fora do JSON.
 * @param cache onde guardar a última resposta de cada tela (troque por um em disco para abrir sem rede).
 * @param nativeRoutes telas nativas por rota; o resto das rotas o app pede ao BFF.
 * @param onDeeplink abre um deeplink; sem ele, o deeplink vai para o sistema como URL.
 * @param onOpenUrlInApp abre uma URL dentro do app (`openUrl` com `mode: inApp`); sem ele, abre fora.
 * @param onExit chamado no voltar da primeira tela (no Android, feche a Activity).
 */
class SduiConfig(
    val baseUrl: String,
    val startRoute: String = "home",
    val httpClient: HttpClient = HttpClient(),
    val cache: ScreenCache = InMemoryScreenCache(),
    val analytics: SduiAnalytics = SduiAnalytics.None,
    val logger: SduiLogger = SduiLogger.Default,
    val icons: SduiIcons = SduiIcons.Default,
    val nativeRoutes: Map<String, @Composable (NativeRoute) -> Unit> = emptyMap(),
    val onDeeplink: ((String) -> Unit)? = null,
    val onOpenUrlInApp: ((String) -> Unit)? = null,
    val onExit: () -> Unit = {},
    val loadingContent: @Composable () -> Unit = { DefaultLoading() },
    val errorContent: @Composable (retry: () -> Unit) -> Unit = { retry -> DefaultError(retry) },
)

/**
 * O app SDUI completo: pilha de telas, carga das telas no BFF (com cache e ETag), telas
 * nativas, tela de erro com "tentar de novo" e voltar do sistema.
 */
@Composable
fun SduiApp(config: SduiConfig, modifier: Modifier = Modifier) {
    val repository = remember(config) { ScreenRepository(config.httpClient, config.baseUrl, config.cache) }
    val http = remember(config) { KtorSduiHttp(config.httpClient, config.baseUrl) }
    val backStack = remember(config) { BackStack(config.startRoute) }
    val uriHandler = LocalUriHandler.current
    // Guarda o estado salvável (posição das listas, por exemplo) de cada tela da pilha.
    val saveableState = rememberSaveableStateHolder()
    val goBack: () -> Boolean = {
        val removed = backStack.pop()
        if (removed != null) saveableState.removeState(removed)
        removed != null
    }

    val host = remember(config, backStack, uriHandler) {
        val openExternal: (String) -> Unit = { url ->
            runCatching { uriHandler.openUri(url) }.onFailure { config.logger.warn("Não foi possível abrir $url: ${it.message}") }
        }
        val navigator = object : SduiNavigator {
            override fun navigate(route: String, params: Map<String, JsonElement>, replace: Boolean) {
                backStack.push(route, params, replace)?.let(saveableState::removeState)
            }

            override fun back() {
                if (!goBack()) config.onExit()
            }

            override fun openDeeplink(uri: String) {
                val onDeeplink = config.onDeeplink
                if (onDeeplink != null) onDeeplink(uri) else openExternal(uri)
            }
        }
        val urlOpener = UrlOpener { url, mode ->
            val inApp = config.onOpenUrlInApp
            if (mode == UrlMode.InApp && inApp != null) inApp(url) else openExternal(url)
        }
        SduiHost(navigator, http, config.analytics, urlOpener, config.logger)
    }

    SduiBackHandler(enabled = backStack.canGoBack) { goBack() }

    val entry = backStack.top
    Surface(modifier = modifier.fillMaxSize()) {
        saveableState.SaveableStateProvider(entry.key) {
            val native = config.nativeRoutes[entry.route]
            if (native != null) {
                native(NativeRoute(entry.params, host.navigator))
            } else {
                RemoteScreen(entry, repository, host, config)
            }
        }
    }
}

/** Uma tela do BFF: mostra o cache na hora, atualiza pela rede e cai na tela de erro se não houver nada. */
@Composable
private fun RemoteScreen(entry: BackStackEntry, repository: ScreenRepository, host: SduiHost, config: SduiConfig) {
    var failed by remember(entry) { mutableStateOf(false) }
    var attempt by remember(entry) { mutableIntStateOf(0) }

    LaunchedEffect(entry, attempt) {
        if (entry.loaded == null) repository.cached(entry.route, entry.params)?.let { entry.loaded = LoadedScreen(it) }
        try {
            val fresh = repository.fetch(entry.route, entry.params)
            // A mesma resposta (por exemplo, um 304) mantém o estado que a pessoa já mexeu.
            if (fresh != entry.loaded?.response) entry.loaded = LoadedScreen(fresh)
            failed = false
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            host.logger.warn("Falha ao carregar ${entry.route}: ${e.message}")
            failed = entry.loaded == null
        }
    }

    val loaded = entry.loaded
    when {
        loaded != null -> SduiScreen(
            screen = loaded.response.screen,
            host = host,
            store = loaded.store,
            icons = config.icons,
            onRefresh = { entry.loaded = LoadedScreen(repository.fetch(entry.route, entry.params)) },
            restoreUi = loaded.savedUi,
            onLeave = { loaded.savedUi = it },
        )
        failed -> config.errorContent {
            failed = false
            attempt++
        }
        else -> config.loadingContent()
    }
}

/**
 * Uma resposta do BFF e o estado da tela, que sobrevivem enquanto a tela estiver na pilha:
 * ao voltar, a tela reaparece com o que a pessoa digitou, as páginas carregadas e os erros.
 */
internal class LoadedScreen(val response: ScreenResponse) {
    val store = SnapshotStateStore(response.screen.state)
    var savedUi: UiState? = null
}

@Stable
internal class BackStackEntry(val key: Int, val route: String, val params: Map<String, JsonElement>) {
    var loaded: LoadedScreen? by mutableStateOf(null)
}

internal class BackStack(startRoute: String) {
    private var nextKey = 0
    private val entries = mutableStateListOf(BackStackEntry(nextKey++, normalize(startRoute), emptyMap()))

    val top: BackStackEntry get() = entries.last()
    val canGoBack: Boolean get() = entries.size > 1

    /** Empilha a rota; com [replace], devolve a chave da tela que saiu. */
    fun push(route: String, params: Map<String, JsonElement>, replace: Boolean): Int? {
        val entry = BackStackEntry(nextKey++, normalize(route), params)
        return if (replace) {
            val removed = entries[entries.lastIndex].key
            entries[entries.lastIndex] = entry
            removed
        } else {
            entries.add(entry)
            null
        }
    }

    /** Desempilha e devolve a chave da tela que saiu; `null` se já está na primeira. */
    fun pop(): Int? {
        if (entries.size <= 1) return null
        return entries.removeAt(entries.lastIndex).key
    }

    private fun normalize(route: String) = route.trim('/')
}

@Composable
private fun DefaultLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun DefaultError(retry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Não foi possível carregar a tela.",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Button(onClick = retry) { Text("Tentar de novo") }
    }
}
