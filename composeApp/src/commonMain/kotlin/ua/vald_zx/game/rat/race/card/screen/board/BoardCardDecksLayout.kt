package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.deck.CardDeck
import ua.vald_zx.game.rat.race.card.screen.board.deck.DiscardPile

@Composable
fun BoxScope.CardDecks(
    layout: CardDeckLayout,
    vm: BoardViewModel,
) {
    val state by vm.uiState.collectAsState()
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(layout.size.width, layout.size.height)
    ) {
        layout.slots.forEach { slot ->
            Box(
                modifier = Modifier
                    .offset(slot.offset.x, slot.offset.y)
                    .size(slot.size.width, slot.size.height)
            ) {
                when (slot.kind) {
                    CardDeckSlotKind.DRAW -> CardDeck(slot.type, slot.size, vm)
                    CardDeckSlotKind.DISCARD -> DiscardPile(slot.type, slot.size, state)
                }
            }
        }
    }
}
