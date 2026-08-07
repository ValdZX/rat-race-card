package ua.vald_zx.game.rat.race.server

import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CoreTrackIds
import ua.vald_zx.game.rat.race.card.shared.activeDeckTypes
import ua.vald_zx.game.rat.race.card.shared.coreContentPackVersions
import ua.vald_zx.game.rat.race.card.shared.requireValidFeatures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoreOnlyFeatureTest {
    @Test
    fun serverRuntimeStartsWithoutCorruptionContent() {
        val board = Board(
            id = "core-only",
            name = "Core only",
            loanLimit = 10_000,
            businessLimit = 3,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = mapOf(
                BoardCardType.Chance to listOf(1, 2),
                BoardCardType.Deputy to listOf(1),
            ),
            generatedCards = mapOf(
                BoardCardType.Chance to mapOf(
                    1 to BoardCard.Chance.RandomJob("", 100),
                    2 to BoardCard.Chance.CorruptLand("", 1_000, 10, 1),
                ),
            ),
            contentPackVersions = coreContentPackVersions(),
        ).requireValidFeatures()

        assertEquals(listOf(1), board.availableCardIds(BoardCardType.Chance, CoreTrackIds.Outer))
        assertTrue(board.availableCardIds(BoardCardType.Deputy, CoreTrackIds.Outer).isEmpty())
        assertTrue(BoardCardType.Deputy !in board.activeDeckTypes())
    }
}
