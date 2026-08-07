package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
enum class Gender { MALE, FEMALE }

@Serializable
data class OfflinePlayer(
    val id: String,
    val name: String,
    val cashFlow: Long,
    val total: Long,
    val room: String,
    val removed: Boolean = false,
    val lastTotals: List<Long> = emptyList(),
    val lastCashFlows: List<Long> = emptyList(),
)

@Serializable
data class PlayerSpeech(
    val text: String,
    val expiresAtEpochMs: Long,
)

@Serializable
data class SendMoneyPack(
    val payerName: String,
    val payerId: String,
    val receiverId: String,
    val amount: Long,
)

@Serializable
data class Player(
    @SerialName("_id")
    val id: String,
    val boardId: String,
    val attrs: PlayerAttributes,
    val card: PlayerCard = PlayerCard(),
    val location: PlayerLocation = PlayerLocation(),
    val cash: Long = 0,
    val deposit: Long = 0,
    val loan: Long = 0,
    val debts: List<Debt> = emptyList(),
    val businesses: List<Business> = emptyList(),
    val isMarried: Boolean = false,
    val babies: Long = 0,
    val cars: Long = 0,
    val apartment: Long = 0,
    val cottage: Long = 0,
    val yacht: Long = 0,
    val flight: Long = 0,
    val animal: Long = 0,
    val inRest: Long = 0,
    val sharesList: List<Shares> = emptyList(),
    val estateList: List<Estate> = emptyList(),
    val landList: List<Land> = emptyList(),
    val funds: List<Fund> = emptyList(),
    val config: Config = Config(),
    val isInactive: Boolean = false,
    val salaryPosition: Int? = null,
    val investmentPosition: Int? = null,
    val startCapitalization: StartCapitalization? = null,
    val lastTotals: List<Long> = emptyList(),
    val lastCashFlows: List<Long> = emptyList(),
    val lastLoans: List<Long> = emptyList(),
    val speech: PlayerSpeech? = null,
    val selectedDreamId: String? = null,
    val purchasedDreamIds: Set<String> = emptySet(),
    val deputies: Int = 0,
)

fun Player.isActiveOn(board: Board): Boolean {
    return !isInactive && id in board.playerIds
}

fun Board.isActivePlayer(player: Player): Boolean {
    return activePlayerId == player.id && player.isActiveOn(this)
}

fun Board.activePlayers(players: Iterable<Player>): List<Player> {
    return players.filter { player -> player.isActiveOn(this) }
}

@Serializable
data class DebugPlayerValues(
    val cash: Long,
    val deposit: Long,
    val loan: Long,
    val babies: Long,
    val cars: Long,
    val apartment: Long,
    val cottage: Long,
    val yacht: Long,
    val flight: Long,
    val animal: Long,
)

@Serializable
data class PlayerAttributes(
    val color: Long,
    val avatar: Int = 0,
)

@Serializable
data class PlayerCard(
    val name: String = "",
    val gender: Gender = Gender.MALE,
    val profession: String = "",
    val salary: Long = 0,
    val rent: Long = 0,
    val food: Long = 0,
    val cloth: Long = 0,
    val transport: Long = 0,
    val phone: Long = 0,
)

@Serializable
data class Business(
    val type: BusinessType,
    val name: String,
    val price: Long,
    val profit: Long,
    val extentions: List<Long> = emptyList(),
    val alarmed: Boolean = false,
    val fromAuction: Boolean = false,
)

@Serializable(with = PlayerLocationSerializer::class)
data class PlayerLocation(
    val position: Int = 1,
    val trackId: TrackId = CoreTrackIds.Inner,
) {
    constructor(position: Int = 1, level: Int) : this(position, level.toTrackId())
}

object PlayerLocationSerializer : KSerializer<PlayerLocation> {
    override val descriptor = PersistedPlayerLocation.serializer().descriptor

    override fun deserialize(decoder: Decoder): PlayerLocation {
        val persisted = decoder.decodeSerializableValue(PersistedPlayerLocation.serializer())
        val trackId = persisted.trackId ?: persisted.level?.toTrackId() ?: CoreTrackIds.Inner
        return PlayerLocation(persisted.position, trackId)
    }

