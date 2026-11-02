package com.fabianofranca.sdui.contract

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Estrutura

@Serializable
data class TopAppBarProps(
    val title: Bindable<String> = fixed(""),
    val style: TopAppBarStyle = TopAppBarStyle.Small,
    /** Em geral um [Component.IconButton] com `navigateBack`. */
    val navigationIcon: Component? = null,
    /** Em geral [Component.IconButton]s. */
    val actions: List<Component> = emptyList(),
)

@Serializable
enum class TopAppBarStyle {
    @SerialName("small") Small,
    @SerialName("center") Center,
    @SerialName("medium") Medium,
    @SerialName("large") Large,
}

/** Column, Row e LazyColumn ocupam a largura disponível. */
@Serializable
data class ColumnProps(
    val spacing: Int = 0,
    val padding: Padding? = null,
    val horizontalAlignment: HorizontalAlignment = HorizontalAlignment.Start,
    val scroll: Boolean = false,
)

@Serializable
data class RowProps(
    val spacing: Int = 0,
    val padding: Padding? = null,
    val verticalAlignment: VerticalAlignment = VerticalAlignment.Center,
    val horizontalArrangement: HorizontalArrangement = HorizontalArrangement.Start,
    /** Quebra a linha quando os filhos não cabem (chips, etiquetas). */
    val wrap: Boolean = false,
)

@Serializable
data class BoxProps(
    val contentAlignment: BoxAlignment = BoxAlignment.TopStart,
    val padding: Padding? = null,
)

@Serializable
data class LazyColumnProps(
    val spacing: Int = 0,
    val padding: Padding? = null,
)

@Serializable
data class SpacerProps(val size: Int = 8)

@Serializable
data class DividerProps(val inset: Int = 0)

// Conteúdo

@Serializable
data class TextProps(
    val text: Bindable<String> = fixed(""),
    val style: TypographyRole = TypographyRole.BodyMedium,
    val color: ColorRole? = null,
    val maxLines: Int? = null,
    val align: TextAlignment? = null,
)

@Serializable
data class ImageProps(
    val url: Bindable<String> = fixed(""),
    val contentScale: ImageScale = ImageScale.Crop,
    /** Largura ÷ altura. Com ele, a imagem ocupa a largura disponível. */
    val aspectRatio: Float? = null,
    /** Nome de um ícone mostrado enquanto a imagem carrega ou se ela falhar. */
    val placeholder: String? = null,
    /** Lado em dp de uma imagem quadrada (miniaturas, avatares). Tem prioridade sobre [aspectRatio]. */
    val size: Int? = null,
)

@Serializable
enum class ImageScale {
    @SerialName("crop") Crop,
    @SerialName("fit") Fit,
    @SerialName("fillBounds") FillBounds,
    @SerialName("fillWidth") FillWidth,
    @SerialName("fillHeight") FillHeight,
    @SerialName("inside") Inside,
    @SerialName("none") None,
}

/** [name] segue os nomes dos Material Symbols em snake_case (`arrow_back`, `settings`). */
@Serializable
data class IconProps(
    val name: String = "",
    val tint: ColorRole? = null,
    val size: Int = 24,
)

@Serializable
data class CardProps(
    val style: CardStyle = CardStyle.Filled,
    /** Espaço entre a borda do card e os filhos. `0` para encostar (uma imagem de capa, por exemplo). */
    val padding: Padding = Padding.all(16),
    val spacing: Int = 8,
)

@Serializable
enum class CardStyle {
    @SerialName("filled") Filled,
    @SerialName("elevated") Elevated,
    @SerialName("outlined") Outlined,
}

@Serializable
data class ListItemProps(
    val headline: Bindable<String> = fixed(""),
    val supporting: Bindable<String>? = null,
    val overline: Bindable<String>? = null,
    /** Em geral um [Component.Icon], [Component.Image] ou [Component.Checkbox]. */
    val leading: Component? = null,
    /** Em geral um [Component.Icon], [Component.Text], [Component.Switch] ou [Component.IconButton]. */
    val trailing: Component? = null,
)

/** Sem [count], o selo é um ponto. Com [count] menor ou igual a zero, não aparece. */
@Serializable
data class BadgeProps(val count: Bindable<Int>? = null)

/** Sem [progress] (0 a 1), o indicador é indeterminado. */
@Serializable
data class ProgressIndicatorProps(
    val kind: ProgressKind = ProgressKind.Circular,
    val progress: Bindable<Float>? = null,
)

@Serializable
enum class ProgressKind {
    @SerialName("linear") Linear,
    @SerialName("circular") Circular,
}

// Ação e formulário

@Serializable
data class ButtonProps(
    val label: Bindable<String> = fixed(""),
    val style: ButtonStyle = ButtonStyle.Filled,
    val icon: String? = null,
    val enabled: Bindable<Boolean> = fixed(true),
    val loading: Bindable<Boolean> = fixed(false),
    val fullWidth: Boolean = false,
)

@Serializable
enum class ButtonStyle {
    @SerialName("filled") Filled,
    @SerialName("tonal") Tonal,
    @SerialName("elevated") Elevated,
    @SerialName("outlined") Outlined,
    @SerialName("text") Text,
}

@Serializable
data class IconButtonProps(
    val icon: String = "",
    val style: IconButtonStyle = IconButtonStyle.Standard,
    val enabled: Bindable<Boolean> = fixed(true),
)

@Serializable
enum class IconButtonStyle {
    @SerialName("standard") Standard,
    @SerialName("filled") Filled,
    @SerialName("tonal") Tonal,
    @SerialName("outlined") Outlined,
}

/** Com [label], o FAB é estendido. */
@Serializable
data class FabProps(
    val icon: String = "",
    val label: Bindable<String>? = null,
    val size: FabSize = FabSize.Regular,
)

