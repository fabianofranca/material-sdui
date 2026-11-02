package com.fabianofranca.sdui.bff.screens

import com.fabianofranca.sdui.bff.data.SampleData
import com.fabianofranca.sdui.bff.dsl.button
import com.fabianofranca.sdui.bff.dsl.chip
import com.fabianofranca.sdui.bff.dsl.column
import com.fabianofranca.sdui.bff.dsl.icon
import com.fabianofranca.sdui.bff.dsl.iconButton
import com.fabianofranca.sdui.bff.dsl.image
import com.fabianofranca.sdui.bff.dsl.navigateBack
import com.fabianofranca.sdui.bff.dsl.openUrl
import com.fabianofranca.sdui.bff.dsl.row
import com.fabianofranca.sdui.bff.dsl.screen
import com.fabianofranca.sdui.bff.dsl.showSnackbar
import com.fabianofranca.sdui.bff.dsl.text
import com.fabianofranca.sdui.bff.dsl.topAppBar
import com.fabianofranca.sdui.bff.dsl.track
import com.fabianofranca.sdui.contract.ButtonStyle
import com.fabianofranca.sdui.contract.ChipKind
import com.fabianofranca.sdui.contract.ColorRole
import com.fabianofranca.sdui.contract.HorizontalAlignment
import com.fabianofranca.sdui.contract.Padding
import com.fabianofranca.sdui.contract.ScreenResponse
import com.fabianofranca.sdui.contract.TypographyRole
import com.fabianofranca.sdui.contract.UrlMode
import com.fabianofranca.sdui.contract.state

/** Detalhe de um artigo. A rota recebe o parâmetro `id`. */
fun articleScreen(data: SampleData, id: String?): ScreenResponse {
    val article = data.article(id) ?: return notFoundScreen()
    return screen(id = Routes.ARTICLE, title = article.title) {
        initialState("saved" to false)
        onAppear(track("article_view", "articleId" to article.id))

        topBar {
            topAppBar(
                id = "top",
                title = article.title,
                navigationIcon = {
                    iconButton(id = "back", icon = "arrow_back") {
                        a11y(label = "Voltar")
                        onClick(navigateBack())
                    }
                },
                actions = {
                    iconButton(id = "share", icon = "share") {
                        a11y(label = "Compartilhar")
                        onClick(openUrl(article.url))
                    }
                },
            )
        }

        content {
            column(id = "content", spacing = 16, scroll = true, padding = Padding(start = 0, top = 0, end = 0, bottom = 24)) {
                image(id = "cover", url = article.imageUrl, aspectRatio = 16f / 9f, placeholder = "image") {
                    a11y(label = "Capa do artigo")
                }
                column(id = "body", spacing = 12, padding = Padding.symmetric(horizontal = 16)) {
                    text(id = "title", text = article.title, style = TypographyRole.HeadlineSmall) { a11y(heading = true) }
                    row(id = "meta", spacing = 8) {
                        icon(id = "author-icon", name = "person", tint = ColorRole.OnSurfaceVariant, size = 18)
                        text(id = "author", text = article.author, style = TypographyRole.LabelLarge, color = ColorRole.OnSurfaceVariant)
                    }
                    row(id = "tags", spacing = 8, wrap = true) {
                        article.tags.forEachIndexed { i, tag -> chip(id = "tag-$i", label = tag, kind = ChipKind.Suggestion) }
                        chip(id = "save", label = "Salvar", kind = ChipKind.Filter, selected = state("saved"), icon = "favorite_border") {
                            onChange(track("article_save_toggle", "articleId" to article.id, "saved" to state("saved")))
                        }
                    }
                    text(id = "text", text = article.body, style = TypographyRole.BodyLarge)
                    column(id = "actions", horizontalAlignment = HorizontalAlignment.Center, spacing = 8) {
                        button(id = "open", label = "Abrir no navegador", style = ButtonStyle.Outlined, icon = "share") {
                            onClick(openUrl(article.url, UrlMode.InApp))
                        }
                        button(id = "report", label = "Reportar um problema", style = ButtonStyle.Text) {
                            onClick(showSnackbar("Obrigado! Vamos olhar."))
                        }
                    }
                }
            }
        }
    }
}

fun notFoundScreen(): ScreenResponse = screen(id = "not-found", title = "Não encontrado") {
    topBar {
        topAppBar(
            id = "top",
            title = "Não encontrado",
            navigationIcon = { iconButton(id = "back", icon = "arrow_back") { onClick(navigateBack()) } },
        )
    }
    content {
        column(id = "content", spacing = 16, padding = Padding.all(24), horizontalAlignment = HorizontalAlignment.Center) {
            icon(id = "icon", name = "warning", tint = ColorRole.Error, size = 48)
            text(id = "message", text = "Esse conteúdo não existe mais.", style = TypographyRole.TitleMedium)
            button(id = "back-button", label = "Voltar") { onClick(navigateBack()) }
        }
    }
}
