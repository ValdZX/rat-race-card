package ua.vald_zx.game.rat.race.card.logic

import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AutoTakeCardTest {

    private fun state(
        canTake: List<BoardCardType> = listOf(BoardCardType.Chance),
        taken: CardLink? = null,
        active: Boolean = true,
        isProgress: Boolean = false,
        connection: BoardConnectionState = BoardConnectionState.Connected,
    ): BoardState {
        val player = Player(id = "me", boardId = "b", attrs = PlayerAttributes(0, 0))
        val board = Board(
            id = "b",
            name = "b",
            loanLimit = 0,
            businessLimit = 0,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            canTakeCard = canTake,
            takenCard = taken,
            playerIds = setOf("me"),
            activePlayerId = if (active) "me" else "rival",
        )
        return BoardState(isProgress, board, player, connection)
    }

    @Test
    fun theOnlyOfferedDeckIsDrawnWithoutAsking() {
        assertEquals(BoardCardType.Chance, state().singleAvailableDeck)
    }

    @Test
    fun aRealChoiceStaysWithThePlayer() {
        assertNull(
            state(canTake = listOf(BoardCardType.Chance, BoardCardType.EventStore)).singleAvailableDeck,
            "вибір між колодами відібрали в гравця",
        )
        assertNull(state(canTake = emptyList()).singleAvailableDeck)
    }

    @Test
    fun deputiesKeepTheirSkipOption() {
        assertNull(
            state(canTake = listOf(BoardCardType.Deputy)).singleAvailableDeck,
            "автотяг з'їв можливість пропустити депутатів",
        )
    }

    @Test
    fun nobodyDrawsOnSomeoneElsesTurn() {
        assertNull(state(active = false).singleAvailableDeck)
    }

    @Test
    fun anOpenCardOrAPendingRequestBlocksTheDraw() {
        assertNull(state(taken = CardLink(BoardCardType.Chance, 1)).singleAvailableDeck)
        assertNull(state(isProgress = true).singleAvailableDeck)
        assertNull(state(connection = BoardConnectionState.Reconnecting(1)).singleAvailableDeck)
    }
}
