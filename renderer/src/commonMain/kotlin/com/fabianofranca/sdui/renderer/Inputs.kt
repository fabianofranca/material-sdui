package com.fabianofranca.sdui.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.fabianofranca.sdui.contract.ButtonStyle
import com.fabianofranca.sdui.contract.ChipKind
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.FabSize
import com.fabianofranca.sdui.contract.IconButtonStyle
import com.fabianofranca.sdui.contract.KeyboardKind
import com.fabianofranca.sdui.contract.TextFieldStyle
import com.fabianofranca.sdui.contract.Trigger
import com.fabianofranca.sdui.engine.asBoolean
import com.fabianofranca.sdui.engine.asText
import com.fabianofranca.sdui.engine.boolean
import com.fabianofranca.sdui.engine.string
import kotlinx.serialization.json.JsonPrimitive

@Composable
internal fun SduiButton(c: Component.Button, modifier: Modifier) {
    val controller = LocalSduiController.current
    val store = controller.store
    val p = c.props
    val loading = store.boolean(p.loading) || c.id in LocalSduiUi.current.loading
    val enabled = store.boolean(p.enabled, default = true) && !loading
    val icon = p.icon?.let(LocalSduiIcons.current::resolve)
    val onClick: () -> Unit = { controller.dispatch(Trigger.OnClick, Trigger.OnSubmit, component = c) }
    val m = if (p.fullWidth) modifier.then(fillWidth()) else modifier
    val content: @Composable RowScope.() -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        }
        Text(store.string(p.label))
    }
    when (p.style) {
        ButtonStyle.Filled -> Button(onClick = onClick, modifier = m, enabled = enabled, content = content)
        ButtonStyle.Tonal -> FilledTonalButton(onClick = onClick, modifier = m, enabled = enabled, content = content)
        ButtonStyle.Elevated -> ElevatedButton(onClick = onClick, modifier = m, enabled = enabled, content = content)
        ButtonStyle.Outlined -> OutlinedButton(onClick = onClick, modifier = m, enabled = enabled, content = content)
        ButtonStyle.Text -> TextButton(onClick = onClick, modifier = m, enabled = enabled, content = content)
    }
}

@Composable
internal fun SduiIconButton(c: Component.IconButton, modifier: Modifier) {
    val controller = LocalSduiController.current
    val p = c.props
    val enabled = controller.store.boolean(p.enabled, default = true) && c.id !in LocalSduiUi.current.loading
    val vector = LocalSduiIcons.current.resolve(p.icon)
    if (vector == null) MissingIcon(p.icon)
    val onClick: () -> Unit = { controller.dispatch(Trigger.OnClick, c) }
    val content: @Composable () -> Unit = { if (vector != null) Icon(vector, contentDescription = null) }
    when (p.style) {
        IconButtonStyle.Standard -> IconButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
        IconButtonStyle.Filled -> FilledIconButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
        IconButtonStyle.Tonal -> FilledTonalIconButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
        IconButtonStyle.Outlined -> OutlinedIconButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
    }
}

@Composable
internal fun SduiFab(c: Component.Fab, modifier: Modifier) {
    val controller = LocalSduiController.current
    val p = c.props
    val vector = LocalSduiIcons.current.resolve(p.icon)
    if (vector == null) MissingIcon(p.icon)
    val label = p.label?.let { controller.store.string(it) }
    val onClick: () -> Unit = { controller.dispatch(Trigger.OnClick, c) }
    val icon: @Composable () -> Unit = { if (vector != null) Icon(vector, contentDescription = null) }
    if (label != null) {
        ExtendedFloatingActionButton(text = { Text(label) }, icon = icon, onClick = onClick, modifier = modifier)
    } else {
        when (p.size) {
            FabSize.Small -> SmallFloatingActionButton(onClick = onClick, modifier = modifier, content = icon)
            FabSize.Regular -> FloatingActionButton(onClick = onClick, modifier = modifier, content = icon)
            FabSize.Large -> LargeFloatingActionButton(onClick = onClick, modifier = modifier, content = icon)
        }
    }
}

/**
 * O valor vem do estado (pela chave ligada em `value`, ou pelo `id`) e cada mudança volta para lá.
 * O erro mostrado é o de `validate` ou do BFF; sem ele, o da prop `error`.
 */
