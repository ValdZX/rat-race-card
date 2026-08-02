package ua.vald_zx.game.rat.race.card.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Діалог за макетом: усі кути 28dp, обводка 1dp + світла кромка зверху,
 * скрим без блюру — на Wasm розмиття коштує забагато.
 */
@Composable
fun DesignDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmLabel: String,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    confirmDisabledReason: String? = null,
    dismissLabel: String? = null,
    onDismissAction: (() -> Unit)? = null,
    dismissOnBackOrOutside: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = Design.colors
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackOrOutside,
            dismissOnClickOutside = dismissOnBackOrOutside,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(DesignShapes.dialog)
                .background(colors.scaffold.surface1)
                .border(1.dp, colors.scaffold.outline, DesignShapes.dialog)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = title,
                style = Design.type.title,
                color = colors.scaffold.onSurface,
            )
            content()
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                if (dismissLabel != null && onDismissAction != null) {
                    DesignButton(
                        text = dismissLabel,
                        kind = DesignButtonKind.Tonal,
                        modifier = Modifier.weight(1f),
                        onClick = onDismissAction,
                    )
                }
                DesignButton(
                    text = confirmLabel,
                    enabled = confirmEnabled,
                    disabledReason = confirmDisabledReason,
                    modifier = Modifier.weight(1.2f),
                    onClick = onConfirm,
                )
            }
        }
    }
}
