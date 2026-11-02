package com.fabianofranca.sdui.contract

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Um componente da tela (seção 5 do briefing). O `type` no JSON é o [SerialName] de cada classe.
 *
 * Todo componente tem [id] (estável e único na tela), [events] (gatilho → ação), [visible]
 * (fixo ou ligado ao estado), [testTag], [a11y] e [analytics]. As props ficam em `props`;
 * os filhos, em `children` (só nos contêineres).
 */
@Serializable
sealed class Component {
    abstract val id: String
    abstract val events: Events
    abstract val visible: Bindable<Boolean>
    abstract val testTag: String?
    abstract val a11y: A11y?
    abstract val analytics: AnalyticsInfo?

    /** Os filhos diretos, nos contêineres. */
    open val children: List<Component> get() = emptyList()


    /** Barra do topo. Vai em `screen.topBar`. */
    @Serializable
    @SerialName("TopAppBar")
    data class TopAppBar(
        override val id: String,
        val props: TopAppBarProps = TopAppBarProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("Column")
    data class Column(
        override val id: String,
        val props: ColumnProps = ColumnProps(),
        override val children: List<Component> = emptyList(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("Row")
    data class Row(
        override val id: String,
        val props: RowProps = RowProps(),
        override val children: List<Component> = emptyList(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    /** Empilha os filhos uns sobre os outros. */
    @Serializable
    @SerialName("Box")
    data class Box(
        override val id: String,
        val props: BoxProps = BoxProps(),
        override val children: List<Component> = emptyList(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    /** Lista longa, com `onLoadMore` (paginação) e `onRefresh` (puxar para atualizar). */
    @Serializable
    @SerialName("LazyColumn")
    data class LazyColumn(
        override val id: String,
        val props: LazyColumnProps = LazyColumnProps(),
        override val children: List<Component> = emptyList(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("Spacer")
    data class Spacer(
        override val id: String,
        val props: SpacerProps = SpacerProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("Divider")
    data class Divider(
        override val id: String,
        val props: DividerProps = DividerProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("Text")
    data class Text(
        override val id: String,
        val props: TextProps = TextProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("Image")
    data class Image(
        override val id: String,
        val props: ImageProps = ImageProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("Icon")
    data class Icon(
        override val id: String,
        val props: IconProps = IconProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    /** Com `onClick`, o card todo é clicável. */
    @Serializable
    @SerialName("Card")
    data class Card(
        override val id: String,
        val props: CardProps = CardProps(),
        override val children: List<Component> = emptyList(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    /** Com `onClick`, o item todo é clicável. */
    @Serializable
    @SerialName("ListItem")
    data class ListItem(
        override val id: String,
        val props: ListItemProps = ListItemProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    /** Um selo sobre os filhos (em geral um Icon). */
    @Serializable
    @SerialName("Badge")
    data class Badge(
        override val id: String,
        val props: BadgeProps = BadgeProps(),
        override val children: List<Component> = emptyList(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("ProgressIndicator")
    data class ProgressIndicator(
        override val id: String,
        val props: ProgressIndicatorProps = ProgressIndicatorProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    /** `onClick` e depois `onSubmit`, se houver. */
    @Serializable
    @SerialName("Button")
    data class Button(
        override val id: String,
        val props: ButtonProps = ButtonProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("IconButton")
    data class IconButton(
        override val id: String,
        val props: IconButtonProps = IconButtonProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    /** O FAB. Vai em `screen.floating`. */
    @Serializable
    @SerialName("FAB")
    data class Fab(
        override val id: String,
        val props: FabProps = FabProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    /** `onChange` a cada mudança; `onSubmit` na tecla de ação do teclado. */
    @Serializable
    @SerialName("TextField")
    data class TextField(
        override val id: String,
        val props: TextFieldProps = TextFieldProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("Checkbox")
    data class Checkbox(
        override val id: String,
        val props: CheckboxProps = CheckboxProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("Switch")
    data class Switch(
        override val id: String,
        val props: SwitchProps = SwitchProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("RadioGroup")
    data class RadioGroup(
        override val id: String,
        val props: RadioGroupProps = RadioGroupProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("Chip")
    data class Chip(
        override val id: String,
        val props: ChipProps = ChipProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    /** Barra de navegação inferior. Vai em `screen.bottomBar`. */
    @Serializable
    @SerialName("NavigationBar")
    data class NavigationBar(
        override val id: String,
        val props: NavigationBarProps = NavigationBarProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    @Serializable
    @SerialName("Tabs")
    data class Tabs(
        override val id: String,
        val props: TabsProps = TabsProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    /** Vai em `screen.modals`. Abre com `openSheet` e fecha com `closeSheet`. */
    @Serializable
    @SerialName("BottomSheet")
    data class BottomSheet(
        override val id: String,
        val props: BottomSheetProps = BottomSheetProps(),
        override val children: List<Component> = emptyList(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    /** Vai em `screen.modals`. Abre com `openDialog` e fecha com `closeDialog`. */
    @Serializable
    @SerialName("Dialog")
    data class Dialog(
        override val id: String,
        val props: DialogProps = DialogProps(),
        override val events: Events = emptyMap(),
        override val visible: Bindable<Boolean> = fixed(true),
        override val testTag: String? = null,
        override val a11y: A11y? = null,
        override val analytics: AnalyticsInfo? = null,
    ) : Component()

    /** Componente de `type` que esta versão não conhece. Não é desenhado (e é registrado em log). */
    @Serializable(with = UnknownComponentSerializer::class)
    data class Unknown(
        val type: String,
        override val id: String = "",
    ) : Component() {
        override val events: Events get() = emptyMap()
        override val visible: Bindable<Boolean> get() = fixed(false)
        override val testTag: String? get() = null
        override val a11y: A11y? get() = null
        override val analytics: AnalyticsInfo? get() = null
    }
}

internal object UnknownComponentSerializer : KSerializer<Component.Unknown> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("unknown")

    override fun deserialize(decoder: Decoder): Component.Unknown = UnknownComponentDeserializer("unknown")
        .deserialize(decoder) as Component.Unknown

    override fun serialize(encoder: Encoder, value: Component.Unknown) {
        encoder.beginStructure(descriptor).endStructure(descriptor)
    }
}

internal class UnknownComponentDeserializer(private val type: String) : DeserializationStrategy<Component> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("unknown:$type")

    override fun deserialize(decoder: Decoder): Component {
        val element = decoder.decodeSerializableValue(JsonObject.serializer())
        val id = (element["id"] as? JsonPrimitive)?.takeIf { it.isString }?.content.orEmpty()
        return Component.Unknown(type, id)
    }
}
