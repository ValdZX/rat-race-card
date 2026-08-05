package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TaxInspectionTest {
    private val board = Board(
        id = "board",
        name = "Board",
        loanLimit = 0,
        businessLimit = 10,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
    )

    @Test
    fun cleanPlayerPaysNothing() {
        assertEquals(0, player(cash = 100_000).taxInspectionBribe(board))
    }

    @Test
    fun corruptBusinessTriggersTwentyPercentOfTotalWealth() {
        val corruptBusiness = Business(
            type = BusinessType.CORRUPTION,
            name = "Deal",
            price = 50_000,
            profit = 10_000,
        )
        val player = player(cash = 100_000).copy(businesses = listOf(corruptBusiness))

        assertEquals(150_000, player.total())
        assertEquals(30_000, player.taxInspectionBribe(board))
    }

    @Test
    fun corruptLandAlsoTriggersTheBribe() {
        val player = player(cash = 100_000).copy(
            landList = listOf(Land("Corrupt land", area = 200, price = 400_000, corrupt = true)),
        )

        assertEquals(20_000, player.taxInspectionBribe(board))
    }

    private fun player(cash: Long) = Player(
        id = "player",
        boardId = board.id,
        attrs = PlayerAttributes(color = 0),
        cash = cash,
    )
}
