package ua.vald_zx.game.rat.race.card.screen.board.cards

import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.PayerType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.shared.PlayerCard
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardLogicTest {

    private val expense = BoardCard.Expenses(
        description = "",
        priceTitle = "",
        price = 100,
        payer = PayerType.FREE_W_OR_MARRIED_M,
    )

    @Test
    fun onlyUnmarriedWomenAndMarriedMenPayTheConditionalExpense() {
        assertTrue(player(Gender.FEMALE, married = false).needPayExpenses(expense))
        assertFalse(player(Gender.FEMALE, married = true).needPayExpenses(expense))
        assertFalse(player(Gender.MALE, married = false).needPayExpenses(expense))
        assertTrue(player(Gender.MALE, married = true).needPayExpenses(expense))
    }

    private fun player(gender: Gender, married: Boolean) = Player(
        id = "player",
        boardId = "board",
        attrs = PlayerAttributes(color = 0),
        card = PlayerCard(gender = gender),
        isMarried = married,
    )
}
