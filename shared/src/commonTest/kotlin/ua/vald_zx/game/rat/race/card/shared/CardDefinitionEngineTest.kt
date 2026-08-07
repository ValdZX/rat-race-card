package ua.vald_zx.game.rat.race.card.shared

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CardDefinitionEngineTest {
    @Test
    fun knownEffectsCreateACompleteCardWithoutNewCommandTypes() {
        val initial = snapshot()
        val definition = CardDefinition(
            id = "bonus-choice",
            deckId = DeckId("test.market"),
            kind = CardKindId("test.bonus"),
            presentation = CardPresentation("Bonus", "Choose a bonus"),
            interactions = listOf(
                InteractionSpec(
                    id = "bonus-choice:choose",
                    kind = StandardInteractionKinds.Choice,
                    title = "Choose",
                    fields = listOf(
                        InteractionField(
                            id = CHOICE_INPUT,
                            type = InteractionFieldType.CHOICE,
                            label = "Bonus",
                            options = listOf(InteractionOption("cash", "Cash")),
                        ),
                    ),
                    branches = mapOf(
                        "cash" to listOf(
                            EffectSpec(
                                StandardEffectTypes.ChangeCash,
                                buildJsonObject { put("amount", 500) },
                            ),
                            EffectSpec(StandardEffectTypes.EndTurn),
                        ),
                    ),
                ),
            ),
        )
        val engine = GameEngine(FixedGameRandom)

        val started = engine.execute(
            initial,
            command(initial, "start", GameCommand.StartCard(definition)),
        ).applied().snapshot
        assertEquals("bonus-choice", started.board.activeCardDefinitionId)
        assertEquals("bonus-choice:choose", started.board.pendingInteractions.single().id)

        val chosen = engine.execute(
            started,
            command(
                started,
                "choose",
                GameCommand.ChooseInteraction(
                    interactionId = "bonus-choice:choose",
                    input = buildJsonObject { put(CHOICE_INPUT, "cash") },
                ),
            ),
        ).applied().snapshot

        assertEquals(500, chosen.player("first").cash)
        assertEquals("second", chosen.board.activePlayerId)
        assertTrue(chosen.board.pendingInteractions.isEmpty())
        assertEquals(2, chosen.board.revision)
    }

    @Test
    fun amountFormUsesItsOnlyBranchWithoutTransportSpecificFields() {
        val initial = snapshot()
        val definition = CardDefinition(
            id = "amount-card",
            deckId = DeckId("test.market"),
            kind = CardKindId("test.amount"),
            presentation = CardPresentation("Amount", "Enter amount"),
            interactions = listOf(
                InteractionSpec(
                    id = "amount-card:amount",
                    kind = StandardInteractionKinds.Amount,
                    title = "Amount",
                    fields = listOf(
                        InteractionField(
                            id = "amount",
                            type = InteractionFieldType.AMOUNT,
                            label = "Amount",
                            minimum = 10,
                            maximum = 100,
                        ),
                    ),
                    branches = mapOf(
                        "submit" to listOf(EffectSpec(StandardEffectTypes.EndTurn)),
                    ),
                ),
            ),
        )
        val engine = GameEngine(FixedGameRandom)
        val started = engine.execute(
            initial,
            command(initial, "start-amount", GameCommand.StartCard(definition)),
        ).applied().snapshot

        val chosen = engine.execute(
            started,
            command(
                started,
                "choose-amount",
                GameCommand.ChooseInteraction(
                    "amount-card:amount",
                    buildJsonObject { put("amount", 50) },
                ),
            ),
        )

        assertIs<GameExecution.Applied>(chosen)
    }

    @Test
    fun legacySimpleCardsAdaptToValidatedDefinitions() {
        val cards = listOf(
            BoardCard.SmallBusiness("Cafe", "Description", 1_000, 100) to
                    CardInteractionKinds.BusinessPurchase,
            BoardCard.Shopping("Description", 500, ShopType.AUTO, "", "Car") to
                    CardInteractionKinds.ShoppingPurchase,
            BoardCard.Chance.RandomJob("Description", 300, "Job") to
                    CardInteractionKinds.RandomJobConfirmation,
            BoardCard.Expenses("Description", "Pay", 200, PayerType.ALL) to
                    CardInteractionKinds.ExpenseConfirmation,
        )
        val validator = CardDefinitionEngine(standardEffectHandlerRegistry())

        cards.forEachIndexed { index, (card, interactionKind) ->
            val definition = card.toCardDefinition(CardLink(card.type, index))
            assertNotNull(definition)
            assertEquals(ValidationResult.Valid, validator.validate(definition))
            assertEquals(interactionKind, definition.interactions.single().kind)
        }
    }

    @Test
    fun pendingInteractionSurvivesJsonRoundTrip() {
        val pending = PendingInteraction(
            id = "interaction",
            cardDefinitionId = "definition",
            playerId = "first",
            kind = StandardInteractionKinds.Sell,
            title = "Sell",
            fields = listOf(
                InteractionField(
                    id = "amount",
                    type = InteractionFieldType.AMOUNT,
                    label = "Amount",
                    minimum = 1,
                    maximum = 10,
                    quickValues = listOf(1, 5, 10),
                ),
            ),
            branches = mapOf("submit" to listOf(EffectSpec(StandardEffectTypes.EndTurn))),
        )

        val encoded = Json.encodeToString(pending)

        assertEquals(pending, Json.decodeFromString<PendingInteraction>(encoded))
    }

    private fun snapshot(): GameSnapshot {
        val board = Board(
            id = "board",
            name = "Board",
            loanLimit = 100_000,
            businessLimit = 3,
            createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
            cards = emptyMap(),
            playerIds = setOf("first", "second"),
            activePlayerId = "first",
        )
        return GameSnapshot(board, listOf(player("first"), player("second")))
    }

    private fun player(id: String) = Player(
        id = id,
        boardId = "board",
        attrs = PlayerAttributes(color = 0),
        location = PlayerLocation(position = 1),
    )

    private fun command(snapshot: GameSnapshot, id: String, command: GameCommand) = GameCommandEnvelope(
        commandId = id,
        boardId = snapshot.board.id,
        playerId = snapshot.board.activePlayerId,
        expectedRevision = snapshot.board.revision,
        command = command,
    )

    private fun GameExecution.applied(): RuleResult = assertIs<GameExecution.Applied>(this).result

    private fun GameSnapshot.player(id: String): Player = players.first { it.id == id }

    private data object FixedGameRandom : GameRandom {
        override fun nextInt(from: Int, until: Int): Int = from
    }
}
