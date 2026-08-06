package ua.vald_zx.game.rat.race.server

import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.shared.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LegacyRulesCharacterizationTest {
    private val board = Board(
        id = "board",
        name = "Board",
        loanLimit = 10_000,
        businessLimit = 3,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        playerIds = linkedSetOf("first", "second", "inactive", "removed"),
        activePlayerId = "first",
    )

    @Test
    fun defaultTracksKeepEveryDocumentedCellCount() {
        val innerCounts = inPlaces.groupingBy { it.name }.eachCount()
        val outerCounts = outPlaces.groupingBy { it.name }.eachCount()

        assertEquals(78, inPlaces.size)
        assertEquals(74, outPlaces.size)
        assertEquals(
            mapOf(
                "Salary" to 4, "Start" to 1, "Business" to 11, "Shopping" to 8,
                "Chance" to 15, "Expenses" to 18, "Store" to 14, "Bankruptcy" to 1,
                "Child" to 1, "Love" to 2, "Rest" to 1, "Divorce" to 1, "Exaltation" to 1,
            ),
            innerCounts,
        )
        assertEquals(4, outerCounts.getValue("Salary"))
        assertEquals(1, outerCounts.getValue("Start"))
        assertEquals(9, outerCounts.getValue("BigBusiness"))
        assertEquals(10, outerCounts.getValue("Shopping"))
        assertEquals(12, outerCounts.getValue("Chance"))
        assertEquals(10, outerCounts.getValue("Store"))
        assertEquals(1, outerCounts.getValue("Bankruptcy"))
        assertEquals(22, outerCounts.getValue("Desire"))
        assertEquals(4, outerCounts.getValue("Deputy"))
        assertEquals(1, outerCounts.getValue("TaxInspection"))
    }

    @Test
    fun everyCardCellOpensItsDocumentedDeck() {
        val player = player("first")

        assertEquals(listOf(BoardCardType.SmallBusiness), legacyCardOptions(PlaceType.Business, player))
        assertEquals(listOf(BoardCardType.BigBusiness), legacyCardOptions(PlaceType.BigBusiness, player))
        assertEquals(listOf(BoardCardType.Chance), legacyCardOptions(PlaceType.Chance, player))
        assertEquals(listOf(BoardCardType.Deputy), legacyCardOptions(PlaceType.Deputy, player))
        assertEquals(listOf(BoardCardType.Expenses), legacyCardOptions(PlaceType.Expenses, player))
        assertEquals(listOf(BoardCardType.Shopping), legacyCardOptions(PlaceType.Shopping, player))
        assertEquals(listOf(BoardCardType.EventStore), legacyCardOptions(PlaceType.Store, player))
        assertTrue(legacyCardOptions(PlaceType.Start, player).isEmpty())
    }

    @Test
    fun businessDeckProgressionFollowsTheHighestOwnedClass() {
        val player = player("first")
        val small = business(BusinessType.SMALL)
        val medium = business(BusinessType.MEDIUM)
        val large = business(BusinessType.LARGE)

        assertEquals(
            listOf(BoardCardType.SmallBusiness, BoardCardType.MediumBusiness),
            legacyCardOptions(PlaceType.Business, player.copy(businesses = listOf(small))),
        )
        assertEquals(
            listOf(BoardCardType.BigBusiness, BoardCardType.MediumBusiness),
            legacyCardOptions(PlaceType.Business, player.copy(businesses = listOf(small, medium))),
        )
        assertEquals(
            listOf(BoardCardType.BigBusiness),
            legacyCardOptions(PlaceType.Business, player.copy(businesses = listOf(small, medium, large))),
        )
    }

    @Test
    fun familyRestResignationAndBankruptcyCellsKeepTheirCurrentEffects() {
        val work = business(BusinessType.WORK)
        val asset = business(BusinessType.SMALL)
        val man = player("first").copy(
            card = PlayerCard(gender = Gender.MALE),
            isMarried = true,
            babies = 2,
            cash = 10_000,
            deposit = 4_000,
            businesses = listOf(work, asset),
        )

        val child = man.afterChildCell()
        assertEquals(3, child.babies)
        assertEquals(11_000, child.cash)
        assertEquals(2, man.afterRestCell().inRest)
        assertEquals(listOf(asset), man.afterResignationCell().businesses)

        val divorced = man.afterDivorceCell()
        assertFalse(divorced.isMarried)
        assertEquals(0, divorced.babies)
        assertEquals(5_000, divorced.cash)
        assertEquals(2_000, divorced.deposit)

        val (bankrupt, removed) = man.afterBankruptcyCell(FixedRandom(0))
        assertEquals(asset, removed)
        assertEquals(listOf(work), bankrupt.businesses)
    }

    @Test
    fun childLoveAndDivorceRespectGenderAndMarriage() {
        val singleMan = player("first").copy(card = PlayerCard(gender = Gender.MALE))
        val singleWoman = player("first").copy(card = PlayerCard(gender = Gender.FEMALE), cash = 100)

        assertEquals(singleMan, singleMan.afterChildCell())
        assertEquals(1, singleWoman.afterChildCell().babies)
        assertTrue(singleMan.afterLoveCell().isMarried)
        assertEquals(singleWoman, singleWoman.afterDivorceCell())

        val divorcedWoman = singleWoman.copy(isMarried = true, babies = 2).afterDivorceCell()
        assertFalse(divorcedWoman.isMarried)
        assertEquals(2, divorcedWoman.babies)
    }

    @Test
    fun queueSkipsInactiveAndRemovedPlayersAndWraps() {
        val players = listOf(
            player("first"),
            player("second"),
            player("inactive").copy(isInactive = true),
            player("not-on-board"),
        )

        assertEquals("second", board.nextActivePlayer(players)?.id)
        assertEquals("first", board.copy(activePlayerId = "second").nextActivePlayer(players)?.id)
        assertEquals("first", board.copy(activePlayerId = "missing").nextActivePlayer(players)?.id)
        assertNull(board.copy(playerIds = emptySet()).nextActivePlayer(players))
    }

    @Test
    fun multiplayerMarketWaitsForEveryActiveOwner() {
        val owners = setOf("first", "second")

        assertFalse(marketEventIsComplete(emptySet(), owners))
        assertFalse(marketEventIsComplete(setOf("first"), owners))
        assertTrue(marketEventIsComplete(owners, owners))
        assertTrue(marketEventIsComplete(emptySet(), emptySet()))
    }

    @Test
    fun outerCircleAndVictoryKeepEveryConfiguredRequirement() {
        val progressing = player("first").copy(
            cash = 200_000,
            cars = 1,
            apartment = 1,
            businesses = listOf(Business(BusinessType.MEDIUM, "Business", 1, 51_000)),
        )
        assertTrue(progressing.canEnterOuterCircle(canRoll = true, OuterCircleConditions()))
        assertFalse(progressing.copy(cars = 0).canEnterOuterCircle(canRoll = true, OuterCircleConditions()))

        val winner = player("first").copy(
            cash = 10_000_000,
            flight = 1,
            cottage = 1,
            selectedDreamId = "world_trip",
            purchasedDreamIds = setOf("world_trip"),
        )
        assertTrue(winner.hasMetVictoryConditions(VictoryConditions()))
        assertFalse(winner.copy(flight = 0).hasMetVictoryConditions(VictoryConditions()))
        assertFalse(winner.copy(purchasedDreamIds = emptySet()).hasMetVictoryConditions(VictoryConditions()))
    }

    @Test
    fun salaryTransportAndTaxRulesRemainStable() {
        val work = Business(BusinessType.WORK, "Work", 0, 5_000)
        val player = player("first").copy(
            card = PlayerCard(food = 500, rent = 1_000, cloth = 500, phone = 100, transport = 400),
            businesses = listOf(work),
            cars = 1,
        )

        assertEquals(1_900, player.cashFlow())
        assertEquals(5, player.movementSteps(dice = 4, transportMovementBonusEnabled = true))
        assertEquals(4, player.movementSteps(dice = 4, transportMovementBonusEnabled = false))
        assertEquals(0, player.taxInspectionBribe(board))

        val corrupt = player.copy(
            cash = 100_000,
            businesses = player.businesses + Business(BusinessType.CORRUPTION, "Deal", 50_000, 10_000),
        )
        assertEquals(corrupt.total() * 20 / 100, corrupt.taxInspectionBribe(board))
    }

    @Test
    fun everyExpensePayerVariantHasAServerPredicate() {
        val base = player("first")
        fun expense(payer: PayerType) = BoardCard.Expenses("", "", 1, payer)

        assertTrue(base.mustPay(expense(PayerType.ALL)))
        assertTrue(base.copy(card = PlayerCard(gender = Gender.MALE)).mustPay(expense(PayerType.MEN)))
        assertTrue(base.copy(cars = 1).mustPay(expense(PayerType.AUTO_OWNER)))
        assertTrue(base.copy(babies = 1).mustPay(expense(PayerType.PARENT)))
        assertTrue(base.copy(card = PlayerCard(gender = Gender.MALE), isMarried = true).mustPay(expense(PayerType.MARRIED_M)))
        assertTrue(base.copy(apartment = 1).mustPay(expense(PayerType.APARTMENT_OWNER)))
        assertTrue(base.copy(cottage = 1).mustPay(expense(PayerType.APARTMENT_OR_HOUSE_OWNER)))
        assertTrue(base.copy(animal = 1).mustPay(expense(PayerType.ANIMAL_OWNER)))
        assertTrue(base.copy(card = PlayerCard(gender = Gender.FEMALE)).mustPay(expense(PayerType.FREE_W_OR_MARRIED_M)))
        assertFalse(base.copy(card = PlayerCard(gender = Gender.FEMALE), isMarried = true).mustPay(expense(PayerType.FREE_W_OR_MARRIED_M)))
    }

    private fun player(id: String) = Player(
        id = id,
        boardId = board.id,
        attrs = PlayerAttributes(color = 0),
    )

    private fun business(type: BusinessType) = Business(type, type.name, 1, 1)

    private class FixedRandom(private val index: Int) : GameRandom {
        override fun nextInt(from: Int, until: Int): Int = from + index
    }
}
