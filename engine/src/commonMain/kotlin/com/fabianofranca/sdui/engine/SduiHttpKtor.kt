package com.fabianofranca.sdui.engine

import com.fabianofranca.sdui.contract.CONTRACT_VERSION
import com.fabianofranca.sdui.contract.CONTRACT_VERSION_HEADER
import com.fabianofranca.sdui.contract.SduiJson
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * [SduiHttp] com Ktor. O [client] é do app: autenticação, cabeçalhos e timeouts se
 * configuram nele (plugins `Auth`, `DefaultRequest`, `HttpTimeout`), fora do JSON.
 */
class KtorSduiHttp(
    private val client: HttpClient,
    private val baseUrl: String,
    private val json: Json = SduiJson,
) : SduiHttp {
    override suspend fun send(call: HttpCall): HttpOutcome {
        val response = client.request(resolveUrl(baseUrl, call.endpoint)) {
            method = HttpMethod.parse(call.method.name.uppercase())
            header(CONTRACT_VERSION_HEADER, CONTRACT_VERSION)
            call.query.forEach { (key, value) -> parameter(key, value.asText()) }
            call.body?.let {
                setBody(TextContent(json.encodeToString(JsonElement.serializer(), it), ContentType.Application.Json))
            }
        }
        val text = response.bodyAsText()
        val body = text.takeIf { it.isNotBlank() }?.let { runCatching { json.parseToJsonElement(it) }.getOrNull() }
        return HttpOutcome(response.status.value, body)
    }
}

/** [endpoint] absoluto fica como está; relativo é resolvido contra [baseUrl]. */
fun resolveUrl(baseUrl: String, endpoint: String): String =
    if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
        endpoint
    } else {
        baseUrl.trimEnd('/') + "/" + endpoint.trimStart('/')
    }
