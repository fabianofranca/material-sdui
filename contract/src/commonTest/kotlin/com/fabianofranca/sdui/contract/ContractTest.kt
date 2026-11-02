package com.fabianofranca.sdui.contract

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ContractTest {

    private fun decodeScreen(json: String): ScreenResponse =
        SduiJson.decodeFromString(ScreenResponse.serializer(), json)

    private fun decodeComponent(json: String): Component =
        SduiJson.decodeFromString(Component.serializer(), json)

    @Test
    fun `lê a tela do briefing`() {
        val response = decodeScreen(
            """
            {
              "version": 1,
              "screen": {
                "id": "settings",
                "title": "Configurações",
                "state": { "notifications": true, "email": "" },
                "topBar": { "type": "TopAppBar", "id": "top", "props": { "title": "Configurações" } },
                "content": { "type": "Column", "id": "content", "children": [] },
                "bottomBar": null,
                "floating": null,
                "modals": [],
                "events": { "onAppear": { "type": "track", "name": "settings_view" } }
              }
            }
            """,
        )
        val screen = response.screen
        assertEquals(1, response.version)
        assertEquals("settings", screen.id)
        assertEquals(true, screen.state.getValue("notifications").jsonPrimitive.boolean)
        val topBar = assertIs<Component.TopAppBar>(screen.topBar)
        assertEquals(fixed("Configurações"), topBar.props.title)
        assertIs<Component.Column>(screen.content)
        assertNull(screen.bottomBar)
        assertEquals(Action.Track("settings_view"), screen.events[Trigger.OnAppear])
    }

    @Test
    fun `lê o botão do briefing`() {
        val button = assertIs<Component.Button>(
            decodeComponent(
                """
                {
                  "type": "Button",
                  "id": "save",
                  "props": { "label": "Salvar", "style": "filled", "enabled": { "${'$'}state": "formValid" } },
                  "events": {
                    "onClick": {
                      "type": "request",
                      "method": "POST",
                      "endpoint": "/profile",
                      "body": { "email": { "${'$'}state": "email" } },
                      "onSuccess": { "type": "navigateBack" },
                      "onError": { "type": "showSnackbar", "message": "Não deu certo. Tente de novo." }
                    }
                  },
                  "visible": true,
                  "testTag": "profile.save",
                  "a11y": { "label": "Salvar perfil" },
                  "analytics": { "name": "profile_save" }
                }
                """,
            ),
        )
        assertEquals(fixed("Salvar"), button.props.label)
        assertEquals(ButtonStyle.Filled, button.props.style)
        assertEquals(state("formValid"), button.props.enabled)
        assertEquals(fixed(true), button.visible)
        assertEquals("profile.save", button.testTag)
        assertEquals("Salvar perfil", button.a11y?.label)
        assertEquals("profile_save", button.analytics?.name)

        val request = assertIs<Action.Request>(button.events[Trigger.OnClick])
        assertEquals(RequestMethod.Post, request.method)
        assertEquals("/profile", request.endpoint)
        assertEquals(state("email"), request.body!!.jsonObject.getValue("email").asStateRef())
        assertEquals(Action.NavigateBack, request.onSuccess)
        assertEquals(Action.ShowSnackbar(fixed("Não deu certo. Tente de novo.")), request.onError)
        assertEquals(LoadingMode.Component, request.loading)
    }

    @Test
    fun `componente desconhecido vira Unknown e não quebra a tela`() {
        val column = assertIs<Component.Column>(
            decodeComponent(
                """
                { "type": "Column", "id": "c", "children": [
                    { "type": "Carousel3D", "id": "novo", "props": { "speed": 3 } },
                    { "type": "Text", "id": "t", "props": { "text": "Oi" } }
                ] }
                """,
            ),
        )
        assertEquals(Component.Unknown("Carousel3D", "novo"), column.children[0])
        assertIs<Component.Text>(column.children[1])
    }

    @Test
    fun `ação, gatilho, prop e valor de enum desconhecidos não quebram`() {
        val button = assertIs<Component.Button>(
            decodeComponent(
                """
                { "type": "Button", "id": "b",
                  "props": { "label": "Ok", "style": "neon", "glow": true },
                  "events": {
                    "onClick": { "type": "vibrate", "pattern": [1, 2] },
                    "onDoubleTap": { "type": "navigateBack" }
                  }
                }
                """,
            ),
        )
        assertEquals(ButtonStyle.Filled, button.props.style)
        assertEquals(mapOf(Trigger.OnClick to Action.Unknown("vibrate")), button.events)
    }

    @Test
    fun `lista de ações vira sequence`() {
        val button = assertIs<Component.Button>(
            decodeComponent(
                """
                { "type": "Button", "id": "b", "events": { "onClick": [
                    { "type": "track", "name": "x" },
                    { "type": "navigate", "route": "/home" }
                ] } }
                """,
            ),
        )
        assertEquals(
            Action.Sequence(listOf(Action.Track("x"), Action.Navigate("/home"))),
            button.events[Trigger.OnClick],
        )
    }

    @Test
    fun `ligação com equals`() {
        val text = assertIs<Component.Text>(
            decodeComponent(
                """{ "type": "Text", "id": "t", "visible": { "${'$'}state": "tab", "equals": "news" } }""",
            ),
        )
        assertEquals(stateEquals("tab", JsonPrimitive("news")), text.visible)
    }

    @Test
    fun `padding aceita número ou objeto`() {
        val a = assertIs<Component.Column>(decodeComponent("""{ "type": "Column", "id": "a", "props": { "padding": 16 } }"""))
        assertEquals(Padding.all(16), a.props.padding)
        val b = assertIs<Component.Column>(
            decodeComponent("""{ "type": "Column", "id": "b", "props": { "padding": { "horizontal": 16, "top": 8 } } }"""),
        )
        assertEquals(Padding(start = 16, top = 8, end = 16, bottom = 0), b.props.padding)
    }

    @Test
    fun `ida e volta mantém a tela`() {
        val screen = Screen(
            id = "s",
            state = mapOf("email" to JsonPrimitive(""), "tab" to JsonPrimitive("a")),
            topBar = Component.TopAppBar(
                id = "top",
                props = TopAppBarProps(
                    title = fixed("Título"),
                    navigationIcon = Component.IconButton("back", IconButtonProps(icon = "arrow_back"), events = mapOf(Trigger.OnClick to Action.NavigateBack)),
                ),
            ),
            content = Component.Column(
                id = "content",
                props = ColumnProps(spacing = 8, padding = Padding.symmetric(horizontal = 16)),
                children = listOf(
                    Component.TextField(
                        id = "email",
                        props = TextFieldProps(
                            value = state("email"),
                            keyboard = KeyboardKind.Email,
                            validation = listOf(ValidationRule(RuleType.Required, message = "Obrigatório")),
                        ),
                    ),
                    Component.Text("t", TextProps(fixed("Oi")), visible = stateEquals("tab", JsonPrimitive("a"))),
                    Component.Card("card", CardProps(padding = Padding.all(0))),
                    Component.Image("img", ImageProps(url = fixed("https://x/y.png"), size = 40)),
                    Component.Row("chips", RowProps(wrap = true), children = listOf(Component.Chip("c1"))),
                    Component.Button(
                        id = "save",
                        props = ButtonProps(label = fixed("Salvar")),
                        events = mapOf(
                            Trigger.OnClick to Action.Validate(
                                fields = listOf("email"),
                                onValid = Action.Request(
                                    method = RequestMethod.Post,
                                    endpoint = "/profile",
                                    body = state("email").toJson(),
                                    onSuccess = sequenceOf(Action.ShowSnackbar(fixed("Salvo")), Action.NavigateBack),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            modals = listOf(
                Component.Dialog(
                    "d",
                    DialogProps(title = fixed("Sair?"), confirm = Component.Button("ok", ButtonProps(fixed("Sair"), style = ButtonStyle.Text))),
                ),
            ),
        )
        val response = ScreenResponse(screen = screen)
        val json = SduiJson.encodeToString(ScreenResponse.serializer(), response)
        assertTrue(json.startsWith("{\"version\":1,"), json)
        assertTrue("\"type\":\"TextField\"" in json, json)
        assertEquals(response, decodeScreen(json))
    }

    @Test
    fun `ids repetidos e tipos desconhecidos`() {
        val screen = Screen(
            id = "s",
            content = Component.Column(
                "c",
                children = listOf(Component.Text("a"), Component.Text("a"), Component.Unknown("Novo", "n")),
            ),
            modals = listOf(Component.Dialog("d", DialogProps(confirm = Component.Button("c")))),
        )
        assertEquals(setOf("a", "c"), screen.duplicateIds())
        assertEquals(setOf("Novo"), screen.unknownTypes())
    }

    @Test
    fun `RequestResult com itens para acrescentar`() {
        val result = SduiJson.decodeFromString(
            RequestResult.serializer(),
            """
            { "state": { "page": 2 }, "errors": { "email": "Já existe" },
              "append": { "feed": [ { "type": "ListItem", "id": "i1", "props": { "headline": "Item" } } ] },
              "action": [ { "type": "refreshScreen" } ] }
            """,
        )
        assertEquals(JsonPrimitive(2), result.state["page"])
        assertEquals("Já existe", result.errors["email"])
        assertIs<Component.ListItem>(result.append.getValue("feed").single())
        assertEquals(Action.Sequence(listOf(Action.RefreshScreen)), result.action)
    }
}