    override fun serialize(encoder: Encoder, value: PlayerLocation) {
        encoder.encodeSerializableValue(
            PersistedPlayerLocation.serializer(),
            PersistedPlayerLocation(position = value.position, trackId = value.trackId),
        )
    }
}

@Serializable
private data class PersistedPlayerLocation(
    val position: Int = 1,
    val trackId: TrackId? = null,
    val level: Int? = null,
)

enum class BusinessType(val klass: Int) {
    WORK(0),
    SMALL(1),
    MEDIUM(2),
    LARGE(3),
    CORRUPTION(3)
}

object SharesType {
    const val GC = "GC"
    const val ShchHP = "ShchHP"
    const val TO = "TO"
    const val CST = "CST"
    const val AGRO = "AGRO"
    const val IT = "IT"
}

object ShareSectors {
    const val ENERGY = "core.energy"
    const val INDUSTRY = "core.industry"
    const val CONSUMER = "core.consumer"
    const val REALTY = "core.realty"
    const val AGRO = "core.agro"
    const val TECH = "core.tech"

    val all: List<String> = listOf(ENERGY, INDUSTRY, CONSUMER, REALTY, AGRO, TECH)
}

internal val legacyShareSectors: Map<String, String> = mapOf(
    SharesType.GC to ShareSectors.ENERGY,
    SharesType.ShchHP to ShareSectors.INDUSTRY,
    SharesType.TO to ShareSectors.CONSUMER,
    SharesType.CST to ShareSectors.REALTY,
    SharesType.AGRO to ShareSectors.AGRO,
    SharesType.IT to ShareSectors.TECH,
)

@Serializable
data class Shares(
    val type: String,
    val count: Long,
    val buyPrice: Long,
) {
    val price: Long
        get() = count * buyPrice
}

@Serializable
data class Estate(
    val name: String,
    val price: Long,
)

@Serializable
data class Land(
    val name: String,
    val area: Long,
    val price: Long,
    val corrupt: Boolean = false,
)

fun Board.isCorruptLand(land: Land): Boolean {
    if (land.corrupt) return true
    val generatedCorruptLand = generatedCards[BoardCardType.Chance]
        .orEmpty()
        .values
        .filterIsInstance<BoardCard.Chance.CorruptLand>()
        .any { card -> card.area == land.area && card.price == land.price }
    return generatedCorruptLand || generatedCards.isEmpty() && land.area >= 200
}

@Serializable
data class Fund(
    val rate: Long,
    val amount: Long,
)

@Serializable
data class Config(
    val depositRate: Long = 2,
    val loadRate: Long = 10,
    val babyCost: Long = 300,
    val carCost: Long = 600,
    val apartmentCost: Long = 200,
    val cottageCost: Long = 1000,
    val yachtCost: Long = 1500,
    val flightCost: Long = 5000,
    val animalCost: Long = 100,
    val fundBaseRate: Long = 20,
    val fundStartRate: Long = 30,
    val marriageCost: Long = 5000,
    val childBenefit: Long = 1000,
    val deputyCardPrice: Long = 50_000,
    val taxInspectionBribePercentage: Long = 20,
    val mediumRiskMultiplier: Long = 2,
    val highRiskMultiplier: Long = 6,
    val salaryFundRates: List<Long> = listOf(20, 15, 10, 5),
    val restTurnCount: Long = 2,
    val divorceAssetRetentionPercentage: Long = 50,
    val carMovementBonus: Int = 1,
    val planeMovementBonus: Int = 2,
    val hasFunds: Boolean = true,
    val tts: Boolean = false,
    val priceIndexPercent: Long = NEUTRAL_INDEX_PERCENT,
    val salaryIndexPercent: Long = NEUTRAL_INDEX_PERCENT,
    val paydayRate: Long = 30,
)

fun Player.total(): Long {
    return financialSnapshot().total()
}

fun Player.taxInspectionBribe(board: Board): Long {
    val ownsCorruptAsset = businesses.any { it.type == BusinessType.CORRUPTION } ||
            landList.any(board::isCorruptLand)
    return if (ownsCorruptAsset) {
        total().coerceAtLeast(0) * config.taxInspectionBribePercentage / 100
    } else {
        0
    }
}

fun Player.balance(): Long {
    return financialSnapshot().balance()
}

