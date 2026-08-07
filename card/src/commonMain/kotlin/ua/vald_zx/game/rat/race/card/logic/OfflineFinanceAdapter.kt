package ua.vald_zx.game.rat.race.card.logic

import ua.vald_zx.game.rat.race.card.beans.Fund
import ua.vald_zx.game.rat.race.card.shared.BusinessType
import ua.vald_zx.game.rat.race.card.shared.Debt
import ua.vald_zx.game.rat.race.card.shared.resolved
import ua.vald_zx.game.rat.race.card.shared.totalPrincipal
import ua.vald_zx.game.rat.race.card.beans.Land as OfflineLand
import ua.vald_zx.game.rat.race.card.beans.Shares as OfflineShares
import ua.vald_zx.game.rat.race.card.shared.FinancialAccount
import ua.vald_zx.game.rat.race.card.shared.FinancialFund
import ua.vald_zx.game.rat.race.card.shared.FinancialSnapshot
import ua.vald_zx.game.rat.race.card.shared.Land
import ua.vald_zx.game.rat.race.card.shared.Shares

internal fun RatRace2CardState.resolvedDebts(): List<Debt> = debts.resolved(loan, config.loadRate)

internal fun RatRace2CardState.withDebts(updated: List<Debt>): RatRace2CardState = copy(
    loan = updated.totalPrincipal(),
    debts = updated,
)

internal fun RatRace2CardState.financialAccount(): FinancialAccount = FinancialAccount(
    cash = cash,
    deposit = deposit,
    debts = resolvedDebts(),
    funds = funds.map { FinancialFund(it.rate, it.amount) },
)

internal fun RatRace2CardState.withFinancialAccount(account: FinancialAccount): RatRace2CardState = copy(
    cash = account.cash,
    deposit = account.deposit,
    loan = account.debts.totalPrincipal(),
    debts = account.debts,
    funds = account.funds.map { Fund(it.rate, it.amount) },
)

internal fun RatRace2CardState.financialSnapshot(): FinancialSnapshot = FinancialSnapshot(
    account = financialAccount(),
    salaryIncome = business.filter { it.type == BusinessType.WORK }
        .map { it.profit + it.extentions.sum() },
    activeIncome = business.filterNot { it.type == BusinessType.WORK }
        .map { it.profit + it.extentions.sum() },
    assetValues = sharesList.map { it.toSharedShares().price } +
            lands.map { it.toSharedLand().price } +
            business.map { it.price },
    baseExpenses = listOf(
        playerCard.food,
        playerCard.rent,
        playerCard.cloth,
        playerCard.phone,
        playerCard.transport,
    ),
    recurringExpenses = listOf(
        babies * config.babyCost,
        cars * config.carCost,
        apartment * config.apartmentCost,
        cottage * config.cottageCost,
        yacht * config.yachtCost,
        flight * config.flightCost,
    ),
    depositRate = config.depositRate,
    loanRate = config.loadRate,
)

internal fun OfflineLand.toSharedLand(): Land = Land(
    name = name,
    area = area,
    price = price,
)

internal fun OfflineShares.toSharedShares(): Shares = Shares(
    type = type.name,
    count = count,
    buyPrice = buyPrice,
)
