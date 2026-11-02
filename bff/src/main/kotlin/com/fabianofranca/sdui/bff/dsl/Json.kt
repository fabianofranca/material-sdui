package com.fabianofranca.sdui.bff.dsl

import com.fabianofranca.sdui.contract.Bindable
import com.fabianofranca.sdui.contract.toJson
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Converte um valor Kotlin em JSON: `null`, texto, número, booleano, mapa, lista, [JsonElement]
 * e ligações ao estado (`state("email")` vira `{ "$state": "email" }`).
 */
fun jsonOf(value: Any?): JsonElement = when (value) {
    null -> JsonNull
    is JsonElement -> value
    is String -> JsonPrimitive(value)
    is Number -> JsonPrimitive(value)
    is Boolean -> JsonPrimitive(value)
    is Bindable.State -> value.toJson()
    is Bindable.Fixed<*> -> jsonOf(value.value)
    is Map<*, *> -> JsonObject(value.entries.associate { (k, v) -> k.toString() to jsonOf(v) })
    is Iterable<*> -> JsonArray(value.map(::jsonOf))
    is Array<*> -> JsonArray(value.map(::jsonOf))
    is Enum<*> -> JsonPrimitive(value.name)
    else -> throw IllegalArgumentException("Sem conversão para JSON: ${value::class}")
}

/** Um objeto JSON: `json("email" to state("email"), "newsletter" to true)`. */
fun json(vararg pairs: Pair<String, Any?>): JsonObject = JsonObject(pairs.associate { (k, v) -> k to jsonOf(v) })

internal fun jsonMap(pairs: Array<out Pair<String, Any?>>): Map<String, JsonElement> =
    pairs.associate { (k, v) -> k to jsonOf(v) }
