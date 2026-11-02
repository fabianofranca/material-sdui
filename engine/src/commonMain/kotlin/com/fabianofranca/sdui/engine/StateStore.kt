package com.fabianofranca.sdui.engine

import kotlinx.serialization.json.JsonElement

/**
 * O estado local de uma tela: chave → valor JSON.
 *
 * O renderer usa uma implementação apoiada no estado do Compose, para que só se redesenhe
 * o que lê a chave que mudou; [MapStateStore] serve a testes e a usos sem Compose.
 */
interface StateStore {
    operator fun get(key: String): JsonElement?

    operator fun set(key: String, value: JsonElement)

    operator fun contains(key: String): Boolean

    fun snapshot(): Map<String, JsonElement>
}

class MapStateStore(initial: Map<String, JsonElement> = emptyMap()) : StateStore {
    private val values = LinkedHashMap(initial)

    override fun get(key: String): JsonElement? = values[key]

    override fun set(key: String, value: JsonElement) {
        values[key] = value
    }

    override fun contains(key: String): Boolean = key in values

    override fun snapshot(): Map<String, JsonElement> = values.toMap()
}
