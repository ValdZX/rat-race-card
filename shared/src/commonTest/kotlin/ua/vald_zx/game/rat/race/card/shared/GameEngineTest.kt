package ua.vald_zx.game.rat.race.card.shared

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GameEngineTest {
    @Test
    fun commandEnvelopeRoundTripsThroughJson() {
        val initial = snapshot()
        val envelope = command(initial, "command-1", GameCommand.EndTurn("manual"))

        val encoded = Json.encodeToString(envelope)
        val decoded = Json.decodeFromString<GameCommandEnvelope>(encoded)

        assertEquals(envelope, decoded)
    }

    @Test
    fun fullTurnRunsWithoutInfrastructure() {
        val engine = GameEngine(FixedGameRandom(3))
        val initial = snapshot()

        val rolled = engine.execute(
            initial,
            command(initial, "roll-1", GameCommand.RollDice("nonce-1")),
        ).applied()
        assertEquals(1, rolled.snapshot.board.revision)
        assertEquals(3, rolled.snapshot.board.dice)
        assertTrue(rolled.snapshot.board.diceRolling)

        val moved = engine.execute(
            rolled.snapshot,
            command(rolled.snapshot, "move-1", GameCommand.CompleteRoll),
        ).applied()
        assertEquals(4, moved.snapshot.player("first").location.position)
        assertEquals(listOf(BoardCardType.Chance), moved.snapshot.board.canTakeCard)
        assertEquals(2, moved.snapshot.board.revision)

        val ended = engine.execute(
            moved.snapshot,
            command(moved.snapshot, "end-1", GameCommand.EndTurn("card skipped")),
        ).applied()
        assertEquals("second", ended.snapshot.board.activePlayerId)
        assertTrue(ended.snapshot.board.canRoll)
        assertEquals(3, ended.snapshot.board.revision)
        assertEquals(listOf("roll-1", "move-1", "end-1"), ended.snapshot.board.processedCommandIds)
    }

    @Test
    fun applicationServicePersistsOnceForCommandRetry() = runTest {
        val repository = InMemoryGameRepository(snapshot())
        val mutex = Mutex()
        val service = GameApplicationService(
            repository = repository,
            engine = GameEngine(FixedGameRandom(2)),
            transactionMutex = { mutex },
        )
        val envelope = command(repository.snapshot, "retry-safe", GameCommand.RollDice("same-nonce"))

        assertIs<GameExecution.Applied>(service.execute(envelope))
        assertIs<GameExecution.Duplicate>(service.execute(envelope))
        assertEquals(1, repository.saveCount)
        assertEquals(1, repository.snapshot.board.revision)
    }

    @Test
    fun staleRevisionIsRejectedWithoutChangingSnapshot() {
        val initial = snapshot().copy(board = snapshot().board.copy(revision = 4))
        val execution = GameEngine(FixedGameRandom(1)).execute(
            initial,
            command(initial, "stale", GameCommand.RollDice("stale-nonce")).copy(expectedRevision = 3),
        )

        val rejected = assertIs<GameExecution.Rejected>(execution)
        assertEquals(GameCommandRejection.REVISION_CONFLICT, rejected.reason)
        assertEquals(initial, rejected.snapshot)
    }

    private fun snapshot(): GameSnapshot {
        val board = Board(
            id = "board",
            name = "Board",
            loanLimit = 100_000,
            businessLimit = 3,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            playerIds = setOf("first", "second"),
            activePlayerId = "first",
        )
        return GameSnapshot(
            board = board,
            players = listOf(player("first"), player("second")),
        )
    }

    private fun player(id: String) = Player(
        id = id,
        boardId = "board",
        attrs = PlayerAttributes(color = 0),
        location = PlayerLocation(position = 1),
    )

    private fun command(snapshot: GameSnapshot, id: String, command: GameCommand) = GameCommandEnvelope(
        commandId = id,
        boardId = snapshot.board.id,
        playerId = snapshot.board.activePlayerId,
        expectedRevision = snapshot.board.revision,
        command = command,
    )

    private fun GameExecution.applied(): RuleResult = assertIs<GameExecution.Applied>(this).result

    private fun GameSnapshot.player(id: String): Player = players.first { it.id == id }

    private class FixedGameRandom(private val value: Int) : GameRandom {
        override fun nextInt(from: Int, until: Int): Int = value
    }

    private class InMemoryGameRepository(var snapshot: GameSnapshot) : GameRepository {
        var saveCount = 0

        override suspend fun load(boardId: String): GameSnapshot = snapshot

        override suspend fun save(previous: GameSnapshot, updated: GameSnapshot) {
            snapshot = updated
            saveCount++
        }
    }
}
