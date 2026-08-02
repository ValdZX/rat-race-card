package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerActivityTest {
    private val activePlayer = player("active")
    private val inactivePlayer = player("inactive", isInactive = true)
    private val removedPlayer = player("removed")
    private val board = Board(
        id = "board",
        name = "Board",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        playerIds = setOf(activePlayer.id, inactivePlayer.id),
        activePlayerId = activePlayer.id,
    )

    @Test
    fun activePlayerMustBelongToBoardAndBeOnline() {
        assertTrue(activePlayer.isActiveOn(board))
        assertTrue(board.isActivePlayer(activePlayer))
        assertFalse(inactivePlayer.isActiveOn(board))
        assertFalse(board.isActivePlayer(inactivePlayer))
        assertFalse(removedPlayer.isActiveOn(board))
    }

    @Test
    fun activePlayersExcludeInactiveAndRemovedPlayers() {
        assertEquals(
            listOf(activePlayer),
            board.activePlayers(listOf(activePlayer, inactivePlayer, removedPlayer)),
        )
    }

    private fun player(id: String, isInactive: Boolean = false): Player {
        return Player(
            id = id,
            boardId = "board",
            attrs = PlayerAttributes(color = 0),
            isInactive = isInactive,
        )
    }
}
