package ua.vald_zx.game.rat.race.card.logic

import kotlinx.serialization.json.Json
import ua.vald_zx.game.rat.race.card.beans.Business
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.beans.Fund
import ua.vald_zx.game.rat.race.card.beans.Land
import ua.vald_zx.game.rat.race.card.shared.PaymentPolicy
import ua.vald_zx.game.rat.race.card.shared.PlayerCard
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.shared.sharedMoneyService
import ua.vald_zx.game.rat.race.card.shared.activeProfit
import ua.vald_zx.game.rat.race.card.shared.balance
import ua.vald_zx.game.rat.race.card.shared.cashFlow
import ua.vald_zx.game.rat.race.card.shared.creditExpenses
import ua.vald_zx.game.rat.race.card.shared.passiveProfit
import ua.vald_zx.game.rat.race.card.shared.total
import ua.vald_zx.game.rat.race.card.shared.totalExpenses
import kotlin.test.Test
import kotlin.test.assertEquals

class OfflineFinanceAdapterTest {
    @Test
    fun legacyKStoreJsonLoadsWithoutMigration() {
        val json = """
            {
              "playerId": "legacy-player",
              "playerCard": {"name":"Alex","profession":"Engineer","salary":4000},
              "cash": 12000,
              "deposit": 3000,
              "loan": 500,
              "business": [
                {"type":"WORK","name":"Engineer","price":0,"profit":4000}
              ],
              "lands": [
                {"name":"Land","area":10,"priceOfUnit":100}
              ],
              "funds": [
                {"rate":10,"amount":2000}
              ]
            }
        """.trimIndent()

        val state = Json.decodeFromString(RatRace2CardState.serializer(), json)

        assertEquals("legacy-player", state.playerId)
        assertEquals(12_000, state.cash)
        assertEquals(1_000, state.lands.single().price)
        assertEquals(2_000, state.funds.single().amount)
        assertEquals(1_000, state.lands.single().toSharedLand().price)
    }

    @Test
    fun legacyStatisticsKStoreJsonLoadsWithoutMigration() {
        val json = """
            {
              "log": [
                {
                  "cash": 500,
                  "business": [
                    {"type":"SMALL","name":"Shop","price":1000,"profit":100}
                  ]
                }
              ],
              "salaryCount": 3
            }
        """.trimIndent()

        val statistics = Json.decodeFromString(Statistics.serializer(), json)

        assertEquals(3, statistics.salaryCount)
        assertEquals(500, statistics.log.single().cash)
        assertEquals(BusinessType.SMALL, statistics.log.single().business.single().type)
    }

    @Test
    fun localStateDelegatesFinancialFormulasToTheSharedSnapshot() {
        val state = state()
        val snapshot = state.financialSnapshot()

        assertEquals(snapshot.balance(), state.balance())
        assertEquals(snapshot.activeProfit(), state.activeProfit())
        assertEquals(snapshot.passiveProfit(), state.passiveProfit())
        assertEquals(snapshot.creditExpenses(), state.creditExpenses())
        assertEquals(snapshot.totalExpenses(), state.totalExpenses())
        assertEquals(snapshot.cashFlow(), state.cashFlow())
        assertEquals(snapshot.total(), state.total())
    }

    @Test
    fun sharedPaymentRoundTripsThroughTheLocalAdapter() {
        val state = state()
        val result = sharedMoneyService.pay(
            account = state.financialAccount(),
            amount = 4_500,
            policy = PaymentPolicy(),
        )

        val paid = state.withFinancialAccount(result.account)

        assertEquals(0, paid.cash)
        assertEquals(0, paid.deposit)
        assertEquals(listOf(Fund(rate = 10, amount = 500)), paid.funds)
        assertEquals(state.loan, paid.loan)
    }

    @Test
    fun onlineAndLocalModesProduceTheSameBaseFinancialResults() {
        val local = state().copy(lands = emptyList())
        val online = Player(
            id = "player",
            boardId = "board",
            attrs = PlayerAttributes(0),
            card = local.playerCard,
            cash = local.cash,
            deposit = local.deposit,
            loan = local.loan,
            businesses = local.business.map {
                ua.vald_zx.game.rat.race.card.shared.Business(
                    type = ua.vald_zx.game.rat.race.card.shared.BusinessType.valueOf(it.type.name),
                    name = it.name,
                    price = it.price,
                    profit = it.profit,
                    extentions = it.extentions,
                    alarmed = it.alarmed,
                )
            },
            funds = local.funds.map { ua.vald_zx.game.rat.race.card.shared.Fund(it.rate, it.amount) },
            cars = local.cars,
        )

        assertEquals(online.balance(), local.balance())
        assertEquals(online.activeProfit(), local.activeProfit())
        assertEquals(online.passiveProfit(), local.passiveProfit())
        assertEquals(online.creditExpenses(), local.creditExpenses())
        assertEquals(online.totalExpenses(), local.totalExpenses())
        assertEquals(online.cashFlow(), local.cashFlow())
        assertEquals(online.total(), local.total())
    }

    private fun state() = RatRace2CardState(
        playerCard = PlayerCard(
            profession = "Engineer",
            salary = 4_000,
            rent = 500,
            food = 300,
            cloth = 100,
            transport = 200,
            phone = 50,
        ),
        cash = 1_000,
        deposit = 1_500,
        loan = 500,
        business = listOf(Business(BusinessType.WORK, "Engineer", 0, 4_000)),
        lands = listOf(Land("Land", area = 10, priceOfUnit = 100)),
        funds = listOf(Fund(rate = 10, amount = 2_500)),
        cars = 1,
    )
}
