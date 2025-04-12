@file:Suppress("DEPRECATION")

package ua.vald_zx.game.rat.race.card.logic

import androidx.compose.runtime.mutableStateOf
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.http.encodedPath
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.krpc.streamScoped
import kotlinx.rpc.withService
import kotlinx.serialization.Serializable
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.beans.Config
import ua.vald_zx.game.rat.race.card.beans.Fund
import ua.vald_zx.game.rat.race.card.beans.Shares
import ua.vald_zx.game.rat.race.card.beans.SharesType
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.AddFund
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.BackToState
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.BuyApartment
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.BuyBusiness
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.BuyCar
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.BuyCottage
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.BuyFlight
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.BuyShares
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.BuyYacht
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.CapitalizeFunds
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.CapitalizeStarsFunds
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.DismissalConfirmed
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.EditFillProfessionCardRat
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.ExtendBusiness
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.FillProfessionCardRat
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.Fired
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.FiredConfirmed
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.FromDeposit
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.FromFund
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.GetLoan
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.GetSalary
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.GetSalaryApproved
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.HideAlarm
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.LoadState
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.RandomBusiness
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.RepayLoan
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.SellBusiness
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.SellShares
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.SellingAllBusinessConfirmed
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.SendCash
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.SideExpenses
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.SideProfit
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.ToDeposit
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.UpdateConfig
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.UpdateFamily
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.UpdateServiceUuid
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardSideEffect.AddCash
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardSideEffect.ConfirmDismissal
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardSideEffect.ConfirmFired
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardSideEffect.ConfirmSellingAllBusiness
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardSideEffect.DepositWithdraw
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardSideEffect.LoanAdded
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardSideEffect.ReceivedCash
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardSideEffect.ShowSalaryApprove
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardSideEffect.SubCash
import ua.vald_zx.game.rat.race.card.raceRate2KStore
import ua.vald_zx.game.rat.race.card.remove
import ua.vald_zx.game.rat.race.card.replace
import ua.vald_zx.game.rat.race.card.shared.Card2State
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import ua.vald_zx.game.rat.race.card.statistics2KStore
import kotlin.math.absoluteValue

val players = mutableStateOf(listOf<Player>())
val client by lazy {
    HttpClient {
        installKrpc()
    }
}

@Serializable
data class Statistics(
    val log: MutableList<RatRace2CardState> = mutableListOf(),
    var salaryCount: Long = 0,
)

@Serializable
data class RatRace2CardState(
    val professionCard: ProfessionCard = ProfessionCard(),
    val cash: Long = 0,
    val deposit: Long = 0,
    val loan: Long = 0,
    val business: List<Business> = emptyList(),
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
    val uuid: String = ""
) : State {

    fun balance(): Long {
        return cash + deposit + funds.sumOf { it.amount }
    }

    fun activeProfit(): Long {
        return business.sumOf { it.profit + it.extentions.sum() }
    }

    fun passiveProfit(): Long {
        return ((deposit / 100.0) * config.depositRate).toLong()
    }

    fun totalProfit(): Long {
        return activeProfit() + passiveProfit()
    }

    fun creditExpenses(): Long {
        return ((loan / 100.0) * config.loadRate).toLong()
    }

    fun totalExpenses(): Long {
        var totalExpenses = 0L
        totalExpenses += professionCard.food
        totalExpenses += professionCard.rent
        totalExpenses += professionCard.cloth
        totalExpenses += professionCard.phone
        totalExpenses += professionCard.transport
        totalExpenses += babies * config.babyCost
        totalExpenses += cars * config.carCost
        totalExpenses += apartment * config.apartmentCost
        totalExpenses += cottage * config.cottageCost
        totalExpenses += yacht * config.yachtCost
        totalExpenses += flight * config.flightCost
        totalExpenses += creditExpenses()
        return totalExpenses
    }

    fun cashFlow(): Long {
        return totalProfit() - totalExpenses()
    }

    fun status(): String {
        return when {
            business.any { it.type == BusinessType.SMALL } -> "${professionCard.profession} - Підприємець"
            business.any { it.type == BusinessType.MEDIUM } -> "${professionCard.profession} - Бізнесмен"
            totalExpenses() > 1_000_000 -> "${professionCard.profession} - Мільйонер"
            else -> professionCard.profession
        }
    }

    fun existsShares(): Set<SharesType> {
        return sharesList.map { it.type }.toSet()
    }

    fun sharesCount(entry: SharesType): Long {
        return sharesList.filter { it.type == entry }.sumOf { it.count }
    }

    fun capitalization(): Long {
        return funds.sumOf { (it.rate / 100.0) * it.amount }.toLong()
    }

    fun capitalizationStart(): Long {
        return funds.sumOf { (config.fundStartRate / 100.0) * it.amount }.toLong()
    }

    fun fundAmount(): Long {
        return funds.sumOf { it.amount }
    }
}

