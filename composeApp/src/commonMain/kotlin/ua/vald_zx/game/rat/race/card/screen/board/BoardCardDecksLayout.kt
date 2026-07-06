package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import ua.vald_zx.game.rat.race.card.logic.BoardState
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.deck.CardDeck
import ua.vald_zx.game.rat.race.card.screen.board.deck.DiscardPile
import ua.vald_zx.game.rat.race.card.shared.BoardCardType

private val leftDeckTypes = listOf(
    BoardCardType.Chance,
    BoardCardType.BigBusiness,
    BoardCardType.MediumBusiness,
    BoardCardType.SmallBusiness,
)

private val rightDeckTypes = listOf(
    BoardCardType.Expenses,
    BoardCardType.Deputy,
    BoardCardType.EventStore,
    BoardCardType.Shopping,
)

@Composable
fun BoxScope.CardDecks(
    size: DpSize,
    vm: BoardViewModel,
) {
    val state by vm.uiState.collectAsState()
    Box(
        modifier = Modifier
            .align(Alignment.Center)
            .size(size.width, size.height)
    ) {
        if (size.width < size.height) {
            VerticalCardDecks(size, vm, state)
        } else {
            HorizontalCardDecks(size, vm, state)
        }
    }
}

@Composable
private fun BoxScope.VerticalCardDecks(
    size: DpSize,
    vm: BoardViewModel,
    state: BoardState,
) {
    val width = size.width / 5
    val height = (width * 3) / 2
    val cardSize = DpSize(width, height)

    Column(
        modifier = Modifier.fillMaxWidth().align(Alignment.TopStart),
        verticalArrangement = Arrangement.spacedBy(width / 2)
    ) {
        CardDeckRow(leftDeckTypes, cardSize, vm)
        DiscardPileRow(leftDeckTypes, cardSize, state)
    }

    Column(
        modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
        verticalArrangement = Arrangement.spacedBy(width / 2)
    ) {
        DiscardPileRow(rightDeckTypes, cardSize, state)
        CardDeckRow(rightDeckTypes, cardSize, vm)
    }
}

@Composable
private fun BoxScope.HorizontalCardDecks(
    size: DpSize,
    vm: BoardViewModel,
    state: BoardState,
) {
    val height = size.height / 5
    val width = (height * 3) / 2
    val cardSize = DpSize(width, height)

    Row(
        modifier = Modifier.fillMaxHeight().align(Alignment.TopStart),
        horizontalArrangement = Arrangement.spacedBy(height / 2)
    ) {
        CardDeckColumn(leftDeckTypes, cardSize, vm)
        DiscardPileColumn(leftDeckTypes, cardSize, state)
    }

    Row(
        modifier = Modifier.fillMaxHeight().align(Alignment.TopEnd),
        horizontalArrangement = Arrangement.spacedBy(height / 2)
    ) {
        DiscardPileColumn(rightDeckTypes, cardSize, state)
        CardDeckColumn(rightDeckTypes, cardSize, vm)
    }
}

@Composable
private fun CardDeckRow(
    types: List<BoardCardType>,
    size: DpSize,
    vm: BoardViewModel,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        types.forEach { type ->
            CardDeck(type, size, vm)
        }
    }
}

@Composable
private fun DiscardPileRow(
    types: List<BoardCardType>,
    size: DpSize,
    state: BoardState,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        types.forEach { type ->
            DiscardPile(type, size, state)
        }
    }
}

@Composable
private fun CardDeckColumn(
    types: List<BoardCardType>,
    size: DpSize,
    vm: BoardViewModel,
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxHeight()
    ) {
        types.forEach { type ->
            CardDeck(type, size, vm)
        }
    }
}

@Composable
private fun DiscardPileColumn(
    types: List<BoardCardType>,
    size: DpSize,
    state: BoardState,
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxHeight()
    ) {
        types.forEach { type ->
            DiscardPile(type, size, state)
        }
    }
}
