package ua.vald_zx.game.rat.race.server

import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeputyDeckCycleTest {

    private val deck = (1..5).toList()

    private fun board(
        cards: List<Int> = deck,
        discard: List<Int> = emptyList(),
        taken: CardLink? = null,
    ) = Board(
        id = "b",
        name = "b",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = mapOf(BoardCardType.Deputy to cards),
        discard = mapOf(BoardCardType.Deputy to discard),
        takenCard = taken,
    )

    private fun Board.allDeputyIds(): List<Int> =
        cards[BoardCardType.Deputy].orEmpty() +
                discard[BoardCardType.Deputy].orEmpty() +
                listOfNotNull(takenCard?.takeIf { it.type == BoardCardType.Deputy }?.id)

    @Test
    fun buyingSeveralInARowKeepsEveryCardInTheGame() {
        var state = board()
        var drawn = 0
        repeat(4) {
            state = state.discardPileB()
            val next = state.cards[BoardCardType.Deputy].orEmpty().first()
            state = state.takeFromDeck(next, BoardCardType.Deputy)
            drawn++
            assertEquals(
                deck.sorted(),
                state.allDeputyIds().sorted(),
                "після $drawn покупок колода недорахувалась карток",
            )
        }
    }

    @Test
    fun theDeckKeepsDealingAfterTheLastCard() {
        var state = board()
        repeat(deck.size * 3) {
            state = state.discardPileB()
            val next = state.cards[BoardCardType.Deputy].orEmpty().firstOrNull()
            assertTrue(next != null, "колода стала порожньою й не повернулась із відбою")
            state = state.takeFromDeck(next, BoardCardType.Deputy)
        }
        assertEquals(deck.sorted(), state.allDeputyIds().sorted())
    }
}
