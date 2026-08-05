package ua.vald_zx.game.rat.race.card.logic

import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.screen.board.cards.eventStoreCards
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCard
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeputyEventsTest {

    private fun state(
        canTake: List<BoardCardType> = listOf(BoardCardType.Deputy),
        taken: CardLink? = null,
        active: Boolean = true,
        isProgress: Boolean = false,
    ): BoardState {
        val player = Player(id = "me", boardId = "b", attrs = PlayerAttributes(0, 0))
        val board = Board(
            id = "b",
            name = "b",
            loanLimit = 0,
            businessLimit = 0,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            canTakeCard = canTake,
            takenCard = taken,
            playerIds = setOf("me"),
            activePlayerId = if (active) "me" else "rival",
        )
        return BoardState(isProgress, board, player)
    }

    @Test
    fun standingOnTheDeputyCellOffersAWayOut() {
        assertTrue(state().canSkipDeputies, "гравця замкнули на клітинці депутатів")
    }

    @Test
    fun theWayOutDisappearsOnceACardIsOpen() {
        assertFalse(state(taken = CardLink(BoardCardType.Deputy, 1)).canSkipDeputies)
    }

    @Test
    fun otherDecksKeepTheirOwnFlow() {
        assertFalse(
            state(canTake = listOf(BoardCardType.Chance)).canSkipDeputies,
            "кнопка пропуску вилізла на чужій колоді",
        )
    }

    @Test
    fun onlyTheActivePlayerCanWalkPast() {
        assertFalse(state(active = false).canSkipDeputies)
        assertFalse(state(isProgress = true).canSkipDeputies)
    }

    @Test
    fun eventDeckCarriesTheLandOffersAndTheReelection() {
        assertEquals((1..eventStoreCards.size).toSet(), eventStoreCards.keys)
        val land = eventStoreCards.values.filterIsInstance<BoardCard.EventStore.Land>()
        val landPrices = listOf(20_000L, 30_000L, 40_000L, 50_000L, 60_000L, 75_000L)
        landPrices.forEach { price ->
            assertTrue(
                land.any { it.price == price },
                "у колоді немає скупки землі по $price",
            )
        }
        assertEquals(
            1,
            eventStoreCards.values.count { it is BoardCard.EventStore.Reelection },
            "перевибори мають бути рівно одні",
        )
        assertEquals(
            3,
            eventStoreCards.values.count { it is BoardCard.EventStore.Announcement },
            "новинних карток має бути три",
        )
        val corruptBusinessSales = eventStoreCards.values.filterIsInstance<BoardCard.EventStore.CorruptBusiness>()
        val corruptLandSales = eventStoreCards.values.filterIsInstance<BoardCard.EventStore.CorruptLand>()
        assertEquals(9, corruptBusinessSales.size)
        assertEquals(5, corruptLandSales.size)
        assertTrue(corruptBusinessSales.all { it.salePercentage > 100 })
        assertTrue(corruptLandSales.all { it.price >= 12_000 })
    }
}
