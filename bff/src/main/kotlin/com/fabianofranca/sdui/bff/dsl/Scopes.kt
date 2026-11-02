package com.fabianofranca.sdui.bff.dsl

import com.fabianofranca.sdui.contract.A11y
import com.fabianofranca.sdui.contract.Action
import com.fabianofranca.sdui.contract.AnalyticsInfo
import com.fabianofranca.sdui.contract.Bindable
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.Screen
import com.fabianofranca.sdui.contract.ScreenResponse
import com.fabianofranca.sdui.contract.Trigger
import com.fabianofranca.sdui.contract.duplicateIds
import com.fabianofranca.sdui.contract.fixed
import com.fabianofranca.sdui.contract.sequenceOf
import kotlinx.serialization.json.JsonElement

@DslMarker
annotation class SduiDsl

/** Onde os componentes são acrescentados: o corpo de um contêiner, uma raiz da tela, um encaixe. */
@SduiDsl
interface ChildrenScope {
    fun <T : Component> add(component: T): T
}

internal class ChildrenList : ChildrenScope {
    val children = mutableListOf<Component>()

    override fun <T : Component> add(component: T): T = component.also { children += it }
}

/** Monta um único componente (um encaixe, como o `leading` de um ListItem). */
internal fun slot(block: (ChildrenScope.() -> Unit)?): Component? {
    if (block == null) return null
    val list = ChildrenList().apply(block).children
    require(list.size <= 1) { "Um encaixe aceita um componente só; vieram ${list.size}" }
    return list.singleOrNull()
}

internal fun many(block: ChildrenScope.() -> Unit): List<Component> = ChildrenList().apply(block).children

/** Monta componentes soltos, fora de uma tela (por exemplo, itens de uma página para `append`). */
fun componentsOf(block: ChildrenScope.() -> Unit): List<Component> = many(block)

/** O que vale para todo componente: gatilhos, `visible`, `testTag`, `a11y` e `analytics`. */
@SduiDsl
open class ComponentScope {
    internal val events = linkedMapOf<Trigger, Action>()

    /** Fixo (`fixed(false)`) ou ligado ao estado (`state("chave")`, `stateEquals("aba", ...)`). */
    var visible: Bindable<Boolean> = fixed(true)
    var testTag: String? = null
    var a11y: A11y? = null
    var analytics: AnalyticsInfo? = null

    fun a11y(label: String? = null, heading: Boolean = false) {
        a11y = A11y(label, heading)
    }

    fun analytics(name: String, vararg properties: Pair<String, Any?>) {
        analytics = AnalyticsInfo(name, jsonMap(properties))
    }

    /** Liga [actions] ao [trigger]; mais de uma vira `sequence`. */
    fun on(trigger: Trigger, vararg actions: Action) {
        if (actions.isNotEmpty()) events[trigger] = sequenceOf(*actions)
    }

    fun onClick(vararg actions: Action) = on(Trigger.OnClick, *actions)
    fun onChange(vararg actions: Action) = on(Trigger.OnChange, *actions)
    fun onSubmit(vararg actions: Action) = on(Trigger.OnSubmit, *actions)
    fun onAppear(vararg actions: Action) = on(Trigger.OnAppear, *actions)
    fun onRefresh(vararg actions: Action) = on(Trigger.OnRefresh, *actions)
    fun onLoadMore(vararg actions: Action) = on(Trigger.OnLoadMore, *actions)
    fun onDismiss(vararg actions: Action) = on(Trigger.OnDismiss, *actions)
}

/** Um contêiner: tem os gatilhos de um componente e recebe filhos. */
@SduiDsl
class ContainerScope : ComponentScope(), ChildrenScope {
    internal val children = mutableListOf<Component>()

    override fun <T : Component> add(component: T): T = component.also { children += it }
}

/** Gatilhos de um item que não é componente (aba, item da barra de navegação). */
@SduiDsl
class ItemScope {
    internal val events = linkedMapOf<Trigger, Action>()

    fun onClick(vararg actions: Action) {
        if (actions.isNotEmpty()) events[Trigger.OnClick] = sequenceOf(*actions)
    }
}

@SduiDsl
class ScreenScope internal constructor(private val id: String, private val title: String?) {
    private val state = linkedMapOf<String, JsonElement>()
    private val events = linkedMapOf<Trigger, Action>()
    private var topBar: Component? = null
    private var content: Component? = null
    private var bottomBar: Component? = null
    private var floating: Component? = null
    private val modals = mutableListOf<Component>()

    /** Valores iniciais do estado local: `initialState("email" to "", "notifications" to true)`. */
    fun initialState(vararg values: Pair<String, Any?>) {
        state += jsonMap(values)
    }

    fun topBar(block: ChildrenScope.() -> Unit) {
        topBar = slot(block)
    }

    fun content(block: ChildrenScope.() -> Unit) {
        content = slot(block)
    }

    fun bottomBar(block: ChildrenScope.() -> Unit) {
        bottomBar = slot(block)
    }

    fun floating(block: ChildrenScope.() -> Unit) {
        floating = slot(block)
    }

    /** BottomSheets e Dialogs da tela, fechados até uma ação abri-los. */
    fun modals(block: ChildrenScope.() -> Unit) {
        modals += many(block)
    }

    fun onAppear(vararg actions: Action) = on(Trigger.OnAppear, *actions)
    fun onRefresh(vararg actions: Action) = on(Trigger.OnRefresh, *actions)
    fun onBack(vararg actions: Action) = on(Trigger.OnBack, *actions)

    private fun on(trigger: Trigger, vararg actions: Action) {
        if (actions.isNotEmpty()) events[trigger] = sequenceOf(*actions)
    }

    internal fun build(): Screen = Screen(
        id = id,
        title = title,
        state = state.toMap(),
        topBar = topBar,
        content = requireNotNull(content) { "A tela $id não tem content" },
        bottomBar = bottomBar,
        floating = floating,
        modals = modals.toList(),
        events = events.toMap(),
    )
}

/**
 * Monta uma tela:
 *
 * ```
 * screen(id = "settings", title = "Configurações") {
 *     initialState("email" to "")
 *     content {
 *         column(id = "content", padding = Padding.all(16)) {
 *             button(id = "salvar", label = "Salvar") { onClick(navigate("home")) }
 *         }
 *     }
 * }
 * ```
 *
 * Falha se dois componentes tiverem o mesmo `id`.
 */
fun screen(id: String, title: String? = null, block: ScreenScope.() -> Unit): ScreenResponse {
    val screen = ScreenScope(id, title).apply(block).build()
    val duplicates = screen.duplicateIds()
    require(duplicates.isEmpty()) { "Ids repetidos na tela $id: $duplicates" }
    return ScreenResponse(screen = screen)
}
