package com.fabianofranca.sdui.renderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fabianofranca.sdui.contract.Component
import com.fabianofranca.sdui.engine.string

/** Uma camada aberta (BottomSheet ou Dialog). */
@Composable
internal fun SduiModal(modal: Component) {
    when (modal) {
        is Component.BottomSheet -> SduiBottomSheet(modal)
        is Component.Dialog -> SduiDialog(modal)
        else -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SduiBottomSheet(c: Component.BottomSheet) {
    val controller = LocalSduiController.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dragHandle: (@Composable () -> Unit)? = if (c.props.dragHandle) DefaultDragHandle else null
    ModalBottomSheet(
        onDismissRequest = { controller.onModalDismissed(c) },
        modifier = Modifier.sduiSemantics(c),
        sheetState = sheetState,
        dragHandle = dragHandle,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SduiChildren(c)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private val DefaultDragHandle: @Composable () -> Unit = { BottomSheetDefaults.DragHandle() }

/** Os botões `confirm` e `dismiss` fecham o Dialog antes da própria ação (o motor cuida disso). */
@Composable
private fun SduiDialog(c: Component.Dialog) {
    val controller = LocalSduiController.current
    val store = controller.store
    val p = c.props
    val confirm = p.confirm
    val dismiss = p.dismiss
    val icon = p.icon?.let(LocalSduiIcons.current::resolve)
    AlertDialog(
        onDismissRequest = { controller.onModalDismissed(c) },
        confirmButton = { if (confirm != null) SduiComponent(confirm) },
        modifier = Modifier.sduiSemantics(c),
        dismissButton = dismiss?.let { button -> @Composable { SduiComponent(button) } },
        icon = icon?.let { vector -> @Composable { Icon(vector, contentDescription = null) } },
        title = textSlot(p.title?.let { store.string(it) }),
        text = textSlot(p.text?.let { store.string(it) }),
    )
}
