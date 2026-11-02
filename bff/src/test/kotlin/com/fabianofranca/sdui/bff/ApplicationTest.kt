package com.fabianofranca.sdui.bff

import com.fabianofranca.sdui.bff.data.SampleData
import com.fabianofranca.sdui.bff.screens.Routes
import com.fabianofranca.sdui.contract.Action
import com.fabianofranca.sdui.contract.CONTRACT_VERSION_HEADER
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.RequestResult
import com.fabianofranca.sdui.contract.ScreenResponse
import com.fabianofranca.sdui.contract.SduiJson
import com.fabianofranca.sdui.contract.Trigger
import com.fabianofranca.sdui.contract.allComponents
import com.fabianofranca.sdui.contract.duplicateIds
import com.fabianofranca.sdui.contract.unknownTypes
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApplicationTest {

    private fun decode(body: String) = SduiJson.decodeFromString(ScreenResponse.serializer(), body)

    @Test
    fun todasAsTelasSaoValidas() = testApplication {
        application { module() }
        for (route in listOf(Routes.HOME, "${Routes.ARTICLE}?id=a3", Routes.SETTINGS, Routes.CATALOG, "${Routes.ARTICLE}?id=nada")) {
            val response = client.get("/screens/$route") { header(CONTRACT_VERSION_HEADER, 1) }
            assertEquals(HttpStatusCode.OK, response.status, route)
            val screen = decode(response.bodyAsText()).screen
            assertEquals(emptySet(), screen.duplicateIds(), route)
            assertEquals(emptySet(), screen.unknownTypes(), route)
            assertTrue(screen.allComponents().all { it.id.isNotBlank() }, route)
        }
    }

    @Test
    fun catalogoTemTodosOsComponentesDoMvp() {
        val types = buildScreen(SampleData(), Routes.CATALOG, io.ktor.http.Parameters.Empty)!!
            .screen.allComponents().map { it::class }.toSet()
        val expected = Component::class.sealedSubclasses.toSet() - Component.Unknown::class - Component.LazyColumn::class
        assertEquals(emptySet(), expected - types, "faltam no catálogo")
    }

    @Test
    fun etagDevolve304() = testApplication {
        application { module() }
        val first = client.get("/screens/settings")
        val etag = assertNotNull(first.headers[HttpHeaders.ETag])
        val second = client.get("/screens/settings") { header(HttpHeaders.IfNoneMatch, etag) }
        assertEquals(HttpStatusCode.NotModified, second.status)
    }

    @Test
    fun telaInexistenteEVersaoAntiga() = testApplication {
        application { module() }
        assertEquals(HttpStatusCode.NotFound, client.get("/screens/nada").status)
        assertEquals(HttpStatusCode.UpgradeRequired, client.get("/screens/home") { header(CONTRACT_VERSION_HEADER, 0) }.status)
    }

    @Test
    fun paginacaoDoFeed() = testApplication {
        application { module() }
        val home = decode(client.get("/screens/home").bodyAsText()).screen
        val feed = home.allComponents().filterIsInstance<Component.LazyColumn>().first { it.id == "feed" }
        assertIs<Action.Request>(feed.events[Trigger.OnLoadMore])

        val page2 = SduiJson.decodeFromString(RequestResult.serializer(), client.get("/api/articles?page=2").bodyAsText())
        assertEquals(10, page2.append.getValue("feed").size)
        assertEquals("3", page2.state.getValue("page").toString())
        val last = SduiJson.decodeFromString(RequestResult.serializer(), client.get("/api/articles?page=9").bodyAsText())
        assertEquals(emptyMap(), last.append)
    }

    @Test
    fun publicarEErrosPorCampo() = testApplication {
        application { module() }
        val empty = client.post("/api/posts") {
            contentType(ContentType.Application.Json)
            setBody("""{"text":"  "}""")
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, empty.status)
        assertEquals(
            "Escreva alguma coisa antes de publicar",
            SduiJson.decodeFromString(RequestResult.serializer(), empty.bodyAsText()).errors["post-text"],
        )

        val created = client.post("/api/posts") {
            contentType(ContentType.Application.Json)
            setBody("""{"text":"Olá"}""")
        }
        assertEquals(HttpStatusCode.Created, created.status)
        val result = SduiJson.decodeFromString(RequestResult.serializer(), created.bodyAsText())
        assertEquals("post-0", result.append.getValue("following").single().id)

        val taken = client.post("/api/profile") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"erro@exemplo.com"}""")
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, taken.status)
    }
}
