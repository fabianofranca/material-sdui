package com.fabianofranca.sdui.bff.dsl

import com.fabianofranca.sdui.contract.BadgeProps
import com.fabianofranca.sdui.contract.Bindable
import com.fabianofranca.sdui.contract.BottomSheetProps
import com.fabianofranca.sdui.contract.BoxAlignment
import com.fabianofranca.sdui.contract.BoxProps
import com.fabianofranca.sdui.contract.ButtonProps
import com.fabianofranca.sdui.contract.ButtonStyle
import com.fabianofranca.sdui.contract.CardProps
import com.fabianofranca.sdui.contract.CardStyle
import com.fabianofranca.sdui.contract.CheckboxProps
import com.fabianofranca.sdui.contract.ChipKind
import com.fabianofranca.sdui.contract.ChipProps
import com.fabianofranca.sdui.contract.ColorRole
import com.fabianofranca.sdui.contract.ColumnProps
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.DialogProps
import com.fabianofranca.sdui.contract.DividerProps
import com.fabianofranca.sdui.contract.FabProps
import com.fabianofranca.sdui.contract.FabSize
import com.fabianofranca.sdui.contract.HorizontalAlignment
import com.fabianofranca.sdui.contract.HorizontalArrangement
import com.fabianofranca.sdui.contract.IconButtonProps
import com.fabianofranca.sdui.contract.IconButtonStyle
import com.fabianofranca.sdui.contract.IconProps
import com.fabianofranca.sdui.contract.ImageProps
import com.fabianofranca.sdui.contract.ImageScale
import com.fabianofranca.sdui.contract.ImeActionKind
import com.fabianofranca.sdui.contract.KeyboardKind
import com.fabianofranca.sdui.contract.LazyColumnProps
import com.fabianofranca.sdui.contract.ListItemProps
import com.fabianofranca.sdui.contract.NavigationBarProps
import com.fabianofranca.sdui.contract.NavigationItem
import com.fabianofranca.sdui.contract.Padding
import com.fabianofranca.sdui.contract.ProgressIndicatorProps
import com.fabianofranca.sdui.contract.ProgressKind
import com.fabianofranca.sdui.contract.RadioGroupProps
import com.fabianofranca.sdui.contract.RadioOption
import com.fabianofranca.sdui.contract.RowProps
import com.fabianofranca.sdui.contract.SpacerProps
import com.fabianofranca.sdui.contract.SwitchProps
import com.fabianofranca.sdui.contract.TabItem
import com.fabianofranca.sdui.contract.TabsProps
import com.fabianofranca.sdui.contract.TabsStyle
import com.fabianofranca.sdui.contract.TextAlignment
import com.fabianofranca.sdui.contract.TextFieldProps
import com.fabianofranca.sdui.contract.TextFieldStyle
import com.fabianofranca.sdui.contract.TextProps
import com.fabianofranca.sdui.contract.TopAppBarProps
import com.fabianofranca.sdui.contract.TopAppBarStyle
import com.fabianofranca.sdui.contract.TypographyRole
import com.fabianofranca.sdui.contract.ValidationRule
import com.fabianofranca.sdui.contract.VerticalAlignment
import com.fabianofranca.sdui.contract.fixed

// Os construtores: uma função por componente da seção 5 do briefing.
// Textos são `String` (o BFF já sabe o valor); props que costumam vir do estado
// (`value`, `checked`, `selected`, `enabled`…) são `Bindable`: `fixed(true)` ou `state("chave")`.

private fun fixedOrNull(value: String?): Bindable<String>? = value?.let { fixed(it) }

// Estrutura

