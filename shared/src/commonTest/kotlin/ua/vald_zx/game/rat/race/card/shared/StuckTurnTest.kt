package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class StuckTurnTest {
    @Test
    fun anInterruptedRollLeavesTheActivePlayerWithNothingToDo() {
        val rolled = execute(snapshot(), "roll", GameCommand.RollDice("nonce"))

        val board = rolled.board
        assertTrue(board.diceRolling, "кидок лишився незавершеним")
        assertTrue(!board.canRoll)
        assertTrue(board.canTakeCard.isEmpty())
        assertEquals(null, board.takenCard)
        assertTrue(board.pendingInteractions.isEmpty())
        assertEquals(null, board.auction)
        assertEquals(
            StuckTurnRecovery.COMPLETE_ROLL,
            board.stuckTurnRecovery(),
            "перерваний кидок має розпізнаватись як застряглий хід",
        )
    }

    @Test
    fun completingTheRollUnblocksTheBoard() {
        val rolled = execute(snapshot(), "roll", GameCommand.RollDice("nonce"))

        val recovered = execute(rolled, "resume", rolled.board.stuckTurnRecovery().command()!!)

        assertEquals(StuckTurnRecovery.NONE, recovered.board.stuckTurnRecovery())
        assertTrue(!recovered.board.diceRolling)
    }

    @Test
    fun aResolvedTurnThatNeverAdvancedIsAlsoRecoverable() {
        val stalled = snapshot().let { initial ->
            initial.copy(board = initial.board.copy(canRoll = false, diceRolling = false))
        }

        assertEquals(StuckTurnRecovery.ADVANCE_TURN, stalled.board.stuckTurnRecovery())

        val recovered = execute(stalled, "resume", stalled.board.stuckTurnRecovery().command()!!)

        assertEquals(StuckTurnRecovery.NONE, recovered.board.stuckTurnRecovery())
        assertTrue(recovered.board.canRoll)
        assertEquals("q", recovered.board.activePlayerId)
    }

    @Test
    fun aHealthyBoardIsNeverRecovered() {
        assertEquals(StuckTurnRecovery.NONE, snapshot().board.stuckTurnRecovery())
    }

    @Test
    fun waitingOnAnOpenDeckIsNotStuck() {
        val waiting = snapshot().let { initial ->
            initial.copy(
                board = initial.board.copy(
                    canRoll = false,
                    canTakeCard = listOf(BoardCardType.Chance),
                ),
            )
        }

        assertEquals(StuckTurnRecovery.NONE, waiting.board.stuckTurnRecovery())
    }

    @Test
    fun waitingOnACardOrAuctionIsNotStuck() {
        val onCard = boardWith { copy(canRoll = false, takenCard = CardLink(BoardCardType.Chance, 1)) }
        val onAuction = boardWith {
            copy(canRoll = false, auction = Auction.EstateAuction(Estate("Дім", 1_000), firstBid = 1_000))
        }

        assertEquals(StuckTurnRecovery.NONE, onCard.stuckTurnRecovery())
        assertEquals(StuckTurnRecovery.NONE, onAuction.stuckTurnRecovery())
    }

    @Test
    fun aFinishedGameIsNeverRecovered() {
        val won = boardWith { copy(canRoll = false, diceRolling = true, winnerId = "p") }

        assertEquals(StuckTurnRecovery.NONE, won.stuckTurnRecovery())
    }

    private fun boardWith(change: Board.() -> Board): Board = snapshot().board.change()

    private fun execute(snapshot: GameSnapshot, id: String, command: GameCommand): GameSnapshot {
        val execution = GameEngine(DefaultGameRandom).execute(
            snapshot,
            GameCommandEnvelope(id, "b", snapshot.board.activePlayerId, snapshot.board.revision, command),
        )
        return assertIs<GameExecution.Applied>(execution).snapshot
    }

    private fun snapshot() = GameSnapshot(
        board = Board(
            id = "b",
            name = "b",
            loanLimit = 100_000,
            businessLimit = 5,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            playerIds = setOf("p", "q"),
            activePlayerId = "p",
        ),
        players = listOf(
            Player(id = "p", boardId = "b", attrs = PlayerAttributes(color = 0)),
            Player(id = "q", boardId = "b", attrs = PlayerAttributes(color = 1)),
        ),
    )
}
