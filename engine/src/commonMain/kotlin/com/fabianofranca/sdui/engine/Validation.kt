package com.fabianofranca.sdui.engine

import com.fabianofranca.sdui.contract.RuleType
import com.fabianofranca.sdui.contract.ValidationRule
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

private val EmailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/**
 * A mensagem da primeira regra que [value] não cumpre, ou `null` se cumpre todas.
 * Fora `required`, as regras só olham campos preenchidos (campo vazio e opcional passa).
 */
fun firstFailure(rules: List<ValidationRule>, value: JsonElement?): String? {
    val text = value?.asText().orEmpty()
    val filled = when (value) {
        null, JsonNull -> false
        is JsonPrimitive -> if (value.isString) text.isNotBlank() else value.booleanOrNull ?: true
        else -> value.asBoolean() == true
    }
    for (rule in rules) {
        val ok = when (rule.type) {
            RuleType.Required -> filled
            RuleType.MinLength -> !filled || text.length >= (rule.value?.toIntOrNull() ?: 0)
            RuleType.MaxLength -> !filled || text.length <= (rule.value?.toIntOrNull() ?: Int.MAX_VALUE)
            RuleType.Pattern -> !filled || rule.value?.let { pattern ->
                runCatching { Regex(pattern).matches(text) }.getOrDefault(true)
            } ?: true
            RuleType.Email -> !filled || EmailRegex.matches(text)
            RuleType.Unknown -> true
        }
        if (!ok) return rule.message
    }
    return null
}
