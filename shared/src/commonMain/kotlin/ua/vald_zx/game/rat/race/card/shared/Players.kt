package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val lastTotals: List<Long> = emptyList(),
    val lastCashFlows: List<Long> = emptyList(),
    val speech: PlayerSpeech? = null,
    val selectedDreamId: String? = null,
    val purchasedDreamIds: Set<String> = emptySet(),
)

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

@Serializable
data class PlayerLocation(
    val position: Int = 1,
    val level: Int = 0,
)

enum class BusinessType(val klass: Int) {
    WORK(0),
    SMALL(1),
    MEDIUM(2),
    LARGE(3),
    CORRUPTION(3)
}

enum class SharesType {
    GC,
    ShchHP,
    TO,
    CST,
    AGRO,
    IT,
}


@Serializable
data class Shares(
    val type: SharesType,
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
)

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
    val fundBaseRate: Long = 20,
    val fundStartRate: Long = 30,
    val marriageCost: Long = 5000,
    val hasFunds: Boolean = true,
    val tts: Boolean = false,
)

fun Player.total(): Long {
    return cash +
            deposit +
            funds.sumOf { it.amount } +
            sharesList.sumOf { it.price } +
            businesses.sumOf { it.price } -
            loan
}

fun Player.balance(): Long {
    return cash + deposit + funds.sumOf { it.amount }
}

fun Player.activeProfit(): Long {
    return businesses.sumOf { it.profit + it.extentions.sum() }
}

fun Player.passiveProfit(): Long {
    return ((deposit / 100.0) * config.depositRate).toLong()
}

fun Player.totalProfit(): Long {
    return activeProfit() + passiveProfit()
}

fun Player.creditExpenses(): Long {
    return ((loan / 100.0) * config.loadRate).toLong()
}

fun Player.totalExpenses(): Long {
    var totalExpenses = 0L
    totalExpenses += card.food
    totalExpenses += card.rent
    totalExpenses += card.cloth
    totalExpenses += card.phone
    totalExpenses += card.transport
    totalExpenses += babies * config.babyCost
    totalExpenses += cars * config.carCost
    totalExpenses += apartment * config.apartmentCost
    totalExpenses += cottage * config.cottageCost
    totalExpenses += yacht * config.yachtCost
    totalExpenses += flight * config.flightCost
    totalExpenses += creditExpenses()
    return totalExpenses
}

fun Player.cashFlow(): Long {
    return totalProfit() - totalExpenses()
}

fun Player.canEnterOuterCircle(canRoll: Boolean, conditions: OuterCircleConditions): Boolean {
    return canRoll
            && location.level == BoardLayer.INNER.level &&
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
            flight > 0 -> 2
            cars > 0 -> 1
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
