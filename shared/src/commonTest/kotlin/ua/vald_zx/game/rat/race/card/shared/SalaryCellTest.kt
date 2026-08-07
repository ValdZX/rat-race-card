package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SalaryCellTest {
    private val registry = legacyCellRuleRegistry()
    private val cell = CellInstance("salary", CoreCellTypes.Salary)

    @Test
    fun landingWithNegativeCashFlowChargesOnceAndLeavesNothingToClaim() {
        val player = indebted()
        val expected = player.cashFlow()
        assertTrue(expected < 0, "передумова тесту: cash flow має бути від'ємним")

        val landed = land(player)

        assertEquals(player.cash + expected, landed.cash, "списання має відбутись рівно один раз")
        assertNull(
            landed.salaryPosition,
            "після автоматичного списання не має лишатись отримання зарплати, інакше гроші знімуться вдруге",
        )
    }

    @Test
    fun landingWithNegativeCashFlowStillOffersInvestments() {
        val landed = land(indebted())

        assertEquals(SALARY_INDEX, landed.investmentPosition, "зупинка на зарплаті відкриває інвестиційні ігри")
    }

    @Test
    fun landingWithPositiveCashFlowLeavesTheSalaryToClaim() {
        val player = earning()
        assertTrue(player.cashFlow() > 0)

        val landed = land(player)

        assertEquals(player.cash, landed.cash, "при додатному cash flow гроші не нараховуються автоматично")
        assertEquals(SALARY_INDEX, landed.salaryPosition)
        assertEquals(SALARY_INDEX, landed.investmentPosition)
    }

    @Test
    fun passingWithNegativeCashFlowChargesWithoutOfferingInvestments() {
        val player = indebted()
        val expected = player.cashFlow()

        val passed = pass(player)

        assertEquals(player.cash + expected, passed.cash)
        assertNull(passed.salaryPosition)
        assertNull(passed.investmentPosition, "проходження повз не відкриває інвестиції")
    }

    @Test
    fun passingWithPositiveCashFlowOnlyMarksTheClaim() {
        val player = earning()

        val passed = pass(player)

        assertEquals(player.cash, passed.cash)
        assertEquals(SALARY_INDEX, passed.salaryPosition)
        assertNull(passed.investmentPosition)
    }

    private fun land(player: Player): Player {
        val afterPass = context(player).let { registry.rule(cell.type).onPass(it, cell) }
        val afterLand = registry.rule(cell.type).onLand(context(afterPass), cell)
        return afterLand.snapshot.players.first()
    }

    private fun pass(player: Player): Player =
        registry.rule(cell.type).onPass(context(player), cell).snapshot.players.first()

    private fun context(player: Player) = context(
        GameSnapshot(board(), listOf(player)),
    )

    private fun context(result: RuleResult) = context(result.snapshot)

    private fun context(snapshot: GameSnapshot) = TurnContext(
        result = RuleResult(snapshot),
        playerId = snapshot.players.first().id,
        cellIndex = SALARY_INDEX,
        isLanding = true,
        random = DefaultGameRandom,
        moneyService = MoneyService(),
    )

    private fun board() = Board(
        id = "b",
        name = "b",
        loanLimit = 1_000_000,
        businessLimit = 5,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        playerIds = setOf("p"),
        activePlayerId = "p",
    )

    private fun indebted() = Player(
        id = "p",
        boardId = "b",
        attrs = PlayerAttributes(color = 0),
        card = PlayerCard(name = "P", profession = "Engineer", rent = 3_000, food = 2_000),
        cash = 50_000,
        location = PlayerLocation(position = SALARY_INDEX),
    )

    private fun earning() = indebted().copy(
        businesses = listOf(Business(BusinessType.WORK, "Робота", 0, 9_000)),
    )

    private companion object {
        const val SALARY_INDEX = 7
    }
}
