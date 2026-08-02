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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.Dp
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.design.DesignCardFrame
import ua.vald_zx.game.rat.race.card.screen.board.visualize.color
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink

@Composable
fun BoxWithConstraintsScope.BoardCardFront(
    card: CardLink,
    size: DpSize,
    vm: BoardViewModel,
) {
    val width = maxWidth
    val height = minHeight
    val scaleX = size.width / width
    val rounding = min(width, height) / 16
    val frameModifier = Modifier.graphicsLayer(scaleX = scaleX, scaleY = 1f)
    CardFrame(card.type, rounding, frameModifier) {
        when (card.type) {
            BoardCardType.Chance -> {
                ChanceCardFront(card, vm)
            }

            BoardCardType.SmallBusiness -> {
                SmallBusinessCardFront(card, vm)
            }

            BoardCardType.MediumBusiness -> {
                MediumBusinessCardFront(card, vm)
            }

            BoardCardType.BigBusiness -> {
                BigBusinessCardFront(card, vm)
            }

            BoardCardType.Expenses -> {
                ExpensesCardFront(card, vm)
            }

            BoardCardType.EventStore -> {
                EventStoreCardFront(card, vm)
            }

            BoardCardType.Shopping -> {
                ShoppingCardFront(card, vm)
            }

            BoardCardType.Deputy -> {
                DeputyCardFront(card, vm)
            }
        }
    }
}

@Composable
private fun CardFrame(
    type: BoardCardType,
    rounding: Dp,
    modifier: Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    if (designV2Enabled.value) {
        DesignCardFrame(type = type, modifier = modifier) {
            BoxWithConstraints(content = content)
        }
    } else {
        LegacyCardFrame(type = type, rounding = rounding, modifier = modifier, content = content)
    }
}
