package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class StandardEffectHandlersTest {
    private val registry = standardEffectHandlerRegistry()

    @Test
    fun everyDeclaredEffectTypeNowHasAHandler() {
        val declared = setOf(
            StandardEffectTypes.ChangeCash,
            StandardEffectTypes.PayAmount,
            StandardEffectTypes.AcquireBusiness,
            StandardEffectTypes.AcquireShopping,
            StandardEffectTypes.PayExpense,
            StandardEffectTypes.RemoveAsset,
            StandardEffectTypes.ChangeRecurringIncome,
            StandardEffectTypes.ChangeRecurringExpense,
            StandardEffectTypes.OfferPurchase,
            StandardEffectTypes.OfferSale,
            StandardEffectTypes.RequirePlayerPredicate,
            StandardEffectTypes.RequireResource,
            StandardEffectTypes.SpendResource,
            StandardEffectTypes.DrawCard,
            StandardEffectTypes.StartAuction,
            StandardEffectTypes.ForEachEligiblePlayer,
            StandardEffectTypes.SetCounter,
            StandardEffectTypes.EmitNotice,
            StandardEffectTypes.EndTurn,
        )

        assertEquals(emptySet(), declared - registry.registeredTypes, "заявлені ефекти без handler'а")
    }

    @Test
    fun removingABusinessNeverTakesTheJob() {
        val context = context(
            player("first").copy(
                businesses = listOf(
                    Business(BusinessType.WORK, "Робота", 0, 5_000),
                    Business(BusinessType.SMALL, "Кав'ярня", 20_000, 2_000),
                ),
            ),
        )

        val applied = execute(context, StandardEffectTypes.RemoveAsset) {
            put("asset", AssetKind.BUSINESS.name)
            put("selector", AssetSelector.ALL.name)
        }

        assertEquals(listOf(BusinessType.WORK), applied.player.businesses.map { it.type })
    }

    @Test
    fun recurringIncomeExtendsABusinessAndSkipsTheJob() {
        val context = context(
            player("first").copy(
                businesses = listOf(
                    Business(BusinessType.WORK, "Робота", 0, 5_000),
                    Business(BusinessType.SMALL, "Кав'ярня", 20_000, 2_000),
                ),
            ),
        )

        val applied = execute(context, StandardEffectTypes.ChangeRecurringIncome) { put("amount", 700L) }

        val extended = applied.player.businesses.first { it.type == BusinessType.SMALL }
        assertEquals(listOf(700L), extended.extentions)
        assertEquals(emptyList(), applied.player.businesses.first { it.type == BusinessType.WORK }.extentions)
    }

    @Test
    fun aMissingBusinessMakesRecurringIncomeANoOp() {
        val context = context(player("first"))

        val applied = execute(context, StandardEffectTypes.ChangeRecurringIncome) { put("amount", 700L) }

        assertEquals(context.player, applied.player)
    }

    @Test
    fun resourceGuardsBlockWhatThePlayerCannotAfford() {
        val poor = context(player("first").copy(deputies = 1))
        val rich = context(player("first").copy(deputies = 5))
        val spec = spec(StandardEffectTypes.RequireResource) {
            put("resource", PlayerResource.DEPUTIES.name)
            put("amount", 3L)
        }

        assertFalse(registry.canApply(poor, listOf(spec), JsonObject(emptyMap())))
        assertTrue(registry.canApply(rich, listOf(spec), JsonObject(emptyMap())))
    }

    @Test
    fun spendingAResourceNeverDrivesItNegative() {
        val context = context(player("first").copy(deputies = 2))

        val applied = execute(context, StandardEffectTypes.SpendResource) {
            put("resource", PlayerResource.DEPUTIES.name)
            put("amount", 5L)
        }

        assertEquals(0, applied.player.deputies)
    }

    @Test
    fun derivedResourcesCannotBeSetDirectly() {
        val context = context(
            player("first").copy(businesses = listOf(Business(BusinessType.SMALL, "К", 1, 1))),
        )

        val applied = execute(context, StandardEffectTypes.SetCounter) {
            put("resource", PlayerResource.BUSINESS_COUNT.name)
            put("value", 0L)
        }

        assertEquals(1, applied.player.businesses.size, "кількість бізнесів є похідною і не задається лічильником")
    }

    @Test
    fun predicateGuardsSeeTheBoardContext() {
        val married = context(player("first").copy(isMarried = true))
        val single = context(player("first"))
        val spec = spec(StandardEffectTypes.RequirePlayerPredicate) {
            put("predicate", PlayerPredicate.IS_MARRIED.name)
        }

        assertTrue(registry.canApply(married, listOf(spec), JsonObject(emptyMap())))
        assertFalse(registry.canApply(single, listOf(spec), JsonObject(emptyMap())))
    }

    @Test
    fun forEachEligiblePlayerTouchesOnlyMatchingPlayersAndRestoresTheOrigin() {
        val snapshot = snapshot(
            player("first").copy(deputies = 3),
            player("second").copy(deputies = 4),
            player("third"),
        )
        val context = TurnContext(
            result = RuleResult(snapshot),
            playerId = "first",
            cellIndex = 1,
            isLanding = true,
            random = FixedRandom,
            moneyService = MoneyService(),
        )

        val applied = execute(context, StandardEffectTypes.ForEachEligiblePlayer) {
            put("effects", buildJsonArray {
                add(
                    Json.encodeToJsonElement(
                        EffectSpec.serializer(),
                        EffectSpec(
                            StandardEffectTypes.SetCounter,
                            buildJsonObject {
                                put("resource", PlayerResource.DEPUTIES.name)
                                put("value", 0L)
                            },
                        ),
                    ),
                )
            })
        }

        assertEquals("first", applied.playerId, "після обходу контекст має повернутись до автора ходу")
        assertTrue(applied.snapshot.players.all { it.deputies == 0 })
    }

    @Test
    fun forEachEligiblePlayerCanSkipTheActingPlayer() {
        val snapshot = snapshot(player("first").copy(deputies = 3), player("second").copy(deputies = 4))
        val context = TurnContext(
            result = RuleResult(snapshot),
            playerId = "first",
            cellIndex = 1,
            isLanding = true,
            random = FixedRandom,
            moneyService = MoneyService(),
        )

        val applied = execute(context, StandardEffectTypes.ForEachEligiblePlayer) {
            put("includeSelf", "false")
            put("effects", buildJsonArray {
                add(
                    Json.encodeToJsonElement(
                        EffectSpec.serializer(),
                        EffectSpec(
                            StandardEffectTypes.SetCounter,
                            buildJsonObject {
                                put("resource", PlayerResource.DEPUTIES.name)
                                put("value", 0L)
                            },
                        ),
                    ),
                )
            })
        }

        assertEquals(3, applied.snapshot.players.first { it.id == "first" }.deputies)
        assertEquals(0, applied.snapshot.players.first { it.id == "second" }.deputies)
    }

    @Test
    fun drawCardOpensOnlyDecksTheBoardActuallyHas() {
        val context = context(player("first"))

        val applied = execute(context, StandardEffectTypes.DrawCard) { put("deck", BoardCardType.Chance.name) }

        assertEquals(listOf(BoardCardType.Chance), applied.board.canTakeCard)
    }

    @Test
    fun startAuctionPutsTheLotOnTheBoard() {
        val context = context(player("first"))
        val auction = Auction.LandAuction(Land("Ділянка", 10, 5_000), firstBid = 5_000)

        val applied = execute(context, StandardEffectTypes.StartAuction) {
            Json.encodeToJsonElement(Auction.serializer(), auction).let { element ->
                (element as JsonObject).forEach { (key, value) -> put(key, value) }
            }
        }

        assertEquals(auction, applied.board.auction)
        assertEquals(emptyList(), applied.board.bidList)
    }

    @Test
    fun offerSalePublishesAFollowUpInteraction() {
        val context = context(player("first"))
        val interaction = InteractionSpec(
            id = "sell-land",
            kind = StandardInteractionKinds.Sell,
            title = "Продати землю",
            fields = listOf(
                InteractionField("area", InteractionFieldType.AMOUNT, "Площа", minimum = 0, maximum = 100),
            ),
            branches = mapOf("sell" to listOf(EffectSpec(StandardEffectTypes.EndTurn))),
        )

        val applied = execute(context, StandardEffectTypes.OfferSale) {
            (Json.encodeToJsonElement(InteractionSpec.serializer(), interaction) as JsonObject)
                .forEach { (key, value) -> put(key, value) }
        }

        val pending = applied.board.pendingInteractions.single()
        assertEquals("sell-land", pending.id)
        assertEquals(StandardInteractionKinds.Sell, pending.kind)
        assertEquals("first", pending.playerId)
    }

    @Test
    fun invalidParametersAreRejectedBeforeExecution() {
        val badEnum = spec(StandardEffectTypes.RemoveAsset) { put("asset", "SPACESHIP") }
        val badNumber = spec(StandardEffectTypes.ChangeRecurringIncome) { put("amount", "soon") }
        val negative = spec(StandardEffectTypes.RequireResource) {
            put("resource", PlayerResource.CASH.name)
            put("amount", -5L)
        }

        assertIs<ValidationResult.Invalid>(registry.validate(listOf(badEnum)))
        assertIs<ValidationResult.Invalid>(registry.validate(listOf(badNumber)))
        assertIs<ValidationResult.Invalid>(registry.validate(listOf(negative)))
    }

    private fun execute(
        context: TurnContext,
        type: EffectTypeId,
        parameters: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit,
    ): TurnContext = registry.execute(context, listOf(spec(type, parameters)), JsonObject(emptyMap()))

    private fun spec(
        type: EffectTypeId,
        parameters: kotlinx.serialization.json.JsonObjectBuilder.() -> Unit = {},
    ) = EffectSpec(type, buildJsonObject(parameters))

    private fun context(vararg players: Player) = TurnContext(
        result = RuleResult(snapshot(*players)),
        playerId = players.first().id,
        cellIndex = 1,
        isLanding = true,
        random = FixedRandom,
        moneyService = MoneyService(),
    )

    private fun snapshot(vararg players: Player) = GameSnapshot(
        board = Board(
            id = "board",
            name = "Board",
            loanLimit = 100_000,
            businessLimit = 5,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = mapOf(BoardCardType.Chance to listOf(1, 2, 3)),
            playerIds = players.map { it.id }.toSet(),
            activePlayerId = players.first().id,
        ),
        players = players.toList(),
    )

    private fun player(id: String) = Player(
        id = id,
        boardId = "board",
        attrs = PlayerAttributes(color = 0),
        location = PlayerLocation(position = 1),
    )

    private object FixedRandom : GameRandom {
        override fun nextInt(from: Int, until: Int): Int = from
    }
}
