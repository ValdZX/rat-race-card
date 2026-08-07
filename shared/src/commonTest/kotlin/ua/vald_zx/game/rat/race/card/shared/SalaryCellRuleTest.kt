package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SalaryCellRuleTest {

    @Test
    fun landingOnSalaryOpensSalaryAndInvestmentsWithoutEndingTheTurn() {
        val moved = moveToSalary()

        val player = moved.players.first { it.id == "first" }
        assertEquals(1, player.salaryPosition)
        assertEquals(1, player.investmentPosition)
        assertEquals("first", moved.board.activePlayerId, "зупинка на зарплаті не завершує хід")
    }

    @Test
    fun passingSalaryMarksOnlySalaryAsAvailable() {
        val initial = snapshot(track(landingOn = CoreCellTypes.Chance))
        val moved = execute(initial, "move", GameCommand.MoveTo(2))

        val player = moved.players.first { it.id == "first" }
        assertEquals(1, player.salaryPosition)
        assertNull(player.investmentPosition)
    }

    @Test
    fun advancingTheTurnClearsSalaryAndInvestmentMarkers() {
        val moved = moveToSalary()
        val ended = execute(moved, "end", GameCommand.EndTurn("done"))

        val player = ended.players.first { it.id == "first" }
        assertNull(player.salaryPosition)
        assertNull(player.investmentPosition)
        assertEquals("second", ended.board.activePlayerId)
    }

    @Test
    fun advancingTheTurnPublishesTheClearedPlayerToClients() {
        val moved = moveToSalary()
        val execution = GameEngine(DefaultGameRandom).execute(
            moved,
            GameCommandEnvelope(
                commandId = "end",
                boardId = moved.board.id,
                playerId = moved.board.activePlayerId,
                expectedRevision = moved.board.revision,
                command = GameCommand.EndTurn("done"),
            ),
        )
        val applied = assertIs<GameExecution.Applied>(execution)

        val cleared = applied.result.events
            .filterIsInstance<DomainEvent.PlayerChanged>()
            .filter { it.player.id == "first" }
        assertTrue(
            cleared.any { it.player.salaryPosition == null && it.player.investmentPosition == null },
            "клієнт має отримати PlayerChanged з очищеними маркерами",
        )
    }

    @Test
    fun aSalaryPassedJustBeforeTheTurnEndsIsNotCarriedForward() {
        val initial = snapshot(track(landingOn = CoreCellTypes.Start))
        val ended = execute(initial, "move", GameCommand.MoveTo(2))

        val player = ended.players.first { it.id == "first" }
        assertNull(player.salaryPosition, "незабрана получка не зберігається після завершення ходу")
        assertEquals("second", ended.board.activePlayerId)
    }

    private fun moveToSalary(): GameSnapshot {
        val initial = snapshot()
        return execute(initial, "move", GameCommand.MoveTo(1))
    }

    private fun execute(snapshot: GameSnapshot, id: String, command: GameCommand): GameSnapshot {
        val execution = GameEngine(DefaultGameRandom).execute(
            snapshot,
            GameCommandEnvelope(
                commandId = id,
                boardId = snapshot.board.id,
                playerId = snapshot.board.activePlayerId,
                expectedRevision = snapshot.board.revision,
                command = command,
            ),
        )
        return assertIs<GameExecution.Applied>(execution).result.snapshot
    }

    private fun snapshot(trackCells: List<CellInstance> = track(landingOn = CoreCellTypes.Chance)): GameSnapshot {
        val board = Board(
            id = "board",
            name = "Board",
            loanLimit = 100_000,
            businessLimit = 3,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            playerIds = setOf("first", "second"),
            activePlayerId = "first",
            tracks = listOf(
                TrackDefinition(
                    id = CoreTrackIds.Inner,
                    order = 1,
                    cells = trackCells,
                    visual = TrackVisualHint(horizontalCells = 3, verticalCells = 2),
                ),
            ),
        )
        return GameSnapshot(
            board = board,
            players = listOf(player("first"), player("second")),
        )
    }

    private fun track(landingOn: CellTypeId): List<CellInstance> = listOf(
        CellInstance("inner-0", CoreCellTypes.Start),
        CellInstance("inner-1", CoreCellTypes.Salary),
        CellInstance("inner-2", landingOn),
    )

    private fun player(id: String) = Player(
        id = id,
        boardId = "board",
        attrs = PlayerAttributes(color = 0),
        location = PlayerLocation(position = 0),
        card = PlayerCard(name = id, profession = "Engineer", salary = 5_000, rent = 800, food = 400),
        businesses = listOf(Business(BusinessType.WORK, "Робота", 0, 5_000)),
    )
}
