package com.fabianofranca.sdui.bff.screens

import com.fabianofranca.sdui.bff.data.Article
import com.fabianofranca.sdui.bff.dsl.ChildrenScope
import com.fabianofranca.sdui.bff.dsl.card
import com.fabianofranca.sdui.bff.dsl.column
import com.fabianofranca.sdui.bff.dsl.icon
import com.fabianofranca.sdui.bff.dsl.image
import com.fabianofranca.sdui.bff.dsl.navItem
import com.fabianofranca.sdui.bff.dsl.navigate
import com.fabianofranca.sdui.bff.dsl.navigationBar
import com.fabianofranca.sdui.bff.dsl.row
import com.fabianofranca.sdui.bff.dsl.text
import com.fabianofranca.sdui.contract.CardStyle
import com.fabianofranca.sdui.contract.ColorRole
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.Padding
import com.fabianofranca.sdui.contract.TypographyRole
import com.fabianofranca.sdui.contract.fixed

/** As rotas das telas do SDUI que este BFF serve. */
object Routes {
    const val HOME = "home"
    const val ARTICLE = "article"
    const val SETTINGS = "settings"
    const val CATALOG = "catalog"
}

/** A barra de navegação das telas principais. */
fun ChildrenScope.mainNavigationBar(selected: String): Component.NavigationBar = navigationBar(
    id = "nav",
    selected = fixed(selected),
    items = listOf(
        navItem("home", icon = "home", label = "Início", route = Routes.HOME),
        navItem("catalog", icon = "list", label = "Catálogo", route = Routes.CATALOG),
        navItem("settings", icon = "settings", label = "Ajustes", route = Routes.SETTINGS),
    ),
)

/** O card de um artigo no feed. Fora de uma tela, para servir também à paginação. */
fun ChildrenScope.articleCard(article: Article): Component.Card =
    card(id = "article-${article.id}", style = CardStyle.Elevated, padding = Padding.all(0), spacing = 0) {
        testTag = "feed.${article.id}"
        analytics("article_card", "articleId" to article.id)
        onClick(navigate(Routes.ARTICLE, "id" to article.id))
        image(id = "article-${article.id}-image", url = article.imageUrl, aspectRatio = 16f / 9f, placeholder = "image") {
            a11y(label = "Capa de ${article.title}")
        }
        column(id = "article-${article.id}-body", spacing = 4, padding = Padding.all(16)) {
            text(id = "article-${article.id}-title", text = article.title, style = TypographyRole.TitleMedium)
            text(
                id = "article-${article.id}-summary",
                text = article.summary,
                style = TypographyRole.BodyMedium,
                color = ColorRole.OnSurfaceVariant,
                maxLines = 2,
            )
            row(id = "article-${article.id}-meta", spacing = 4, padding = Padding(start = 0, top = 8, end = 0, bottom = 0)) {
                icon(id = "article-${article.id}-author-icon", name = "person", tint = ColorRole.OnSurfaceVariant, size = 16)
                text(
                    id = "article-${article.id}-author",
                    text = article.author,
                    style = TypographyRole.LabelMedium,
                    color = ColorRole.OnSurfaceVariant,
                )
            }
        }
    }
