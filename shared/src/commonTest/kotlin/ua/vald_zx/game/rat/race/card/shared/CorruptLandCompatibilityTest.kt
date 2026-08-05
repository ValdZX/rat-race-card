package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CorruptLandCompatibilityTest {
    @Test
    fun legacyStaticCorruptLandRemainsRecognizable() {
        val board = board()

        assertTrue(board.isCorruptLand(Land("legacy corrupt", 200, 400_000)))
        assertFalse(board.isCorruptLand(Land("regular", 60, 120_000)))
    }

    @Test
    fun generatedCorruptLandRemainsRecognizableWithoutTheNewFlag() {
        val card = BoardCard.Chance.CorruptLand("deal", 300_000, 100, 2)
        val board = board(
            generatedCards = mapOf(BoardCardType.Chance to mapOf(1 to card)),
        )

        assertTrue(board.isCorruptLand(Land("saved before migration", card.area, card.price)))
        assertFalse(board.isCorruptLand(Land("regular", card.area, card.price + 1)))
    }

    private fun board(
        generatedCards: Map<BoardCardType, Map<Int, BoardCard>> = emptyMap(),
    ) = Board(
        id = "board",
        name = "Board",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        generatedCards = generatedCards,
    )
}
