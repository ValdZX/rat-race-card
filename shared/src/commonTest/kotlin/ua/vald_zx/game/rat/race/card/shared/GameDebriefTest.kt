package ua.vald_zx.game.rat.race.card.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameDebriefTest {
    @Test
    fun spendingIsGroupedByReason() {
        val debrief = GameDebrief(
            playerId = "first",
            entries = listOf(
                entry(0, LedgerReason.OTHER, total = 10_000),
                entry(1, LedgerReason.SALARY, total = 14_000),
                entry(2, LedgerReason.CONSUMER_PURCHASE, total = 9_000),
                entry(3, LedgerReason.SCAM, total = 4_000),
                entry(4, LedgerReason.SALARY, total = 8_000),
            ),
        )

        val salary = debrief.flows.single { it.reason == LedgerReason.SALARY }
        val consumer = debrief.flows.single { it.reason == LedgerReason.CONSUMER_PURCHASE }
        val scam = debrief.flows.single { it.reason == LedgerReason.SCAM }

        assertEquals(8_000, salary.inflow)
        assertEquals(5_000, consumer.outflow)
        assertEquals(5_000, scam.outflow)
    }

    @Test
    fun theBiggestDrainIsReportedFirst() {
        val debrief = GameDebrief(
            playerId = "first",
            entries = listOf(
                entry(0, LedgerReason.OTHER, total = 100_000),
                entry(1, LedgerReason.CONSUMER_PURCHASE, total = 90_000),
                entry(2, LedgerReason.SCAM, total = 20_000),
            ),
        )

        assertEquals(LedgerReason.SCAM, debrief.flows.first().reason)
    }

    @Test
    fun theMomentCashFlowBrokeIsFound() {
        val debrief = GameDebrief(
            playerId = "first",
            entries = listOf(
                entry(0, LedgerReason.SALARY, total = 10_000, cashFlow = 500),
                entry(1, LedgerReason.CONSUMER_PURCHASE, total = 9_000, cashFlow = -200),
                entry(2, LedgerReason.SALARY, total = 8_800, cashFlow = -900),
            ),
        )

        assertEquals(1, debrief.firstNegativeCashFlow?.sequence)
    }

    @Test
    fun aHealthyGameReportsNoCashFlowBreak() {
        val debrief = GameDebrief("first", listOf(entry(0, LedgerReason.SALARY, total = 10_000, cashFlow = 500)))

        assertNull(debrief.firstNegativeCashFlow)
    }

    @Test
    fun interestIsCountedOnlyOnPayDays() {
        val debrief = GameDebrief(
            playerId = "first",
            entries = listOf(
                entry(0, LedgerReason.SALARY, total = 10_000, creditExpenses = 300),
                entry(1, LedgerReason.CONSUMER_PURCHASE, total = 9_000, creditExpenses = 300),
                entry(2, LedgerReason.SALARY, total = 9_500, creditExpenses = 400),
            ),
        )

        assertEquals(700, debrief.totalPaidInterest)
    }

    @Test
    fun inflationEatsIntoTheNominalResult() {
        val debrief = GameDebrief(
            playerId = "first",
            entries = listOf(
                entry(0, LedgerReason.SALARY, total = 100_000, priceIndexPercent = 100),
                entry(1, LedgerReason.SALARY, total = 200_000, priceIndexPercent = 150),
            ),
        )

        assertEquals(133_333, debrief.finish?.realTotal)
        assertTrue(debrief.inflationLostValue > 0)
    }

    @Test
    fun assetShareShowsWhetherWealthSatInCashOrInAssets() {
        val hoarder = GameDebrief("h", listOf(entry(0, LedgerReason.SALARY, total = 100, cash = 100)))
        val owner = GameDebrief(
            "o",
            listOf(entry(0, LedgerReason.SALARY, total = 100, cash = 20, businessValue = 80)),
        )

        assertEquals(0, hoarder.assetShareAtFinishPercent)
        assertEquals(80, owner.assetShareAtFinishPercent)
    }

    @Test
    fun anEmptyLedgerIsSafeToRender() {
        val debrief = GameDebrief("first", emptyList())

        assertNull(debrief.start)
        assertNull(debrief.finish)
        assertEquals(emptyList(), debrief.flows)
        assertEquals(0, debrief.inflationLostValue)
        assertEquals(0, debrief.assetShareAtFinishPercent)
    }

    private fun entry(
        sequence: Long,
        reason: LedgerReason,
        total: Long,
        cashFlow: Long = 0,
        creditExpenses: Long = 0,
        priceIndexPercent: Long = NEUTRAL_INDEX_PERCENT,
        cash: Long = 0,
        businessValue: Long = 0,
    ) = LedgerEntry(
        playerId = "first",
        sequence = sequence,
        atEpochMs = 1_000 + sequence,
        reason = reason,
        economyPeriod = 0,
        priceIndexPercent = priceIndexPercent,
        cash = cash,
        deposit = 0,
        loan = 0,
        funds = 0,
        businessValue = businessValue,
        shareValue = 0,
        total = total,
        cashFlow = cashFlow,
        creditExpenses = creditExpenses,
        livingExpenses = 0,
    )
}
