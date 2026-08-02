package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.screen.board.visualize.color
import ua.vald_zx.game.rat.race.card.shared.BoardCardType

@Composable
internal fun LegacyCardFrame(
    type: BoardCardType,
    rounding: Dp,
    modifier: Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(rounding))
            .border(2.dp, type.color(), RoundedCornerShape(rounding))
            .background(MaterialTheme.colorScheme.background),
        content = content,
    )
}
