package com.fabianofranca.sdui.contract

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull

/** Acessibilidade. [label] é o `contentDescription`; [heading] marca um título para leitores de tela. */
@Serializable
data class A11y(
    val label: String? = null,
    val heading: Boolean = false,
)

/** Com [name], o componente dispara impressão (ao aparecer) e clique com esse nome. */
@Serializable
data class AnalyticsInfo(
    val name: String,
    val properties: Map<String, JsonElement> = emptyMap(),
)

/**
 * Espaçamento interno em dp. No JSON, um número (os quatro lados) ou um objeto com
 * `all`, `horizontal`, `vertical`, `start`, `top`, `end` e `bottom` (o mais específico vence).
 */
@Serializable(with = PaddingSerializer::class)
data class Padding(
    val start: Int = 0,
    val top: Int = 0,
    val end: Int = 0,
    val bottom: Int = 0,
) {
    companion object {
        fun all(value: Int): Padding = Padding(value, value, value, value)

        fun symmetric(horizontal: Int = 0, vertical: Int = 0): Padding =
            Padding(horizontal, vertical, horizontal, vertical)
    }
}

@Serializable
private data class PaddingSurrogate(
    val all: Int? = null,
    val horizontal: Int? = null,
    val vertical: Int? = null,
    val start: Int? = null,
    val top: Int? = null,
    val end: Int? = null,
    val bottom: Int? = null,
)

internal object PaddingSerializer : KSerializer<Padding> {
    override val descriptor: SerialDescriptor = PaddingSurrogate.serializer().descriptor

    override fun deserialize(decoder: Decoder): Padding {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Padding só é lido de JSON")
        val element = input.decodeJsonElement()
        if (element is JsonPrimitive) {
            return Padding.all(element.intOrNull ?: throw SerializationException("padding inválido: $element"))
        }
        val s = input.json.decodeFromJsonElement(PaddingSurrogate.serializer(), element)
        val all = s.all ?: 0
        return Padding(
            start = s.start ?: s.horizontal ?: all,
            top = s.top ?: s.vertical ?: all,
            end = s.end ?: s.horizontal ?: all,
            bottom = s.bottom ?: s.vertical ?: all,
        )
    }

    override fun serialize(encoder: Encoder, value: Padding) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("Padding só é escrito em JSON")
        if (value.start == value.top && value.top == value.end && value.end == value.bottom) {
            output.encodeJsonElement(JsonPrimitive(value.start))
        } else {
            output.encodeJsonElement(
                output.json.encodeToJsonElement(
                    PaddingSurrogate.serializer(),
                    PaddingSurrogate(start = value.start, top = value.top, end = value.end, bottom = value.bottom),
                ),
            )
        }
    }
}

/** Papéis de cor do Material 3. O JSON nunca traz cores soltas. */
@Serializable
enum class ColorRole {
    @SerialName("primary") Primary,
    @SerialName("onPrimary") OnPrimary,
    @SerialName("primaryContainer") PrimaryContainer,
    @SerialName("onPrimaryContainer") OnPrimaryContainer,
    @SerialName("secondary") Secondary,
    @SerialName("onSecondary") OnSecondary,
    @SerialName("secondaryContainer") SecondaryContainer,
    @SerialName("onSecondaryContainer") OnSecondaryContainer,
    @SerialName("tertiary") Tertiary,
    @SerialName("onTertiary") OnTertiary,
    @SerialName("tertiaryContainer") TertiaryContainer,
    @SerialName("onTertiaryContainer") OnTertiaryContainer,
    @SerialName("error") Error,
    @SerialName("onError") OnError,
    @SerialName("errorContainer") ErrorContainer,
    @SerialName("onErrorContainer") OnErrorContainer,
    @SerialName("background") Background,
    @SerialName("onBackground") OnBackground,
    @SerialName("surface") Surface,
    @SerialName("onSurface") OnSurface,
    @SerialName("surfaceVariant") SurfaceVariant,
    @SerialName("onSurfaceVariant") OnSurfaceVariant,
    @SerialName("surfaceContainerLowest") SurfaceContainerLowest,
    @SerialName("surfaceContainerLow") SurfaceContainerLow,
    @SerialName("surfaceContainer") SurfaceContainer,
    @SerialName("surfaceContainerHigh") SurfaceContainerHigh,
    @SerialName("surfaceContainerHighest") SurfaceContainerHighest,
    @SerialName("inverseSurface") InverseSurface,
    @SerialName("inverseOnSurface") InverseOnSurface,
    @SerialName("inversePrimary") InversePrimary,
    @SerialName("outline") Outline,
    @SerialName("outlineVariant") OutlineVariant,
}

/** Papéis de tipografia do Material 3. */
@Serializable
enum class TypographyRole {
    @SerialName("displayLarge") DisplayLarge,
    @SerialName("displayMedium") DisplayMedium,
    @SerialName("displaySmall") DisplaySmall,
    @SerialName("headlineLarge") HeadlineLarge,
    @SerialName("headlineMedium") HeadlineMedium,
    @SerialName("headlineSmall") HeadlineSmall,
    @SerialName("titleLarge") TitleLarge,
    @SerialName("titleMedium") TitleMedium,
    @SerialName("titleSmall") TitleSmall,
    @SerialName("bodyLarge") BodyLarge,
    @SerialName("bodyMedium") BodyMedium,
    @SerialName("bodySmall") BodySmall,
    @SerialName("labelLarge") LabelLarge,
    @SerialName("labelMedium") LabelMedium,
    @SerialName("labelSmall") LabelSmall,
}

@Serializable
enum class HorizontalAlignment {
    @SerialName("start") Start,
    @SerialName("center") Center,
    @SerialName("end") End,
}

@Serializable
enum class VerticalAlignment {
    @SerialName("top") Top,
    @SerialName("center") Center,
    @SerialName("bottom") Bottom,
}

@Serializable
enum class HorizontalArrangement {
    @SerialName("start") Start,
    @SerialName("center") Center,
    @SerialName("end") End,
    @SerialName("spaceBetween") SpaceBetween,
    @SerialName("spaceAround") SpaceAround,
    @SerialName("spaceEvenly") SpaceEvenly,
}

@Serializable
enum class BoxAlignment {
    @SerialName("topStart") TopStart,
    @SerialName("topCenter") TopCenter,
    @SerialName("topEnd") TopEnd,
    @SerialName("centerStart") CenterStart,
    @SerialName("center") Center,
    @SerialName("centerEnd") CenterEnd,
    @SerialName("bottomStart") BottomStart,
    @SerialName("bottomCenter") BottomCenter,
    @SerialName("bottomEnd") BottomEnd,
}

@Serializable
enum class TextAlignment {
    @SerialName("start") Start,
    @SerialName("center") Center,
    @SerialName("end") End,
    @SerialName("justify") Justify,
}
