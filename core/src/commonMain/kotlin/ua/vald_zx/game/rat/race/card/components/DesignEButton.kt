package ua.vald_zx.game.rat.race.card.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import ua.vald_zx.game.rat.race.card.design.DesignButton
import ua.vald_zx.game.rat.race.card.design.DesignButtonKind

/** Кнопка карти в новій мові. Стара — LegacyEButton. */
@Composable
internal fun DesignEButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    kind: DesignButtonKind,
    unitTS: TextUnit,
    unitDp: Dp,
) {
    DesignButton(
        text = title,
        modifier = modifier,
        kind = kind,
        enabled = enabled,
        height = unitDp * 34,
        fontSize = unitTS * 12,
        padding = unitDp * 9,
        onClick = onClick,
    )
}
