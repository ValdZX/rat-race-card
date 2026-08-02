package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.min
import ua.vald_zx.game.rat.race.card.screen.board.deck.BoardCardText
import ua.vald_zx.game.rat.race.card.shared.BoardCardType

/** Нова сорочка карти: тон колоди й кромка, як у слоті. Стара — LegacyCardBack. */
@Composable
fun DesignCardBack(card: BoardCardType, size: DpSize, isVertical: Boolean, modifier: Modifier) {
    val trueHeight = min(size.width, size.height)
    val shape = RoundedCornerShape(trueHeight / 8)
    val tone = card.tone()
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(tone.fill)
            .border(trueHeight / 40f, tone.edge, shape)
    ) {
        BoardCardText(card, size, isVertical, outlineColor = tone.edge)
    }
}
