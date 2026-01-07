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
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.visualize.color
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink


@Composable
fun BoxWithConstraintsScope.BoardCardFront(
    card: CardLink,
    isActive: Boolean,
    size: DpSize,
    vm: BoardViewModel,
) {
    val width = maxWidth
    val height = minHeight
    val scaleX = size.width / width
    val rounding = min(width, height) / 16
    BoxWithConstraints(
        modifier = Modifier
            .graphicsLayer(
                scaleX = scaleX,
                scaleY = 1f
            )
            .clip(RoundedCornerShape(rounding))
            .border(2.dp, card.type.color(), RoundedCornerShape(rounding))
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (card.type) {
            BoardCardType.Chance -> {
                ChanceCardFront(card, isActive, vm)
            }

            BoardCardType.SmallBusiness -> {
                SmallBusinessCardFront(card, isActive, vm)
            }

            BoardCardType.MediumBusiness -> {
                MediumBusinessCardFront(card, isActive, vm)
            }

            BoardCardType.BigBusiness -> {
                BigBusinessCardFront(card, isActive, vm)
            }

            BoardCardType.Expenses -> {
                ExpensesCardFront(card, isActive, vm)
            }

            BoardCardType.EventStore -> {
                EventStoreCardFront(card, isActive, vm)
            }

            BoardCardType.Shopping -> {
                ShoppingCardFront(card, isActive, vm)
            }

            BoardCardType.Deputy -> {
                DeputyCardFront(card, isActive, vm)
            }
        }
    }
}



