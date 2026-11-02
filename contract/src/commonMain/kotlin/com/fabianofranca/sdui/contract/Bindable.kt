package com.fabianofranca.sdui.contract

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

/** A chave que marca uma ligação ao estado: `{ "$state": "chave" }`. */
const val STATE_REF_KEY: String = "\$state"

/** Chave opcional de uma ligação: `{ "$state": "aba", "equals": "noticias" }` vale `true` ou `false`. */
const val STATE_EQUALS_KEY: String = "equals"

/**
 * Uma prop que é um valor fixo ([Fixed]) ou uma ligação ao estado da tela ([State]).
 *
 * No JSON, o valor fixo é o próprio valor (`"Salvar"`, `true`, `3`) e a ligação é um objeto:
 * `{ "$state": "email" }`. Com `equals`, a ligação vira uma comparação que dá `true` ou `false`:
 * `{ "$state": "aba", "equals": "noticias" }` (para `visible`, `enabled`, `checked`…).
 */
@Serializable(with = BindableSerializer::class)
sealed interface Bindable<out T> {
    data class Fixed<out T>(val value: T) : Bindable<T>

    data class State(val key: String, val equals: JsonElement? = null) : Bindable<Nothing>
}

/** Um valor fixo. */
fun <T> fixed(value: T): Bindable<T> = Bindable.Fixed(value)

/** Uma ligação à chave [key] do estado da tela. */
fun state(key: String): Bindable.State = Bindable.State(key)

/** Uma ligação que vale `true` quando o estado em [key] é igual a [value]. */
fun stateEquals(key: String, value: JsonElement): Bindable.State = Bindable.State(key, value)

/** Lê uma ligação ao estado em qualquer [JsonElement]; `null` se não for uma. */
fun JsonElement.asStateRef(): Bindable.State? {
    if (this !is JsonObject) return null
    val key = (this[STATE_REF_KEY] as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return null
    if (keys.any { it != STATE_REF_KEY && it != STATE_EQUALS_KEY }) return null
    return Bindable.State(key, this[STATE_EQUALS_KEY])
}

/** O JSON de uma ligação ao estado. */
fun Bindable.State.toJson(): JsonObject = buildJsonObject {
    put(STATE_REF_KEY, JsonPrimitive(key))
    equals?.let { put(STATE_EQUALS_KEY, it) }
}

class BindableSerializer<T>(private val valueSerializer: KSerializer<T>) : KSerializer<Bindable<T>> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        SerialDescriptor("com.fabianofranca.sdui.contract.Bindable", JsonElement.serializer().descriptor)

    override fun deserialize(decoder: Decoder): Bindable<T> {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Bindable só é lido de JSON")
        val element = input.decodeJsonElement()
        element.asStateRef()?.let { return it }
        return Bindable.Fixed(input.json.decodeFromJsonElement(valueSerializer, element))
    }

    override fun serialize(encoder: Encoder, value: Bindable<T>) {
        val output = encoder as? JsonEncoder ?: throw SerializationException("Bindable só é escrito em JSON")
        val element = when (value) {
            is Bindable.Fixed -> output.json.encodeToJsonElement(valueSerializer, value.value)
            is Bindable.State -> value.toJson()
        }
        output.encodeJsonElement(element)
    }
}
