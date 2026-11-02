package com.fabianofranca.sdui.engine

import com.fabianofranca.sdui.contract.Bindable
import com.fabianofranca.sdui.contract.asStateRef
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull

// Leitura das props: valor fixo ou ligação ao estado ({ "$state": "chave" }).

/** O valor de uma ligação. Com `equals`, o resultado é `true` ou `false`. */
fun StateStore.read(binding: Bindable.State): JsonElement {
    val value = this[binding.key] ?: JsonNull
    val expected = binding.equals ?: return value
    return JsonPrimitive(value == expected)
}

fun <T> StateStore.resolve(bindable: Bindable<T>, convert: (JsonElement) -> T?, default: T): T = when (bindable) {
    is Bindable.Fixed -> bindable.value
    is Bindable.State -> convert(read(bindable)) ?: default
}

fun StateStore.string(bindable: Bindable<String>): String = resolve(bindable, JsonElement::asText, "")

fun StateStore.stringOrNull(bindable: Bindable<String>?): String? = bindable?.let { string(it) }

fun StateStore.boolean(bindable: Bindable<Boolean>, default: Boolean = false): Boolean =
    resolve(bindable, JsonElement::asBoolean, default)

fun StateStore.int(bindable: Bindable<Int>?): Int? = bindable?.let { resolveNullable(it) { e -> (e as? JsonPrimitive)?.intOrNull } }

fun StateStore.float(bindable: Bindable<Float>?): Float? =
    bindable?.let { resolveNullable(it) { e -> (e as? JsonPrimitive)?.floatOrNull } }

private fun <T : Any> StateStore.resolveNullable(bindable: Bindable<T>, convert: (JsonElement) -> T?): T? = when (bindable) {
    is Bindable.Fixed -> bindable.value
    is Bindable.State -> convert(read(bindable))
}

/** Troca, em qualquer nível, cada `{ "$state": ... }` pelo valor do estado. */
fun StateStore.resolveJson(element: JsonElement): JsonElement {
    element.asStateRef()?.let { return read(it) }
    return when (element) {
        is JsonObject -> JsonObject(element.mapValues { (_, value) -> resolveJson(value) })
        is JsonArray -> JsonArray(element.map { resolveJson(it) })
        else -> element
    }
}

fun StateStore.resolveJson(values: Map<String, JsonElement>): Map<String, JsonElement> =
    values.mapValues { (_, value) -> resolveJson(value) }

/** Texto de um valor do estado: o conteúdo de um primitivo, vazio para `null`. */
fun JsonElement.asText(): String = when (this) {
    is JsonNull -> ""
    is JsonPrimitive -> content
    else -> toString()
}

fun JsonElement.asBoolean(): Boolean? = when (this) {
    is JsonNull -> null
    is JsonPrimitive -> booleanOrNull ?: content.isNotEmpty().takeIf { isString }
    is JsonArray -> isNotEmpty()
    is JsonObject -> isNotEmpty()
}

/** O valor fixo de uma prop como JSON (para semear o estado de um campo). */
fun Bindable.Fixed<*>.toJson(): JsonElement = when (val v = value) {
    null -> JsonNull
    is JsonElement -> v
    is Boolean -> JsonPrimitive(v)
    is Number -> JsonPrimitive(v)
    else -> JsonPrimitive(v.toString())
}
