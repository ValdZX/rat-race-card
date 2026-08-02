package ua.vald_zx.game.rat.race.card.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import ua.vald_zx.game.rat.race.card.design.DesignButtonKind
import ua.vald_zx.game.rat.race.card.designV2Enabled

/**
 * Кнопка дії на лиці карти. Розмір веде від ширини карти, тому обидві мови
 * приймають ті самі одиниці масштабу.
 */
@Composable
fun EButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    kind: DesignButtonKind = DesignButtonKind.Tonal,
    unitTS: TextUnit,
    unitDp: Dp,
) {
    if (designV2Enabled.value) {
        DesignEButton(title, onClick, modifier, enabled, kind, unitTS, unitDp)
    } else {
        LegacyEButton(title, onClick, modifier, enabled, unitTS, unitDp)
    }
}
