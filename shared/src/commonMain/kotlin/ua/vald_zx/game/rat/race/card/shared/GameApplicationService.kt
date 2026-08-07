package ua.vald_zx.game.rat.race.card.shared

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface GameRepository {
    suspend fun load(boardId: String): GameSnapshot?

    suspend fun save(previous: GameSnapshot, updated: GameSnapshot)
}

class GameApplicationService(
    private val repository: GameRepository,
    private val engine: GameEngine,
    private val transactionMutex: (String) -> Mutex,
    private val clock: GameClock = SystemGameClock,
    private val log: GameCommandLog = GameCommandLog.None,
) {
    suspend fun execute(envelope: GameCommandEnvelope): GameExecution? {
        return transactionMutex(envelope.boardId).withLock {
            val loadStart = clock.nowEpochMilliseconds()
            val snapshot = repository.load(envelope.boardId)
            val engineStart = clock.nowEpochMilliseconds()
            if (snapshot == null) {
                log.record(entry(envelope, GameCommandOutcome.BOARD_NOT_FOUND, loadMillis = engineStart - loadStart))
                return@withLock null
            }
            val execution = engine.execute(snapshot, envelope)
            val commitStart = clock.nowEpochMilliseconds()
            if (execution is GameExecution.Applied) {
                repository.save(snapshot, execution.snapshot)
            }
            log.record(
                entry(
                    envelope = envelope,
                    outcome = execution.outcome,
                    before = snapshot,
                    execution = execution,
                    loadMillis = engineStart - loadStart,
                    engineMillis = commitStart - engineStart,
                    commitMillis = clock.nowEpochMilliseconds() - commitStart,
                ),
            )
            execution
        }
    }

    private fun entry(
        envelope: GameCommandEnvelope,
        outcome: GameCommandOutcome,
        before: GameSnapshot? = null,
        execution: GameExecution? = null,
        loadMillis: Long = 0,
        engineMillis: Long = 0,
        commitMillis: Long = 0,
    ): GameCommandLogEntry {
        val board = execution?.snapshot?.board ?: before?.board
        return GameCommandLogEntry(
            boardId = envelope.boardId,
            commandId = envelope.commandId,
            playerId = envelope.playerId,
            command = envelope.command.logName,
            outcome = outcome,
            rejection = (execution as? GameExecution.Rejected)?.reason,
            revisionBefore = before?.board?.revision ?: -1,
            revisionAfter = board?.revision ?: -1,
            schemaVersion = board?.schemaVersion ?: -1,
            rulesVersion = board?.rulesVersion ?: -1,
            contentPackVersions = board?.contentPackVersions.orEmpty(),
            domainEvents = (execution as? GameExecution.Applied)?.result?.events.orEmpty()
                .map { it.logName },
            loadMillis = loadMillis,
            engineMillis = engineMillis,
            commitMillis = commitMillis,
        )
    }
}

private val GameExecution.outcome: GameCommandOutcome
    get() = when (this) {
        is GameExecution.Applied -> GameCommandOutcome.APPLIED
        is GameExecution.Duplicate -> GameCommandOutcome.DUPLICATE
        is GameExecution.Rejected -> GameCommandOutcome.REJECTED
    }

private val GameCommand.logName: String
    get() = when (this) {
        is GameCommand.RollDice -> "RollDice"
        GameCommand.CompleteRoll -> "CompleteRoll"
        is GameCommand.EndTurn -> "EndTurn"
        GameCommand.AdvanceTurn -> "AdvanceTurn"
        is GameCommand.MoveTo -> "MoveTo"
        is GameCommand.EnterTransition -> "EnterTransition:$transitionId"
        is GameCommand.StartCard -> "StartCard:${definition.id}"
        is GameCommand.ChooseInteraction -> "ChooseInteraction:$interactionId"
    }

private val DomainEvent.logName: String
    get() = this::class.simpleName ?: "DomainEvent"
