package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.server.generation.LlmQuotaTracker

import ua.vald_zx.game.rat.race.card.shared.GenerationQuotaType
import kotlin.test.Test
import kotlin.test.assertEquals

class LlmQuotaTrackerTest {
    @Test
    fun minuteQuotaRemainingFollowsTheLocalRequestWindow() {
        var now = 1_000_000L
        val tracker = LlmQuotaTracker { now }
        repeat(3) { tracker.recordSuccess("gemini", "model", 100) }

        val limited = tracker.recordLimit(
            provider = "gemini",
            model = "model",
            type = GenerationQuotaType.REQUESTS_PER_MINUTE,
            limit = 5,
            retryDelayMillis = 30_000,
        )
        assertEquals(5, limited?.used)

        now += 61_000
        val recovered = tracker.recordSuccess("gemini", "model", 100)
        assertEquals(1, recovered?.used)
        assertEquals(5, recovered?.limit)
    }

    @Test
    fun tokenQuotaUsesInputTokensOnly() {
        var now = 2_000_000L
        val tracker = LlmQuotaTracker { now }
        tracker.recordSuccess("gemini", "model", 700)
        tracker.recordLimit(
            provider = "gemini",
            model = "model",
            type = GenerationQuotaType.INPUT_TOKENS_PER_MINUTE,
            limit = 1_000,
            retryDelayMillis = 20_000,
        )

        now += 61_000
        val recovered = tracker.recordSuccess("gemini", "model", 250)
        assertEquals(250, recovered?.used)
    }
}
