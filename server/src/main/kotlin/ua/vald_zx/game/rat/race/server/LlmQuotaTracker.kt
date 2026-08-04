package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.GenerationQuotaType
import java.time.Instant
import java.time.ZoneId

internal data class LlmQuotaSnapshot(
    val type: GenerationQuotaType,
    val limit: Long,
    val used: Long,
    val resetAtEpochMs: Long,
)

internal class LlmQuotaTracker(
    private val nowEpochMs: () -> Long = System::currentTimeMillis,
) {
    private data class UsageEvent(
        val provider: String,
        val model: String,
        val timestamp: Long,
        val inputTokens: Long,
    )

    private data class QuotaKey(
        val provider: String,
        val model: String,
        val type: GenerationQuotaType,
    )

    private val lock = Any()
    private val events = mutableListOf<UsageEvent>()
    private val limits = mutableMapOf<QuotaKey, Long>()
    private val activeTypes = mutableMapOf<Pair<String, String>, GenerationQuotaType>()

    fun recordSuccess(provider: String, model: String, inputTokens: Long): LlmQuotaSnapshot? = synchronized(lock) {
        val now = nowEpochMs()
        events += UsageEvent(provider, model, now, inputTokens)
        prune(now)
        val type = activeTypes[provider to model] ?: return@synchronized null
        if (!type.isLocallyTrackable) return@synchronized null
        snapshot(QuotaKey(provider, model, type), now)
    }

    fun recordLimit(
        provider: String,
        model: String,
        type: GenerationQuotaType,
        limit: Long,
        retryDelayMillis: Long,
    ): LlmQuotaSnapshot? = synchronized(lock) {
        if (!type.isLocallyTrackable || limit <= 0) return@synchronized null
        val now = nowEpochMs()
        prune(now)
        val key = QuotaKey(provider, model, type)
        limits[key] = limit
        activeTypes[provider to model] = type
        val measured = measuredUsage(key, now)
        val resetAt = if (type.isDaily) nextPacificReset(now) else now + retryDelayMillis
        LlmQuotaSnapshot(type, limit, maxOf(measured, limit), resetAt)
    }

    private fun snapshot(key: QuotaKey, now: Long): LlmQuotaSnapshot? {
        val limit = limits[key] ?: return null
        val relevant = relevantEvents(key, now)
        val used = when (key.type) {
            GenerationQuotaType.REQUESTS_PER_MINUTE,
            GenerationQuotaType.REQUESTS_PER_DAY -> relevant.size.toLong()

            GenerationQuotaType.INPUT_TOKENS_PER_MINUTE,
            GenerationQuotaType.INPUT_TOKENS_PER_DAY -> relevant.sumOf { it.inputTokens }

            else -> return null
        }
        val resetAt = when {
            key.type.isDaily -> nextPacificReset(now)
            relevant.isNotEmpty() -> relevant.minOf { it.timestamp } + MINUTE_MILLIS
            else -> now + MINUTE_MILLIS
        }
        return LlmQuotaSnapshot(key.type, limit, used, resetAt)
    }

    private fun measuredUsage(key: QuotaKey, now: Long): Long {
        val relevant = relevantEvents(key, now)
        return when (key.type) {
            GenerationQuotaType.REQUESTS_PER_MINUTE,
            GenerationQuotaType.REQUESTS_PER_DAY -> relevant.size.toLong()

            GenerationQuotaType.INPUT_TOKENS_PER_MINUTE,
            GenerationQuotaType.INPUT_TOKENS_PER_DAY -> relevant.sumOf { it.inputTokens }

            else -> 0
        }
    }

    private fun relevantEvents(key: QuotaKey, now: Long): List<UsageEvent> {
        val start = if (key.type.isDaily) currentPacificDayStart(now) else now - MINUTE_MILLIS
        return events.filter { event ->
            event.provider == key.provider && event.model == key.model && event.timestamp >= start
        }
    }

    private fun prune(now: Long) {
        val oldestUsefulTimestamp = currentPacificDayStart(now).coerceAtMost(now - MINUTE_MILLIS)
        events.removeAll { it.timestamp < oldestUsefulTimestamp }
    }

    private companion object {
        const val MINUTE_MILLIS = 60_000L
        val PACIFIC_TIME: ZoneId = ZoneId.of("America/Los_Angeles")

        fun currentPacificDayStart(epochMs: Long): Long = Instant.ofEpochMilli(epochMs)
            .atZone(PACIFIC_TIME)
            .toLocalDate()
            .atStartOfDay(PACIFIC_TIME)
            .toInstant()
            .toEpochMilli()

        fun nextPacificReset(epochMs: Long): Long = Instant.ofEpochMilli(epochMs)
            .atZone(PACIFIC_TIME)
            .toLocalDate()
            .plusDays(1)
            .atStartOfDay(PACIFIC_TIME)
            .toInstant()
            .toEpochMilli()
    }
}

private val GenerationQuotaType.isDaily: Boolean
    get() = this == GenerationQuotaType.REQUESTS_PER_DAY || this == GenerationQuotaType.INPUT_TOKENS_PER_DAY

private val GenerationQuotaType.isLocallyTrackable: Boolean
    get() = this == GenerationQuotaType.REQUESTS_PER_MINUTE ||
            this == GenerationQuotaType.INPUT_TOKENS_PER_MINUTE ||
            isDaily

