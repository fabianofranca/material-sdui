package com.fabianofranca.sdui.contract

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.buildJsonObject

/** Quando algo acontece (seção 6 do briefing). A [key] é o nome no JSON. */
enum class Trigger(val key: String) {
    /** Toque: botões, card, item de lista, chip, aba, item da barra de navegação. */
    OnClick("onClick"),

    /** Valor mudou: TextField, Checkbox, Switch, RadioGroup, Chip de filtro, Tabs, NavigationBar. */
    OnChange("onChange"),

    /** Envio do formulário: tecla de ação do TextField, Button. */
    OnSubmit("onSubmit"),

    /** A tela ou o componente ficou visível. */
    OnAppear("onAppear"),

    /** Puxar para atualizar: tela, LazyColumn. */
    OnRefresh("onRefresh"),

    /** Chegou ao fim da lista: LazyColumn. */
    OnLoadMore("onLoadMore"),

    /** A pessoa fechou a camada: BottomSheet, Dialog, Snackbar. */
    OnDismiss("onDismiss"),

    /** Voltar do sistema: tela. */
    OnBack("onBack"),
    ;

    companion object {
        private val byKey = entries.associateBy { it.key }

        fun fromKey(key: String): Trigger? = byKey[key]
    }
}

/** Gatilho → ação. No JSON, um objeto `{ "onClick": { ... } }`. */
typealias Events = @Serializable(with = EventsSerializer::class) Map<Trigger, Action>

/** Uma ação no JSON: um objeto ou uma lista de ações (que vira [Action.Sequence]). */
typealias ActionRef = @Serializable(with = ActionSerializer::class) Action

/** Aceita uma lista de ações no lugar de uma ação, como atalho para `sequence`. */
object ActionSerializer : JsonTransformingSerializer<Action>(Action.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        if (element is JsonArray) {
            buildJsonObject {
                put("type", JsonPrimitive(Action.Sequence.TYPE))
                put("actions", element)
            }
        } else {
            element
        }
}

/** Lê o mapa de eventos descartando gatilhos que esta versão não conhece. */
object EventsSerializer : KSerializer<Map<Trigger, Action>> {
    override val descriptor: SerialDescriptor =
        MapSerializer(String.serializer(), JsonElement.serializer()).descriptor

    override fun deserialize(decoder: Decoder): Map<Trigger, Action> {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Eventos só são lidos de JSON")
        val element = input.decodeJsonElement() as? JsonObject ?: return emptyMap()
        return buildMap {
            for ((key, value) in element) {
                val trigger = Trigger.fromKey(key) ?: continue
                put(trigger, input.json.decodeFromJsonElement(ActionSerializer, value))
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Map<Trigger, Action>) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("Eventos só são escritos em JSON")
        output.encodeJsonElement(
            buildJsonObject {
                for ((trigger, action) in value) {
                    put(trigger.key, output.json.encodeToJsonElement(Action.serializer(), action))
                }
            },
        )
    }
}
