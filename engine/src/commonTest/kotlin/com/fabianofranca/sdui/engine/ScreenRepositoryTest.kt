package com.fabianofranca.sdui.engine

import com.fabianofranca.sdui.contract.CONTRACT_VERSION_HEADER
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ScreenRepositoryTest {

    private val body = """
        { "version": 1, "screen": { "id": "home", "content": { "type": "Column", "id": "c" } } }
    """.trimIndent()

    @Test
    fun usaOCacheQuandoOBffResponde304() = runTest {
        val requests = mutableListOf<Map<String, String?>>()
        val engine = MockEngine { request ->
            requests += mapOf(
                "url" to request.url.toString(),
                "version" to request.headers[CONTRACT_VERSION_HEADER],
                "ifNoneMatch" to request.headers[HttpHeaders.IfNoneMatch],
            )
            if (request.headers[HttpHeaders.IfNoneMatch] == "\"v1\"") {
                respond("", HttpStatusCode.NotModified)
            } else {
                respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ETag, "\"v1\""))
            }
        }
        val repository = ScreenRepository(HttpClient(engine), "http://bff.test/")

        assertEquals("home", repository.fetch("/home").screen.id)
        assertEquals("home", repository.fetch("/home").screen.id)
        assertEquals("http://bff.test/screens/home", requests[0]["url"])
        assertEquals("1", requests[0]["version"])
        assertEquals(null, requests[0]["ifNoneMatch"])
        assertEquals("\"v1\"", requests[1]["ifNoneMatch"])
        assertEquals("home", repository.cached("home")?.screen?.id)
    }

    @Test
    fun semRedeUsaOCacheOuFalha() = runTest {
        var online = true
        val engine = MockEngine {
            if (online) respond(body, HttpStatusCode.OK) else throw RuntimeException("offline")
        }
        val repository = ScreenRepository(HttpClient(engine), "http://bff.test")
        assertFailsWith<ScreenLoadException> {
            online = false
            repository.fetch("home")
        }
        online = true
        repository.fetch("home")
        online = false
        assertEquals("home", repository.fetch("home").screen.id)
    }

    @Test
    fun erroDoBffSemCacheFalha() = runTest {
        val repository = ScreenRepository(HttpClient(MockEngine { respondError(HttpStatusCode.InternalServerError) }), "http://bff.test")
        assertFailsWith<ScreenLoadException> { repository.fetch("home") }
    }
}
