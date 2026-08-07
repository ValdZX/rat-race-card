package ua.vald_zx.game.rat.race.card.shared

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameCommandLogTest {
    @Test
    fun appliedCommandRecordsRevisionsAndVersions() = runTest {
        val recorded = mutableListOf<GameCommandLogEntry>()
        val service = service(recorded)

        service.execute(command("roll-1", GameCommand.RollDice("nonce")))

        val entry = recorded.single()
        assertEquals(GameCommandOutcome.APPLIED, entry.outcome)
        assertEquals("board", entry.boardId)
        assertEquals("roll-1", entry.commandId)
        assertEquals("first", entry.playerId)
        assertEquals("RollDice", entry.command)
        assertEquals(0, entry.revisionBefore)
        assertEquals(1, entry.revisionAfter)
        assertEquals(CURRENT_SCHEMA_VERSION, entry.schemaVersion)
        assertEquals(CURRENT_RULES_VERSION, entry.rulesVersion)
        assertEquals(listOf("DiceRolled"), entry.domainEvents)
    }

    @Test
    fun duplicateAndRejectionAreDistinguishable() = runTest {
        val recorded = mutableListOf<GameCommandLogEntry>()
        val service = service(recorded)
        val envelope = command("roll-1", GameCommand.RollDice("nonce"))

        service.execute(envelope)
        service.execute(envelope)
        service.execute(command("stale", GameCommand.RollDice("other")))

        assertEquals(
            listOf(GameCommandOutcome.APPLIED, GameCommandOutcome.DUPLICATE, GameCommandOutcome.REJECTED),
            recorded.map { it.outcome },
        )
        assertEquals(GameCommandRejection.REVISION_CONFLICT, recorded.last().rejection)
    }

    @Test
    fun missingBoardIsRecordedWithoutSnapshot() = runTest {
        val recorded = mutableListOf<GameCommandLogEntry>()
        val mutex = Mutex()
        val service = GameApplicationService(
            repository = object : GameRepository {
                override suspend fun load(boardId: String): GameSnapshot? = null
                override suspend fun save(previous: GameSnapshot, updated: GameSnapshot) = Unit
            },
            engine = GameEngine(FixedGameRandom),
            transactionMutex = { mutex },
            log = recorded::add,
        )

        assertEquals(null, service.execute(command("lost", GameCommand.AdvanceTurn)))

        val entry = recorded.single()
        assertEquals(GameCommandOutcome.BOARD_NOT_FOUND, entry.outcome)
        assertEquals(-1, entry.revisionBefore)
        assertEquals(-1, entry.revisionAfter)
    }

    @Test
    fun formattedLineCarriesEveryTracedField() {
        val line = GameCommandLogEntry(
            boardId = "board",
            commandId = "cmd",
            playerId = "first",
            command = "EnterTransition:inner-outer",
            outcome = GameCommandOutcome.APPLIED,
            revisionBefore = 7,
            revisionAfter = 8,
            schemaVersion = 2,
            rulesVersion = 1,
            contentPackVersions = mapOf(StandardFeatures.Corruption to 1),
            domainEvents = listOf("PlayerChanged"),
            loadMillis = 3,
            engineMillis = 1,
            commitMillis = 5,
        ).format()

        listOf(
            "boardId=board",
            "commandId=cmd",
            "playerId=first",
            "command=EnterTransition:inner-outer",
            "outcome=APPLIED",
            "revisionBefore=7",
            "revisionAfter=8",
            "schemaVersion=2",
            "rulesVersion=1",
            "events=PlayerChanged",
            "loadMs=3",
            "engineMs=1",
            "commitMs=5",
        ).forEach { assertContains(line, it) }
        assertTrue(line.startsWith("game.command "))
    }

    private fun service(recorded: MutableList<GameCommandLogEntry>): GameApplicationService {
        val mutex = Mutex()
        return GameApplicationService(
            repository = InMemoryGameRepository(snapshot()),
            engine = GameEngine(FixedGameRandom),
            transactionMutex = { mutex },
            log = recorded::add,
        )
    }

    private fun command(id: String, command: GameCommand) = GameCommandEnvelope(
        commandId = id,
        boardId = "board",
        playerId = "first",
        expectedRevision = 0,
        command = command,
    )

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

    private object FixedGameRandom : GameRandom {
        override fun nextInt(from: Int, until: Int): Int = 3
    }

    private class InMemoryGameRepository(private var snapshot: GameSnapshot) : GameRepository {
        override suspend fun load(boardId: String): GameSnapshot = snapshot

        override suspend fun save(previous: GameSnapshot, updated: GameSnapshot) {
            snapshot = updated
        }
    }
}
