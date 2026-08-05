package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.Dream
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.splitDecimal

@Composable
fun DesignDreamDetailsDialog(
    dream: Dream,
    selectors: List<Player>,
    currentPlayerId: String,
    isPurchased: Boolean,
    canSelect: Boolean,
    onSelect: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = Design.colors
    val isSelected = selectors.any { it.id == currentPlayerId }
    DesignDialog(
        title = stringResource(Res.string.desire),
        onDismissRequest = onDismiss,
        confirmLabel = stringResource(Res.string.select_action),
        confirmEnabled = !isSelected && canSelect,
        confirmDisabledReason = when {
            isPurchased -> stringResource(Res.string.dream_already_purchased)
            isSelected -> stringResource(Res.string.dream_already_selected)
            else -> stringResource(Res.string.dream_taken_by_other)
        },
        onConfirm = onSelect,
        dismissLabel = stringResource(Res.string.close),
        onDismissAction = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(DesignShapes.md)
                .background(colors.scaffold.surface2)
                .border(1.dp, Design.semantic.dream.edge, DesignShapes.md)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = dream.name,
                style = Design.type.subtitle,
                color = colors.scaffold.onSurface,
            )
            Text(
                text = dream.price.splitDecimal(),
                style = Design.type.amountLg,
                color = Design.semantic.dream.edge,
                maxLines = 1,
                softWrap = false,
            )
            if (dream.description.isNotBlank()) {
                Text(
                    text = dream.description,
                    style = Design.type.body,
                    color = colors.scaffold.onSurfaceMuted,
                )
            }
            if (selectors.isNotEmpty()) {
                Text(
                    text = "${stringResource(Res.string.dream_chosen_by)}: " +
                            selectors.joinToString { it.card.name.ifBlank { it.card.profession } },
                    style = Design.type.label,
                    color = colors.scaffold.onSurfaceMuted,
                )
            }
        }
    }
}
