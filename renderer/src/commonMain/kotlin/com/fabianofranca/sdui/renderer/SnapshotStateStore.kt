package com.fabianofranca.sdui.renderer

import androidx.compose.runtime.mutableStateMapOf
import com.fabianofranca.sdui.engine.StateStore
import kotlinx.serialization.json.JsonElement

/**
 * O estado da tela no estado do Compose: cada leitura é observada por chave, então mudar
 * `email` só redesenha quem lê `email`.
 */
class SnapshotStateStore(initial: Map<String, JsonElement> = emptyMap()) : StateStore {
    private val values = mutableStateMapOf<String, JsonElement>().apply { putAll(initial) }

    override fun get(key: String): JsonElement? = values[key]

    override fun set(key: String, value: JsonElement) {
        values[key] = value
    }

    override fun contains(key: String): Boolean = key in values

    override fun snapshot(): Map<String, JsonElement> = values.toMap()
}
