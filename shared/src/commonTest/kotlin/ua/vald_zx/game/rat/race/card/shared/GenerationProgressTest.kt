package ua.vald_zx.game.rat.race.card.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class GenerationProgressTest {
    @Test
    fun runningTimeIncludesTheCurrentActiveSegment() {
        val progress = BoardGenerationProgress(
            isRunning = true,
            activeSinceEpochMs = 5_000,
            elapsedMillis = 2_000,
        )

        assertEquals(10_000, progress.elapsedMillisAt(13_000))
    }

    @Test
    fun pausedTimeDoesNotKeepGrowing() {
        val progress = BoardGenerationProgress(
            isRunning = false,
            elapsedMillis = 8_000,
        )

        assertEquals(8_000, progress.elapsedMillisAt(13_000))
    }

    @Test
    fun remainingTimeUsesCompletedWorkAndCurrentQuotaWait() {
        val progress = BoardGenerationProgress(
            completed = 40,
            total = 100,
            isRunning = true,
            activeSinceEpochMs = 0,
            retryAtEpochMs = 70_000,
        )

        assertEquals(95_000, progress.estimatedRemainingMillisAt(50_000))
    }

    @Test
    fun quotaRemainingNeverBecomesNegative() {
        val progress = BoardGenerationProgress(quotaLimit = 20, quotaUsed = 21)

        assertEquals(0, progress.quotaRemaining)
    }
}
