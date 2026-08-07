@file:Suppress("DEPRECATION")

package ua.vald_zx.game.rat.race.card.logic

import io.github.aakira.napier.Napier
import io.ktor.client.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import org.koin.core.Koin
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.beans.Config
import ua.vald_zx.game.rat.race.card.beans.Fund
import ua.vald_zx.game.rat.race.card.beans.Land
import ua.vald_zx.game.rat.race.card.beans.Shares
import ua.vald_zx.game.rat.race.card.beans.SharesType
import ua.vald_zx.game.rat.race.card.di.getRaceRatCardService
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.*
import ua.vald_zx.game.rat.race.card.raceRate2KStore
import ua.vald_zx.game.rat.race.card.screen.second.offlinePlayers
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.statistics2KStore
import kotlin.math.absoluteValue

@Serializable
data class Statistics(
    val log: MutableList<RatRace2CardState> = mutableListOf(),
    var salaryCount: Long = 0,
)

@Serializable
data class RatRace2CardState(
    val playerId: String = "",
    val playerCard: PlayerCard = PlayerCard(),
    val cash: Long = 0,
    val deposit: Long = 0,
    val loan: Long = 0,
    val debts: List<Debt> = emptyList(),
    val business: List<Business> = emptyList(),
    val lands: List<Land> = emptyList(),
    val isMarried: Boolean = false,
    val babies: Long = 0,
    val cars: Long = 0,
    val apartment: Long = 0,
    val cottage: Long = 0,
    val yacht: Long = 0,
    val flight: Long = 0,
    val sharesList: List<Shares> = emptyList(),
    val funds: List<Fund> = emptyList(),
    val config: Config = Config(),
    val lastTotals: List<Long> = emptyList(),
    val lastCashFlows: List<Long> = emptyList(),
    val room: String = "",
) : State {

    fun balance(): Long {
        return financialSnapshot().balance()
    }

    fun activeProfit(): Long {
        return financialSnapshot().activeProfit()
    }

    fun passiveProfit(): Long {
        return financialSnapshot().passiveProfit()
    }

    fun totalProfit(): Long {
        return financialSnapshot().totalProfit()
    }

    fun creditExpenses(): Long {
        return financialSnapshot().creditExpenses()
    }

    fun totalExpenses(): Long {
        return financialSnapshot().totalExpenses()
    }

    fun cashFlow(): Long {
        return financialSnapshot().cashFlow()
    }

    fun status(): String {
        return when {
            business.any { it.type == BusinessType.SMALL } -> "${playerCard.profession} - Підприємець"
            business.any { it.type == BusinessType.MEDIUM } -> "${playerCard.profession} - Бізнесмен"
            totalExpenses() > 1_000_000 -> "${playerCard.profession} - Мільйонер"
            else -> playerCard.profession
        }
    }

    fun existsShares(): Set<SharesType> {
        return sharesList.map { it.type }.toSet()
    }

    fun sharesCount(entry: SharesType): Long {
        return sharesList.filter { it.type == entry }.sumOf { it.count }
    }

    fun capitalization(): Long {
        return sharedMoneyService.capitalize(
            funds = financialAccount().funds,
            rateOverride = null,
            baseRate = config.fundBaseRate,
        ).profit
    }

    fun capitalizationStart(): Long {
        return sharedMoneyService.capitalize(
            funds = financialAccount().funds,
            rateOverride = config.fundStartRate,
            baseRate = config.fundBaseRate,
        ).profit
    }

    fun fundAmount(): Long {
        return funds.sumOf { it.amount }
    }
}

