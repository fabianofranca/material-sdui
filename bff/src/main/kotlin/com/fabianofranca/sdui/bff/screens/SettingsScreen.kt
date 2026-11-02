package com.fabianofranca.sdui.bff.screens

import com.fabianofranca.sdui.bff.data.SampleData
import com.fabianofranca.sdui.bff.dsl.button
import com.fabianofranca.sdui.bff.dsl.card
import com.fabianofranca.sdui.bff.dsl.checkbox
import com.fabianofranca.sdui.bff.dsl.column
import com.fabianofranca.sdui.bff.dsl.dialog
import com.fabianofranca.sdui.bff.dsl.divider
import com.fabianofranca.sdui.bff.dsl.email
import com.fabianofranca.sdui.bff.dsl.icon
import com.fabianofranca.sdui.bff.dsl.json
import com.fabianofranca.sdui.bff.dsl.listItem
import com.fabianofranca.sdui.bff.dsl.navigate
import com.fabianofranca.sdui.bff.dsl.openDialog
import com.fabianofranca.sdui.bff.dsl.option
import com.fabianofranca.sdui.bff.dsl.radioGroup
import com.fabianofranca.sdui.bff.dsl.request
import com.fabianofranca.sdui.bff.dsl.required
import com.fabianofranca.sdui.bff.dsl.screen
import com.fabianofranca.sdui.bff.dsl.showSnackbar
import com.fabianofranca.sdui.bff.dsl.switch
import com.fabianofranca.sdui.bff.dsl.text
import com.fabianofranca.sdui.bff.dsl.textField
import com.fabianofranca.sdui.bff.dsl.topAppBar
import com.fabianofranca.sdui.bff.dsl.track
import com.fabianofranca.sdui.bff.dsl.validate
import com.fabianofranca.sdui.contract.ButtonStyle
import com.fabianofranca.sdui.contract.CardStyle
import com.fabianofranca.sdui.contract.ColorRole
import com.fabianofranca.sdui.contract.ImeActionKind
import com.fabianofranca.sdui.contract.KeyboardKind
import com.fabianofranca.sdui.contract.LoadingMode
import com.fabianofranca.sdui.contract.Padding
import com.fabianofranca.sdui.contract.RequestMethod
import com.fabianofranca.sdui.contract.ScreenResponse
import com.fabianofranca.sdui.contract.TypographyRole
import com.fabianofranca.sdui.contract.state

/** Ajustes: o exemplo do briefing (estado, validação, request, Snackbar e Dialog). */
fun settingsScreen(data: SampleData): ScreenResponse {
    val profile = data.profile
    return screen(id = Routes.SETTINGS, title = "Configurações") {
        initialState(
            "notifications" to profile.notifications,
            "newsletter" to profile.newsletter,
            "email" to profile.email,
            "theme" to profile.theme,
        )
        onAppear(track("settings_view"))

        topBar { topAppBar(id = "top", title = "Configurações") }

        // O pedido de salvar, usado pelo botão e pela tecla de ação do teclado.
        val save = validate(
            "email",
            onValid = request(
                method = RequestMethod.Post,
                endpoint = "/api/profile",
                body = json(
                    "email" to state("email"),
                    "notifications" to state("notifications"),
                    "newsletter" to state("newsletter"),
                    "theme" to state("theme"),
                ),
                loading = LoadingMode.Screen,
                onSuccess = showSnackbar("Configurações salvas"),
                onError = showSnackbar("Não deu certo. Tente de novo."),
            ),
        )

        content {
            column(id = "content", spacing = 16, padding = Padding.all(16), scroll = true) {
                card(id = "account", style = CardStyle.Outlined) {
                    listItem(
                        id = "account-item",
                        headline = profile.name,
                        supporting = profile.email,
                        leading = { icon(id = "account-icon", name = "account_circle", size = 40, tint = ColorRole.Primary) },
                    )
                }

                text(id = "notifications-title", text = "Notificações", style = TypographyRole.TitleMedium) { a11y(heading = true) }
                switch(id = "notifications", checked = state("notifications"), label = "Receber notificações") {
                    testTag = "settings.notifications"
                    onChange(track("settings_notifications_toggle", "enabled" to state("notifications")))
                }
                checkbox(id = "newsletter", checked = state("newsletter"), label = "Quero receber a newsletter por e-mail")

                divider(id = "divider-1")

                text(id = "email-title", text = "Conta", style = TypographyRole.TitleMedium) { a11y(heading = true) }
                textField(
                    id = "email",
                    value = state("email"),
                    label = "E-mail",
                    supportingText = "Use erro@exemplo.com para ver o erro do BFF",
                    keyboard = KeyboardKind.Email,
                    imeAction = ImeActionKind.Done,
                    validation = listOf(required("Informe o e-mail"), email("E-mail inválido")),
                ) {
                    testTag = "settings.email"
                    onSubmit(save)
                }

                text(id = "theme-title", text = "Tema", style = TypographyRole.TitleMedium) { a11y(heading = true) }
                radioGroup(
                    id = "theme",
                    selected = state("theme"),
                    options = listOf(option("system", "Padrão do sistema"), option("light", "Claro"), option("dark", "Escuro")),
                )

                button(id = "save", label = "Salvar", fullWidth = true) {
                    testTag = "settings.save"
                    a11y(label = "Salvar configurações")
                    analytics("settings_save")
                    onClick(save)
                }
                button(id = "logout", label = "Sair da conta", style = ButtonStyle.Text, icon = "exit_to_app", fullWidth = true) {
                    onClick(openDialog("logout-dialog"))
                }
            }
        }

        bottomBar { mainNavigationBar(selected = "settings") }

        modals {
            dialog(
                id = "logout-dialog",
                title = "Sair da conta?",
                text = "Você vai precisar entrar de novo para ver seus artigos salvos.",
                icon = "exit_to_app",
                confirm = {
                    button(id = "logout-confirm", label = "Sair", style = ButtonStyle.Text) {
                        onClick(
                            request(
                                method = RequestMethod.Post,
                                endpoint = "/api/logout",
                                loading = LoadingMode.Screen,
                                onSuccess = navigate(Routes.HOME, replace = true),
                            ),
                        )
                    }
                },
                dismiss = { button(id = "logout-cancel", label = "Cancelar", style = ButtonStyle.Text) },
            ) {
                onDismiss(track("logout_dialog_dismissed"))
            }
        }
    }
}
