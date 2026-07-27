package ua.vald_zx.game.rat.race.card.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayerProgressionTest {

    @Test
    fun defaultOuterCircleConditionsRequireEveryConfiguredMilestone() {
        val eligiblePlayer = player(
            cash = 100_000,
            deposit = 100_000,
            cars = 1,
            apartment = 1,
            businesses = listOf(
                Business(
                    type = BusinessType.MEDIUM,
                    name = "Business",
                    price = 0,
                    profit = 50_000,
                )
            ),
        )

        assertTrue(eligiblePlayer.canEnterOuterCircle(OuterCircleConditions()))
        assertFalse(eligiblePlayer.copy(cars = 0).canEnterOuterCircle(OuterCircleConditions()))
        assertFalse(eligiblePlayer.copy(apartment = 0).canEnterOuterCircle(OuterCircleConditions()))
        assertFalse(eligiblePlayer.copy(deposit = 99_999).canEnterOuterCircle(OuterCircleConditions()))
        assertFalse(
            eligiblePlayer.copy(businesses = emptyList())
                .canEnterOuterCircle(OuterCircleConditions())
        )
    }

    @Test
    fun optionalAssetsCanBeDisabledForOuterCircle() {
        val player = player(
            cash = 200_000,
            businesses = listOf(
                Business(
                    type = BusinessType.MEDIUM,
                    name = "Business",
                    price = 0,
                    profit = 50_000,
                )
            ),
        )

        assertTrue(
            player.canEnterOuterCircle(
                OuterCircleConditions(
                    apartmentRequired = false,
                    carRequired = false,
                )
            )
        )
    }

    @Test
    fun playerAlreadyOnOuterCircleCannotEnterAgain() {
        val player = player(
            cash = 200_000,
            cars = 1,
            apartment = 1,
            location = PlayerLocation(level = BoardLayer.OUTER.level),
            businesses = listOf(
                Business(
                    type = BusinessType.MEDIUM,
                    name = "Business",
                    price = 0,
                    profit = 50_000,
                )
            ),
        )

        assertFalse(player.canEnterOuterCircle(OuterCircleConditions()))
    }

    @Test
    fun victoryRequiresDreamPlaneEstateAndTenMillionOnAccounts() {
        val winner = player(
            cash = 10_000_000,
            flight = 1,
            cottage = 1,
            selectedDreamId = "world_trip",
            purchasedDreamIds = setOf("world_trip"),
        )

        val conditions = VictoryConditions()

        assertTrue(winner.hasMetVictoryConditions(conditions))
        assertFalse(winner.copy(purchasedDreamIds = emptySet()).hasMetVictoryConditions(conditions))
        assertFalse(winner.copy(selectedDreamId = "another_dream").hasMetVictoryConditions(conditions))
        assertFalse(winner.copy(flight = 0).hasMetVictoryConditions(conditions))
        assertFalse(winner.copy(cottage = 0).hasMetVictoryConditions(conditions))
        assertFalse(winner.copy(cash = 9_999_999).hasMetVictoryConditions(conditions))
    }

    @Test
    fun optionalVictoryAssetsCanBeDisabledAndBalanceCanBeConfigured() {
        val player = player(cash = 500_000)

        assertTrue(
            player.hasMetVictoryConditions(
                VictoryConditions(
                    dreamRequired = false,
                    planeRequired = false,
                    estateRequired = false,
                    minimumAccountBalance = 500_000,
                )
            )
        )
        assertFalse(
            player.hasMetVictoryConditions(
                VictoryConditions(
                    dreamRequired = false,
                    planeRequired = false,
                    estateRequired = false,
                    minimumAccountBalance = 500_001,
                )
            )
        )
    }

    @Test
    fun transportChangesMovementStepsWhenBoardRuleIsEnabled() {
        assertEquals(
            4,
            player().movementSteps(dice = 4, transportMovementBonusEnabled = true),
        )
        assertEquals(
            5,
            player(cars = 1).movementSteps(dice = 4, transportMovementBonusEnabled = true),
        )
        assertEquals(
            6,
            player(cars = 1, flight = 1)
                .movementSteps(dice = 4, transportMovementBonusEnabled = true),
        )
        assertEquals(
            4,
            player(cars = 1, flight = 1)
                .movementSteps(dice = 4, transportMovementBonusEnabled = false),
        )
    }

    @Test
    fun debugValuesUpdatePlayerAndClampNegativeNumbers() {
        val updated = player().withDebugValues(
            DebugPlayerValues(
                cash = 1_000_000,
                deposit = 200_000,
                loan = -1,
                babies = 2,
                cars = 1,
                apartment = 3,
                cottage = 1,
                yacht = 2,
                flight = 4,
                animal = 5,
            )
        )

        assertEquals(1_000_000, updated.cash)
        assertEquals(200_000, updated.deposit)
        assertEquals(0, updated.loan)
        assertEquals(2, updated.babies)
        assertEquals(1, updated.cars)
        assertEquals(3, updated.apartment)
        assertEquals(1, updated.cottage)
        assertEquals(2, updated.yacht)
        assertEquals(4, updated.flight)
        assertEquals(5, updated.animal)
    }

    @Test
    fun nearestCardPlaceIsSelectedInForwardMovementOrder() {
        assertEquals(
            3,
            BoardLayer.INNER.nearestPlacePosition(
                currentPosition = 2,
                cardType = BoardCardType.Shopping,
            )
        )
        assertEquals(
            7,
            BoardLayer.INNER.nearestPlacePosition(
                currentPosition = 4,
                cardType = BoardCardType.Chance,
            )
        )
        assertEquals(
            4,
            BoardLayer.INNER.nearestPlacePosition(
                currentPosition = BoardLayer.INNER.places.lastIndex,
                cardType = BoardCardType.Chance,
            )
        )
        assertNull(
            BoardLayer.INNER.nearestPlacePosition(
                currentPosition = 0,
                cardType = BoardCardType.BigBusiness,
            )
        )
    }

    @Test
    fun everyDesireSpaceHasOneUniqueDream() {
        val desireIds = outPlaces.filterIsInstance<PlaceType.Desire>()
            .map { it.dreamId }
            .toSet()

        assertTrue(ratRaceDreams.map { it.id }.toSet() == desireIds)
        assertTrue(ratRaceDreams.map { it.id }.distinct().size == ratRaceDreams.size)
    }

    private fun player(
        cash: Long = 0,
        deposit: Long = 0,
        cars: Long = 0,
        apartment: Long = 0,
        cottage: Long = 0,
        flight: Long = 0,
        businesses: List<Business> = emptyList(),
        location: PlayerLocation = PlayerLocation(),
        selectedDreamId: String? = null,
        purchasedDreamIds: Set<String> = emptySet(),
    ): Player {
        return Player(
            id = "player",
            boardId = "board",
            attrs = PlayerAttributes(color = 0),
            cash = cash,
            deposit = deposit,
            cars = cars,
            apartment = apartment,
            cottage = cottage,
            flight = flight,
            businesses = businesses,
            location = location,
            selectedDreamId = selectedDreamId,
            purchasedDreamIds = purchasedDreamIds,
        )
    }
}
