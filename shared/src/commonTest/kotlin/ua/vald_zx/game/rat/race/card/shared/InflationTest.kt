package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InflationTest {
    private val settings = InflationSettings(enabled = true, periodRatePercent = 10, salaryIndexationPercent = 50)

    @Test
    fun disabledInflationKeepsEveryFormulaUnchanged() {
        val initial = snapshot(InflationSettings())
        val cashFlowBefore = initial.player("first").cashFlow()

        val advanced = lap(initial, "first", "second")

        assertEquals(EconomyIndex(), advanced.board.economy)
        assertEquals(cashFlowBefore, advanced.player("first").cashFlow())
        assertEquals(initial.board.victoryConditions, advanced.board.victoryConditions)
    }

    @Test
    fun everyPlayerMustPassStartBeforePricesMove() {
        val initial = snapshot(settings)

        val afterFirst = lap(initial, "first")

        assertEquals(0, afterFirst.board.economy.period, "одного гравця замало для нового періоду")
        assertEquals(setOf("first"), afterFirst.board.economyLapPlayerIds)

        val afterBoth = lap(afterFirst, "second")

        assertEquals(1, afterBoth.board.economy.period)
        assertEquals(110, afterBoth.board.economy.priceIndexPercent)
        assertEquals(105, afterBoth.board.economy.salaryIndexPercent)
        assertEquals(emptySet(), afterBoth.board.economyLapPlayerIds, "лічильник кола має скинутись")
        afterBoth.players.forEach { player ->
            assertEquals(110, player.config.priceIndexPercent)
            assertEquals(105, player.config.salaryIndexPercent)
        }
    }

    @Test
    fun aFastPlayerCannotRunInflationUpAlone() {
        var current = snapshot(settings)

        repeat(5) { step -> current = lap(current, "first") }

        assertEquals(0, current.board.economy.period, "п'ять кіл одного гравця не рухають ціни")
    }

    @Test
    fun turnAdvancesNoLongerTouchTheEconomy() {
        val engine = GameEngine(FixedGameRandom)
        var current = snapshot(settings)

        repeat(4) { step -> current = advance(engine, current, "turn-$step") }

        assertEquals(0, current.board.economy.period)
    }

    @Test
    fun restingDoesNotAmplifyInflation() {
        val engine = GameEngine(FixedGameRandom)
        val resting = snapshot(settings).let { snapshot ->
            snapshot.copy(players = snapshot.players.map { it.copy(inRest = 3) })
        }

        val advanced = advance(engine, resting, "rest")

        assertEquals(0, advanced.board.economy.period, "пропуски ходів більше не множать періоди")
    }

    @Test
    fun aSoloBoardTicksOncePerOwnLap() {
        val solo = snapshot(settings, listOf("only"))

        val afterOne = lap(solo, "only")
        val afterTwo = lap(afterOne, "only")

        assertEquals(1, afterOne.board.economy.period)
        assertEquals(2, afterTwo.board.economy.period)
    }

    @Test
    fun inflationCompoundsAcrossPeriods() {
        var index = EconomyIndex()

        repeat(3) { index = index.advanced(settings) }

        assertEquals(3, index.period)
        assertEquals(133, index.priceIndexPercent)
        assertEquals(115, index.salaryIndexPercent)
    }

    @Test
    fun goalPostsMoveWithPricesSoHoardingCashLosesGround() {
        val initial = snapshot(settings)
        val victoryBefore = initial.board.victoryConditions.minimumAccountBalance
        val exitBefore = initial.board.outerCircleConditions.minimumAccountBalance

        val advanced = lap(initial, "first", "second")

        assertEquals(victoryBefore * 110 / 100, advanced.board.victoryConditions.minimumAccountBalance)
        assertEquals(exitBefore * 110 / 100, advanced.board.outerCircleConditions.minimumAccountBalance)
    }

    @Test
    fun businessIncomeKeepsPaceWhileSalaryFallsBehind() {
        val indexed = EconomyIndex().advanced(settings)
        val salaryOnly = player("salary")
        val entrepreneur = player("business").copy(
            businesses = listOf(Business(BusinessType.SMALL, "Кав'ярня", 20_000, 5_000)),
        )

        val salaryReal = realValue(salaryOnly, indexed)
        val businessReal = realValue(entrepreneur, indexed)

        assertTrue(
            salaryReal < salaryOnly.activeProfit(),
            "зарплата має втратити купівельну спроможність: було ${salaryOnly.activeProfit()}, стало $salaryReal",
        )
        assertEquals(
            entrepreneur.activeProfit(),
            businessReal,
            "бізнес-дохід має тримати темп інфляції",
        )
    }

    @Test
    fun inflationDoesNotTouchCreditExpenses() {
        val indebted = player("first").copy(loan = 100_000)

        val before = indebted.creditExpenses()
        val after = indebted.withEconomy(EconomyIndex().advanced(settings)).creditExpenses()

        assertEquals(before, after, "борг номінальний, тому інфляція його знецінює, а не збільшує")
    }

    @Test
    fun rejectsOutOfRangeSettings() {
        assertIs<ValidationResult.Invalid>(InflationSettings(periodRatePercent = -1).validate())
        assertIs<ValidationResult.Invalid>(InflationSettings(periodRatePercent = 51).validate())
        assertIs<ValidationResult.Invalid>(InflationSettings(salaryIndexationPercent = 101).validate())
        assertIs<ValidationResult.Valid>(settings.validate())
    }

    private fun realValue(player: Player, index: EconomyIndex): Long {
        val nominal = player.withEconomy(index).activeProfit()
        return nominal * NEUTRAL_INDEX_PERCENT / index.priceIndexPercent
    }

    private fun Player.withEconomy(index: EconomyIndex): Player = copy(config = config.withEconomyIndex(index))

    private fun lap(snapshot: GameSnapshot, vararg playerIds: String): GameSnapshot {
        return playerIds.fold(snapshot) { current, playerId ->
            TurnContext(
                result = RuleResult(current),
                playerId = playerId,
                cellIndex = 0,
                isLanding = false,
                random = FixedGameRandom,
                moneyService = MoneyService(),
            ).passStart().result.snapshot
        }
    }

    private fun advance(engine: GameEngine, snapshot: GameSnapshot, commandId: String): GameSnapshot {
        val execution = engine.execute(
            snapshot,
            GameCommandEnvelope(
                commandId = commandId,
                boardId = snapshot.board.id,
                playerId = snapshot.board.activePlayerId,
                expectedRevision = snapshot.board.revision,
                command = GameCommand.AdvanceTurn,
            ),
        )
        return assertIs<GameExecution.Applied>(execution).snapshot
    }

    private fun snapshot(
        inflation: InflationSettings,
        playerIds: List<String> = listOf("first", "second"),
    ): GameSnapshot {
        val board = Board(
            id = "board",
            name = "Board",
            loanLimit = 100_000,
            businessLimit = 3,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            playerIds = playerIds.toSet(),
            activePlayerId = playerIds.first(),
            inflation = inflation,
            outerCircleConditions = OuterCircleConditions(minimumAccountBalance = 200_000),
            victoryConditions = VictoryConditions(minimumAccountBalance = 10_000_000),
        )
        return GameSnapshot(board = board, players = playerIds.map(::player))
    }

    private fun player(id: String) = Player(
        id = id,
        boardId = "board",
        attrs = PlayerAttributes(color = 0),
        location = PlayerLocation(position = 1),
        card = PlayerCard(name = id, profession = "Engineer", salary = 5_000, rent = 800, food = 400),
        businesses = listOf(Business(BusinessType.WORK, "Робота", 0, 5_000)),
    )

    private fun GameSnapshot.player(id: String): Player = players.first { it.id == id }

    private object FixedGameRandom : GameRandom {
        override fun nextInt(from: Int, until: Int): Int = 3
    }
}
