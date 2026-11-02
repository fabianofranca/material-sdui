@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.fabianofranca.sdui.engine

import com.fabianofranca.sdui.contract.Action
import com.fabianofranca.sdui.contract.ButtonProps
import com.fabianofranca.sdui.contract.CheckboxProps
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.DialogProps
import com.fabianofranca.sdui.contract.AnalyticsInfo
import com.fabianofranca.sdui.contract.LoadingMode
import com.fabianofranca.sdui.contract.NavigationBarProps
import com.fabianofranca.sdui.contract.NavigationItem
import com.fabianofranca.sdui.contract.RequestMethod
import com.fabianofranca.sdui.contract.RuleType
import com.fabianofranca.sdui.contract.Screen
import com.fabianofranca.sdui.contract.TextFieldProps
import com.fabianofranca.sdui.contract.Trigger
import com.fabianofranca.sdui.contract.ValidationRule
import com.fabianofranca.sdui.contract.fixed
import com.fabianofranca.sdui.contract.state
import com.fabianofranca.sdui.contract.stateEquals
import com.fabianofranca.sdui.contract.toJson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScreenControllerTest {

    private class FakeNavigator : SduiNavigator {
        val calls = mutableListOf<String>()

        override fun navigate(route: String, params: Map<String, JsonElement>, replace: Boolean) {
            calls += "navigate:$route:$params:$replace"
        }

        override fun back() {
            calls += "back"
        }

        override fun openDeeplink(uri: String) {
            calls += "deeplink:$uri"
        }
    }

    private class Fixture(
        screen: Screen,
        scope: TestScope,
        http: SduiHttp = SduiHttp { HttpOutcome(200) },
    ) {
        val navigator = FakeNavigator()
        val events = mutableListOf<AnalyticsEvent>()
        val logs = mutableListOf<String>()
        val controller = ScreenController(
            screen = screen,
            host = SduiHost(navigator, http, analytics = { events += it }, logger = { logs += it }),
            scope = scope,
        )
    }

    private val email = Component.TextField(
        id = "email",
        props = TextFieldProps(
            value = state("email"),
            validation = listOf(
                ValidationRule(RuleType.Required, message = "Informe o e-mail"),
                ValidationRule(RuleType.Email, message = "E-mail inválido"),
            ),
        ),
        events = mapOf(Trigger.OnChange to Action.SetState("dirty", JsonPrimitive(true))),
    )

    private fun screenWith(vararg children: Component, modals: List<Component> = emptyList()) = Screen(
        id = "s",
        state = mapOf("email" to JsonPrimitive("")),
        content = Component.Column("content", children = children.toList()),
        modals = modals,
    )

    @Test
    fun campoGravaNaChaveLigadaEDisparaOnChange() = runTest {
        val f = Fixture(screenWith(email), this)
        f.controller.onValueChange(email, JsonPrimitive("a@b.co"))
        advanceUntilIdle()
        assertEquals(JsonPrimitive("a@b.co"), f.controller.store["email"])
        assertEquals(JsonPrimitive(true), f.controller.store["dirty"])
    }

    @Test
    fun campoComValorFixoGuardaPeloId() = runTest {
        val name = Component.TextField("name", TextFieldProps(value = fixed("Ana")))
        val f = Fixture(screenWith(name), this)
        assertEquals("name", f.controller.fieldKey(name))
        assertEquals(JsonPrimitive("Ana"), f.controller.fieldValue(name))
    }

    @Test
    fun validateGravaErrosELimpaQuandoOCampoMuda() = runTest {
        val terms = Component.Checkbox(
            "terms",
            CheckboxProps(validation = listOf(ValidationRule(RuleType.Required, message = "Aceite os termos"))),
        )
        val f = Fixture(screenWith(email, terms), this)
        f.controller.execute(
            Action.Validate(
                fields = listOf("email", "terms"),
                onValid = Action.SetState("result", JsonPrimitive("ok")),
                onInvalid = Action.SetState("result", JsonPrimitive("erro")),
            ),
        )
        assertEquals(mapOf("email" to "Informe o e-mail", "terms" to "Aceite os termos"), f.controller.ui.value.errors)
        assertEquals(JsonPrimitive("erro"), f.controller.store["result"])

        f.controller.onValueChange(email, JsonPrimitive("nao-e-email"))
        assertEquals(setOf("terms"), f.controller.ui.value.errors.keys)
        assertFalse(f.controller.validate(listOf("email")))
        assertEquals("E-mail inválido", f.controller.ui.value.errors["email"])

        f.controller.onValueChange(email, JsonPrimitive("ana@exemplo.com"))
        f.controller.onValueChange(terms, JsonPrimitive(true))
        assertTrue(f.controller.validate(listOf("email", "terms")))
        assertEquals(emptyMap(), f.controller.ui.value.errors)
    }

    @Test
    fun requestResolveOCorpoMostraCarregamentoEAplicaOResultado() = runTest {
        val gate = CompletableDeferred<HttpOutcome>()
        val calls = mutableListOf<HttpCall>()
        val save = Component.Button(
            id = "save",
            props = ButtonProps(label = fixed("Salvar")),
            events = mapOf(
                Trigger.OnClick to Action.Request(
                    method = RequestMethod.Post,
                    endpoint = "/profile",
                    body = buildJsonObject { put("email", state("email").toJson()) },
                    onSuccess = Action.NavigateBack,
                ),
            ),
        )
        val f = Fixture(screenWith(email, save), this, http = { calls += it; gate.await() })
        f.controller.store["email"] = JsonPrimitive("ana@exemplo.com")

        f.controller.dispatch(Trigger.OnClick, save)
        runCurrent()
        assertEquals(setOf("save"), f.controller.ui.value.loading)
        assertEquals(null, f.controller.dispatch(Trigger.OnClick, save), "toque repetido é ignorado enquanto carrega")

        gate.complete(
            HttpOutcome(
                200,
                buildJsonObject {
                    put("state", buildJsonObject { put("saved", true) })
                    put(
                        "append",
                        buildJsonObject {
                            put("content", kotlinx.serialization.json.JsonArray(listOf(buildJsonObject {
                                put("type", "Text")
                                put("id", "novo")
                            })))
                        },
                    )
                },
            ),
        )
        advanceUntilIdle()

        assertEquals(1, calls.size)
        assertEquals(JsonPrimitive("ana@exemplo.com"), calls.single().body!!.jsonObject["email"])
        assertEquals(emptySet(), f.controller.ui.value.loading)
        assertEquals(JsonPrimitive(true), f.controller.store["saved"])
        assertEquals(listOf("novo"), f.controller.ui.value.appended["content"]?.map { it.id })
        assertEquals(listOf("back"), f.navigator.calls)
    }

    @Test
    fun requestComErroExecutaOnErrorEAplicaErrosDoBff() = runTest {
        val f = Fixture(
            screenWith(email),
            this,
            http = { HttpOutcome(422, buildJsonObject { put("errors", buildJsonObject { put("email", "Já cadastrado") }) }) },
        )
        val snackbar = Action.ShowSnackbar(fixed("Não deu certo. Tente de novo."))
        f.controller.execute(Action.Request(RequestMethod.Post, "/profile", loading = LoadingMode.Screen, onError = snackbar))
        assertEquals("Já cadastrado", f.controller.ui.value.errors["email"])
        assertEquals(0, f.controller.ui.value.screenLoading)
        assertEquals("Não deu certo. Tente de novo.", f.controller.snackbars.first().message)
    }

    @Test
    fun falhaDeRedeExecutaOnError() = runTest {
        val f = Fixture(screenWith(), this, http = { error("sem rede") })
        f.controller.execute(Action.Request(endpoint = "/x", onError = Action.SetState("failed", JsonPrimitive(true))))
        assertEquals(JsonPrimitive(true), f.controller.store["failed"])
    }

    @Test
    fun sequenceExecutaEmOrdemESetStateCopiaOutraChave() = runTest {
        val f = Fixture(screenWith(), this)
        f.controller.store["email"] = JsonPrimitive("x@y.z")
        f.controller.execute(
            Action.Sequence(
                listOf(
                    Action.SetState("copy", state("email").toJson()),
                    Action.Navigate("/details", params = mapOf("email" to state("email").toJson())),
                    Action.NavigateBack,
                ),
            ),
        )
        assertEquals(JsonPrimitive("x@y.z"), f.controller.store["copy"])
        assertEquals(listOf("navigate:/details:{email=\"x@y.z\"}:false", "back"), f.navigator.calls)
    }

    @Test
    fun botaoDoDialogFechaODialogAntesDaAcao() = runTest {
        val ok = Component.Button("ok", events = mapOf(Trigger.OnClick to Action.SetState("confirmed", JsonPrimitive(true))))
        val dialog = Component.Dialog("logout", DialogProps(title = fixed("Sair?"), confirm = ok))
        val f = Fixture(screenWith(modals = listOf(dialog)), this)

        f.controller.execute(Action.OpenDialog("logout"))
        assertEquals(listOf("logout"), f.controller.ui.value.openModals)

        f.controller.dispatch(Trigger.OnClick, ok)
        advanceUntilIdle()
        assertEquals(emptyList(), f.controller.ui.value.openModals)
        assertEquals(JsonPrimitive(true), f.controller.store["confirmed"])
    }

    @Test
    fun camadaInexistenteEAcaoDesconhecidaSoGeramLog() = runTest {
        val f = Fixture(screenWith(), this)
        f.controller.execute(Action.OpenSheet("nada"))
        f.controller.execute(Action.Unknown("vibrate"))
        assertEquals(emptyList(), f.controller.ui.value.openModals)
        assertEquals(2, f.logs.size)
    }

    @Test
    fun analyticsDeImpressaoEClique() = runTest {
        val card = Component.Card("promo", analytics = AnalyticsInfo("promo_card"))
        val f = Fixture(screenWith(card), this)
        f.controller.onAppear(card)
        f.controller.dispatch(Trigger.OnClick, card)
        assertEquals(
            listOf(AnalyticsEvent.Kind.Impression, AnalyticsEvent.Kind.Click),
            f.events.map { it.kind },
        )
        assertTrue(f.events.all { it.name == "promo_card" && it.componentId == "promo" && it.screenId == "s" })
    }

    @Test
    fun itemDaBarraDeNavegacaoSelecionaENavega() = runTest {
        val bar = Component.NavigationBar(
            "nav",
            NavigationBarProps(
                items = listOf(
                    NavigationItem("home", "home", "Início", route = "/home"),
                    NavigationItem("settings", "settings", "Ajustes", route = "/settings"),
                ),
                selected = fixed("home"),
            ),
        )
        val f = Fixture(screenWith(bar), this)
        f.controller.onNavigationItemClick(bar, bar.props.items[0])
        f.controller.onNavigationItemClick(bar, bar.props.items[1])
        advanceUntilIdle()
        assertEquals(JsonPrimitive("settings"), f.controller.fieldValue(bar))
        assertEquals(listOf("navigate:/settings:{}:true"), f.navigator.calls)
    }

    @Test
    fun ligacaoComEquals() {
        val store = MapStateStore(mapOf("tab" to JsonPrimitive("news")))
        assertTrue(store.boolean(stateEquals("tab", JsonPrimitive("news"))))
        assertFalse(store.boolean(stateEquals("tab", JsonPrimitive("feed"))))
        assertEquals("", store.string(state("missing")))
    }
}
