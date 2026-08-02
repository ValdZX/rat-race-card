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
import ua.vald_zx.game.rat.race.card.splitDecimal

@Composable
fun DesignDreamDialog(
    dream: Dream?,
    canPay: Boolean,
    onBuy: () -> Unit,
    onPass: () -> Unit,
) {
    val colors = Design.colors
    DesignDialog(
        title = stringResource(Res.string.dream_offer_title),
        onDismissRequest = {},
        dismissOnBackOrOutside = false,
        confirmLabel = dream?.let {
            "${stringResource(Res.string.buy)} ${it.price.splitDecimal()}"
        } ?: stringResource(Res.string.buy),
        confirmEnabled = dream != null && canPay,
        confirmDisabledReason = stringResource(Res.string.not_enough_money),
        onConfirm = onBuy,
        dismissLabel = stringResource(Res.string.pass),
        onDismissAction = onPass,
    ) {
        if (dream == null) return@DesignDialog
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
        }
    }
}
