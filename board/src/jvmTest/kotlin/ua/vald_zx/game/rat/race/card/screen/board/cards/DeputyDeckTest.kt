package ua.vald_zx.game.rat.race.card.screen.board.cards

import ua.vald_zx.game.rat.race.card.shared.corruptDeputyIds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeputyDeckTest {

    @Test
    fun deckMatchesTheSpreadsheet() {
        assertEquals(75, deputyCards.size)
        assertEquals(37, deputyCards.values.count { it.corrupt })
        assertEquals(38, deputyCards.values.count { !it.corrupt })
    }

    @Test
    fun serverAndClientAgreeOnWhichDeputiesAreForSale() {
        val fromDeck = deputyCards.filterValues { it.corrupt }.keys
        assertEquals(
            corruptDeputyIds,
            fromDeck,
            "сервер нарахує депутатів не за тими картками, які показує клієнт",
        )
    }

    @Test
    fun refusalsCarryTheirTextAndPurchasesDoNot() {
        deputyCards.forEach { (id, card) ->
            if (card.corrupt) {
                assertTrue(card.description.isEmpty(), "картка $id не потребує тексту відмови")
            } else {
                assertTrue(card.description.isNotBlank(), "картка $id мовчить про причину відмови")
            }
        }
    }
}
