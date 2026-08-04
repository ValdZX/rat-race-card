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
}
