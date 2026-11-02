package com.fabianofranca.sdui.contract

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/** O que acontece quando um gatilho dispara (seção 6 do briefing). O `type` no JSON é o [SerialName]. */
@Serializable
sealed class Action {

    // Navegação

    /** Abre a rota [route]: uma tela do SDUI (o app pede ao BFF) ou uma tela nativa registrada no app. */
    @Serializable
    @SerialName("navigate")
    data class Navigate(
        val route: String,
        /** Parâmetros da rota. Aceitam ligações ao estado. */
        val params: Map<String, JsonElement> = emptyMap(),
        /** Troca a tela atual em vez de empilhar. */
        val replace: Boolean = false,
    ) : Action()

    @Serializable
    @SerialName("navigateBack")
    data object NavigateBack : Action()

    @Serializable
    @SerialName("openDeeplink")
    data class OpenDeeplink(val uri: String) : Action()

    @Serializable
    @SerialName("openUrl")
    data class OpenUrl(val url: String, val mode: UrlMode = UrlMode.External) : Action()

    // Camadas

    @Serializable
    @SerialName("openSheet")
    data class OpenSheet(val id: String) : Action()

    /** Fecha o BottomSheet [id]; sem [id], o último aberto. */
    @Serializable
    @SerialName("closeSheet")
    data class CloseSheet(val id: String? = null) : Action()

    @Serializable
    @SerialName("openDialog")
    data class OpenDialog(val id: String) : Action()

    /** Fecha o Dialog [id]; sem [id], o último aberto. */
    @Serializable
    @SerialName("closeDialog")
    data class CloseDialog(val id: String? = null) : Action()

    @Serializable
    @SerialName("showSnackbar")
    data class ShowSnackbar(
        val message: Bindable<String>,
        val actionLabel: String? = null,
        val action: ActionRef? = null,
        val duration: SnackbarLength = SnackbarLength.Short,
        /** Quando o Snackbar some sem a pessoa tocar na ação. */
        val onDismiss: ActionRef? = null,
    ) : Action()

    // Dados

    /**
     * Chama o BFF. O corpo da resposta pode trazer um [RequestResult]
     * (estado, erros por campo, itens para acrescentar e uma ação).
     */
    @Serializable
    @SerialName("request")
    data class Request(
        val method: RequestMethod = RequestMethod.Get,
        /** Relativo à URL base do BFF, ou absoluto. */
        val endpoint: String,
        /** Parâmetros de consulta. Aceitam ligações ao estado. */
        val query: Map<String, JsonElement> = emptyMap(),
        /** Corpo JSON. Aceita ligações ao estado em qualquer nível. */
        val body: JsonElement? = null,
        /** Onde mostrar o carregamento. */
        val loading: LoadingMode = LoadingMode.Component,
        val onSuccess: ActionRef? = null,
        val onError: ActionRef? = null,
    ) : Action()

    /** Grava [value] em [key]. O valor aceita ligação ao estado (copia de outra chave). */
    @Serializable
    @SerialName("setState")
    data class SetState(val key: String, val value: JsonElement) : Action()

    /** Valida os campos [fields] (ids) pelas regras de cada um. */
    @Serializable
    @SerialName("validate")
    data class Validate(
        val fields: List<String>,
        val onValid: ActionRef? = null,
        val onInvalid: ActionRef? = null,
    ) : Action()

    /** Pede a tela de novo ao BFF. */
    @Serializable
    @SerialName("refreshScreen")
    data object RefreshScreen : Action()

    // Observabilidade

    @Serializable
    @SerialName("track")
    data class Track(
        val name: String,
        /** Aceitam ligações ao estado. */
        val properties: Map<String, JsonElement> = emptyMap(),
    ) : Action()

    // Composição

    /** Executa [actions] em ordem; cada uma termina antes da próxima começar. */
    @Serializable
    @SerialName(Sequence.TYPE)
    data class Sequence(val actions: List<ActionRef>) : Action() {
        companion object {
            const val TYPE = "sequence"
        }
    }

    /** Ação de `type` que esta versão não conhece. Não faz nada (e é registrada em log). */
    @Serializable(with = UnknownActionSerializer::class)
    data class Unknown(val type: String) : Action()
}

/** Atalho: várias ações em ordem. */
fun sequenceOf(vararg actions: Action): Action =
    if (actions.size == 1) actions.single() else Action.Sequence(actions.toList())

@Serializable
enum class UrlMode {
    @SerialName("external")
    External,

    @SerialName("inApp")
    InApp,
}

@Serializable
enum class SnackbarLength {
    @SerialName("short")
    Short,

    @SerialName("long")
    Long,

    @SerialName("indefinite")
    Indefinite,
}

@Serializable
enum class RequestMethod {
    @SerialName("GET")
    Get,

    @SerialName("POST")
    Post,

    @SerialName("PUT")
    Put,

    @SerialName("PATCH")
    Patch,

    @SerialName("DELETE")
    Delete,
}

@Serializable
enum class LoadingMode {
    /** Nenhum indicador. */
    @SerialName("none")
    None,

    /** Cobre a tela e bloqueia o toque. */
    @SerialName("screen")
    Screen,

    /** No componente que disparou a ação (o Button mostra um indicador e fica desabilitado). */
    @SerialName("component")
    Component,
}

internal object UnknownActionSerializer : KSerializer<Action.Unknown> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("unknown")

    override fun deserialize(decoder: Decoder): Action.Unknown = Action.Unknown("unknown")

    override fun serialize(encoder: Encoder, value: Action.Unknown) {
        encoder.beginStructure(descriptor).endStructure(descriptor)
    }
}

internal class UnknownActionDeserializer(private val type: String) : DeserializationStrategy<Action> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("unknown:$type")

    override fun deserialize(decoder: Decoder): Action {
        // Consome o objeto inteiro sem interpretá-lo.
        decoder.decodeSerializableValue(JsonObject.serializer())
        return Action.Unknown(type)
    }
}
