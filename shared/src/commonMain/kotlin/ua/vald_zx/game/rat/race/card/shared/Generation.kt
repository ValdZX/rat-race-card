package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

const val DEFAULT_LOCALE = "uk"

val generatedLocales = listOf("uk", "en")

@Serializable
data class BoardGeneration(
    val enabled: Boolean = false,
    val theme: String = "",
    val locality: String = "",
    val epoch: String = "",
    val seed: Long = 0,
) {
    val describesWorld: Boolean
        get() = theme.isNotBlank() || locality.isNotBlank() || epoch.isNotBlank()
}

@Serializable
enum class BoardGenerationStage {
    READY,
    PREPARING,
    BALANCE,
    PROFESSIONS,
    CARDS,
    PLACES,
    TEXTS,
    FAILED,
}

@Serializable
enum class GenerationQuotaType {
    UNKNOWN,
    REQUESTS_PER_MINUTE,
    INPUT_TOKENS_PER_MINUTE,
    REQUESTS_PER_DAY,
    INPUT_TOKENS_PER_DAY,
    SPEND_PER_TEN_MINUTES,
}

@Serializable
data class BoardGenerationProgress(
    val stage: BoardGenerationStage = BoardGenerationStage.READY,
    val completed: Int = 1,
    val total: Int = 1,
    val detail: String = "",
    val error: String = "",
    val isRunning: Boolean = false,
    val activeSinceEpochMs: Long? = null,
    val elapsedMillis: Long = 0,
    val inputTokens: Long = 0,
    val outputTokens: Long = 0,
    val totalTokens: Long = 0,
    val retryStartedAtEpochMs: Long? = null,
    val retryAtEpochMs: Long? = null,
    val retryProvider: String = "",
    val requestCount: Long = 0,
    val quotaType: GenerationQuotaType = GenerationQuotaType.UNKNOWN,
    val quotaLimit: Long = 0,
    val quotaUsed: Long = 0,
    val quotaResetAtEpochMs: Long? = null,
) {
    val isReady: Boolean
        get() = stage == BoardGenerationStage.READY

    val isFailed: Boolean
        get() = stage == BoardGenerationStage.FAILED

    val fraction: Float
        get() = if (total <= 0) 0f else completed.toFloat().div(total).coerceIn(0f, 1f)

    fun elapsedMillisAt(nowEpochMs: Long): Long = elapsedMillis + if (isRunning) {
        activeSinceEpochMs?.let { (nowEpochMs - it).coerceAtLeast(0) } ?: 0
    } else {
        0
    }

    fun estimatedRemainingMillisAt(nowEpochMs: Long): Long? {
        if (!isRunning || total <= 0 || completed <= 0 || completed >= total) return null
        val currentWaitElapsed = retryStartedAtEpochMs?.let { (nowEpochMs - it).coerceAtLeast(0) } ?: 0
        val productiveElapsed = (elapsedMillisAt(nowEpochMs) - currentWaitElapsed).coerceAtLeast(0)
        val averageUnitMillis = productiveElapsed.div(completed)
        val generationMillis = averageUnitMillis * (total - completed)
        val currentWaitMillis = retryAtEpochMs?.let { (it - nowEpochMs).coerceAtLeast(0) } ?: 0
        return generationMillis + currentWaitMillis
    }

    val quotaRemaining: Long
        get() = (quotaLimit - quotaUsed).coerceAtLeast(0)
}

@Serializable
data class GeneratedChanceWeights(
    val randomJob: Int,
    val estate: Int,
    val land: Int,
    val shares: Int,
    val corruptBusiness: Int,
    val corruptLand: Int,
)

@Serializable
data class GeneratedEventWeights(
    val land: Int,
    val estate: Int,
    val shares: Int,
    val businessExtending: Int,
    val reelection: Int,
    val announcement: Int,
)

@Serializable
data class GeneratedShare(
    val id: String,
    val ticker: String,
    val names: Map<String, String>,
)

