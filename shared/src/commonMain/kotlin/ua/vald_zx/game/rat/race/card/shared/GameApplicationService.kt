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
) {
    suspend fun execute(envelope: GameCommandEnvelope): GameExecution? {
        return transactionMutex(envelope.boardId).withLock {
            val snapshot = repository.load(envelope.boardId) ?: return@withLock null
            val execution = engine.execute(snapshot, envelope)
            if (execution is GameExecution.Applied) {
                repository.save(snapshot, execution.snapshot)
            }
            execution
        }
    }
}