sealed class RatRace2CardAction : Action {
    data class LoadState(val state: RatRace2CardState) : RatRace2CardAction()
    data class FillProfessionCardRat(val playerCard: PlayerCard) : RatRace2CardAction()
    data class EditFillProfessionCardRat(val playerCard: PlayerCard) : RatRace2CardAction()
    data object Fired : RatRace2CardAction()
    data object FiredConfirmed : RatRace2CardAction()
    data object GetSalary : RatRace2CardAction()
    data object GetSalaryApproved : RatRace2CardAction()
    data class AddFund(val fund: Fund) : RatRace2CardAction()
    data class FromFund(val fund: Fund, val amount: Long) : RatRace2CardAction()
    data object CapitalizeFunds : RatRace2CardAction()
    data object CapitalizeStarsFunds : RatRace2CardAction()
    data object RandomBusiness : RatRace2CardAction()
    data object HideAlarm : RatRace2CardAction()
    data class Connected(val playerId: String, val room: String) : RatRace2CardAction()
    data class BuyLand(val land: Land) : RatRace2CardAction()
    data class SellLand(val priceOfUnit: Long, val area: Long) : RatRace2CardAction()
    data class BuyBusiness(val business: Business) : RatRace2CardAction()
    data class SellBusiness(val business: Business, val amount: Long) : RatRace2CardAction()
    data class DismissalConfirmed(val business: Business) : RatRace2CardAction()
    data class DismissalConfirmedOnExtention(val extention: ExtendBusiness) : RatRace2CardAction()
    data class SellingAllBusinessConfirmed(val business: Business) : RatRace2CardAction()
    data class ExtendBusiness(val amount: Long, val business: Business) : RatRace2CardAction()
    data class SideProfit(val amount: Long) : RatRace2CardAction()
    data class ReceivedCash(val payerId: String, val amount: Long) : RatRace2CardAction()
    data class SideExpenses(val amount: Long) : RatRace2CardAction()
    data class GetLoan(val amount: Long) : RatRace2CardAction()
    data class RepayLoan(val amount: Long, val debtId: String? = null) : RatRace2CardAction()
    data class ToDeposit(val amount: Long) : RatRace2CardAction()
    data class FromDeposit(val amount: Long) : RatRace2CardAction()
    data class BuyCar(val price: Long) : RatRace2CardAction()
    data class BuyApartment(val price: Long) : RatRace2CardAction()
    data class BuyCottage(val price: Long) : RatRace2CardAction()
    data class BuyYacht(val price: Long) : RatRace2CardAction()
    data class BuyFlight(val price: Long) : RatRace2CardAction()
    data class BuyShares(val shares: Shares) : RatRace2CardAction()
    data class UpdateConfig(val config: Config) : RatRace2CardAction()
    data class BackToState(val state: RatRace2CardState, val backCount: Int) : RatRace2CardAction()
    data class SellShares(val type: SharesType, val count: Long, val sellPrice: Long) :
        RatRace2CardAction()

    data class UpdateFamily(
        val isMarried: Boolean,
        val halfCash: Boolean = false,
        val marriageCost: Long = 0,
        val babies: Long? = null
    ) : RatRace2CardAction()

    data object AddBaby : RatRace2CardAction()
    data object Disconnect : RatRace2CardAction()

    data class Connect(val room: String) : RatRace2CardAction()
    data class SendMoney(val receiverId: String, val amount: Long) : RatRace2CardAction()
}

sealed class RatRace2CardSideEffect : Effect {
    data class ConfirmDismissal(val business: Business) : RatRace2CardSideEffect()
    data class ConfirmDismissalOnExtention(val extention: ExtendBusiness) : RatRace2CardSideEffect()
    data class ConfirmSellingAllBusiness(val business: Business) : RatRace2CardSideEffect()
    data class DepositWithdraw(val balance: Long) : RatRace2CardSideEffect()
    data class LoanAdded(val balance: Long) : RatRace2CardSideEffect()
    data object ConfirmFired : RatRace2CardSideEffect()
    data class AddCash(val amount: Long) : RatRace2CardSideEffect()
    data class SubCash(val amount: Long) : RatRace2CardSideEffect()
    data class Capitalized(val amount: Long) : RatRace2CardSideEffect()
    data class ReceivedCash(val payerId: String, val amount: Long) : RatRace2CardSideEffect()
    data object ShowSalaryApprove : RatRace2CardSideEffect()
}