@Serializable
data class GeneratedBalance(
    val salaries: List<Long>,
    val rentPercentages: List<Long>,
    val foodPercentages: List<Long>,
    val clothPercentages: List<Long>,
    val transportPercentages: List<Long>,
    val phonePercentages: List<Long>,
    val smallBusinessPrices: List<Long>,
    val smallBusinessReturnPercentages: List<Long>,
    val mediumBusinessPrices: List<Long>,
    val mediumBusinessReturnPercentages: List<Long>,
    val bigBusinessPrices: List<Long>,
    val bigBusinessReturnPercentages: List<Long>,
    val shoppingPrices: Map<ShopType, List<Long>>,
    val shopWeights: Map<ShopType, Int>,
    val expensePrices: List<Long>,
    val chancePrices: List<Long>,
    val eventPrices: List<Long>,
    val shares: List<GeneratedShare> = emptyList(),
    val sharePrices: List<Long>,
    val forcedShareSalePrices: List<Long> = emptyList(),
    val shareCounts: List<Long>,
    val businessExtensionProfits: List<Long>,
    val landAreas: List<Long>,
    val landPricePerUnit: List<Long>,
    val eventLandPricePercentages: List<Long>,
    val corruptBusinessPrices: List<Long>,
    val corruptBusinessReturnPercentages: List<Long>,
    val corruptOneTimeReturnPercentages: List<Long>,
    val corruptBusinessDeputies: List<Int>,
    val corruptLandPricePerUnit: List<Long>,
    val corruptLandAreas: List<Long>,
    val corruptLandDeputies: List<Int>,
    val corruptDeputyPercentage: Int,
    val corruptOneTimePercentage: Int,
    val forcedShareSalePercentage: Int = 0,
    val strayAnimalPercentage: Int = 0,
    val depositRate: Long = 2,
    val loanRate: Long = 10,
    val babyRecurringCost: Long = 300,
    val carRecurringCost: Long = 600,
    val apartmentRecurringCost: Long = 200,
    val houseRecurringCost: Long = 1_000,
    val yachtRecurringCost: Long = 1_500,
    val planeRecurringCost: Long = 5_000,
    val animalRecurringCost: Long = 100,
    val marriageCost: Long = 5_000,
    val childBenefit: Long = 1_000,
    val dreamMinPrice: Long = 1_000_000,
    val dreamMaxPrice: Long = 20_000_000,
    val chanceWeights: GeneratedChanceWeights,
    val eventWeights: GeneratedEventWeights,
    val loanLimit: Long = 0,
    val businessLimit: Long = 0,
    val transportMovementBonusEnabled: Boolean = true,
    val outerCircleMinimumCashFlow: Long = 0,
    val outerCircleMinimumAccountBalance: Long = 0,
    val outerCircleApartmentRequired: Boolean = true,
    val outerCircleCarRequired: Boolean = true,
    val victoryMinimumAccountBalance: Long = 0,
    val victoryDreamRequired: Boolean = true,
    val victoryPlaneRequired: Boolean = true,
    val victoryEstateRequired: Boolean = true,
)

@Serializable
data class CardText(
    val name: String = "",
    val description: String = "",
)

@Serializable
data class GeneratedText(
    val cards: Map<BoardCardType, Map<Int, CardText>> = emptyMap(),
    val professions: Map<Int, String> = emptyMap(),
    val professionDescriptions: Map<Int, String> = emptyMap(),
    val dreams: Map<String, CardText> = emptyMap(),
)

fun BoardGeneration.seedFor(salt: String, index: Int): Long {
    var mixed = seed
    mixed = mixed * 31 + salt.hashCode()
    mixed = mixed * 31 + index
    mixed = mixed * 31 + theme.hashCode()
    mixed = mixed * 31 + locality.hashCode()
    mixed = mixed * 31 + epoch.hashCode()
    return mixed
}

fun BoardGeneration.seedFor(type: BoardCardType, cardId: Int): Long = seedFor(type.name, cardId)