sealed class RatRace2CardAction : Action {
    data class LoadState(val state: RatRace2CardState) : RatRace2CardAction()
    data class FillProfessionCardRat(val professionCard: ProfessionCard) : RatRace2CardAction()
    data class EditFillProfessionCardRat(val professionCard: ProfessionCard) : RatRace2CardAction()
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
    data object OnPause : RatRace2CardAction()
    data object OnResume : RatRace2CardAction()
    data class BuyBusiness(val business: Business) : RatRace2CardAction()
    data class SellBusiness(val business: Business, val amount: Long) : RatRace2CardAction()
    data class DismissalConfirmed(val business: Business) : RatRace2CardAction()
    data class SellingAllBusinessConfirmed(val business: Business) : RatRace2CardAction()
    data class ExtendBusiness(val amount: Long, val business: Business) : RatRace2CardAction()
    data class SideProfit(val amount: Long) : RatRace2CardAction()
    data class SideExpenses(val amount: Long) : RatRace2CardAction()
    data class GetLoan(val amount: Long) : RatRace2CardAction()
    data class RepayLoan(val amount: Long) : RatRace2CardAction()
    data class ToDeposit(val amount: Long) : RatRace2CardAction()
    data class FromDeposit(val amount: Long) : RatRace2CardAction()
    data class BuyCar(val price: Long) : RatRace2CardAction()
    data class BuyApartment(val price: Long) : RatRace2CardAction()
    data class BuyCottage(val price: Long) : RatRace2CardAction()
    data class BuyYacht(val price: Long) : RatRace2CardAction()
    data class BuyFlight(val price: Long) : RatRace2CardAction()
    data class BuyShares(val shares: Shares) : RatRace2CardAction()
    data class UpdateConfig(val config: Config) : RatRace2CardAction()
    data class UpdateServiceUuid(val uuid: String) : RatRace2CardAction()
    data class BackToState(val state: RatRace2CardState, val backCount: Int) : RatRace2CardAction()
    data class SendCash(val id: String, val cash: Long) : RatRace2CardAction()
    data class SellShares(val type: SharesType, val count: Long, val sellPrice: Long) :
        RatRace2CardAction()

    data class UpdateFamily(val isMarried: Boolean, val babies: Long) : RatRace2CardAction()
}

sealed class RatRace2CardSideEffect : Effect {
    data class ConfirmDismissal(val business: Business) : RatRace2CardSideEffect()
    data class ConfirmSellingAllBusiness(val business: Business) : RatRace2CardSideEffect()
    data class DepositWithdraw(val balance: Long) : RatRace2CardSideEffect()
    data class LoanAdded(val balance: Long) : RatRace2CardSideEffect()
    data object ConfirmFired : RatRace2CardSideEffect()
    data class AddCash(val amount: Long) : RatRace2CardSideEffect()
    data class SubCash(val amount: Long) : RatRace2CardSideEffect()
    data class ReceivedCash(val amount: Long) : RatRace2CardSideEffect()
    data object ShowSalaryApprove : RatRace2CardSideEffect()
}

