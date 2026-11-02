package com.fabianofranca.sdui.engine

import com.fabianofranca.sdui.contract.Action
import com.fabianofranca.sdui.contract.Bindable
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.LoadingMode
import com.fabianofranca.sdui.contract.NavigationItem
import com.fabianofranca.sdui.contract.RequestResult
import com.fabianofranca.sdui.contract.Screen
import com.fabianofranca.sdui.contract.SduiJson
import com.fabianofranca.sdui.contract.SnackbarLength
import com.fabianofranca.sdui.contract.TabItem
import com.fabianofranca.sdui.contract.Trigger
import com.fabianofranca.sdui.contract.ValidationRule
import com.fabianofranca.sdui.contract.allComponents
import com.fabianofranca.sdui.contract.descendantsAndSelf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** O que muda na tela além do estado: camadas abertas, carregamentos, erros e itens acrescentados. */
data class UiState(
    /** Ids de BottomSheet e Dialog abertos, na ordem em que abriram. */
    val openModals: List<String> = emptyList(),
    /** Ids dos componentes que mostram carregamento (`loading: component`). */
    val loading: Set<String> = emptySet(),
    /** Quantas requisições com `loading: screen` estão em andamento. */
    val screenLoading: Int = 0,
    /** Erros por id de campo, de `validate` ou do BFF. */
    val errors: Map<String, String> = emptyMap(),
    /** Filhos acrescentados por id de contêiner (paginação). */
    val appended: Map<String, List<Component>> = emptyMap(),
) {
    val isScreenLoading: Boolean get() = screenLoading > 0
}

/** Um Snackbar pedido pela ação `showSnackbar`. */
data class SnackbarRequest(
    val message: String,
    val actionLabel: String?,
    val duration: SnackbarLength,
    val action: Action?,
    val onDismiss: Action?,
)

/**
 * O motor de uma tela: guarda o estado, dispara os gatilhos e executa as ações.
 * Não depende do Compose; o renderer lê [store] e [ui] e chama os métodos `on…`.
 */
