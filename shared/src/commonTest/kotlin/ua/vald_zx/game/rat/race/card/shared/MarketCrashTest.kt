package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarketCrashTest {
    private val crash = BoardCard.EventStore.MarketCrash(
        description = "",
        sector = ShareSectors.TECH,
        sectorDropPercentage = 60,
        marketDropPercentage = 10,
    )

    @Test
    fun legacyTickersKeepStableSectors() {
        val board = board()

        assertEquals(ShareSectors.TECH, board.sectorOf(SharesType.IT))
        assertEquals(ShareSectors.AGRO, board.sectorOf(SharesType.AGRO))
        assertEquals(ShareSectors.ENERGY, board.sectorOf(SharesType.GC))
    }

    @Test
    fun generatedSectorWinsOverLegacyDefault() {
        val board = board().copy(
            generatedBalance = null,
        )

        assertEquals(ShareSectors.INDUSTRY, board.sectorOf("unknown_company"))
    }

    @Test
    fun struckSectorLosesMoreThanTheRestOfTheMarket() {
        val board = board()
        val holdings = listOf(
            Shares(SharesType.IT, count = 10, buyPrice = 100),
            Shares(SharesType.AGRO, count = 10, buyPrice = 100),
        )

        val outcome = board.applyMarketCrash(holdings, crash)

        assertEquals(40, outcome.shares.first { it.type == SharesType.IT }.buyPrice)
        assertEquals(90, outcome.shares.first { it.type == SharesType.AGRO }.buyPrice)
        assertEquals(600 + 100, outcome.lostValue)
    }

    @Test
    fun concentratedPortfolioLosesMoreThanADiversifiedOne() {
        val board = board()
        val concentrated = listOf(Shares(SharesType.IT, count = 20, buyPrice = 100))
        val diversified = listOf(
            Shares(SharesType.IT, count = 10, buyPrice = 100),
            Shares(SharesType.AGRO, count = 10, buyPrice = 100),
        )

        val concentratedLoss = board.applyMarketCrash(concentrated, crash).lostValue
        val diversifiedLoss = board.applyMarketCrash(diversified, crash).lostValue

        assertTrue(
            concentratedLoss > diversifiedLoss,
            "концентрований портфель має втратити більше: $concentratedLoss проти $diversifiedLoss",
        )
    }

    @Test
    fun emptyPortfolioIsUntouched() {
        val outcome = board().applyMarketCrash(emptyList(), crash)

        assertEquals(emptyList(), outcome.shares)
        assertEquals(0, outcome.lostValue)
    }

    @Test
    fun aFullWipeOutNeverTurnsIntoNegativeValue() {
        val total = crash.copy(sectorDropPercentage = 100, marketDropPercentage = 100)
        val outcome = board().applyMarketCrash(listOf(Shares(SharesType.IT, 5, 100)), total)

        assertEquals(0, outcome.shares.single().buyPrice)
        assertEquals(500, outcome.lostValue)
    }

    private fun board() = Board(
        id = "board",
        name = "Board",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
    )
}
