package com.fabianofranca.sdui.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.contract.Trigger
import com.fabianofranca.sdui.engine.boolean
import kotlinx.coroutines.launch

@Composable
internal fun SduiColumn(c: Component.Column, modifier: Modifier) {
    val p = c.props
    val scroll = if (p.scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier
    Column(
        modifier = modifier.then(fillWidth()).sduiClickable(c).then(scroll).padding(p.padding.toPaddingValues()),
        verticalArrangement = Arrangement.spacedBy(p.spacing.dp),
        horizontalAlignment = p.horizontalAlignment.toCompose(),
    ) {
        CompositionLocalProvider(
            LocalFillWidth provides true,
            LocalInVerticalScroll provides (p.scroll || LocalInVerticalScroll.current),
        ) {
            SduiChildren(c)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SduiRow(c: Component.Row, modifier: Modifier) {
    val p = c.props
    val m = modifier.then(fillWidth()).sduiClickable(c).padding(p.padding.toPaddingValues())
    CompositionLocalProvider(LocalFillWidth provides false) {
        if (p.wrap) {
            FlowRow(
                modifier = m,
                horizontalArrangement = p.horizontalArrangement.toCompose(p.spacing),
                verticalArrangement = Arrangement.spacedBy(p.spacing.dp),
            ) {
                SduiChildren(c)
            }
        } else {
            Row(
                modifier = m,
                horizontalArrangement = p.horizontalArrangement.toCompose(p.spacing),
                verticalAlignment = p.verticalAlignment.toCompose(),
            ) {
                // Quem quer a largura divide o espaço que sobra, em vez de empurrar os vizinhos para fora.
                for (child in childrenOf(c)) {
                    key(child.id) { SduiComponent(child, if (child.fillsWidth()) Modifier.weight(1f) else Modifier) }
                }
            }
        }
    }
}

@Composable
internal fun SduiBox(c: Component.Box, modifier: Modifier) {
    Box(
        modifier = modifier.sduiClickable(c).padding(c.props.padding.toPaddingValues()),
        contentAlignment = c.props.contentAlignment.toCompose(),
    ) {
        SduiChildren(c)
    }
}

/**
 * Lista longa. Com `onLoadMore`, dispara ao chegar perto do fim (uma vez por tamanho de lista);
 * com `onRefresh`, ganha o puxar para atualizar.
 */
@Composable
internal fun SduiLazyColumn(c: Component.LazyColumn, modifier: Modifier) {
    val controller = LocalSduiController.current
    val ui = LocalSduiUi.current
    val p = c.props
    if (LocalInVerticalScroll.current) {
        // Uma lista preguiçosa dentro de uma Column que já rola quebra (altura infinita).
        LaunchedEffect(c.id) { controller.host.logger.warn("LazyColumn ${c.id} dentro de Column com scroll: desenhada como Column") }
        Column(modifier.then(fillWidth()).padding(p.padding.toPaddingValues()), verticalArrangement = Arrangement.spacedBy(p.spacing.dp)) {
            SduiChildren(c)
        }
        return
    }
    // Ids repetidos quebram a LazyColumn (chaves duplicadas); o primeiro vence. Invisíveis não ocupam lugar.
    val children = childrenOf(c).distinctBy { it.id }
        .filter { it !is Component.Unknown && controller.store.boolean(it.visible) }
    val listState = rememberLazyListState()
    val loadingMore = c.id in ui.loading

    if (Trigger.OnLoadMore in c.events) {
        val nearEnd by remember(listState) {
            derivedStateOf {
                val info = listState.layoutInfo
                val last = info.visibleItemsInfo.lastOrNull()?.index ?: -1
                info.totalItemsCount > 0 && last >= info.totalItemsCount - LOAD_MORE_THRESHOLD
            }
        }
        LaunchedEffect(nearEnd, children.size) {
            if (nearEnd) controller.dispatch(Trigger.OnLoadMore, c)
        }
    }

    val list: @Composable () -> Unit = {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = p.padding.toPaddingValues(),
            verticalArrangement = Arrangement.spacedBy(p.spacing.dp),
        ) {
            items(children, key = { it.id }) { child -> SduiComponent(child) }
            if (loadingMore) {
                item(key = "\$loading") {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    if (Trigger.OnRefresh in c.events) {
        RefreshBox(onRefresh = { controller.run(Trigger.OnRefresh, c) }) { list() }
    } else {
        list()
    }
}

private const val LOAD_MORE_THRESHOLD = 3

/** Puxar para atualizar: o indicador fica até a ação terminar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RefreshBox(
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            scope.launch {
                refreshing = true
                try {
                    onRefresh()
                } finally {
                    refreshing = false
                }
            }
        },
        modifier = modifier.fillMaxSize(),
    ) {
        content()
    }
}