class RatRace2CardStore(
    private val koin: Koin,
    private val random: GameRandom = DefaultGameRandom,
) :
    Store<RatRace2CardState, RatRace2CardAction, RatRace2CardSideEffect>,
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val state = MutableStateFlow(RatRace2CardState())
    private val sideEffect = MutableSharedFlow<RatRace2CardSideEffect>()
    var statistics: Statistics? = null

    suspend fun connect(room: String) {
        val handler = CoroutineExceptionHandler { _, t ->
            Napier.e("Invalid server", t)
            val service = koin.get<HttpClient>().getRaceRatCardService()
            koin.declare(service, allowOverride = true)
            dispatch(RatRace2CardAction.Disconnect)
        }
        val service = koin.get<RaceRatCardService>()
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch(handler) {
            service.playersObserve().collect { player ->
                if (player.removed) {
                    offlinePlayers.value = service.getPlayers()
                } else if (offlinePlayers.value.find { it.id == player.id } != null) {
                    offlinePlayers.update { list ->
                        list.find { it.id == player.id }?.let { oldPlayer ->
                            list.replace(oldPlayer, player)
                        } ?: (list + player)
                    }
                } else {
                    offlinePlayers.value = service.getPlayers()
                }
            }
        }
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch(handler) {
            service.sendMoneyObserve().collect {
                dispatch(RatRace2CardAction.ReceivedCash(it.payerId, it.amount))
            }
        }
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch(handler) {
            val playerId = service.hello(
                OfflinePlayer(
                    id = state.value.playerId,
                    name = state.value.playerCard.name.ifEmpty { state.value.playerCard.profession },
                    cashFlow = state.value.cashFlow(),
                    total = state.value.total(),
                    room = room,
                    lastTotals = state.value.lastTotals,
                    lastCashFlows = state.value.lastCashFlows,
                )
            )
            offlinePlayers.value = service.getPlayers()
            dispatch(Connected(playerId, room))
        }
    }

    override fun observeState(): StateFlow<RatRace2CardState> = state

    override fun observeSideEffect(): Flow<RatRace2CardSideEffect> = sideEffect

    override fun dispatch(action: RatRace2CardAction) {
        val oldState = state.value
        val newState = when (action) {
            is LoadState -> {
                action.state.apply {
                    launch { if (room.isNotEmpty()) connect(room) }
                }
            }

            is FillProfessionCardRat -> {
                statistics = Statistics()
                RatRace2CardState(
                    playerCard = action.playerCard,
                    business = listOf(
                        Business(
                            type = BusinessType.WORK,
                            name = action.playerCard.profession,
                            price = 0,
                            profit = action.playerCard.salary
                        )
                    )
                )
            }

            is EditFillProfessionCardRat -> {
                oldState.copy(playerCard = action.playerCard)
            }

            is ReceivedCash -> {
                launch {
                    sideEffect.emit(
                        RatRace2CardSideEffect.ReceivedCash(
                            action.payerId,
                            action.amount
                        )
                    )
                }
                oldState.plusCash(action.amount)
            }

            is SideProfit -> {
                oldState.plusCash(action.amount)
            }

            is SideExpenses -> {
                oldState.minusCash(action.amount)
            }

            HideAlarm -> {
                oldState.copy(business = oldState.business.map { it.copy(alarmed = false) })
            }

            RandomBusiness -> {
                val business = random.choose(oldState.business)
                if (business == null) {
                    oldState
                } else {
                    val businessList = oldState.business.map { it.copy(alarmed = business == it) }
                    oldState.copy(business = businessList)
                }
            }

            is BuyBusiness -> {
                val currentBusiness = oldState.business
                if (action.business.type == BusinessType.SMALL
                    && currentBusiness.any { it.type == BusinessType.WORK }
                    && currentBusiness.count { it.type == BusinessType.SMALL } == 1
                ) {
                    launch { sideEffect.emit(RatRace2CardSideEffect.ConfirmDismissal(action.business)) }
                    oldState
                } else if (currentBusiness.isNotEmpty()
                    && currentBusiness.first().type.klass != action.business.type.klass
                    && !currentBusiness.any { it.type == BusinessType.WORK }
                ) {
                    launch { sideEffect.emit(RatRace2CardSideEffect.ConfirmSellingAllBusiness(action.business)) }
                    oldState
                } else oldState.copy(business = currentBusiness + action.business)
                    .minusCash(action.business.price)
            }

            is SellBusiness -> {
                val business = oldState.business.toMutableList()
                business.remove(action.business)
                oldState.copy(business = business).plusCash(action.amount)
            }

            is BuyLand -> {
                oldState.copy(lands = oldState.lands + action.land)
                    .minusCash(action.land.priceOfUnit * action.land.area)
            }

            is SellLand -> {
                val lands = oldState.lands.toMutableList()
                val totalArea = lands.sumOf { it.area }
                if (totalArea >= action.area) {
                    val updatedLands = if (totalArea == action.area) {
                        emptyList()
                    } else {
                        var remainder = action.area
                        val newLands = lands.toMutableList()
                        lands.forEach { land ->
                            if (remainder == 0L) return@forEach
                            newLands -= land
                            if (land.area <= remainder) {
                                remainder -= land.area
                            } else {
                                newLands += land.copy(area = land.area - remainder)
                                remainder = 0
                            }
                        }
                        newLands
                    }
                    oldState.copy(lands = updatedLands).plusCash(action.area * action.priceOfUnit)
                } else {
                    oldState
                }
            }

            is ExtendBusiness -> {
                val currentBusiness = oldState.business
                val business = action.business
                val extended = business.copy(extentions = business.extentions + action.amount)
                if (action.business.type == BusinessType.SMALL
                    && currentBusiness.any { it.type == BusinessType.WORK }
                    && currentBusiness.count { it.type == BusinessType.SMALL } == 1
                ) {
                    launch { sideEffect.emit(RatRace2CardSideEffect.ConfirmDismissalOnExtention(action)) }
                    oldState
                } else {
                    oldState.copy(business = oldState.business.replace(business, extended))
                }
            }

            is DismissalConfirmed -> {
                val business =
                    oldState.business.filter { it.type != BusinessType.WORK } + action.business
                oldState.copy(business = business).minusCash(action.business.price)
            }

            is DismissalConfirmedOnExtention -> {
                val businessList =
                    oldState.business.filter { it.type != BusinessType.WORK }
                val business = action.extention.business
                val extended = business.copy(extentions = business.extentions + action.extention.amount)
                oldState.copy(business = businessList.replace(action.extention.business, extended))
            }

            is SellingAllBusinessConfirmed -> {
                oldState.copy(business = listOf(action.business))
                    .plusCash(oldState.business.sumOf { it.price })
                    .minusCash(action.business.price)
            }

            is BuyShares -> {
                val sharesList = oldState.sharesList + action.shares
                oldState.copy(sharesList = sharesList).minusCash(action.shares.price)
            }

            GetSalary -> {
                launch { sideEffect.emit(RatRace2CardSideEffect.ShowSalaryApprove) }
                oldState
            }

            GetSalaryApproved -> {
                statistics?.salaryCount = statistics?.salaryCount?.plus(1) ?: 0
                val cashFlow = oldState.cashFlow()
                if (cashFlow >= 0) {
                    oldState.plusCash(cashFlow)
                } else {
                    oldState.minusCash(cashFlow.absoluteValue)
                }
            }

            is SellShares -> {
                var resultList = oldState.sharesList.toMutableList()
                val sharesByType = resultList.filter { it.type == action.type }
                var needToSell = action.count
                var index = 0
                while (needToSell != 0L && index < sharesByType.size) {
                    val shares = sharesByType[index]
                    if (needToSell < shares.count) {
                        resultList = resultList.replace(
                            shares,
                            shares.copy(count = shares.count - needToSell)
                        ).toMutableList()
                        break
                    } else if (needToSell == shares.count) {
                        resultList.remove(shares)
                        break
                    } else {
                        resultList.remove(shares)
                        needToSell -= shares.count
                        index += 1
                    }
                }
                oldState.copy(sharesList = resultList).plusCash(action.count * action.sellPrice)
            }

            is RepayLoan -> {
                val target = action.debtId ?: oldState.resolvedDebts().avalancheOrder().firstOrNull()?.id
                val repaid = if (target == null) {
                    oldState.resolvedDebts()
                } else {
                    oldState.resolvedDebts().repay(target, action.amount)
                }
                oldState.withDebts(repaid).minusCash(action.amount)
            }

            is GetLoan -> {
                oldState.withDebts(
                    oldState.resolvedDebts().borrow(
                        id = CREDIT_LINE_DEBT_ID,
                        kind = DebtKind.CREDIT_LINE,
                        amount = action.amount,
                        ratePercent = oldState.config.loadRate,
                    ),
                ).plusCash(action.amount)
            }

            is FromDeposit -> {
                oldState.copy(deposit = oldState.deposit - action.amount).plusCash(action.amount)
            }

            is ToDeposit -> {
                oldState.copy(
                    deposit = oldState.deposit + action.amount
                ).minusCash(action.amount)
            }

            is UpdateFamily -> {
                oldState.copy(
                    isMarried = action.isMarried,
                    babies = action.babies ?: oldState.babies
                ).minusCash(
                    if (action.isMarried) {
                        action.marriageCost
                    } else {
                        if (action.halfCash) {
                            (oldState.cash + oldState.deposit) / 2
                        } else {
                            0
                        }
                    }
                )
            }

            AddBaby -> {
                oldState.copy(babies = oldState.babies + 1).plusCash(1000)
            }

            is BuyApartment -> {
                oldState.copy(
                    apartment = oldState.apartment + 1
                ).minusCash(action.price)
            }

            is BuyCar -> {
                oldState.copy(
                    cars = oldState.cars + 1
                ).minusCash(action.price)
            }

            is BuyCottage -> {
                oldState.copy(
                    cottage = oldState.cottage + 1
                ).minusCash(action.price)
            }

            is BuyFlight -> {
                oldState.copy(
                    flight = oldState.flight + 1
                ).minusCash(action.price)
            }

            is BuyYacht -> {
                oldState.copy(
                    yacht = oldState.yacht + 1
                ).minusCash(action.price)
            }

            is UpdateConfig -> {
                oldState.copy(config = action.config)
            }

            is AddFund -> {
                val currentFund = oldState.funds.find { it.rate == action.fund.rate }
                val funds = if (currentFund != null) {
                    oldState.funds.replace(
                        currentFund,
                        currentFund.copy(amount = currentFund.amount + action.fund.amount)
                    )
                } else {
                    oldState.funds + action.fund
                }
                oldState.copy(funds = funds).minusCash(action.fund.amount, true)
            }

            is FromFund -> {
                if ((action.fund.amount - action.amount) == 0L) {
                    oldState.copy(funds = oldState.funds.remove(action.fund))
                        .plusCash(action.amount)
                } else {
                    val newFund = action.fund.copy(amount = action.fund.amount - action.amount)
                    val funds = oldState.funds.replace(action.fund, newFund)
                    oldState.copy(funds = funds).plusCash(action.amount)
                }
            }

            CapitalizeFunds -> {
                val capitalization = oldState.capitalization()
                val amount = oldState.funds.sumOf { it.amount } + capitalization
                val funds = listOf(Fund(rate = oldState.config.fundBaseRate, amount))
                launch { sideEffect.emit(RatRace2CardSideEffect.Capitalized(capitalization)) }
                oldState.copy(funds = funds)
            }

            CapitalizeStarsFunds -> {
                val capitalization = oldState.capitalizationStart()
                val amount = oldState.funds.sumOf { it.amount } + capitalization
                val funds = listOf(Fund(rate = oldState.config.fundBaseRate, amount))
                launch { sideEffect.emit(RatRace2CardSideEffect.Capitalized(capitalization)) }
                oldState.copy(funds = funds)
            }

            Fired -> {
                launch { sideEffect.emit(RatRace2CardSideEffect.ConfirmFired) }
                oldState
            }

            FiredConfirmed -> {
                oldState.copy(business = oldState.business.filter { it.type != BusinessType.WORK })
            }

            is BackToState -> {
                repeat(action.backCount) { statistics?.log?.removeLastOrNull() }
                action.state
            }

            is Connected -> {
                oldState.copy(playerId = action.playerId, room = action.room)
            }

            is Connect -> {
                launch { connect(action.room) }
                oldState
            }

            is Disconnect -> {
                oldState.copy(room = "")
            }

            is SendMoney -> {
                launch {
                    val service = koin.get<RaceRatCardService>()
                    service.sendMoney(
                        SendMoneyPack(
                            payerName = oldState.playerCard.name,
                            payerId = oldState.playerId,
                            receiverId = action.receiverId,
                            amount = action.amount
                        )
                    )
                }
                oldState.minusCash(action.amount)
            }
        }.updateLasts()
        if (newState != oldState) {
            state.value = newState
            saveState(newState)
        }
    }

    private fun RatRace2CardState.updateLasts(): RatRace2CardState {
        val previous = statistics?.log?.lastOrNull() ?: return this
        val currentTotal = total()
        val previousTotal = previous.total()
        val currentCashFlow = cashFlow()
        val previousCashFlow = previous.cashFlow()
        return copy(
            lastTotals = appendRecentChange(lastTotals, previousTotal, currentTotal),
            lastCashFlows = appendRecentChange(lastCashFlows, previousCashFlow, currentCashFlow),
        )
    }

    private fun saveState(newState: RatRace2CardState) {
        launch(Dispatchers.Default) {
            raceRate2KStore.set(newState)
            val storedStatistics =
                statistics ?: runCatching { statistics2KStore.get() }.getOrNull() ?: Statistics()
            storedStatistics.log += newState
            statistics2KStore.set(storedStatistics)
            statistics = storedStatistics
            offlinePlayers.value.find { it.id == newState.playerId }?.let { player ->
                val offlinePlayer = player.copy(
                    cashFlow = newState.cashFlow(),
                    total = newState.total(),
                    lastTotals = newState.lastTotals,
                    lastCashFlows = newState.lastCashFlows,
                )
                val service = koin.get<RaceRatCardService>()
                service.updatePlayer(offlinePlayer)
            }
        }
    }

    private fun RatRace2CardState.plusCash(value: Long): RatRace2CardState {
        launch { sideEffect.emit(RatRace2CardSideEffect.AddCash(value)) }
        return withFinancialAccount(sharedMoneyService.addCash(financialAccount(), value))
    }

    private fun RatRace2CardState.minusCash(
        value: Long,
        isFundBuy: Boolean = false
    ): RatRace2CardState {
        if (value == 0L) return this
        launch { sideEffect.emit(RatRace2CardSideEffect.SubCash(value)) }
        val result = sharedMoneyService.pay(
            account = financialAccount(),
            amount = value,
            policy = PaymentPolicy(
                useFunds = !isFundBuy && config.hasFunds,
                creditRatePercent = config.loadRate,
                paydayRatePercent = config.paydayRate,
            ),
        )
        val usedFunds = result.events.any { it is PaymentEvent.FundsWithdrawn }
        result.events.forEach { paymentEvent ->
            when (paymentEvent) {
                is PaymentEvent.PaydayLoanTaken -> Unit
                is PaymentEvent.DepositWithdrawn -> if (!usedFunds && result.account.loan == loan) {
                    launch { sideEffect.emit(RatRace2CardSideEffect.DepositWithdraw(paymentEvent.amount)) }
                }

                is PaymentEvent.LoanAdded -> if (!usedFunds) {
                    launch { sideEffect.emit(RatRace2CardSideEffect.LoanAdded(paymentEvent.amount)) }
                }

                is PaymentEvent.FundsWithdrawn,
                PaymentEvent.LoanLimitExceeded -> Unit
            }
        }
        return withFinancialAccount(result.account)
    }
}

fun RatRace2CardState.total(): Long {
    return financialSnapshot().total()
}
