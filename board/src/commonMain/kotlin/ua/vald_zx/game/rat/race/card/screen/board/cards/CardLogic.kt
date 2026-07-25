package ua.vald_zx.game.rat.race.card.screen.board.cards

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.PayerType
import ua.vald_zx.game.rat.race.card.shared.Player


fun Player.needPayExpenses(
    expenses: BoardCard.Expenses,
): Boolean {
    return when (expenses.payer) {
        PayerType.ALL -> true
        PayerType.FREE_W_OR_MARRIED_M -> card.gender == Gender.FEMALE || isMarried
        PayerType.AUTO_OWNER -> cars > 0
        PayerType.MEN -> card.gender == Gender.MALE
        PayerType.PARENT -> babies > 0
        PayerType.MARRIED_M -> isMarried && card.gender == Gender.MALE
        PayerType.APARTMENT_OWNER -> apartment > 0
        PayerType.APARTMENT_OR_HOUSE_OWNER -> cottage > 0 || apartment > 0
        PayerType.ANIMAL_OWNER -> animal > 0
    }
}