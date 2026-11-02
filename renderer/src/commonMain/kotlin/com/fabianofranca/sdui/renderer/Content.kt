package com.fabianofranca.sdui.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabianofranca.sdui.contract.CardStyle
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.ProgressKind
import com.fabianofranca.sdui.contract.Trigger
import com.fabianofranca.sdui.engine.float
import com.fabianofranca.sdui.engine.int
import com.fabianofranca.sdui.engine.string

@Composable
internal fun SduiText(c: Component.Text, modifier: Modifier) {
    val store = LocalSduiController.current.store
    val p = c.props
    val align = p.align
    val maxLines = p.maxLines
    Text(
        text = store.string(p.text),
        modifier = (if (align != null) modifier.then(fillWidth()) else modifier).sduiClickable(c),
        style = p.style.toTextStyle(),
        color = p.color?.toColor() ?: Color.Unspecified,
        maxLines = maxLines ?: Int.MAX_VALUE,
        overflow = if (maxLines != null) TextOverflow.Ellipsis else TextOverflow.Clip,
        textAlign = align?.toCompose() ?: TextAlign.Unspecified,
    )
}

@Composable
internal fun SduiImage(c: Component.Image, modifier: Modifier) {
    val store = LocalSduiController.current.store
    val icons = LocalSduiIcons.current
    val p = c.props
    val placeholderIcon = p.placeholder?.let(icons::resolve)
    val placeholder: Painter = if (placeholderIcon != null) {
        rememberVectorPainter(placeholderIcon)
    } else {
        ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    }
    val size = p.size
    val aspectRatio = p.aspectRatio
    val sized = when {
        size != null -> Modifier.size(size.dp)
        aspectRatio != null -> fillWidth().aspectRatio(aspectRatio)
        else -> Modifier
    }
    AsyncImage(
        model = store.string(p.url),
        contentDescription = null, // vem de a11y.label, pelo modifier
        modifier = modifier.then(sized).sduiClickable(c),
        placeholder = placeholder,
        error = placeholder,
        contentScale = p.contentScale.toCompose(),
    )
}

@Composable
internal fun SduiIcon(c: Component.Icon, modifier: Modifier) {
    val p = c.props
    val vector = LocalSduiIcons.current.resolve(p.name)
    if (vector == null) {
        MissingIcon(p.name)
        return
    }
    Icon(
        imageVector = vector,
        contentDescription = null, // vem de a11y.label, pelo modifier
        modifier = modifier.size(p.size.dp).sduiClickable(c),
        tint = p.tint?.toColor() ?: LocalContentColor.current,
    )
}

/** Ícone que o app não conhece: não desenha nada e registra em log. */
@Composable
internal fun MissingIcon(name: String) {
    val logger = LocalSduiController.current.host.logger
    LaunchedEffect(name) { logger.warn("Ícone desconhecido: $name") }
}

@Composable
internal fun SduiCard(c: Component.Card, modifier: Modifier) {
    val controller = LocalSduiController.current
    val p = c.props
    val m = modifier.then(fillWidth())
    val body: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.fillMaxWidth().padding(p.padding.toPaddingValues()),
            verticalArrangement = Arrangement.spacedBy(p.spacing.dp),
        ) {
            CompositionLocalProvider(LocalFillWidth provides true) { SduiChildren(c) }
        }
    }
    if (Trigger.OnClick in c.events) {
        val onClick: () -> Unit = { controller.dispatch(Trigger.OnClick, c) }
        when (p.style) {
            CardStyle.Filled -> Card(onClick = onClick, modifier = m, content = body)
            CardStyle.Elevated -> ElevatedCard(onClick = onClick, modifier = m, content = body)
            CardStyle.Outlined -> OutlinedCard(onClick = onClick, modifier = m, content = body)
        }
    } else {
        when (p.style) {
            CardStyle.Filled -> Card(modifier = m, content = body)
            CardStyle.Elevated -> ElevatedCard(modifier = m, content = body)
            CardStyle.Outlined -> OutlinedCard(modifier = m, content = body)
        }
    }
}

@Composable
internal fun SduiListItem(c: Component.ListItem, modifier: Modifier) {
    val store = LocalSduiController.current.store
    val p = c.props
    val overline = p.overline?.let { store.string(it) }
    val supporting = p.supporting?.let { store.string(it) }
    val leading = p.leading
    val trailing = p.trailing
    ListItem(
        headlineContent = { Text(store.string(p.headline)) },
        modifier = modifier.sduiClickable(c),
        overlineContent = textSlot(overline),
        supportingContent = textSlot(supporting),
        leadingContent = leading?.let { component -> @Composable { SduiComponent(component) } },
        trailingContent = trailing?.let { component -> @Composable { SduiComponent(component) } },
    )
}

@Composable
internal fun SduiBadge(c: Component.Badge, modifier: Modifier) {
    val store = LocalSduiController.current.store
    val countProp = c.props.count
    val count = store.int(countProp)
    BadgedBox(
        badge = {
            when {
                countProp == null -> Badge()
                count != null && count > 0 -> Badge { Text(if (count > 99) "99+" else count.toString()) }
            }
        },
        modifier = modifier,
    ) {
        SduiChildren(c)
    }
}

@Composable
internal fun SduiProgressIndicator(c: Component.ProgressIndicator, modifier: Modifier) {
    val progress = LocalSduiController.current.store.float(c.props.progress)?.coerceIn(0f, 1f)
    when (c.props.kind) {
        ProgressKind.Linear -> if (progress != null) {
            LinearProgressIndicator(progress = { progress }, modifier = modifier.then(fillWidth()))
        } else {
            LinearProgressIndicator(modifier = modifier.then(fillWidth()))
        }
        ProgressKind.Circular -> if (progress != null) {
            CircularProgressIndicator(progress = { progress }, modifier = modifier)
        } else {
            CircularProgressIndicator(modifier = modifier)
        }
    }
}

internal fun textSlot(text: String?): (@Composable () -> Unit)? = text?.let { value -> @Composable { Text(value) } }
