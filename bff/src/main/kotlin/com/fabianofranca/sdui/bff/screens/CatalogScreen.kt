package com.fabianofranca.sdui.bff.screens

import com.fabianofranca.sdui.bff.dsl.ChildrenScope
import com.fabianofranca.sdui.bff.dsl.badge
import com.fabianofranca.sdui.bff.dsl.box
import com.fabianofranca.sdui.bff.dsl.button
import com.fabianofranca.sdui.bff.dsl.card
import com.fabianofranca.sdui.bff.dsl.checkbox
import com.fabianofranca.sdui.bff.dsl.chip
import com.fabianofranca.sdui.bff.dsl.closeSheet
import com.fabianofranca.sdui.bff.dsl.column
import com.fabianofranca.sdui.bff.dsl.dialog
import com.fabianofranca.sdui.bff.dsl.divider
import com.fabianofranca.sdui.bff.dsl.fab
import com.fabianofranca.sdui.bff.dsl.bottomSheet
import com.fabianofranca.sdui.bff.dsl.icon
import com.fabianofranca.sdui.bff.dsl.iconButton
import com.fabianofranca.sdui.bff.dsl.image
import com.fabianofranca.sdui.bff.dsl.listItem
import com.fabianofranca.sdui.bff.dsl.openDialog
import com.fabianofranca.sdui.bff.dsl.openSheet
import com.fabianofranca.sdui.bff.dsl.option
import com.fabianofranca.sdui.bff.dsl.progressIndicator
import com.fabianofranca.sdui.bff.dsl.radioGroup
import com.fabianofranca.sdui.bff.dsl.row
import com.fabianofranca.sdui.bff.dsl.screen
import com.fabianofranca.sdui.bff.dsl.setState
import com.fabianofranca.sdui.bff.dsl.showSnackbar
import com.fabianofranca.sdui.bff.dsl.spacer
import com.fabianofranca.sdui.bff.dsl.switch
import com.fabianofranca.sdui.bff.dsl.tab
import com.fabianofranca.sdui.bff.dsl.tabs
import com.fabianofranca.sdui.bff.dsl.text
import com.fabianofranca.sdui.bff.dsl.textField
import com.fabianofranca.sdui.bff.dsl.topAppBar
import com.fabianofranca.sdui.contract.BoxAlignment
import com.fabianofranca.sdui.contract.ButtonStyle
import com.fabianofranca.sdui.contract.CardStyle
import com.fabianofranca.sdui.contract.ChipKind
import com.fabianofranca.sdui.contract.ColorRole
import com.fabianofranca.sdui.contract.FabSize
import com.fabianofranca.sdui.contract.HorizontalArrangement
import com.fabianofranca.sdui.contract.IconButtonStyle
import com.fabianofranca.sdui.contract.KeyboardKind
import com.fabianofranca.sdui.contract.Padding
import com.fabianofranca.sdui.contract.ProgressKind
import com.fabianofranca.sdui.contract.ScreenResponse
import com.fabianofranca.sdui.contract.SnackbarLength
import com.fabianofranca.sdui.contract.TabsStyle
import com.fabianofranca.sdui.contract.TextFieldStyle
import com.fabianofranca.sdui.contract.TopAppBarStyle
import com.fabianofranca.sdui.contract.TypographyRole
import com.fabianofranca.sdui.contract.fixed
import com.fabianofranca.sdui.contract.state
import com.fabianofranca.sdui.contract.stateEquals
import kotlinx.serialization.json.JsonPrimitive

