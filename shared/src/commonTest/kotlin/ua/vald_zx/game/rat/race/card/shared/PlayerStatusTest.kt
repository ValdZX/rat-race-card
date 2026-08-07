package ua.vald_zx.game.rat.race.card.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class PlayerStatusTest {

    private fun player(
        businesses: List<Business> = emptyList(),
        cash: Long = 0,
        loan: Long = 0,
        flight: Long = 0,
        yacht: Long = 0,
        funds: List<Fund> = emptyList(),
        babies: Long = 0,
        isMarried: Boolean = false,
        deputies: Int = 0,
        trackId: TrackId = CoreTrackIds.Inner,
    ) = Player(
        id = "p",
        boardId = "b",
        attrs = PlayerAttributes(color = 0),
        card = PlayerCard(profession = "Інженер"),
        location = PlayerLocation(position = 1, trackId = trackId),
        businesses = businesses,
        cash = cash,
        loan = loan,
        flight = flight,
        yacht = yacht,
        funds = funds,
        babies = babies,
        isMarried = isMarried,
        deputies = deputies,
    )

    private fun business(type: BusinessType) = Business(type, "Бізнес", 10_000, 1_000)

    @Test
    fun finalistOnTheOuterTrackBeatsEverything() {
        val p = player(businesses = listOf(business(BusinessType.LARGE)), cash = 5_000_000, trackId = CoreTrackIds.Outer)
        assertEquals(PlayerStatus.FINALIST, p.status())
    }

    @Test
    fun multimillionaireNeedsTenMillion() {
        assertEquals(PlayerStatus.MULTIMILLIONAIRE, player(cash = 10_000_000).status())
        assertEquals(PlayerStatus.MILLIONAIRE, player(cash = 9_999_999).status())
    }

    @Test
    fun corruptWinsOverMagnate() {
        val p = player(
            businesses = listOf(business(BusinessType.CORRUPTION), business(BusinessType.LARGE)),
            cash = 5_000_000,
        )
        assertEquals(PlayerStatus.CORRUPT, p.status())
    }

    @Test
    fun magnateWinsOverMillionaire() {
        val p = player(
            businesses = listOf(business(BusinessType.LARGE), business(BusinessType.SMALL)),
            cash = 5_000_000,
        )
        assertEquals(PlayerStatus.MAGNATE, p.status())
    }

    @Test
    fun millionaireNeedsTotalAtOrAboveOneMillion() {
        assertEquals(PlayerStatus.MILLIONAIRE, player(cash = 1_000_000).status())
        assertEquals(PlayerStatus.FIRED, player(cash = 999_999).status())
    }

    @Test
    fun debtsOverAssetsMeanBroke() {
        assertEquals(PlayerStatus.BROKE, player(cash = 10_000, loan = 50_000).status())
    }

    @Test
    fun brokeBeatsBusinessTier() {
        val p = player(businesses = listOf(business(BusinessType.MEDIUM)), cash = 10_000, loan = 50_000)
        assertEquals(PlayerStatus.BROKE, p.status())
    }

    @Test
    fun businessTierFallsBackThroughMediumToSmall() {
        assertEquals(PlayerStatus.BUSINESSMAN, player(businesses = listOf(business(BusinessType.MEDIUM))).status())
        assertEquals(PlayerStatus.ENTREPRENEUR, player(businesses = listOf(business(BusinessType.SMALL))).status())
    }

    @Test
    fun deputiesAreLoudEnoughToShout() {
        assertEquals(PlayerStatus.DEPUTY, player(deputies = 1).status())
        assertEquals(PlayerStatus.ENTREPRENEUR, player(businesses = listOf(business(BusinessType.SMALL)), deputies = 3).status())
    }

    @Test
    fun yachtAndPlaneOwnersAreRentiers() {
        val job = listOf(business(BusinessType.WORK))
        assertEquals(PlayerStatus.RENTIER, player(businesses = job, yacht = 1).status())
        assertEquals(PlayerStatus.RENTIER, player(businesses = job, flight = 1).status())
    }

    @Test
    fun fundHoldersAreInvestors() {
        val job = listOf(business(BusinessType.WORK))
        assertEquals(
            PlayerStatus.INVESTOR,
            player(businesses = job, funds = listOf(Fund(rate = 20, amount = 10_000))).status(),
        )
    }

    @Test
    fun firedPlayerHasNoJobAtAll() {
        assertEquals(PlayerStatus.FIRED, player().status())
    }

    @Test
    fun firedWinsOverModestHoldings() {
        assertEquals(PlayerStatus.FIRED, player(funds = listOf(Fund(rate = 20, amount = 10_000))).status())
        assertEquals(PlayerStatus.FIRED, player(yacht = 1).status())
        assertEquals(PlayerStatus.FIRED, player(isMarried = true).status())
    }

    @Test
    fun bigFamilyBeatsMarriage() {
        val job = listOf(business(BusinessType.WORK))
        assertEquals(PlayerStatus.FAMILY, player(businesses = job, babies = 2).status())
        assertEquals(PlayerStatus.MARRIED, player(businesses = job, isMarried = true).status())
    }

    @Test
    fun employeeKeepsTheirProfession() {
        assertEquals(PlayerStatus.EMPLOYEE, player(businesses = listOf(business(BusinessType.WORK))).status())
    }
}