class ScreenController(
    val screen: Screen,
    val host: SduiHost,
    val store: StateStore = MapStateStore(screen.state),
    private val scope: CoroutineScope,
    /** Pede a tela de novo ao BFF (ação `refreshScreen`). */
    private val refreshScreen: suspend () -> Unit = {},
    private val json: Json = SduiJson,
) {
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private val snackbarChannel = Channel<SnackbarRequest>(Channel.BUFFERED)
    val snackbars: Flow<SnackbarRequest> = snackbarChannel.receiveAsFlow()

    private val index = mutableMapOf<String, Component>()
    private val dialogOfButton = mutableMapOf<String, String>()

    init {
        register(screen.allComponents())
    }

    fun component(id: String): Component? = index[id]

    // Campos

    /** A prop que guarda o valor de um campo. */
    fun fieldBinding(component: Component): Bindable<*>? = when (component) {
        is Component.TextField -> component.props.value
        is Component.Checkbox -> component.props.checked
        is Component.Switch -> component.props.checked
        is Component.RadioGroup -> component.props.selected
        is Component.Chip -> component.props.selected
        is Component.Tabs -> component.props.selected
        is Component.NavigationBar -> component.props.selected
        else -> null
    }

    /** A chave do estado em que o campo grava: a da ligação ou, com valor fixo, o `id` do componente. */
    fun fieldKey(component: Component): String =
        (fieldBinding(component) as? Bindable.State)?.key ?: component.id

    fun fieldValue(component: Component): JsonElement? = store[fieldKey(component)]

    /** A pessoa mudou o valor de um campo. */
    fun onValueChange(component: Component, value: JsonElement) {
        store[fieldKey(component)] = value
        if (component.id in _ui.value.errors) _ui.update { it.copy(errors = it.errors - component.id) }
        dispatch(Trigger.OnChange, component)
    }

    fun onNavigationItemClick(bar: Component.NavigationBar, item: NavigationItem) {
        val key = fieldKey(bar)
        val changed = store[key]?.asText() != item.id
        store[key] = JsonPrimitive(item.id)
        val itemAction = item.events[Trigger.OnClick]
            ?: item.route?.takeIf { changed }?.let { Action.Navigate(it, replace = true) }
        val barAction = bar.events[Trigger.OnChange]?.takeIf { changed }
        launchAll(listOfNotNull(itemAction, barAction), bar.id)
    }

    fun onTabClick(tabs: Component.Tabs, item: TabItem) {
        val key = fieldKey(tabs)
        val changed = store[key]?.asText() != item.id
        store[key] = JsonPrimitive(item.id)
        val barAction = tabs.events[Trigger.OnChange]?.takeIf { changed }
        launchAll(listOfNotNull(item.events[Trigger.OnClick], barAction), tabs.id)
    }

    // Gatilhos

    /**
     * Dispara [triggers] de [component] (ou da tela, sem componente), em ordem.
     * Ignora o toque enquanto o componente mostra carregamento.
     */
    fun dispatch(vararg triggers: Trigger, component: Component? = null): Job? {
        val events = component?.events ?: screen.events
        if (component != null && Trigger.OnClick in triggers) {
            component.analytics?.let { track(it.name, AnalyticsEvent.Kind.Click, component.id, it.properties) }
            dialogOfButton[component.id]?.let(::closeModal)
        }
        val actions = triggers.mapNotNull { events[it] }
        if (actions.isEmpty()) return null
        if (component != null && component.id in _ui.value.loading) return null
        return launchAll(actions, component?.id)
    }

    fun dispatch(trigger: Trigger, component: Component?): Job? = dispatch(*arrayOf(trigger), component = component)

    /** Executa o gatilho e espera terminar (puxar para atualizar mostra o indicador até lá). */
    suspend fun run(trigger: Trigger, component: Component? = null) {
        val action = (component?.events ?: screen.events)[trigger] ?: return
        execute(action, component?.id)
    }

    /** O componente apareceu: impressão de analytics e `onAppear`. */
    fun onAppear(component: Component) {
        component.analytics?.let { track(it.name, AnalyticsEvent.Kind.Impression, component.id, it.properties) }
        dispatch(Trigger.OnAppear, component)
    }

    fun onScreenAppear() {
        dispatch(Trigger.OnAppear, null)
    }

    fun onBack() {
        dispatch(Trigger.OnBack, null)
    }

    /** A pessoa fechou a camada (arrastou, tocou fora, voltou). */
    fun onModalDismissed(modal: Component) {
        closeModal(modal.id)
        dispatch(Trigger.OnDismiss, modal)
    }

    fun onSnackbarResult(request: SnackbarRequest, actionPerformed: Boolean) {
        val action = if (actionPerformed) request.action else request.onDismiss
        action?.let { launchAll(listOf(it), null) }
    }

    /** Executa [action] fora de um gatilho (por exemplo, a ação de um Snackbar). */
    fun perform(action: Action): Job = launchAll(listOf(action), null)

    private fun launchAll(actions: List<Action>, origin: String?): Job = scope.launch {
        for (action in actions) execute(action, origin)
    }

    // Ações

    suspend fun execute(action: Action, origin: String? = null) {
        when (action) {
            is Action.Navigate -> host.navigator.navigate(action.route, store.resolveJson(action.params), action.replace)
            Action.NavigateBack -> host.navigator.back()
            is Action.OpenDeeplink -> host.navigator.openDeeplink(action.uri)
            is Action.OpenUrl -> host.urlOpener.open(action.url, action.mode)
            is Action.OpenSheet -> openModal<Component.BottomSheet>(action.id)
            is Action.CloseSheet -> closeModal(action.id ?: lastOpen<Component.BottomSheet>())
            is Action.OpenDialog -> openModal<Component.Dialog>(action.id)
            is Action.CloseDialog -> closeModal(action.id ?: lastOpen<Component.Dialog>())
            is Action.ShowSnackbar -> snackbarChannel.send(
                SnackbarRequest(
                    message = store.string(action.message),
                    actionLabel = action.actionLabel,
                    duration = action.duration,
                    action = action.action,
                    onDismiss = action.onDismiss,
                ),
            )
            is Action.Request -> request(action, origin)
            is Action.SetState -> store[action.key] = store.resolveJson(action.value)
            is Action.Validate -> {
                val next = if (validate(action.fields)) action.onValid else action.onInvalid
                next?.let { execute(it, origin) }
            }
            Action.RefreshScreen -> try {
                refreshScreen()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                host.logger.warn("Não foi possível atualizar a tela ${screen.id}: ${e.message}")
            }
            is Action.Track -> track(action.name, AnalyticsEvent.Kind.Custom, origin, action.properties)
            is Action.Sequence -> action.actions.forEach { execute(it, origin) }
            is Action.Unknown -> host.logger.warn("Ação desconhecida ignorada: type=${action.type}")
        }
    }

    private suspend fun request(action: Action.Request, origin: String?) {
        val call = HttpCall(
            method = action.method,
            endpoint = action.endpoint,
            query = store.resolveJson(action.query),
            body = action.body?.let { store.resolveJson(it) },
        )
        val loadingId = origin.takeIf { action.loading == LoadingMode.Component }
        setLoading(action.loading, loadingId, active = true)
        val outcome = try {
            host.http.send(call)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            host.logger.warn("Falha em ${action.method} ${action.endpoint}: ${e.message}")
            null
        } finally {
            setLoading(action.loading, loadingId, active = false)
        }
        val result = (outcome?.body as? JsonObject)?.let {
            runCatching { json.decodeFromJsonElement(RequestResult.serializer(), it) }.getOrNull()
        }
        result?.let(::apply)
        if (outcome?.isSuccess == true) {
            result?.action?.let { execute(it, origin) }
            action.onSuccess?.let { execute(it, origin) }
        } else {
            val onError = action.onError
            if (onError != null) {
                execute(onError, origin)
            } else {
                host.logger.warn("${action.method} ${action.endpoint} falhou (${outcome?.status ?: "rede"}) sem onError")
            }
        }
    }

    private fun apply(result: RequestResult) {
        result.state.forEach { (key, value) -> store[key] = value }
        result.append.values.forEach { children -> register(children.asSequence().flatMap { it.descendantsAndSelf() }) }
        _ui.update { ui ->
            ui.copy(
                errors = ui.errors + result.errors,
                appended = result.append.entries.fold(ui.appended) { acc, (id, children) ->
                    acc + (id to acc[id].orEmpty() + children)
                },
            )
        }
    }

    /** Valida os campos [fields]; grava ou limpa o erro de cada um. `true` se todos passaram. */
    fun validate(fields: List<String>): Boolean {
        var valid = true
        val errors = _ui.value.errors.toMutableMap()
        for (id in fields) {
            val component = index[id]
            if (component == null) {
                host.logger.warn("validate: campo $id não existe na tela ${screen.id}")
                continue
            }
            val message = firstFailure(rulesOf(component), fieldValue(component))
            if (message == null) {
                errors.remove(id)
            } else {
                errors[id] = message
                valid = false
            }
        }
        _ui.update { it.copy(errors = errors) }
        return valid
    }

    private fun rulesOf(component: Component): List<ValidationRule> = when (component) {
        is Component.TextField -> component.props.validation
        is Component.Checkbox -> component.props.validation
        is Component.RadioGroup -> component.props.validation
        else -> emptyList()
    }

    // Camadas e carregamento

    private inline fun <reified T : Component> openModal(id: String) {
        if (index[id] !is T) {
            host.logger.warn("Camada $id não existe na tela ${screen.id} (ou não é ${T::class.simpleName})")
            return
        }
        _ui.update { if (id in it.openModals) it else it.copy(openModals = it.openModals + id) }
    }

    private fun closeModal(id: String?) {
        if (id == null) return
        _ui.update { it.copy(openModals = it.openModals - id) }
    }

    private inline fun <reified T : Component> lastOpen(): String? =
        _ui.value.openModals.lastOrNull { index[it] is T }

    private fun setLoading(mode: LoadingMode, componentId: String?, active: Boolean) {
        when (mode) {
            LoadingMode.None -> Unit
            LoadingMode.Screen -> _ui.update { it.copy(screenLoading = it.screenLoading + if (active) 1 else -1) }
            LoadingMode.Component -> if (componentId != null) {
                _ui.update { it.copy(loading = if (active) it.loading + componentId else it.loading - componentId) }
            }
        }
    }

    // Registro

    private fun register(components: Sequence<Component>) {
        for (component in components) {
            if (component is Component.Unknown) {
                host.logger.warn("Componente desconhecido ignorado: type=${component.type} id=${component.id}")
                continue
            }
            index[component.id] = component
            if (component is Component.Dialog) {
                listOfNotNull(component.props.confirm, component.props.dismiss).forEach { dialogOfButton[it.id] = component.id }
            }
            val fixed = fieldBinding(component) as? Bindable.Fixed<*>
            if (fixed != null && component.id !in store) store[component.id] = fixed.toJson()
        }
    }

    private fun track(name: String, kind: AnalyticsEvent.Kind, componentId: String?, properties: Map<String, JsonElement>) {
        host.analytics.track(AnalyticsEvent(name, kind, screen.id, componentId, store.resolveJson(properties)))
    }
}
