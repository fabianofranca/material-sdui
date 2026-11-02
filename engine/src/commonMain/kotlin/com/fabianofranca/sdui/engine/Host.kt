package com.fabianofranca.sdui.engine

import com.fabianofranca.sdui.contract.RequestMethod
import com.fabianofranca.sdui.contract.UrlMode
import kotlinx.serialization.json.JsonElement

/**
 * O que é da plataforma e o app entrega ao renderer: navegação, cliente HTTP, analytics,
 * abrir URL e log.
 */
class SduiHost(
    val navigator: SduiNavigator,
    val http: SduiHttp,
    val analytics: SduiAnalytics = SduiAnalytics.None,
    val urlOpener: UrlOpener = UrlOpener { _, _ -> },
    val logger: SduiLogger = SduiLogger.Default,
)

interface SduiNavigator {
    /** Abre [route]: uma tela do SDUI ou uma tela nativa registrada no app. */
    fun navigate(route: String, params: Map<String, JsonElement>, replace: Boolean)

    fun back()

    fun openDeeplink(uri: String)
}

/** Uma chamada da ação `request`, já com as ligações ao estado resolvidas. */
data class HttpCall(
    val method: RequestMethod,
    val endpoint: String,
    val query: Map<String, JsonElement> = emptyMap(),
    val body: JsonElement? = null,
)

/** A resposta: o código HTTP e o corpo, se for JSON. */
data class HttpOutcome(val status: Int, val body: JsonElement? = null) {
    val isSuccess: Boolean get() = status in 200..299
}

/**
 * Faz as chamadas da ação `request`. Autenticação e cabeçalhos do app ficam aqui,
 * fora do JSON (veja [KtorSduiHttp]). Falha de rede é uma exceção.
 */
fun interface SduiHttp {
    suspend fun send(call: HttpCall): HttpOutcome
}

data class AnalyticsEvent(
    val name: String,
    val kind: Kind,
    val screenId: String,
    val componentId: String? = null,
    val properties: Map<String, JsonElement> = emptyMap(),
) {
    enum class Kind {
        /** O componente com `analytics.name` apareceu. */
        Impression,

        /** O componente com `analytics.name` foi tocado. */
        Click,

        /** A ação `track`. */
        Custom,
    }
}

fun interface SduiAnalytics {
    fun track(event: AnalyticsEvent)

    companion object {
        val None: SduiAnalytics = SduiAnalytics { }
    }
}

fun interface UrlOpener {
    fun open(url: String, mode: UrlMode)
}

fun interface SduiLogger {
    fun warn(message: String)

    companion object {
        val Default: SduiLogger = SduiLogger { println("[sdui] $it") }
    }
}