@Serializable
enum class FabSize {
    @SerialName("small") Small,
    @SerialName("regular") Regular,
    @SerialName("large") Large,
}

/**
 * Com [value] ligado ao estado, o que a pessoa digita é gravado nessa chave.
 * Com valor fixo, o campo guarda o valor no estado pela chave igual ao `id` do componente.
 */
@Serializable
data class TextFieldProps(
    val value: Bindable<String> = fixed(""),
    val label: Bindable<String>? = null,
    val placeholder: Bindable<String>? = null,
    val supportingText: Bindable<String>? = null,
    /** Mensagem de erro; vazia ou ausente = sem erro. Erros de `validate` e do BFF têm prioridade. */
    val error: Bindable<String>? = null,
    val keyboard: KeyboardKind = KeyboardKind.Text,
    val imeAction: ImeActionKind = ImeActionKind.Default,
    val maxLength: Int? = null,
    val style: TextFieldStyle = TextFieldStyle.Filled,
    val singleLine: Boolean = true,
    val enabled: Bindable<Boolean> = fixed(true),
    val validation: List<ValidationRule> = emptyList(),
)

@Serializable
enum class KeyboardKind {
    @SerialName("text") Text,
    @SerialName("email") Email,
    @SerialName("number") Number,
    @SerialName("phone") Phone,
    @SerialName("password") Password,
}

@Serializable
enum class ImeActionKind {
    @SerialName("default") Default,
    @SerialName("done") Done,
    @SerialName("next") Next,
    @SerialName("search") Search,
    @SerialName("send") Send,
    @SerialName("go") Go,
}

@Serializable
enum class TextFieldStyle {
    @SerialName("filled") Filled,
    @SerialName("outlined") Outlined,
}

@Serializable
data class CheckboxProps(
    val checked: Bindable<Boolean> = fixed(false),
    val label: Bindable<String>? = null,
    val enabled: Bindable<Boolean> = fixed(true),
    /** Só `required` faz sentido aqui: o campo precisa estar marcado. */
    val validation: List<ValidationRule> = emptyList(),
)

@Serializable
data class SwitchProps(
    val checked: Bindable<Boolean> = fixed(false),
    val label: Bindable<String>? = null,
    val enabled: Bindable<Boolean> = fixed(true),
)

@Serializable
data class RadioGroupProps(
    val options: List<RadioOption> = emptyList(),
    /** O `value` da opção marcada. */
    val selected: Bindable<String> = fixed(""),
    val enabled: Bindable<Boolean> = fixed(true),
    val validation: List<ValidationRule> = emptyList(),
)

@Serializable
data class RadioOption(
    val value: String,
    val label: String,
    val enabled: Boolean = true,
)

/** No chip de filtro, o toque inverte [selected] (gravado no estado como nos campos). */
@Serializable
data class ChipProps(
    val label: Bindable<String> = fixed(""),
    val kind: ChipKind = ChipKind.Assist,
    val selected: Bindable<Boolean> = fixed(false),
    val icon: String? = null,
    val enabled: Bindable<Boolean> = fixed(true),
)

@Serializable
enum class ChipKind {
    @SerialName("assist") Assist,
    @SerialName("filter") Filter,
    @SerialName("input") Input,
    @SerialName("suggestion") Suggestion,
}

/** Uma regra de validação de campo, usada pela ação `validate`. */
@Serializable
data class ValidationRule(
    val type: RuleType = RuleType.Unknown,
    /** O tamanho em `minLength`/`maxLength`; a expressão regular em `pattern`. */
    val value: String? = null,
    val message: String,
)

/** Regra desconhecida passa sempre (compatibilidade). */
@Serializable
enum class RuleType {
    @SerialName("required") Required,
    @SerialName("minLength") MinLength,
    @SerialName("maxLength") MaxLength,
    @SerialName("pattern") Pattern,
    @SerialName("email") Email,
    @SerialName("unknown") Unknown,
}

// Navegação e camadas

/**
 * O toque num item grava o id dele em [selected] e executa o `onClick` do item; sem `onClick`,
 * navega para a `route` do item trocando a tela atual.
 */
@Serializable
data class NavigationBarProps(
    val items: List<NavigationItem> = emptyList(),
    val selected: Bindable<String> = fixed(""),
)

@Serializable
data class NavigationItem(
    val id: String,
    val icon: String,
    val label: String,
    val route: String? = null,
    /** Selo no ícone, como em [BadgeProps]. Ausente = sem selo. */
    val badge: BadgeProps? = null,
    val testTag: String? = null,
    val events: Events = emptyMap(),
)

/** O toque numa aba grava o id dela em [selected] e executa o `onClick` da aba. */
@Serializable
data class TabsProps(
    val items: List<TabItem> = emptyList(),
    val selected: Bindable<String> = fixed(""),
    val style: TabsStyle = TabsStyle.Primary,
)

@Serializable
data class TabItem(
    val id: String,
    val label: String,
    val icon: String? = null,
    val testTag: String? = null,
    val events: Events = emptyMap(),
)

@Serializable
enum class TabsStyle {
    @SerialName("primary") Primary,
    @SerialName("secondary") Secondary,
}

@Serializable
data class BottomSheetProps(val dragHandle: Boolean = true)

/** Tocar em [confirm] ou [dismiss] fecha o Dialog e depois executa o `onClick` do botão. */
@Serializable
data class DialogProps(
    val title: Bindable<String>? = null,
    val text: Bindable<String>? = null,
    val icon: String? = null,
    /** Em geral um [Component.Button] de estilo `text`. */
    val confirm: Component? = null,
    val dismiss: Component? = null,
)
