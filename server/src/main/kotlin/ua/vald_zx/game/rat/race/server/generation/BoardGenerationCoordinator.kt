package ua.vald_zx.game.rat.race.server.generation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardGenerationProgress
import ua.vald_zx.game.rat.race.card.shared.BoardGenerationStage
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.TrackDefinition
import ua.vald_zx.game.rat.race.card.shared.dreamSlotIds
import ua.vald_zx.game.rat.race.card.shared.code
import ua.vald_zx.game.rat.race.card.shared.defaultTrackDefinition
import ua.vald_zx.game.rat.race.card.shared.ratRaceDreams
import ua.vald_zx.game.rat.race.card.shared.StandardFeatures
import ua.vald_zx.game.rat.race.card.shared.activeDeckTypes
import ua.vald_zx.game.rat.race.card.shared.hasFeature
import ua.vald_zx.game.rat.race.card.shared.requireValidFeatures
import ua.vald_zx.game.rat.race.card.shared.resolvedTracks
import ua.vald_zx.game.rat.race.card.shared.toCellInstance
import ua.vald_zx.game.rat.race.card.shared.toPlaceType
import ua.vald_zx.game.rat.race.server.LOGGER
import ua.vald_zx.game.rat.race.server.boardMutex
import ua.vald_zx.game.rat.race.server.data.Storage
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal object BoardGenerationCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val jobs = ConcurrentHashMap<String, Job>()

    fun continueGeneration(boardId: String) = launchGeneration(boardId, restart = false)

    fun restartGeneration(boardId: String) = launchGeneration(boardId, restart = true)

    suspend fun cancelGeneration(boardId: String) {
        jobs.remove(boardId)?.cancelAndJoin()
    }

    private fun launchGeneration(boardId: String, restart: Boolean) {
        val previous = jobs[boardId]
        jobs[boardId] = scope.launch {
            previous?.cancelAndJoin()
            if (restart) resetGeneration(boardId)
            runGeneration(boardId)
        }.also { job ->
            job.invokeOnCompletion { jobs.remove(boardId, job) }
        }
    }

    suspend fun markPendingAsPaused() {
        Storage.boards()
            .filter { board ->
                board.generation.enabled &&
                        !board.generationProgress.isReady &&
                        board.generationProgress.isRunning
            }
            .forEach { board ->
                boardMutex(board.id).withLock {
                    if (jobs.containsKey(board.id)) return@withLock
                    val current = Storage.getBoardOrNull(board.id) ?: return@withLock
                    val progress = current.generationProgress
                    val now = Clock.System.now().toEpochMilliseconds()
                    Storage.updateBoard(
                        current.copy(
                            generationProgress = progress.copy(
                                isRunning = false,
                                activeSinceEpochMs = null,
                                elapsedMillis = progress.elapsedMillisAt(now),
                                retryStartedAtEpochMs = null,
                                retryAtEpochMs = null,
                                retryProvider = "",
                            )
                        )
                    )
                }
            }
    }

    private suspend fun resetGeneration(boardId: String) {
        boardMutex(boardId).withLock {
            val board = Storage.getBoardOrNull(boardId) ?: return
            Storage.updateBoard(
                board.copy(
                    generatedBalance = null,
                    generatedCards = emptyMap(),
                    generatedProfessions = emptyList(),
                    generatedPlaces = emptyMap(),
                    trackDefinitions = emptyMap(),
                    tracks = emptyList(),
                    generatedTexts = emptyMap(),
                    dreams = ratRaceDreams,
                    generationProgress = BoardGenerationProgress(
                        stage = BoardGenerationStage.PREPARING,
                        completed = 0,
                        total = 1,
                        isRunning = true,
                        activeSinceEpochMs = Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            )
        }
    }

    private suspend fun runGeneration(boardId: String) {
        try {
            check(LlmSettings.enabled) { "No LLM provider is configured" }
            val initial = Storage.getBoard(boardId)
            val usageRecorder: suspend (LlmTokenUsage) -> Unit = { usage ->
                recordTokenUsage(boardId, usage)
            }
            val quotaRecorder: suspend (LlmQuotaSnapshot) -> Unit = { quota ->
                recordQuota(boardId, quota)
            }
            val retryRecorder: suspend (LlmRetryWait) -> Unit = { retry ->
                recordRetryWait(boardId, retry)
            }

            val activeDecks = initial.activeDeckTypes()
            val dreamCount = if (initial.hasFeature(StandardFeatures.Dreams)) dreamSlotIds.size else 0
            val baseUnits = 1 + 1 + activeDecks.size + 1
            val deckSizes = initial.cards.mapValues { it.value.size }
            val textGenerator = LlmTextGenerator(
                chat = LlmSettings.textChat(usageRecorder, quotaRecorder, retryRecorder),
                reviewer = LlmSettings.textReviewer(usageRecorder, quotaRecorder),
            )
            val textUnits = textGenerator.workUnits(
                deckSizes = deckSizes.values,
                professionCount = BoardGenerator.professionCount,
                dreamCount = dreamCount,
            )
            val totalUnits = baseUnits + textUnits

            progress(boardId, BoardGenerationStage.BALANCE, 0, totalUnits)
            val cachedBalance = initial.generatedBalance?.takeIf { balance ->
                runCatching { balance.validate() }.isSuccess
            }
            val balance = cachedBalance
                ?: llmBalanceGenerator(usageRecorder, quotaRecorder, retryRecorder)
                    .generate(initial.generation, deckSizes)
            checkpoint(boardId, BoardGenerationStage.BALANCE, 1, totalUnits) {
                copy(
                    loanLimit = balance.loanLimit,
                    businessLimit = balance.businessLimit,
                    transportMovementBonusEnabled = balance.transportMovementBonusEnabled,
                    outerCircleConditions = balance.outerCircleConditions(),
                    victoryConditions = balance.victoryConditions(),
                    generatedBalance = balance,
                    generatedCards = if (cachedBalance == null) emptyMap() else generatedCards,
                    generatedProfessions = if (cachedBalance == null) emptyList() else generatedProfessions,
                    generatedPlaces = if (cachedBalance == null) emptyMap() else generatedPlaces,
                    trackDefinitions = if (cachedBalance == null) emptyMap() else trackDefinitions,
                    tracks = if (cachedBalance == null) emptyList() else tracks,
                    generatedTexts = if (cachedBalance == null) emptyMap() else generatedTexts,
                )
            }
            val generator = BoardGenerator(initial.generation, balance)
            val dreams = if (dreamCount > 0) generator.generateDreams() else emptyList()

            progress(boardId, BoardGenerationStage.PROFESSIONS, 1, totalUnits)
            val cachedProfessions = initial.generatedProfessions.takeIf { professions ->
                cachedBalance != null &&
                        professions.size == BoardGenerator.professionCount &&
                        professions.map { it.id }.distinct().size == professions.size
            }
            val professions = cachedProfessions ?: generator.generateProfessions().also { generated ->
                checkpoint(boardId, BoardGenerationStage.PROFESSIONS, 2, totalUnits) {
                    copy(
                        generatedProfessions = generated,
                        generatedTexts = generatedTexts.mapValues { (_, texts) ->
                            texts.copy(professions = emptyMap(), professionDescriptions = emptyMap())
                        },
                    )
                }
            }
            progress(boardId, BoardGenerationStage.PROFESSIONS, 2, totalUnits)

            val generatedCards = initial.generatedCards.filter { (type, deck) ->
                cachedBalance != null && generatedDeckIsCurrent(type, deck, deckSizes[type] ?: 0)
            }.toMutableMap()
            activeDecks.forEachIndexed { index, type ->
                progress(boardId, BoardGenerationStage.CARDS, 2 + index, totalUnits, type.name)
                if (type !in generatedCards) {
                    val generated = generator.generate(mapOf(type to (deckSizes[type] ?: 0)))[type].orEmpty()
                    generatedCards[type] = generated
                    checkpoint(boardId, BoardGenerationStage.CARDS, 3 + index, totalUnits, type.name) {
                        copy(
                            generatedCards = generatedCards.toMap(),
                            generatedTexts = generatedTexts.mapValues { (_, texts) ->
                                texts.copy(cards = texts.cards - type)
                            },
                        )
                    }
                }
            }
            progress(
                boardId,
                BoardGenerationStage.CARDS,
                2 + activeDecks.size,
                totalUnits,
            )

            val textOffset = 2 + activeDecks.size
            progress(boardId, BoardGenerationStage.PLACES, textOffset, totalUnits)
            val cachedPlaces = initial.generatedPlaces.takeIf { places ->
                cachedBalance != null && BoardLayer.entries.all { layer ->
                    places[layer]?.size == layer.places.size
                }
            }
            val cachedTracks = initial.trackDefinitions.takeIf { tracks ->
                cachedBalance != null && BoardLayer.entries.all { layer ->
                    tracks[layer]?.cells?.size == layer.places.size
                }
            }
            val cachedDynamicTracks = initial.tracks.takeIf { tracks ->
                cachedBalance != null && tracks.isNotEmpty() && tracks.all { it.cells.isNotEmpty() }
            }
            val legacyTracks = cachedTracks ?: cachedPlaces?.mapValues { (layer, codes) ->
                layer.defaultTrackDefinition().copy(
                    cells = codes.mapIndexedNotNull { index, code ->
                        ua.vald_zx.game.rat.race.card.shared.placeTypeOfCode(code)
                            ?.toCellInstance("${layer.name.lowercase()}-$index")
                    },
                )
            }?.takeIf { definitions ->
                BoardLayer.entries.all { layer -> definitions[layer]?.cells?.size == layer.places.size }
            } ?: generator.generateTracks()
            val generatedTracks = cachedDynamicTracks ?: legacyTracks.values.sortedBy(TrackDefinition::order)
            val dynamicTracks = initial.copy(tracks = generatedTracks).resolvedTracks()
            initial.copy(tracks = dynamicTracks).requireValidFeatures()
            val places = legacyTracks.mapValues { (_, track) -> track.cells.map { it.toPlaceType().code() } }
            if (cachedTracks == null && cachedDynamicTracks == null) {
                checkpoint(boardId, BoardGenerationStage.PLACES, textOffset + 1, totalUnits) {
                    copy(
                        generatedPlaces = places,
                        trackDefinitions = legacyTracks,
                        tracks = dynamicTracks,
                        dreams = dreams,
                    )
                }
            }
            progress(boardId, BoardGenerationStage.PLACES, textOffset + 1, totalUnits)

            val storedBeforeTexts = Storage.getBoard(boardId)
            val texts = textGenerator.generateComplete(
                world = initial.generation,
                cards = generatedCards,
                professions = professions,
                dreams = dreams,
                existingTexts = storedBeforeTexts.generatedTexts,
                shareNames = balance.shares.associate { share ->
                    val uk = share.names["uk"] ?: share.ticker
                    val en = share.names["en"] ?: share.ticker
                    share.id to "$uk / $en"
                },
            ) { checkpointedTexts, completed, _, detail ->
                checkpoint(
                    boardId = boardId,
                    stage = BoardGenerationStage.TEXTS,
                    completed = textOffset + 1 + completed,
                    total = totalUnits,
                    detail = detail,
                ) {
                    copy(generatedTexts = checkpointedTexts)
                }
            }

            checkpoint(boardId, BoardGenerationStage.READY, 1, 1) {
                copy(
                    generatedCards = generatedCards,
                    generatedProfessions = professions,
                    generatedPlaces = places,
                    trackDefinitions = legacyTracks,
                    tracks = dynamicTracks,
                    generatedTexts = texts,
                    generatedBalance = balance,
                    dreams = dreams,
                )
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Throwable) {
            LOGGER.warn("Board generation $boardId failed: ${error.message}")
            fail(boardId, error.message ?: error::class.simpleName.orEmpty())
        }
    }

    private suspend fun fail(boardId: String, error: String) {
        boardMutex(boardId).withLock {
            val board = Storage.getBoardOrNull(boardId) ?: return
            val progress = board.generationProgress
            val now = Clock.System.now().toEpochMilliseconds()
            Storage.updateBoard(
                board.copy(
                    generationProgress = progress.copy(
                        stage = BoardGenerationStage.FAILED,
                        error = error,
                        isRunning = false,
                        activeSinceEpochMs = null,
                        elapsedMillis = progress.elapsedMillisAt(now),
                        retryStartedAtEpochMs = null,
                        retryAtEpochMs = null,
                        retryProvider = "",
                    )
                )
            )
        }
    }

    private suspend fun recordTokenUsage(boardId: String, usage: LlmTokenUsage) {
        boardMutex(boardId).withLock {
            val board = Storage.getBoardOrNull(boardId) ?: return
            val progress = board.generationProgress
            val now = Clock.System.now().toEpochMilliseconds()
            val stillWaiting = progress.retryAtEpochMs?.let { it > now } == true
            Storage.updateBoard(
                board.copy(
                    generationProgress = progress.copy(
                        inputTokens = progress.inputTokens + usage.input,
                        outputTokens = progress.outputTokens + usage.output,
                        totalTokens = progress.totalTokens + usage.total,
                        requestCount = progress.requestCount + 1,
                        quotaType = usage.quota?.type ?: progress.quotaType,
                        quotaLimit = usage.quota?.limit ?: progress.quotaLimit,
                        quotaUsed = usage.quota?.used ?: progress.quotaUsed,
                        quotaResetAtEpochMs = usage.quota?.resetAtEpochMs ?: progress.quotaResetAtEpochMs,
                        retryStartedAtEpochMs = progress.retryStartedAtEpochMs.takeIf { stillWaiting },
                        retryAtEpochMs = progress.retryAtEpochMs.takeIf { stillWaiting },
                        retryProvider = if (stillWaiting) progress.retryProvider else "",
                    )
                )
            )
        }
    }

    private suspend fun recordQuota(boardId: String, quota: LlmQuotaSnapshot) {
        boardMutex(boardId).withLock {
            val board = Storage.getBoardOrNull(boardId) ?: return
            val progress = board.generationProgress
            Storage.updateBoard(
                board.copy(
                    generationProgress = progress.copy(
                        quotaType = quota.type,
                        quotaLimit = quota.limit,
                        quotaUsed = quota.used,
                        quotaResetAtEpochMs = quota.resetAtEpochMs,
                    )
                )
            )
        }
    }

    private suspend fun recordRetryWait(boardId: String, retry: LlmRetryWait) {
        boardMutex(boardId).withLock {
            val board = Storage.getBoardOrNull(boardId) ?: return
            val progress = board.generationProgress
            val now = Clock.System.now().toEpochMilliseconds()
            Storage.updateBoard(
                board.copy(
                    generationProgress = progress.copy(
                        retryStartedAtEpochMs = now,
                        retryAtEpochMs = now + retry.delayMillis,
                        retryProvider = "${retry.provider}/${retry.model}",
                        quotaType = retry.quota?.type ?: progress.quotaType,
                        quotaLimit = retry.quota?.limit ?: progress.quotaLimit,
                        quotaUsed = retry.quota?.used ?: progress.quotaUsed,
                        quotaResetAtEpochMs = retry.quota?.resetAtEpochMs ?: progress.quotaResetAtEpochMs,
                    )
                )
            )
        }
    }

    private suspend fun progress(
        boardId: String,
        stage: BoardGenerationStage,
        completed: Int,
        total: Int,
        detail: String = "",
        error: String = "",
    ) = checkpoint(boardId, stage, completed, total, detail, error)

    private suspend fun checkpoint(
        boardId: String,
        stage: BoardGenerationStage,
        completed: Int,
        total: Int,
        detail: String = "",
        error: String = "",
        transform: Board.() -> Board = { this },
    ) {
        boardMutex(boardId).withLock {
            val board = Storage.getBoardOrNull(boardId) ?: return
            val previousProgress = board.generationProgress
            val now = Clock.System.now().toEpochMilliseconds()
            val isRunning = stage != BoardGenerationStage.READY && stage != BoardGenerationStage.FAILED
            Storage.updateBoard(
                board.transform().copy(
                    generationProgress = BoardGenerationProgress(
                        stage = stage,
                        completed = completed,
                        total = total,
                        detail = detail,
                        error = error,
                        isRunning = isRunning,
                        activeSinceEpochMs = now.takeIf { isRunning },
                        elapsedMillis = previousProgress.elapsedMillisAt(now),
                        inputTokens = previousProgress.inputTokens,
                        outputTokens = previousProgress.outputTokens,
                        totalTokens = previousProgress.totalTokens,
                        requestCount = previousProgress.requestCount,
                        quotaType = previousProgress.quotaType,
                        quotaLimit = previousProgress.quotaLimit,
                        quotaUsed = previousProgress.quotaUsed,
                        quotaResetAtEpochMs = previousProgress.quotaResetAtEpochMs,
                    )
                )
            )
        }
    }
}

private fun generatedDeckIsCurrent(
    type: BoardCardType,
    deck: Map<Int, BoardCard>,
    size: Int,
): Boolean {
    if (deck.keys != (1..size).toSet()) return false
    if (type != BoardCardType.EventStore) return true
    val hasCorruptBusiness = size < 7 || deck.values.any { it is BoardCard.EventStore.CorruptBusiness }
    val hasCorruptLand = size < 8 || deck.values.any { it is BoardCard.EventStore.CorruptLand }
    return hasCorruptBusiness && hasCorruptLand
}
