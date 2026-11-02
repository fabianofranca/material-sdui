package com.fabianofranca.sdui.renderer

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.fabianofranca.sdui.contract.BadgeProps
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.TabsStyle
import com.fabianofranca.sdui.contract.TopAppBarStyle
import com.fabianofranca.sdui.engine.asText
import com.fabianofranca.sdui.engine.int
import com.fabianofranca.sdui.engine.string

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SduiTopAppBar(c: Component.TopAppBar, modifier: Modifier) {
    val store = LocalSduiController.current.store
    val p = c.props
    val navigationIcon = p.navigationIcon
    val title: @Composable () -> Unit = { Text(store.string(p.title), maxLines = 1, overflow = TextOverflow.Ellipsis) }
    val navigation: @Composable () -> Unit = { if (navigationIcon != null) SduiComponent(navigationIcon) }
    val actions: @Composable RowScope.() -> Unit = { p.actions.forEach { SduiComponent(it) } }
    when (p.style) {
        TopAppBarStyle.Small -> TopAppBar(title = title, modifier = modifier, navigationIcon = navigation, actions = actions)
        TopAppBarStyle.Center -> CenterAlignedTopAppBar(title = title, modifier = modifier, navigationIcon = navigation, actions = actions)
        TopAppBarStyle.Medium -> MediumTopAppBar(title = title, modifier = modifier, navigationIcon = navigation, actions = actions)
        TopAppBarStyle.Large -> LargeTopAppBar(title = title, modifier = modifier, navigationIcon = navigation, actions = actions)
    }
}

@Composable
internal fun SduiNavigationBar(c: Component.NavigationBar, modifier: Modifier) {
    val controller = LocalSduiController.current
    val icons = LocalSduiIcons.current
    val selected = controller.fieldValue(c)?.asText()
    NavigationBar(modifier = modifier) {
        for (item in c.props.items) {
            val tag = item.testTag
            NavigationBarItem(
                selected = item.id == selected,
                onClick = { controller.onNavigationItemClick(c, item) },
                icon = { BadgedIcon(item.icon, item.badge, icons) },
                label = { Text(item.label) },
                modifier = if (tag != null) Modifier.testTag(tag) else Modifier,
            )
        }
    }
}

@Composable
private fun BadgedIcon(name: String, badge: BadgeProps?, icons: SduiIcons) {
    val vector = icons.resolve(name)
    if (vector == null) {
        MissingIcon(name)
        return
    }
    if (badge == null) {
        Icon(vector, contentDescription = null)
        return
    }
    val countProp = badge.count
    val count = LocalSduiController.current.store.int(countProp)
    BadgedBox(
        badge = {
            when {
                countProp == null -> Badge()
                count != null && count > 0 -> Badge { Text(if (count > 99) "99+" else count.toString()) }
            }
        },
    ) {
        Icon(vector, contentDescription = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SduiTabs(c: Component.Tabs, modifier: Modifier) {
    val controller = LocalSduiController.current
    val icons = LocalSduiIcons.current
    val items = c.props.items
    if (items.isEmpty()) return
    val selected = controller.fieldValue(c)?.asText()
    val selectedIndex = items.indexOfFirst { it.id == selected }.coerceAtLeast(0)
    val tabs: @Composable () -> Unit = {
        items.forEachIndexed { index, item ->
            val tag = item.testTag
            val icon = item.icon?.let(icons::resolve)
            Tab(
                selected = index == selectedIndex,
                onClick = { controller.onTabClick(c, item) },
                modifier = if (tag != null) Modifier.testTag(tag) else Modifier,
                text = { Text(item.label) },
                icon = icon?.let { vector -> @Composable { Icon(vector, contentDescription = null) } },
            )
        }
    }
    when (c.props.style) {
        TabsStyle.Primary -> PrimaryTabRow(selectedTabIndex = selectedIndex, modifier = modifier, tabs = tabs)
        TabsStyle.Secondary -> SecondaryTabRow(selectedTabIndex = selectedIndex, modifier = modifier, tabs = tabs)
    }
}
