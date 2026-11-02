package com.fabianofranca.sdui.engine

import com.fabianofranca.sdui.contract.CONTRACT_VERSION
import com.fabianofranca.sdui.contract.CONTRACT_VERSION_HEADER
import com.fabianofranca.sdui.contract.ScreenResponse
import com.fabianofranca.sdui.contract.SduiJson
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/** A última resposta de uma tela, como veio do BFF, com o `ETag`. */
data class CachedScreen(val body: String, val etag: String?)

/** Guarda a última resposta de cada tela, para abrir rápido e sem rede. */
interface ScreenCache {
    fun get(key: String): CachedScreen?

    fun put(key: String, value: CachedScreen)
}

/** Cache em memória. O app pode trocar por um que grave em disco. */
class InMemoryScreenCache : ScreenCache {
    private val entries = mutableMapOf<String, CachedScreen>()

    override fun get(key: String): CachedScreen? = entries[key]

    override fun put(key: String, value: CachedScreen) {
        entries[key] = value
    }
}

class ScreenLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Pede as telas ao BFF em `GET {baseUrl}/screens/{rota}`, mandando a versão do contrato
 * e o `If-None-Match`. Com `304`, usa o cache; sem rede, também (se houver).
 */
class ScreenRepository(
    private val client: HttpClient,
    private val baseUrl: String,
    private val cache: ScreenCache = InMemoryScreenCache(),
    private val json: Json = SduiJson,
    private val screensPath: String = "screens",
) {
    /** A última versão guardada da tela, sem ir à rede. */
    fun cached(route: String, params: Map<String, JsonElement> = emptyMap()): ScreenResponse? =
        cache.get(cacheKey(route, params))?.let { runCatching { decode(it.body) }.getOrNull() }

    suspend fun fetch(route: String, params: Map<String, JsonElement> = emptyMap()): ScreenResponse {
        val key = cacheKey(route, params)
        val cached = cache.get(key)
        val response = try {
            client.get(resolveUrl(baseUrl, "$screensPath/${route.trim('/')}")) {
                header(CONTRACT_VERSION_HEADER, CONTRACT_VERSION)
                cached?.etag?.let { header(HttpHeaders.IfNoneMatch, it) }
                params.forEach { (name, value) -> parameter(name, value.asText()) }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return cached?.let { decode(it.body) } ?: throw ScreenLoadException("Sem rede para $route", e)
        }
        return when {
            response.status == HttpStatusCode.NotModified && cached != null -> decode(cached.body)
            response.status.isSuccess() -> {
                val body = response.bodyAsText()
                val screen = decode(body)
                cache.put(key, CachedScreen(body, response.headers[HttpHeaders.ETag]))
                screen
            }
            else -> cached?.let { decode(it.body) }
                ?: throw ScreenLoadException("O BFF respondeu ${response.status.value} para $route")
        }
    }

    private fun decode(body: String): ScreenResponse {
        val response = json.decodeFromString(ScreenResponse.serializer(), body)
        if (response.version > CONTRACT_VERSION) {
            throw ScreenLoadException("Contrato v${response.version} é mais novo que o do app (v$CONTRACT_VERSION)")
        }
        return response
    }

    private fun cacheKey(route: String, params: Map<String, JsonElement>): String =
        route.trim('/') + params.entries.sortedBy { it.key }.joinToString("") { "&${it.key}=${it.value.asText()}" }
}