class RatRace2CardStore : Store<RatRace2CardState, RatRace2CardAction, RatRace2CardSideEffect>,
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val state = MutableStateFlow(RatRace2CardState())
    private val sideEffect = MutableSharedFlow<RatRace2CardSideEffect>()
    var statistics: Statistics? = null
        private set

    private var service: RaceRatService? = null
    private var serviceJob: Job? = null

    override fun observeState(): StateFlow<RatRace2CardState> = state

    override fun observeSideEffect(): Flow<RatRace2CardSideEffect> = sideEffect

    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Napier.e("CoroutineExceptionHandler", exception)
    }

    private fun recallService() {
        serviceJob?.cancel()
        serviceJob = launch(exceptionHandler) {
            var delayToRecall = 0L
            while (true) {
                try {
                    supervisorScope {
                        delay(delayToRecall)
                        delayToRecall = 5000
                        subscribeOnListUpdate()
                    }
                    return@launch
                } catch (e: Exception) {
                    Napier.e("subscribeOnListUpdate", e)
                }
            }
        }
    }

    suspend fun subscribeOnListUpdate() {
        service = client.rpc {
            url {
                host = "race-rat-1033277102369.us-central1.run.app"
                port = 80
                encodedPath = "/api"
            }

            rpcConfig {
                serialization {
                    json()
                }
            }
        }.withService()
        val state = state.value
        val uuid = service?.init(
            Player(
                professionCard = state.professionCard,
                state = Card2State(
                    totalExpenses = state.totalProfit(),
                    cashFlow = state.cashFlow()
                )
            )
        ).orEmpty()
        if (uuid.isNotEmpty()) {
            dispatch(UpdateServiceUuid(uuid))
            Napier.d(uuid)
        }
        async {
            streamScoped {
                service?.playersObserve()?.collect { list ->
                    players.value = list
                }
            }
        }
        async {
            streamScoped {
                service?.inputCashObserve()?.collect { cash ->
                    sideEffect.emit(ReceivedCash(cash))
                    dispatch(SideProfit(cash))
                }
            }
        }
    }

    override fun dispatch(action: RatRace2CardAction) {
        val oldState = state.value
        val newState = when (action) {
            is LoadState -> {
                action.state
            }

            is FillProfessionCardRat -> {
                RatRace2CardState(
                    professionCard = action.professionCard,
                    business = listOf(
                        Business(
                            type = BusinessType.WORK,
                            name = action.professionCard.profession,
                            price = 0,
                            profit = action.professionCard.salary
                        )
                    )
                )
            }

            is EditFillProfessionCardRat -> {
                oldState.copy(professionCard = action.professionCard)
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
                val random = (0..<oldState.business.size).random()
                val business = oldState.business[random]
                val businessList = oldState.business.map { it.copy(alarmed = business == it) }
                oldState.copy(business = businessList)
            }

            is BuyBusiness -> {
                val currentBusiness = oldState.business
                if (action.business.type == BusinessType.SMALL
                    && currentBusiness.any { it.type == BusinessType.WORK }
                    && currentBusiness.count { it.type == BusinessType.SMALL } == 1
                ) {
                    launch { sideEffect.emit(ConfirmDismissal(action.business)) }
                    oldState
                } else if (currentBusiness.isNotEmpty()
                    && currentBusiness.first().type.klass != action.business.type.klass
                    && !currentBusiness.any { it.type == BusinessType.WORK }
                ) {
                    launch { sideEffect.emit(ConfirmSellingAllBusiness(action.business)) }
                    oldState
                } else oldState.copy(business = currentBusiness + action.business)
                    .minusCash(action.business.price)
            }

            is SellBusiness -> {
                val business = oldState.business.toMutableList()
                business.remove(action.business)
                oldState.copy(business = business).plusCash(action.amount)
            }

            is ExtendBusiness -> {
                val business = action.business
                val extended = business.copy(extentions = business.extentions + action.amount)
                oldState.copy(business = oldState.business.replace(business, extended))
            }

            is DismissalConfirmed -> {
                val business =
                    oldState.business.filter { it.type != BusinessType.WORK } + action.business
                oldState.copy(business = business).minusCash(action.business.price)
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
                launch { sideEffect.emit(ShowSalaryApprove) }
                oldState
            }

            GetSalaryApproved -> {
                statistics?.salaryCount += 1
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
                oldState.copy(
                    loan = oldState.loan - action.amount
                ).minusCash(action.amount)
            }

            is GetLoan -> {
                oldState.copy(loan = oldState.loan + action.amount).plusCash(action.amount)
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
                oldState.copy(isMarried = action.isMarried, babies = action.babies)
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
                val amount = oldState.funds.sumOf { it.amount } + oldState.capitalization()
                val funds = listOf(Fund(rate = oldState.config.fundBaseRate, amount))
                oldState.copy(funds = funds)
            }

            CapitalizeStarsFunds -> {
                val amount = oldState.funds.sumOf { it.amount } + oldState.capitalizationStart()
                val funds = listOf(Fund(rate = oldState.config.fundBaseRate, amount))
                oldState.copy(funds = funds)
            }

            Fired -> {
                launch { sideEffect.emit(ConfirmFired) }
                oldState
            }

            FiredConfirmed -> {
                oldState.copy(business = oldState.business.filter { it.type != BusinessType.WORK })
            }

            is BackToState -> {
                repeat(action.backCount) { statistics?.log?.removeLastOrNull() }
                action.state
            }

            is SendCash -> {
                launch {
                    service?.sendMoney(action.id, action.cash)
                    dispatch(SideExpenses(action.cash))
                }
                oldState
            }

            is UpdateServiceUuid -> {
                oldState.copy(uuid = action.uuid)
            }

            RatRace2CardAction.OnPause -> {
                client.close()
                client.engine.close()
                serviceJob?.cancel()
                oldState
            }

            RatRace2CardAction.OnResume -> {
                recallService()
                oldState
            }
        }
        if (newState != oldState) {
            state.value = newState
            launch {
                raceRate2KStore.set(newState)
                val storedStatistics = statistics ?: statistics2KStore.get() ?: Statistics()
                storedStatistics.log += newState
                statistics2KStore.set(storedStatistics)
                statistics = storedStatistics
                service?.update(
                    Card2State(
                        totalExpenses = newState.total(),
                        cashFlow = newState.cashFlow()
                    )
                )
            }
        }
    }

    private fun RatRace2CardState.plusCash(value: Long): RatRace2CardState {
        launch { sideEffect.emit(AddCash(value)) }
        return copy(cash = cash + value)
    }

    private fun RatRace2CardState.minusCash(
        value: Long,
        isFundBuy: Boolean = false
    ): RatRace2CardState {
        launch { sideEffect.emit(SubCash(value)) }
        return if (cash > value) {
            copy(cash = cash - value)
        } else if ((cash + deposit) > value) {
            launch { sideEffect.emit(DepositWithdraw(value - cash)) }
            copy(cash = 0, deposit = (deposit + cash) - value)
        } else if (!isFundBuy && config.hasFunds && funds.isNotEmpty()) {
            var stub = cash + deposit
            var newFunds = funds.toList()
            funds.sortedBy { it.rate }.firstOrNull { fund ->
                if (stub + fund.amount > value) {
                    newFunds = funds.replace(fund, fund.copy(amount = stub + fund.amount - value))
                    true
                } else {
                    stub += fund.amount
                    newFunds = newFunds.remove(fund)
                    false
                }
            }
            if (newFunds.isEmpty() && stub < value) {
                copy(cash = 0, deposit = 0, funds = emptyList(), loan = loan + (value - stub))
            } else {
                copy(cash = 0, deposit = 0, funds = newFunds)
            }
        } else {
            launch { sideEffect.emit(LoanAdded(value - (cash + deposit))) }
            copy(cash = 0, deposit = 0, loan = loan + (value - (deposit + cash)))
        }
    }
}

fun RatRace2CardState.total(): Long {
    return cash +
            deposit +
            funds.sumOf { it.amount } +
            sharesList.sumOf { it.price } +
            business.sumOf { it.price } -
            loan
}