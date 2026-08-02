package ua.vald_zx.game.rat.race.card.screen.board.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.min
import ua.vald_zx.game.rat.race.card.screen.board.visualize.color
import ua.vald_zx.game.rat.race.card.shared.BoardCardType

@Composable
internal fun LegacyCardBack(card: BoardCardType, size: DpSize, isVertical: Boolean, modifier: Modifier) {
    val rounding = min(size.width, size.height) / 16
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(rounding))
            .background(card.color())
    ) {
        BoardCardText(card, size, isVertical)
    }
}