fun Player.activeProfit(): Long {
    return financialSnapshot().activeProfit()
}

fun Player.passiveProfit(): Long {
    return financialSnapshot().passiveProfit()
}

fun Player.totalProfit(): Long {
    return financialSnapshot().totalProfit()
}

fun Player.creditExpenses(): Long {
    return financialSnapshot().creditExpenses()
}

fun Player.totalExpenses(): Long {
    return financialSnapshot().totalExpenses()
}

fun Player.cashFlow(): Long {
    return financialSnapshot().cashFlow()
}

fun Player.resolvedDebts(): List<Debt> = debts.resolved(loan, config.loadRate)

fun Player.financialAccount(): FinancialAccount = FinancialAccount(
    cash = cash,
    deposit = deposit,
    debts = resolvedDebts(),
    funds = funds.map { FinancialFund(it.rate, it.amount) },
)

fun Player.withFinancialAccount(account: FinancialAccount): Player = copy(
    cash = account.cash,
    deposit = account.deposit,
    loan = account.debts.totalPrincipal(),
    debts = account.debts,
    funds = account.funds.map { Fund(it.rate, it.amount) },
)

fun Player.financialSnapshot(): FinancialSnapshot = FinancialSnapshot(
    account = financialAccount(),
    salaryIncome = businesses.filter { it.type == BusinessType.WORK }
        .map { it.profit + it.extentions.sum() },
    activeIncome = businesses.filterNot { it.type == BusinessType.WORK }
        .map { it.profit + it.extentions.sum() },
    assetValues = sharesList.map { it.price } + businesses.map { it.price },
    baseExpenses = listOf(card.food, card.rent, card.cloth, card.phone, card.transport),
    recurringExpenses = listOf(
        babies * config.babyCost,
        cars * config.carCost,
        apartment * config.apartmentCost,
        cottage * config.cottageCost,
        yacht * config.yachtCost,
        flight * config.flightCost,
        animal * config.animalCost,
    ),
    depositRate = config.depositRate,
    loanRate = config.loadRate,
    priceIndexPercent = config.priceIndexPercent,
    salaryIndexPercent = config.salaryIndexPercent,
)

fun Player.canEnterOuterCircle(canRoll: Boolean, conditions: OuterCircleConditions): Boolean {
    return canRoll
            && location.trackId == CoreTrackIds.Inner &&
            cashFlow() >= conditions.minimumCashFlow &&
            (!conditions.apartmentRequired || apartment > 0) &&
            (!conditions.carRequired || cars > 0) &&
            balance() >= conditions.minimumAccountBalance
}

fun Player.hasMetVictoryConditions(conditions: VictoryConditions): Boolean {
    val selectedDreamPurchased = selectedDreamId != null &&
            selectedDreamId in purchasedDreamIds
    return (!conditions.dreamRequired || selectedDreamPurchased) &&
            (!conditions.planeRequired || flight > 0) &&
            (!conditions.estateRequired || cottage > 0) &&
            balance() >= conditions.minimumAccountBalance
}

fun Player.movementSteps(dice: Int, transportMovementBonusEnabled: Boolean): Int {
    val transportBonus = if (transportMovementBonusEnabled) {
        when {
            flight > 0 -> config.planeMovementBonus
            cars > 0 -> config.carMovementBonus
            else -> 0
        }
    } else {
        0
    }
    return dice + transportBonus
}

fun Player.withDebugValues(values: DebugPlayerValues): Player {
    return copy(
        cash = values.cash.coerceAtLeast(0),
        deposit = values.deposit.coerceAtLeast(0),
        loan = values.loan.coerceAtLeast(0),
        debts = legacyCreditLine(values.loan.coerceAtLeast(0), config.loadRate),
        babies = values.babies.coerceAtLeast(0),
        cars = values.cars.coerceAtLeast(0),
        apartment = values.apartment.coerceAtLeast(0),
        cottage = values.cottage.coerceAtLeast(0),
        yacht = values.yacht.coerceAtLeast(0),
        flight = values.flight.coerceAtLeast(0),
        animal = values.animal.coerceAtLeast(0),
    )
}

fun Player.fundAmount(): Long {
    return funds.sumOf { it.amount }
}
