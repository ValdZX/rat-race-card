package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class StuckTurnTest {
    @Test
    fun anInterruptedRollLeavesTheActivePlayerWithNothingToDo() {
        val initial = snapshot()
        val rolled = execute(initial, "roll", GameCommand.RollDice("n"))

        val board = rolled.board
        println("diceRolling=${board.diceRolling} canRoll=${board.canRoll} canTakeCard=${board.canTakeCard} " +
            "takenCard=${board.takenCard} pending=${board.pendingInteractions.size} auction=${board.auction}")
        assertTrue(board.diceRolling)
        assertTrue(!board.canRoll)
    }

    private fun execute(snapshot: GameSnapshot, id: String, command: GameCommand): GameSnapshot {
        val execution = GameEngine(DefaultGameRandom).execute(
            snapshot,
            GameCommandEnvelope(id, "b", snapshot.board.activePlayerId, snapshot.board.revision, command),
        )
        return assertIs<GameExecution.Applied>(execution).snapshot
    }

    private fun snapshot() = GameSnapshot(
        board = Board(
            id = "b", name = "b", loanLimit = 100_000, businessLimit = 5,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0), cards = emptyMap(),
            playerIds = setOf("p", "q"), activePlayerId = "p",
        ),
        players = listOf(
            Player(id = "p", boardId = "b", attrs = PlayerAttributes(color = 0)),
            Player(id = "q", boardId = "b", attrs = PlayerAttributes(color = 1)),
        ),
    )
}