@Composable
internal fun SduiTextField(c: Component.TextField, modifier: Modifier) {
    val controller = LocalSduiController.current
    val store = controller.store
    val p = c.props
    val value = controller.fieldValue(c)?.asText().orEmpty()
    val error = LocalSduiUi.current.errors[c.id]
        ?: p.error?.let { store.string(it) }?.takeIf { it.isNotEmpty() }
    val supporting = error ?: p.supportingText?.let { store.string(it) }
    val maxLength = p.maxLength
    val onValueChange: (String) -> Unit = { text ->
        controller.onValueChange(c, JsonPrimitive(if (maxLength != null) text.take(maxLength) else text))
    }
    val submit: KeyboardActionScope.() -> Unit = { controller.dispatch(Trigger.OnSubmit, c) }
    val keyboardActions = if (Trigger.OnSubmit in c.events) {
        KeyboardActions(onDone = submit, onGo = submit, onSearch = submit, onSend = submit)
    } else {
        KeyboardActions.Default
    }
    val keyboardOptions = KeyboardOptions(keyboardType = p.keyboard.toKeyboardType(), imeAction = p.imeAction.toImeAction())
    val visualTransformation =
        if (p.keyboard == KeyboardKind.Password) PasswordVisualTransformation() else VisualTransformation.None
    val enabled = store.boolean(p.enabled, default = true)
    val label = textSlot(p.label?.let { store.string(it) })
    val placeholder = textSlot(p.placeholder?.let { store.string(it) })
    val supportingText = textSlot(supporting)
    val m = modifier.then(fillWidth())

    when (p.style) {
        TextFieldStyle.Filled -> TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = m,
            enabled = enabled,
            label = label,
            placeholder = placeholder,
            supportingText = supportingText,
            isError = error != null,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = p.singleLine,
        )
        TextFieldStyle.Outlined -> OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = m,
            enabled = enabled,
            label = label,
            placeholder = placeholder,
            supportingText = supportingText,
            isError = error != null,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = p.singleLine,
        )
    }
}

@Composable
internal fun SduiCheckbox(c: Component.Checkbox, modifier: Modifier) {
    val controller = LocalSduiController.current
    val p = c.props
    val checked = controller.fieldValue(c)?.asBoolean() == true
    val enabled = controller.store.boolean(p.enabled, default = true)
    val label = p.label?.let { controller.store.string(it) }
    val onChange: (Boolean) -> Unit = { controller.onValueChange(c, JsonPrimitive(it)) }
    if (label == null) {
        Checkbox(checked = checked, onCheckedChange = onChange, modifier = modifier, enabled = enabled)
        return
    }
    Column(modifier.then(fillWidth())) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onChange)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
            Text(label, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyLarge)
        }
        FieldError(LocalSduiUi.current.errors[c.id])
    }
}

@Composable
internal fun SduiSwitch(c: Component.Switch, modifier: Modifier) {
    val controller = LocalSduiController.current
    val p = c.props
    val checked = controller.fieldValue(c)?.asBoolean() == true
    val enabled = controller.store.boolean(p.enabled, default = true)
    val label = p.label?.let { controller.store.string(it) }
    val onChange: (Boolean) -> Unit = { controller.onValueChange(c, JsonPrimitive(it)) }
    if (label == null) {
        Switch(checked = checked, onCheckedChange = onChange, modifier = modifier, enabled = enabled)
        return
    }
    Row(
        modifier = modifier
            .then(fillWidth())
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
internal fun SduiRadioGroup(c: Component.RadioGroup, modifier: Modifier) {
    val controller = LocalSduiController.current
    val p = c.props
    val selected = controller.fieldValue(c)?.asText()
    val enabled = controller.store.boolean(p.enabled, default = true)
    Column(modifier.then(fillWidth()).selectableGroup()) {
        for (option in p.options) {
            val isSelected = option.value == selected
            val optionEnabled = enabled && option.enabled
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        enabled = optionEnabled,
                        role = Role.RadioButton,
                        onClick = { if (!isSelected) controller.onValueChange(c, JsonPrimitive(option.value)) },
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = isSelected, onClick = null, enabled = optionEnabled)
                Text(option.label, modifier = Modifier.padding(start = 12.dp), style = MaterialTheme.typography.bodyLarge)
            }
        }
        FieldError(LocalSduiUi.current.errors[c.id])
    }
}

@Composable
internal fun SduiChip(c: Component.Chip, modifier: Modifier) {
    val controller = LocalSduiController.current
    val store = controller.store
    val p = c.props
    val selected = controller.fieldValue(c)?.asBoolean() == true
    val enabled = store.boolean(p.enabled, default = true)
    val icon = p.icon?.let(LocalSduiIcons.current::resolve)
    val label: @Composable () -> Unit = { Text(store.string(p.label)) }
    val leading = iconSlot(icon)
    val onClick: () -> Unit = {
        if (p.kind == ChipKind.Filter) controller.onValueChange(c, JsonPrimitive(!selected))
        controller.dispatch(Trigger.OnClick, c)
    }
    when (p.kind) {
        ChipKind.Assist -> AssistChip(onClick = onClick, label = label, modifier = modifier, enabled = enabled, leadingIcon = leading)
        ChipKind.Filter -> FilterChip(
            selected = selected,
            onClick = onClick,
            label = label,
            modifier = modifier,
            enabled = enabled,
            leadingIcon = if (selected) iconSlot(Icons.Filled.Check) else leading,
        )
        ChipKind.Input -> InputChip(selected = selected, onClick = onClick, label = label, modifier = modifier, enabled = enabled, leadingIcon = leading)
        ChipKind.Suggestion -> SuggestionChip(onClick = onClick, label = label, modifier = modifier, enabled = enabled, icon = leading)
    }
}

private fun iconSlot(icon: ImageVector?): (@Composable () -> Unit)? =
    icon?.let { vector -> @Composable { Icon(vector, contentDescription = null, modifier = Modifier.size(18.dp)) } }

@Composable
private fun FieldError(message: String?) {
    if (message == null) return
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
    )
}