/** Catálogo: todos os componentes do MVP numa tela só, para conferir o renderer nas duas plataformas. */
fun catalogScreen(): ScreenResponse = screen(id = Routes.CATALOG, title = "Catálogo") {
    initialState(
        "section" to "content",
        "progress" to 0.6,
        "unread" to 3,
        "name" to "",
        "accepted" to false,
        "wifi" to true,
        "size" to "m",
        "kotlin" to true,
        "compose" to false,
    )

    topBar {
        topAppBar(
            id = "top",
            title = "Catálogo",
            style = TopAppBarStyle.Medium,
            actions = {
                iconButton(id = "info", icon = "info") {
                    a11y(label = "Sobre o catálogo")
                    onClick(openDialog("about-dialog"))
                }
            },
        )
    }

    content {
        column(id = "content") {
            tabs(
                id = "sections",
                selected = state("section"),
                style = TabsStyle.Secondary,
                items = listOf(tab("content", "Conteúdo"), tab("actions", "Ações"), tab("forms", "Formulário")),
            )
            column(id = "content-section", spacing = 16, padding = Padding.all(16), scroll = true) {
                visible = stateEquals("section", JsonPrimitive("content"))
                contentSection()
            }
            column(id = "actions-section", spacing = 16, padding = Padding.all(16), scroll = true) {
                visible = stateEquals("section", JsonPrimitive("actions"))
                actionsSection()
            }
            column(id = "forms-section", spacing = 16, padding = Padding.all(16), scroll = true) {
                visible = stateEquals("section", JsonPrimitive("forms"))
                formsSection()
            }
        }
    }

    floating {
        fab(id = "fab", icon = "add", size = FabSize.Regular) {
            a11y(label = "Abrir o BottomSheet")
            onClick(openSheet("catalog-sheet"))
        }
    }

    bottomBar { mainNavigationBar(selected = "catalog") }

    modals {
        bottomSheet(id = "catalog-sheet") {
            column(id = "sheet-body", spacing = 12) {
                text(id = "sheet-title", text = "BottomSheet", style = TypographyRole.TitleLarge)
                text(id = "sheet-text", text = "Abre com openSheet e fecha com closeSheet, arrastando ou tocando fora.")
                button(id = "sheet-close", label = "Fechar", fullWidth = true) { onClick(closeSheet()) }
            }
        }
        dialog(
            id = "about-dialog",
            title = "Catálogo do SDUI",
            text = "Cada componente desta tela veio do BFF em JSON e foi desenhado com o Material 3.",
            icon = "info",
            confirm = { button(id = "about-ok", label = "Entendi", style = ButtonStyle.Text) },
        )
    }
}

private fun ChildrenScope.section(id: String, title: String) {
    text(id = id, text = title, style = TypographyRole.TitleMedium, color = ColorRole.Primary) { a11y(heading = true) }
}

private fun ChildrenScope.contentSection() {
    section("typography-title", "Text")
    text(id = "display", text = "Display small", style = TypographyRole.DisplaySmall)
    text(id = "headline", text = "Headline medium", style = TypographyRole.HeadlineMedium)
    text(id = "body", text = "Body large com a cor onSurfaceVariant.", style = TypographyRole.BodyLarge, color = ColorRole.OnSurfaceVariant)
    text(id = "label", text = "LABEL SMALL", style = TypographyRole.LabelSmall)

    section("image-title", "Image e Icon")
    image(id = "picture", url = "https://picsum.photos/seed/catalog/800/400", aspectRatio = 2f, placeholder = "image") {
        a11y(label = "Uma foto de exemplo")
    }
    row(id = "icons", spacing = 16, wrap = true) {
        listOf("home", "favorite", "star", "settings", "notifications").forEach { name ->
            icon(id = "icon-$name", name = name, tint = ColorRole.Primary) { a11y(label = name) }
        }
        badge(id = "badge", count = state("unread")) { icon(id = "badge-icon", name = "email") }
        badge(id = "dot") { icon(id = "dot-icon", name = "notifications") }
    }

    section("card-title", "Card")
    CardStyle.entries.forEach { style ->
        card(id = "card-${style.name.lowercase()}", style = style) {
            onClick(showSnackbar("Card ${style.name.lowercase()} tocado"))
            text(id = "card-${style.name.lowercase()}-title", text = "Card ${style.name.lowercase()}", style = TypographyRole.TitleMedium)
            text(id = "card-${style.name.lowercase()}-text", text = "Com onClick, o card todo é clicável.")
        }
    }

    section("list-title", "ListItem e Divider")
    listItem(
        id = "list-1",
        overline = "Overline",
        headline = "Headline",
        supporting = "Supporting text",
        leading = { icon(id = "list-1-icon", name = "person") },
        trailing = { text(id = "list-1-trailing", text = "100+", style = TypographyRole.LabelSmall) },
    )
    divider(id = "list-divider", inset = 16)
    listItem(
        id = "list-2",
        headline = "Com um Switch no fim",
        trailing = { switch(id = "list-2-switch", checked = state("wifi")) },
    )

    section("progress-title", "ProgressIndicator")
    progressIndicator(id = "progress-linear", kind = ProgressKind.Linear, progress = state("progress"))
    row(id = "progress-row", spacing = 24) {
        progressIndicator(id = "progress-circular", progress = fixed(0.75f))
        progressIndicator(id = "progress-indeterminate")
    }

    section("box-title", "Box e Spacer")
    box(id = "box", contentAlignment = BoxAlignment.Center, padding = Padding.all(8)) {
        image(id = "box-image", url = "https://picsum.photos/seed/box/600/200", aspectRatio = 3f)
        text(id = "box-text", text = "Texto sobre a imagem", style = TypographyRole.TitleLarge, color = ColorRole.OnPrimary)
    }
    spacer(id = "spacer", size = 32)
}