fun ChildrenScope.topAppBar(
    id: String,
    title: String,
    style: TopAppBarStyle = TopAppBarStyle.Small,
    navigationIcon: (ChildrenScope.() -> Unit)? = null,
    actions: ChildrenScope.() -> Unit = {},
    block: ComponentScope.() -> Unit = {},
): Component.TopAppBar {
    val s = ComponentScope().apply(block)
    return add(
        Component.TopAppBar(
            id = id,
            props = TopAppBarProps(fixed(title), style, slot(navigationIcon), many(actions)),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.column(
    id: String,
    spacing: Int = 0,
    padding: Padding? = null,
    horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Start,
    scroll: Boolean = false,
    block: ContainerScope.() -> Unit,
): Component.Column {
    val s = ContainerScope().apply(block)
    return add(
        Component.Column(
            id = id,
            props = ColumnProps(spacing, padding, horizontalAlignment, scroll),
            children = s.children.toList(),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.row(
    id: String,
    spacing: Int = 0,
    padding: Padding? = null,
    verticalAlignment: VerticalAlignment = VerticalAlignment.Center,
    horizontalArrangement: HorizontalArrangement = HorizontalArrangement.Start,
    wrap: Boolean = false,
    block: ContainerScope.() -> Unit,
): Component.Row {
    val s = ContainerScope().apply(block)
    return add(
        Component.Row(
            id = id,
            props = RowProps(spacing, padding, verticalAlignment, horizontalArrangement, wrap),
            children = s.children.toList(),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.box(
    id: String,
    contentAlignment: BoxAlignment = BoxAlignment.TopStart,
    padding: Padding? = null,
    block: ContainerScope.() -> Unit,
): Component.Box {
    val s = ContainerScope().apply(block)
    return add(
        Component.Box(
            id = id,
            props = BoxProps(contentAlignment, padding),
            children = s.children.toList(),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.lazyColumn(
    id: String,
    spacing: Int = 0,
    padding: Padding? = null,
    block: ContainerScope.() -> Unit,
): Component.LazyColumn {
    val s = ContainerScope().apply(block)
    return add(
        Component.LazyColumn(
            id = id,
            props = LazyColumnProps(spacing, padding),
            children = s.children.toList(),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.spacer(id: String, size: Int = 8): Component.Spacer =
    add(Component.Spacer(id = id, props = SpacerProps(size)))

fun ChildrenScope.divider(id: String, inset: Int = 0, block: ComponentScope.() -> Unit = {}): Component.Divider {
    val s = ComponentScope().apply(block)
    return add(
        Component.Divider(
            id = id,
            props = DividerProps(inset),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

// Conteúdo

fun ChildrenScope.text(
    id: String,
    text: String,
    style: TypographyRole = TypographyRole.BodyMedium,
    color: ColorRole? = null,
    maxLines: Int? = null,
    align: TextAlignment? = null,
    block: ComponentScope.() -> Unit = {},
): Component.Text = text(id, fixed(text), style, color, maxLines, align, block)

/** Texto ligado ao estado: `text("total", state("total"))`. */
fun ChildrenScope.text(
    id: String,
    text: Bindable<String>,
    style: TypographyRole = TypographyRole.BodyMedium,
    color: ColorRole? = null,
    maxLines: Int? = null,
    align: TextAlignment? = null,
    block: ComponentScope.() -> Unit = {},
): Component.Text {
    val s = ComponentScope().apply(block)
    return add(
        Component.Text(
            id = id,
            props = TextProps(text, style, color, maxLines, align),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.image(
    id: String,
    url: String,
    contentScale: ImageScale = ImageScale.Crop,
    aspectRatio: Float? = null,
    placeholder: String? = null,
    size: Int? = null,
    block: ComponentScope.() -> Unit = {},
): Component.Image {
    val s = ComponentScope().apply(block)
    return add(
        Component.Image(
            id = id,
            props = ImageProps(fixed(url), contentScale, aspectRatio, placeholder, size),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.icon(
    id: String,
    name: String,
    tint: ColorRole? = null,
    size: Int = 24,
    block: ComponentScope.() -> Unit = {},
): Component.Icon {
    val s = ComponentScope().apply(block)
    return add(
        Component.Icon(
            id = id,
            props = IconProps(name, tint, size),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.card(
    id: String,
    style: CardStyle = CardStyle.Filled,
    padding: Padding = Padding.all(16),
    spacing: Int = 8,
    block: ContainerScope.() -> Unit,
): Component.Card {
    val s = ContainerScope().apply(block)
    return add(
        Component.Card(
            id = id,
            props = CardProps(style, padding, spacing),
            children = s.children.toList(),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.listItem(
    id: String,
    headline: String,
    supporting: String? = null,
    overline: String? = null,
    leading: (ChildrenScope.() -> Unit)? = null,
    trailing: (ChildrenScope.() -> Unit)? = null,
    block: ComponentScope.() -> Unit = {},
): Component.ListItem {
    val s = ComponentScope().apply(block)
    return add(
        Component.ListItem(
            id = id,
            props = ListItemProps(fixed(headline), fixedOrNull(supporting), fixedOrNull(overline), slot(leading), slot(trailing)),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

/** Sem [count], o selo é um ponto. */
fun ChildrenScope.badge(
    id: String,
    count: Bindable<Int>? = null,
    block: ContainerScope.() -> Unit,
): Component.Badge {
    val s = ContainerScope().apply(block)
    return add(
        Component.Badge(
            id = id,
            props = BadgeProps(count),
            children = s.children.toList(),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

/** Sem [progress], o indicador é indeterminado. */
fun ChildrenScope.progressIndicator(
    id: String,
    kind: ProgressKind = ProgressKind.Circular,
    progress: Bindable<Float>? = null,
    block: ComponentScope.() -> Unit = {},
): Component.ProgressIndicator {
    val s = ComponentScope().apply(block)
    return add(
        Component.ProgressIndicator(
            id = id,
            props = ProgressIndicatorProps(kind, progress),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

// Ação e formulário

fun ChildrenScope.button(
    id: String,
    label: String,
    style: ButtonStyle = ButtonStyle.Filled,
    icon: String? = null,
    enabled: Bindable<Boolean> = fixed(true),
    loading: Bindable<Boolean> = fixed(false),
    fullWidth: Boolean = false,
    block: ComponentScope.() -> Unit = {},
): Component.Button {
    val s = ComponentScope().apply(block)
    return add(
        Component.Button(
            id = id,
            props = ButtonProps(fixed(label), style, icon, enabled, loading, fullWidth),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.iconButton(
    id: String,
    icon: String,
    style: IconButtonStyle = IconButtonStyle.Standard,
    enabled: Bindable<Boolean> = fixed(true),
    block: ComponentScope.() -> Unit = {},
): Component.IconButton {
    val s = ComponentScope().apply(block)
    return add(
        Component.IconButton(
            id = id,
            props = IconButtonProps(icon, style, enabled),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

/** Com [label], o FAB é estendido. */
fun ChildrenScope.fab(
    id: String,
    icon: String,
    label: String? = null,
    size: FabSize = FabSize.Regular,
    block: ComponentScope.() -> Unit = {},
): Component.Fab {
    val s = ComponentScope().apply(block)
    return add(
        Component.Fab(
            id = id,
            props = FabProps(icon, fixedOrNull(label), size),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.textField(
    id: String,
    value: Bindable<String> = fixed(""),
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    error: Bindable<String>? = null,
    keyboard: KeyboardKind = KeyboardKind.Text,
    imeAction: ImeActionKind = ImeActionKind.Default,
    maxLength: Int? = null,
    style: TextFieldStyle = TextFieldStyle.Filled,
    singleLine: Boolean = true,
    enabled: Bindable<Boolean> = fixed(true),
    validation: List<ValidationRule> = emptyList(),
    block: ComponentScope.() -> Unit = {},
): Component.TextField {
    val s = ComponentScope().apply(block)
    return add(
        Component.TextField(
            id = id,
            props = TextFieldProps(
                value = value,
                label = fixedOrNull(label),
                placeholder = fixedOrNull(placeholder),
                supportingText = fixedOrNull(supportingText),
                error = error,
                keyboard = keyboard,
                imeAction = imeAction,
                maxLength = maxLength,
                style = style,
                singleLine = singleLine,
                enabled = enabled,
                validation = validation,
            ),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.checkbox(
    id: String,
    checked: Bindable<Boolean> = fixed(false),
    label: String? = null,
    enabled: Bindable<Boolean> = fixed(true),
    validation: List<ValidationRule> = emptyList(),
    block: ComponentScope.() -> Unit = {},
): Component.Checkbox {
    val s = ComponentScope().apply(block)
    return add(
        Component.Checkbox(
            id = id,
            props = CheckboxProps(checked, fixedOrNull(label), enabled, validation),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.switch(
    id: String,
    checked: Bindable<Boolean> = fixed(false),
    label: String? = null,
    enabled: Bindable<Boolean> = fixed(true),
    block: ComponentScope.() -> Unit = {},
): Component.Switch {
    val s = ComponentScope().apply(block)
    return add(
        Component.Switch(
            id = id,
            props = SwitchProps(checked, fixedOrNull(label), enabled),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun option(value: String, label: String, enabled: Boolean = true) = RadioOption(value, label, enabled)

fun ChildrenScope.radioGroup(
    id: String,
    options: List<RadioOption>,
    selected: Bindable<String> = fixed(""),
    enabled: Bindable<Boolean> = fixed(true),
    validation: List<ValidationRule> = emptyList(),
    block: ComponentScope.() -> Unit = {},
): Component.RadioGroup {
    val s = ComponentScope().apply(block)
    return add(
        Component.RadioGroup(
            id = id,
            props = RadioGroupProps(options, selected, enabled, validation),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.chip(
    id: String,
    label: String,
    kind: ChipKind = ChipKind.Assist,
    selected: Bindable<Boolean> = fixed(false),
    icon: String? = null,
    enabled: Bindable<Boolean> = fixed(true),
    block: ComponentScope.() -> Unit = {},
): Component.Chip {
    val s = ComponentScope().apply(block)
    return add(
        Component.Chip(
            id = id,
            props = ChipProps(fixed(label), kind, selected, icon, enabled),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

// Navegação e camadas

/** Um item da barra de navegação. Sem `onClick`, o toque navega para [route] trocando a tela. */
fun navItem(
    id: String,
    icon: String,
    label: String,
    route: String? = null,
    badge: Int? = null,
    block: ItemScope.() -> Unit = {},
) = NavigationItem(
    id = id,
    icon = icon,
    label = label,
    route = route,
    badge = badge?.let { BadgeProps(fixed(it)) },
    testTag = "nav.$id",
    events = ItemScope().apply(block).events.toMap(),
)

fun ChildrenScope.navigationBar(
    id: String,
    items: List<NavigationItem>,
    selected: Bindable<String>,
    block: ComponentScope.() -> Unit = {},
): Component.NavigationBar {
    val s = ComponentScope().apply(block)
    return add(
        Component.NavigationBar(
            id = id,
            props = NavigationBarProps(items, selected),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun tab(id: String, label: String, icon: String? = null, block: ItemScope.() -> Unit = {}) =
    TabItem(id, label, icon, testTag = "tab.$id", events = ItemScope().apply(block).events.toMap())

fun ChildrenScope.tabs(
    id: String,
    items: List<TabItem>,
    selected: Bindable<String>,
    style: TabsStyle = TabsStyle.Primary,
    block: ComponentScope.() -> Unit = {},
): Component.Tabs {
    val s = ComponentScope().apply(block)
    return add(
        Component.Tabs(
            id = id,
            props = TabsProps(items, selected, style),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

fun ChildrenScope.bottomSheet(
    id: String,
    dragHandle: Boolean = true,
    block: ContainerScope.() -> Unit,
): Component.BottomSheet {
    val s = ContainerScope().apply(block)
    return add(
        Component.BottomSheet(
            id = id,
            props = BottomSheetProps(dragHandle),
            children = s.children.toList(),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}

/** Tocar em [confirm] ou [dismiss] fecha o Dialog antes de executar o `onClick` do botão. */
fun ChildrenScope.dialog(
    id: String,
    title: String? = null,
    text: String? = null,
    icon: String? = null,
    confirm: (ChildrenScope.() -> Unit)? = null,
    dismiss: (ChildrenScope.() -> Unit)? = null,
    block: ComponentScope.() -> Unit = {},
): Component.Dialog {
    val s = ComponentScope().apply(block)
    return add(
        Component.Dialog(
            id = id,
            props = DialogProps(fixedOrNull(title), fixedOrNull(text), icon, slot(confirm), slot(dismiss)),
            events = s.events.toMap(), visible = s.visible, testTag = s.testTag, a11y = s.a11y, analytics = s.analytics,
        ),
    )
}
