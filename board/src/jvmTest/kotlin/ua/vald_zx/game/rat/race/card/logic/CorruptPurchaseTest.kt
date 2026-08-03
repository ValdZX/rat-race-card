package ua.vald_zx.game.rat.race.card.logic

import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.screen.board.cards.chanceCards
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CorruptPurchaseTest {

    private fun state(cash: Long, deputies: Int): BoardState {
        val player = Player(
            id = "me",
            boardId = "b",
            attrs = PlayerAttributes(0, 0),
            cash = cash,
            deputies = deputies,
        )
        val board = Board(
            id = "b",
            name = "b",
            loanLimit = 0,
            businessLimit = 10,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            playerIds = setOf("me"),
            activePlayerId = "me",
        )
        return BoardState(false, board, player)
    }

    @Test
    fun moneyAloneDoesNotOpenACorruptDeal() {
        assertFalse(
            state(cash = 5_000_000, deputies = 1).canBuyCorrupt(price = 1_000_000, deputies = 3),
            "оборудку відкрили без потрібних депутатів",
        )
    }

    @Test
    fun deputiesAloneDoNotOpenACorruptDeal() {
        assertFalse(
            state(cash = 100, deputies = 9).canBuyCorrupt(price = 1_000_000, deputies = 3),
            "оборудку відкрили без грошей",
        )
    }

    @Test
    fun enoughOfBothOpensTheDeal() {
        assertTrue(state(cash = 5_000_000, deputies = 3).canBuyCorrupt(1_000_000, 3))
        assertTrue(state(cash = 5_000_000, deputies = 4).canBuyCorrupt(1_000_000, 3))
    }

    @Test
    fun everyCorruptCardAsksForAtLeastOneDeputy() {
        val corrupt = chanceCards.values.mapNotNull { card ->
            when (card) {
                is BoardCard.Chance.CorruptBusiness -> card.deputies
                is BoardCard.Chance.CorruptLand -> card.deputies
                else -> null
            }
        }
        assertTrue(corrupt.size == 18, "у колоді ${corrupt.size} корупційних карток замість 18")
        assertTrue(corrupt.all { it >= 1 }, "є корупційна картка, яку беруть без депутатів")
    }

    @Test
    fun aCorruptDealPaysEitherMonthlyOrOnce() {
        chanceCards.values.filterIsInstance<BoardCard.Chance.CorruptBusiness>().forEach { card ->
            val monthly = card.profit > 0
            val once = card.oneTimeProfit > 0
            assertTrue(monthly != once, "картка з ціною ${card.price} має обидва або жодного виду прибутку")
        }
    }
}
