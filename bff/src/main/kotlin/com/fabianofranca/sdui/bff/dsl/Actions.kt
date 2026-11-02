package com.fabianofranca.sdui.bff.dsl

import com.fabianofranca.sdui.contract.Action
import com.fabianofranca.sdui.contract.LoadingMode
import com.fabianofranca.sdui.contract.RequestMethod
import com.fabianofranca.sdui.contract.RuleType
import com.fabianofranca.sdui.contract.SnackbarLength
import com.fabianofranca.sdui.contract.UrlMode
import com.fabianofranca.sdui.contract.ValidationRule
import com.fabianofranca.sdui.contract.fixed

// Ações (seção 6 do briefing). Cada função devolve a ação; quem a liga a um gatilho é o
// `onClick(...)`, `onChange(...)` etc. do componente. Várias ações num gatilho viram `sequence`.

fun navigate(route: String, vararg params: Pair<String, Any?>, replace: Boolean = false): Action =
    Action.Navigate(route, jsonMap(params), replace)

fun navigateBack(): Action = Action.NavigateBack

fun openDeeplink(uri: String): Action = Action.OpenDeeplink(uri)

fun openUrl(url: String, mode: UrlMode = UrlMode.External): Action = Action.OpenUrl(url, mode)

fun openSheet(id: String): Action = Action.OpenSheet(id)

fun closeSheet(id: String? = null): Action = Action.CloseSheet(id)

fun openDialog(id: String): Action = Action.OpenDialog(id)

fun closeDialog(id: String? = null): Action = Action.CloseDialog(id)

fun showSnackbar(
    message: String,
    actionLabel: String? = null,
    action: Action? = null,
    duration: SnackbarLength = SnackbarLength.Short,
    onDismiss: Action? = null,
): Action = Action.ShowSnackbar(fixed(message), actionLabel, action, duration, onDismiss)

fun request(
    method: RequestMethod,
    endpoint: String,
    body: Any? = null,
    query: Map<String, Any?> = emptyMap(),
    loading: LoadingMode = LoadingMode.Component,
    onSuccess: Action? = null,
    onError: Action? = null,
): Action = Action.Request(
    method = method,
    endpoint = endpoint,
    query = query.mapValues { (_, v) -> jsonOf(v) },
    body = body?.let(::jsonOf),
    loading = loading,
    onSuccess = onSuccess,
    onError = onError,
)

/** Grava [value] em [key]. [value] pode ser `state("outra")` para copiar outra chave. */
fun setState(key: String, value: Any?): Action = Action.SetState(key, jsonOf(value))

fun validate(vararg fields: String, onValid: Action? = null, onInvalid: Action? = null): Action =
    Action.Validate(fields.toList(), onValid, onInvalid)

fun refreshScreen(): Action = Action.RefreshScreen

fun track(name: String, vararg properties: Pair<String, Any?>): Action = Action.Track(name, jsonMap(properties))

fun sequence(vararg actions: Action): Action = Action.Sequence(actions.toList())

// Regras de validação

fun required(message: String) = ValidationRule(RuleType.Required, message = message)

fun minLength(length: Int, message: String) = ValidationRule(RuleType.MinLength, length.toString(), message)

fun maxLength(length: Int, message: String) = ValidationRule(RuleType.MaxLength, length.toString(), message)

fun pattern(regex: String, message: String) = ValidationRule(RuleType.Pattern, regex, message)

fun email(message: String) = ValidationRule(RuleType.Email, message = message)
