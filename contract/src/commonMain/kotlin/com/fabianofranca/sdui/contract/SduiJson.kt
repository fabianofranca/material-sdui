package com.fabianofranca.sdui.contract

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/** Versão do contrato que este código entende (e o BFF gera). */
const val CONTRACT_VERSION: Int = 1

/** Cabeçalho em que o app manda a maior versão do contrato que entende. */
const val CONTRACT_VERSION_HEADER: String = "X-SDUI-Version"

/**
 * O [Json] do contrato. O BFF serializa e os apps desserializam com ele.
 *
 * Compatibilidade (seção 7 do briefing):
 * - chave desconhecida é ignorada (prop nova é sempre opcional);
 * - valor de enum desconhecido cai no padrão da prop;
 * - componente de `type` desconhecido vira [Component.Unknown] (não é desenhado);
 * - ação de `type` desconhecido vira [Action.Unknown] (não faz nada);
 * - gatilho desconhecido é descartado.
 */
val SduiJson: Json = Json {
    classDiscriminator = "type"
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
    encodeDefaults = false
    serializersModule = SerializersModule {
        polymorphic(Component::class) {
            defaultDeserializer { type -> UnknownComponentDeserializer(type.orEmpty()) }
        }
        polymorphic(Action::class) {
            defaultDeserializer { type -> UnknownActionDeserializer(type.orEmpty()) }
        }
    }
}
