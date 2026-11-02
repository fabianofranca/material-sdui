package com.fabianofranca.sdui.bff.screens

import com.fabianofranca.sdui.bff.data.SampleData
import com.fabianofranca.sdui.bff.dsl.ChildrenScope
import com.fabianofranca.sdui.bff.dsl.bottomSheet
import com.fabianofranca.sdui.bff.dsl.componentsOf
import com.fabianofranca.sdui.bff.dsl.button
import com.fabianofranca.sdui.bff.dsl.closeSheet
import com.fabianofranca.sdui.bff.dsl.column
import com.fabianofranca.sdui.bff.dsl.fab
import com.fabianofranca.sdui.bff.dsl.icon
import com.fabianofranca.sdui.bff.dsl.iconButton
import com.fabianofranca.sdui.bff.dsl.json
import com.fabianofranca.sdui.bff.dsl.lazyColumn
import com.fabianofranca.sdui.bff.dsl.listItem
import com.fabianofranca.sdui.bff.dsl.openSheet
import com.fabianofranca.sdui.bff.dsl.refreshScreen
import com.fabianofranca.sdui.bff.dsl.request
import com.fabianofranca.sdui.bff.dsl.required
import com.fabianofranca.sdui.bff.dsl.screen
import com.fabianofranca.sdui.bff.dsl.sequence
import com.fabianofranca.sdui.bff.dsl.setState
import com.fabianofranca.sdui.bff.dsl.showSnackbar
import com.fabianofranca.sdui.bff.dsl.tab
import com.fabianofranca.sdui.bff.dsl.tabs
import com.fabianofranca.sdui.bff.dsl.text
import com.fabianofranca.sdui.bff.dsl.textField
import com.fabianofranca.sdui.bff.dsl.topAppBar
import com.fabianofranca.sdui.bff.dsl.track
import com.fabianofranca.sdui.bff.dsl.validate
import com.fabianofranca.sdui.contract.ButtonStyle
import com.fabianofranca.sdui.contract.ColorRole
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.RequestResult
import com.fabianofranca.sdui.contract.Padding
import com.fabianofranca.sdui.contract.RequestMethod
import com.fabianofranca.sdui.contract.ScreenResponse
import com.fabianofranca.sdui.contract.TextFieldStyle
import com.fabianofranca.sdui.contract.TopAppBarStyle
import com.fabianofranca.sdui.contract.TypographyRole
import com.fabianofranca.sdui.contract.state
import com.fabianofranca.sdui.contract.stateEquals
import kotlinx.serialization.json.JsonPrimitive

/** Início: abas, feed paginado com puxar para atualizar, FAB que abre um BottomSheet. */
fun homeScreen(data: SampleData): ScreenResponse = screen(id = Routes.HOME, title = "Início") {
    initialState("tab" to "for-you", "page" to 2, "postText" to "")
    onAppear(track("home_view"))

    topBar {
        topAppBar(
            id = "top",
            title = "Início",
            style = TopAppBarStyle.Center,
            actions = {
                iconButton(id = "search", icon = "search") {
                    a11y(label = "Buscar")
                    onClick(showSnackbar("A busca chega na próxima versão."))
                }
            },
        )
    }

    content {
        column(id = "content") {
            tabs(
                id = "tabs",
                selected = state("tab"),
                items = listOf(tab("for-you", "Para você"), tab("following", "Seguindo")),
            )
            lazyColumn(id = "feed", spacing = 12, padding = Padding.all(16)) {
                visible = stateEquals("tab", JsonPrimitive("for-you"))
                testTag = "feed"
                onRefresh(refreshScreen())
                // A resposta acrescenta os próximos cards em "feed" e avança "page" (RequestResult).
                onLoadMore(request(RequestMethod.Get, "/api/articles", query = mapOf("page" to state("page"))))
                data.page(1).forEach { articleCard(it) }
            }
            lazyColumn(id = "following") {
                visible = stateEquals("tab", JsonPrimitive("following"))
                data.following.forEachIndexed { i, name ->
                    listItem(
                        id = "following-$i",
                        headline = name,
                        supporting = "${3 + i} artigos novos",
                        leading = { icon(id = "following-$i-icon", name = "account_circle", size = 40) },
                        trailing = { icon(id = "following-$i-arrow", name = "keyboard_arrow_right") },
                    ) {
                        onClick(showSnackbar("Perfil de $name"))
                    }
                }
                data.posts.forEachIndexed { i, post -> postItem(i, post) }
            }
        }
    }

    floating {
        fab(id = "new-post", icon = "edit", label = "Escrever") {
            testTag = "home.newPost"
            onClick(openSheet("new-post-sheet"))
        }
    }

    bottomBar { mainNavigationBar(selected = "home") }

    modals {
        bottomSheet(id = "new-post-sheet") {
            onDismiss(track("new_post_dismissed"))
            column(id = "new-post-body", spacing = 16) {
                text(id = "new-post-title", text = "Nova publicação", style = TypographyRole.TitleLarge) { a11y(heading = true) }
                textField(
                    id = "post-text",
                    value = state("postText"),
                    label = "O que você quer contar?",
                    style = TextFieldStyle.Outlined,
                    singleLine = false,
                    maxLength = 280,
                    supportingText = "Até 280 caracteres",
                    validation = listOf(required("Escreva alguma coisa antes de publicar")),
                )
                button(id = "publish", label = "Publicar", icon = "send", fullWidth = true) {
                    testTag = "home.publish"
                    onClick(
                        validate(
                            "post-text",
                            onValid = request(
                                method = RequestMethod.Post,
                                endpoint = "/api/posts",
                                body = json("text" to state("postText")),
                                // A resposta acrescenta a publicação em "following" e troca a aba.
                                onSuccess = sequence(
                                    closeSheet("new-post-sheet"),
                                    setState("postText", ""),
                                    showSnackbar("Publicado!"),
                                ),
                                onError = showSnackbar("Não deu certo. Tente de novo."),
                            ),
                        ),
                    )
                }
                button(id = "cancel-post", label = "Cancelar", style = ButtonStyle.Text, fullWidth = true) {
                    onClick(closeSheet("new-post-sheet"))
                }
            }
        }
    }
}

/** Uma publicação da pessoa na aba "Seguindo". */
fun ChildrenScope.postItem(index: Int, text: String): Component.ListItem = listItem(
    id = "post-$index",
    overline = "Você publicou",
    headline = text,
    leading = { icon(id = "post-$index-icon", name = "edit", tint = ColorRole.Primary) },
)

/** A resposta de `GET /api/articles?page=N`: os cards da página e a próxima página. */
fun articlesPage(data: SampleData, page: Int): RequestResult {
    val cards = componentsOf { data.page(page).forEach { articleCard(it) } }
    return RequestResult(
        state = mapOf("page" to JsonPrimitive(page + 1)),
        append = if (cards.isEmpty()) emptyMap() else mapOf("feed" to cards),
    )
}

/** A resposta de `POST /api/posts`: a publicação nova em "Seguindo", já com a aba trocada. */
fun postCreated(index: Int, text: String): RequestResult = RequestResult(
    state = mapOf("tab" to JsonPrimitive("following")),
    append = mapOf("following" to componentsOf { postItem(index, text) }),
)
