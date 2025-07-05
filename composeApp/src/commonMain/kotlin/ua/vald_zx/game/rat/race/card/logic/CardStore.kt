@file:Suppress("DEPRECATION")

package ua.vald_zx.game.rat.race.card.logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.beans.Config
import ua.vald_zx.game.rat.race.card.beans.Fund
import ua.vald_zx.game.rat.race.card.beans.Shares
import ua.vald_zx.game.rat.race.card.beans.SharesType
import ua.vald_zx.game.rat.race.card.logic.CardAction.AddBaby
import ua.vald_zx.game.rat.race.card.logic.CardAction.AddFund
import ua.vald_zx.game.rat.race.card.logic.CardAction.BackToState
import ua.vald_zx.game.rat.race.card.logic.CardAction.BuyApartment
import ua.vald_zx.game.rat.race.card.logic.CardAction.BuyBusiness
import ua.vald_zx.game.rat.race.card.logic.CardAction.BuyCar
import ua.vald_zx.game.rat.race.card.logic.CardAction.BuyCottage
import ua.vald_zx.game.rat.race.card.logic.CardAction.BuyFlight
import ua.vald_zx.game.rat.race.card.logic.CardAction.BuyShares
import ua.vald_zx.game.rat.race.card.logic.CardAction.BuyYacht
import ua.vald_zx.game.rat.race.card.logic.CardAction.CapitalizeFunds
import ua.vald_zx.game.rat.race.card.logic.CardAction.CapitalizeStarsFunds
import ua.vald_zx.game.rat.race.card.logic.CardAction.DismissalConfirmed
import ua.vald_zx.game.rat.race.card.logic.CardAction.ExtendBusiness
import ua.vald_zx.game.rat.race.card.logic.CardAction.Fired
import ua.vald_zx.game.rat.race.card.logic.CardAction.FiredConfirmed
import ua.vald_zx.game.rat.race.card.logic.CardAction.FromDeposit
import ua.vald_zx.game.rat.race.card.logic.CardAction.FromFund
import ua.vald_zx.game.rat.race.card.logic.CardAction.GetLoan
import ua.vald_zx.game.rat.race.card.logic.CardAction.GetSalary
import ua.vald_zx.game.rat.race.card.logic.CardAction.GetSalaryApproved
import ua.vald_zx.game.rat.race.card.logic.CardAction.HideAlarm
import ua.vald_zx.game.rat.race.card.logic.CardAction.LoadState
import ua.vald_zx.game.rat.race.card.logic.CardAction.RandomBusiness
import ua.vald_zx.game.rat.race.card.logic.CardAction.RepayLoan
import ua.vald_zx.game.rat.race.card.logic.CardAction.SellBusiness
import ua.vald_zx.game.rat.race.card.logic.CardAction.SellShares
import ua.vald_zx.game.rat.race.card.logic.CardAction.SellingAllBusinessConfirmed
import ua.vald_zx.game.rat.race.card.logic.CardAction.SendCash
import ua.vald_zx.game.rat.race.card.logic.CardAction.SideExpenses
import ua.vald_zx.game.rat.race.card.logic.CardAction.SideProfit
import ua.vald_zx.game.rat.race.card.logic.CardAction.ToDeposit
import ua.vald_zx.game.rat.race.card.logic.CardAction.UpdateConfig
import ua.vald_zx.game.rat.race.card.logic.CardAction.UpdateFamily
import ua.vald_zx.game.rat.race.card.logic.CardSideEffect.AddCash
import ua.vald_zx.game.rat.race.card.logic.CardSideEffect.ConfirmDismissal
import ua.vald_zx.game.rat.race.card.logic.CardSideEffect.ConfirmFired
import ua.vald_zx.game.rat.race.card.logic.CardSideEffect.ConfirmSellingAllBusiness
import ua.vald_zx.game.rat.race.card.logic.CardSideEffect.DepositWithdraw
import ua.vald_zx.game.rat.race.card.logic.CardSideEffect.LoanAdded
import ua.vald_zx.game.rat.race.card.logic.CardSideEffect.ShowSalaryApprove
import ua.vald_zx.game.rat.race.card.logic.CardSideEffect.SubCash
import ua.vald_zx.game.rat.race.card.remove
import ua.vald_zx.game.rat.race.card.replace
import ua.vald_zx.game.rat.race.card.service
import ua.vald_zx.game.rat.race.card.shared.Player
import kotlin.math.absoluteValue

