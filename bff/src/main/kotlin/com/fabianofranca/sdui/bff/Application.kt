package com.fabianofranca.sdui.bff

import com.fabianofranca.sdui.bff.data.SampleData
import com.fabianofranca.sdui.bff.screens.Routes
import com.fabianofranca.sdui.bff.screens.articleScreen
import com.fabianofranca.sdui.bff.screens.articlesPage
import com.fabianofranca.sdui.bff.screens.catalogScreen
import com.fabianofranca.sdui.bff.screens.homeScreen
import com.fabianofranca.sdui.bff.screens.postCreated
import com.fabianofranca.sdui.bff.screens.settingsScreen
import com.fabianofranca.sdui.contract.CONTRACT_VERSION
import com.fabianofranca.sdui.contract.CONTRACT_VERSION_HEADER
import com.fabianofranca.sdui.contract.RequestResult
import com.fabianofranca.sdui.contract.ScreenResponse
import com.fabianofranca.sdui.contract.SduiJson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receiveText
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import java.security.MessageDigest

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0") { module() }.start(wait = true)
}

/** As telas que este BFF sabe montar, por rota. */
fun buildScreen(data: SampleData, route: String, params: Parameters): ScreenResponse? = when (route) {
    Routes.HOME -> homeScreen(data)
    Routes.ARTICLE -> articleScreen(data, params["id"])
    Routes.SETTINGS -> settingsScreen(data)
    Routes.CATALOG -> catalogScreen()
    else -> null
}

fun Application.module(data: SampleData = SampleData()) {
    install(CallLogging)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Erro em ${call.request.local.uri}", cause)
            call.respondText("Erro interno", status = HttpStatusCode.InternalServerError)
        }
    }

    routing {
        get("/health") { call.respondText("ok") }

        // GET /screens/{rota}?parâmetros — a tela em JSON, com ETag.
        get("/screens/{route...}") {
            val clientVersion = call.request.headers[CONTRACT_VERSION_HEADER]?.toIntOrNull() ?: CONTRACT_VERSION
            if (clientVersion < 1) {
                call.respondText("Contrato v$clientVersion não é mais suportado", status = HttpStatusCode.UpgradeRequired)
                return@get
            }
            val route = call.parameters.getAll("route").orEmpty().joinToString("/")
            val screen = buildScreen(data, route, call.request.queryParameters)
            if (screen == null) {
                call.respondText("Tela $route não existe", status = HttpStatusCode.NotFound)
                return@get
            }
            call.respondScreen(screen)
        }

        route("/api") {
            // Paginação do feed (onLoadMore da LazyColumn "feed").
            get("/articles") {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                call.respondResult(articlesPage(data, page))
            }

            post("/posts") {
                val text = call.jsonBody()["text"].string()?.trim().orEmpty()
                if (text.isEmpty()) {
                    call.respondResult(RequestResult(errors = mapOf("post-text" to "Escreva alguma coisa antes de publicar")), HttpStatusCode.UnprocessableEntity)
                    return@post
                }
                data.posts += text
                call.respondResult(postCreated(data.posts.lastIndex, text), HttpStatusCode.Created)
            }

            post("/profile") {
                val body = call.jsonBody()
                val email = body["email"].string().orEmpty()
                // Simula uma validação que só o BFF sabe fazer (erro por campo).
                if (email.equals("erro@exemplo.com", ignoreCase = true)) {
                    call.respondResult(RequestResult(errors = mapOf("email" to "Este e-mail já está em uso")), HttpStatusCode.UnprocessableEntity)
                    return@post
                }
                data.profile = data.profile.copy(
                    email = email,
                    notifications = body["notifications"].boolean() ?: data.profile.notifications,
                    newsletter = body["newsletter"].boolean() ?: data.profile.newsletter,
                    theme = body["theme"].string() ?: data.profile.theme,
                )
                call.respondResult(RequestResult())
            }

            post("/logout") { call.respond(HttpStatusCode.NoContent) }
        }
    }
}

private suspend fun RoutingCall.respondScreen(screen: ScreenResponse) {
    val body = SduiJson.encodeToString(ScreenResponse.serializer(), screen)
    val etag = "\"" + sha256(body).take(32) + "\""
    response.header(HttpHeaders.ETag, etag)
    response.header(HttpHeaders.CacheControl, "no-cache")
    if (request.headers[HttpHeaders.IfNoneMatch] == etag) {
        respond(HttpStatusCode.NotModified)
    } else {
        respondText(body, ContentType.Application.Json)
    }
}

private suspend fun RoutingCall.respondResult(result: RequestResult, status: HttpStatusCode = HttpStatusCode.OK) {
    respondText(SduiJson.encodeToString(RequestResult.serializer(), result), ContentType.Application.Json, status)
}

private suspend fun RoutingCall.jsonBody(): JsonObject =
    runCatching { SduiJson.parseToJsonElement(receiveText()) as? JsonObject }.getOrNull() ?: JsonObject(emptyMap())

private fun kotlinx.serialization.json.JsonElement?.string(): String? = (this as? JsonPrimitive)?.contentOrNull

private fun kotlinx.serialization.json.JsonElement?.boolean(): Boolean? = (this as? JsonPrimitive)?.booleanOrNull

private fun sha256(text: String): String =
    MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
