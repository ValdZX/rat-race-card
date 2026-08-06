package ua.vald_zx.game.rat.race.server

import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.PayerType
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.financialAccount
import ua.vald_zx.game.rat.race.card.shared.isActivePlayer

internal fun Board.canMakeVoluntaryPurchase(player: Player, price: Long): Boolean {
    return isActivePlayer(player) && hasAvailableCredit(player, price)
}

internal fun Board.hasAvailableCredit(player: Player, price: Long): Boolean {
    return player.financialAccount().canAffordVoluntaryPurchase(price, loanLimit)
}

internal fun Board.canBuyBusiness(player: Player, price: Long): Boolean {
    return canMakeVoluntaryPurchase(player, price) && player.businesses.size.toLong() <= businessLimit
}

internal fun Board.canBuyWithCashAndDeposit(player: Player, price: Long): Boolean {
    return isActivePlayer(player) && player.financialAccount().canAffordVoluntaryPurchase(
        price = price,
        loanLimit = loanLimit,
        useFunds = false,
    )
}

internal fun Player.mustPay(expenses: BoardCard.Expenses): Boolean = when (expenses.payer) {
    PayerType.ALL -> true
    PayerType.FREE_W_OR_MARRIED_M ->
        card.gender == Gender.FEMALE && !isMarried || card.gender == Gender.MALE && isMarried

    PayerType.AUTO_OWNER -> cars > 0
    PayerType.MEN -> card.gender == Gender.MALE
    PayerType.PARENT -> babies > 0
    PayerType.MARRIED_M -> isMarried && card.gender == Gender.MALE
    PayerType.APARTMENT_OWNER -> apartment > 0
    PayerType.APARTMENT_OR_HOUSE_OWNER -> apartment > 0 || cottage > 0
    PayerType.ANIMAL_OWNER -> animal > 0
}

internal fun Board.isResolvingCard(vararg types: BoardCardType): Boolean {
    return takenCard?.type?.let { it in types } == true
}
