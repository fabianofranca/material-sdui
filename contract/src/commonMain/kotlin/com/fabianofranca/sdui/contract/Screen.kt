package com.fabianofranca.sdui.contract

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** A resposta do BFF para uma tela: a versão do contrato usada e a tela. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ScreenResponse(
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val version: Int = CONTRACT_VERSION,
    val screen: Screen,
)

/**
 * Uma tela. As raízes são [topBar], [content], [bottomBar], [floating] (o FAB, ancorado)
 * e [modals] (BottomSheet e Dialog, fechados até uma ação abri-los).
 */
@Serializable
data class Screen(
    val id: String,
    val title: String? = null,
    /** Valores iniciais do estado local da tela. */
    val state: Map<String, JsonElement> = emptyMap(),
    val topBar: Component? = null,
    val content: Component,
    val bottomBar: Component? = null,
    val floating: Component? = null,
    val modals: List<Component> = emptyList(),
    /** Gatilhos da tela: `onAppear`, `onRefresh` e `onBack`. */
    val events: Events = emptyMap(),
)

/**
 * O que o BFF pode devolver no corpo de uma resposta a [Action.Request] (tudo opcional).
 * Um corpo vazio, ou que não seja um objeto, é ignorado.
 */
@Serializable
data class RequestResult(
    /** Valores mesclados ao estado da tela. */
    val state: Map<String, JsonElement> = emptyMap(),
    /** Erros por id de campo (validação do lado do BFF). Vale em sucesso e em erro. */
    val errors: Map<String, String> = emptyMap(),
    /** Filhos acrescentados ao contêiner de cada id (paginação de [Component.LazyColumn]). */
    val append: Map<String, List<Component>> = emptyMap(),
    /** Ação executada antes do `onSuccess` da requisição. */
    val action: ActionRef? = null,
)
