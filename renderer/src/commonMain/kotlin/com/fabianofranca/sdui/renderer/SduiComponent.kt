package com.fabianofranca.sdui.renderer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.Trigger
import com.fabianofranca.sdui.engine.boolean

/**
 * Desenha um componente do contrato com o composable do material3 correspondente.
 * Componente invisível não é desenhado; componente de tipo desconhecido também não.
 */
@Composable
fun SduiComponent(component: Component, modifier: Modifier = Modifier) {
    if (component is Component.Unknown) return
    val controller = LocalSduiController.current
    if (!controller.store.boolean(component.visible)) return

    LaunchedEffect(component.id) { controller.onAppear(component) }

    val m = modifier.sduiSemantics(component)
    when (component) {
        is Component.TopAppBar -> SduiTopAppBar(component, m)
        is Component.Column -> SduiColumn(component, m)
        is Component.Row -> SduiRow(component, m)
        is Component.Box -> SduiBox(component, m)
        is Component.LazyColumn -> SduiLazyColumn(component, m)
        is Component.Spacer -> Spacer(m.size(component.props.size.dp))
        is Component.Divider -> HorizontalDivider(m.padding(start = component.props.inset.dp))
        is Component.Text -> SduiText(component, m)
        is Component.Image -> SduiImage(component, m)
        is Component.Icon -> SduiIcon(component, m)
        is Component.Card -> SduiCard(component, m)
        is Component.ListItem -> SduiListItem(component, m)
        is Component.Badge -> SduiBadge(component, m)
        is Component.ProgressIndicator -> SduiProgressIndicator(component, m)
        is Component.Button -> SduiButton(component, m)
        is Component.IconButton -> SduiIconButton(component, m)
        is Component.Fab -> SduiFab(component, m)
        is Component.TextField -> SduiTextField(component, m)
        is Component.Checkbox -> SduiCheckbox(component, m)
        is Component.Switch -> SduiSwitch(component, m)
        is Component.RadioGroup -> SduiRadioGroup(component, m)
        is Component.Chip -> SduiChip(component, m)
        is Component.NavigationBar -> SduiNavigationBar(component, m)
        is Component.Tabs -> SduiTabs(component, m)
        // As camadas são desenhadas pela tela, quando abertas (SduiScreen).
        is Component.BottomSheet, is Component.Dialog -> Unit
        is Component.Unknown -> Unit
    }
}

/** Os filhos do contêiner, mais os acrescentados por uma resposta do BFF (paginação). */
@Composable
internal fun childrenOf(component: Component): List<Component> {
    val appended = LocalSduiUi.current.appended[component.id]
    return if (appended.isNullOrEmpty()) component.children else component.children + appended
}

@Composable
internal fun SduiChildren(component: Component) {
    for (child in childrenOf(component)) {
        key(child.id) { SduiComponent(child) }
    }
}

/** `testTag` e `a11y` de todo componente. */
internal fun Modifier.sduiSemantics(component: Component): Modifier {
    var m = this
    val tag = component.testTag
    if (tag != null) m = m.testTag(tag)
    val a11y = component.a11y
    if (a11y != null && (a11y.label != null || a11y.heading)) {
        val label = a11y.label
        m = m.semantics {
            if (label != null) contentDescription = label
            if (a11y.heading) heading()
        }
    }
    return m
}

/** Torna clicável o componente que tem `onClick` (contêineres, Text, Image, Icon). */
@Composable
internal fun Modifier.sduiClickable(component: Component): Modifier {
    if (Trigger.OnClick !in component.events) return this
    val controller = LocalSduiController.current
    return clickable { controller.dispatch(Trigger.OnClick, component) }
}