private fun ChildrenScope.actionsSection() {
    section("buttons-title", "Button")
    ButtonStyle.entries.forEach { style ->
        button(id = "button-${style.name.lowercase()}", label = style.name, style = style, icon = "check") {
            onClick(showSnackbar("Button ${style.name.lowercase()}"))
        }
    }
    button(id = "button-disabled", label = "Desabilitado", enabled = fixed(false))
    button(id = "button-loading", label = "Carregando", loading = fixed(true))

    section("icon-buttons-title", "IconButton")
    row(id = "icon-buttons", spacing = 8, wrap = true) {
        IconButtonStyle.entries.forEach { style ->
            iconButton(id = "icon-button-${style.name.lowercase()}", icon = "favorite", style = style) {
                a11y(label = "Favoritar (${style.name.lowercase()})")
            }
        }
    }

    section("chips-title", "Chip")
    row(id = "chips", spacing = 8, horizontalArrangement = HorizontalArrangement.Start, wrap = true) {
        chip(id = "chip-assist", label = "Assist", kind = ChipKind.Assist, icon = "call")
        chip(id = "chip-filter", label = "Kotlin", kind = ChipKind.Filter, selected = state("kotlin"))
        chip(id = "chip-input", label = "Compose", kind = ChipKind.Input, selected = state("compose"))
        chip(id = "chip-suggestion", label = "Sugestão", kind = ChipKind.Suggestion)
    }

    section("snackbar-title", "Snackbar e Dialog")
    button(id = "show-snackbar", label = "Mostrar Snackbar", style = ButtonStyle.Tonal) {
        onClick(
            showSnackbar(
                message = "Item arquivado",
                actionLabel = "Desfazer",
                action = showSnackbar("Desfeito"),
                duration = SnackbarLength.Long,
            ),
        )
    }
    button(id = "show-dialog", label = "Abrir Dialog", style = ButtonStyle.Tonal) { onClick(openDialog("about-dialog")) }
    button(id = "show-sheet", label = "Abrir BottomSheet", style = ButtonStyle.Tonal) { onClick(openSheet("catalog-sheet")) }
    button(id = "set-progress", label = "Progresso para 100%", style = ButtonStyle.Outlined) {
        onClick(setState("progress", 1.0), setState("unread", 0), showSnackbar("Progresso e selo atualizados"))
    }
}

private fun ChildrenScope.formsSection() {
    section("text-fields-title", "TextField")
    textField(id = "name", value = state("name"), label = "Nome (filled)", placeholder = "Como você se chama?")
    textField(id = "phone", label = "Telefone (outlined)", keyboard = KeyboardKind.Phone, style = TextFieldStyle.Outlined)
    textField(id = "password", label = "Senha", keyboard = KeyboardKind.Password, style = TextFieldStyle.Outlined)
    textField(id = "with-error", label = "Com erro", error = fixed("Este campo tem um erro"))
    text(id = "hello", text = state("name"), style = TypographyRole.TitleMedium) { visible = state("name") }

    section("toggles-title", "Checkbox, Switch e RadioGroup")
    checkbox(id = "accept", checked = state("accepted"), label = "Aceito os termos")
    switch(id = "wifi", checked = state("wifi"), label = "Wi-Fi")
    radioGroup(
        id = "size",
        selected = state("size"),
        options = listOf(option("s", "Pequeno"), option("m", "Médio"), option("l", "Grande"), option("xl", "Esgotado", enabled = false)),
    )
}