@Serializable
data class CardState(
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

    fun totalExpenses(player: Player): Long {
        var totalExpenses = 0L
        totalExpenses += player.playerCard.food
        totalExpenses += player.playerCard.rent
        totalExpenses += player.playerCard.cloth
        totalExpenses += player.playerCard.phone
        totalExpenses += player.playerCard.transport
        totalExpenses += babies * config.babyCost
        totalExpenses += cars * config.carCost
        totalExpenses += apartment * config.apartmentCost
        totalExpenses += cottage * config.cottageCost
        totalExpenses += yacht * config.yachtCost
        totalExpenses += flight * config.flightCost
        totalExpenses += creditExpenses()
        return totalExpenses
    }

    fun cashFlow(player: Player): Long {
        return totalProfit() - totalExpenses(player)
    }

    fun status(player: Player): String {
        return when {
            business.any { it.type == BusinessType.SMALL } -> "${player.playerCard.name} - Підприємець"
            business.any { it.type == BusinessType.MEDIUM } -> "${player.playerCard.name} - Бізнесмен"
            totalExpenses(player) > 1_000_000 -> "${player.playerCard.name} - Мільйонер"
            else -> player.playerCard.name
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

sealed class CardAction : Action {
    data class LoadState(val state: CardState) : CardAction()
    data object Fired : CardAction()
    data object FiredConfirmed : CardAction()
    data object GetSalary : CardAction()
    data object GetSalaryApproved : CardAction()
    data class AddFund(val fund: Fund) : CardAction()
    data class FromFund(val fund: Fund, val amount: Long) : CardAction()
    data object CapitalizeFunds : CardAction()
    data object CapitalizeStarsFunds : CardAction()
    data object RandomBusiness : CardAction()
    data object HideAlarm : CardAction()
    data class BuyBusiness(val business: Business) : CardAction()
    data class SellBusiness(val business: Business, val amount: Long) : CardAction()
    data class DismissalConfirmed(val business: Business) : CardAction()
    data class SellingAllBusinessConfirmed(val business: Business) : CardAction()
    data class ExtendBusiness(val amount: Long, val business: Business) : CardAction()
    data class SideProfit(val amount: Long) : CardAction()
    data class ReceivedCash(val payerId: String, val amount: Long) : CardAction()
    data class SideExpenses(val amount: Long) : CardAction()
    data class GetLoan(val amount: Long) : CardAction()
    data class RepayLoan(val amount: Long) : CardAction()
    data class ToDeposit(val amount: Long) : CardAction()
    data class FromDeposit(val amount: Long) : CardAction()
    data class BuyCar(val price: Long) : CardAction()
    data class BuyApartment(val price: Long) : CardAction()
    data class BuyCottage(val price: Long) : CardAction()
    data class BuyYacht(val price: Long) : CardAction()
    data class BuyFlight(val price: Long) : CardAction()
    data class BuyShares(val shares: Shares) : CardAction()
    data class UpdateConfig(val config: Config) : CardAction()
    data class BackToState(val state: CardState, val backCount: Int) : CardAction()
    data class SendCash(val id: String, val cash: Long) : CardAction()
    data class SellShares(val type: SharesType, val count: Long, val sellPrice: Long) :
        CardAction()

    data class UpdateFamily(
        val isMarried: Boolean,
        val halfCash: Boolean = false,
        val marriageCost: Long = 0,
        val babies: Long? = null
    ) : CardAction()

    data object AddBaby : CardAction()
}

sealed class CardSideEffect : Effect {
    data class ConfirmDismissal(val business: Business) : CardSideEffect()
    data class ConfirmSellingAllBusiness(val business: Business) : CardSideEffect()
    data class DepositWithdraw(val balance: Long) : CardSideEffect()
    data class LoanAdded(val balance: Long) : CardSideEffect()
    data object ConfirmFired : CardSideEffect()
    data class AddCash(val amount: Long) : CardSideEffect()
    data class SubCash(val amount: Long) : CardSideEffect()
    data class ReceivedCash(val payerId: String, val amount: Long) : CardSideEffect()
    data object ShowSalaryApprove : CardSideEffect()
}

class CardStore(private val boardState: MutableStateFlow<BoardState>) : Store<CardState, CardAction, CardSideEffect>,
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val state = MutableStateFlow(CardState())
    private val sideEffect = MutableSharedFlow<CardSideEffect>()
    var statistics: Statistics? = null
        private set

    override fun observeState(): StateFlow<CardState> = state

    override fun observeSideEffect(): Flow<CardSideEffect> = sideEffect

    override fun dispatch(action: CardAction) {
        val oldState = state.value
        val newState = when (action) {
            is LoadState -> {
                action.state
            }

            is CardAction.ReceivedCash -> {
                launch {
                    sideEffect.emit(
                        CardSideEffect.ReceivedCash(
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
                statistics?.salaryCount = statistics?.salaryCount?.plus(1) ?: 0
                boardState.value.currentPlayer?.let {currentPlayer->
                    val cashFlow = oldState.cashFlow(currentPlayer)
                    if (cashFlow >= 0) {
                        oldState.plusCash(cashFlow)
                    } else {
                        oldState.minusCash(cashFlow.absoluteValue)
                    }
                }?:oldState
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
                oldState.copy(babies = oldState.babies + 1)
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
        }
        if (newState != oldState) {
            state.value = newState
        }
    }

    private fun CardState.plusCash(value: Long): CardState {
        launch { sideEffect.emit(AddCash(value)) }
        return copy(cash = cash + value)
    }

    private fun CardState.minusCash(
        value: Long,
        isFundBuy: Boolean = false
    ): CardState {
        if (value == 0L) return this
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

fun CardState.total(): Long {
    return cash +
            deposit +
            funds.sumOf { it.amount } +
            sharesList.sumOf { it.price } +
            business.sumOf { it.price } -
            loan
}