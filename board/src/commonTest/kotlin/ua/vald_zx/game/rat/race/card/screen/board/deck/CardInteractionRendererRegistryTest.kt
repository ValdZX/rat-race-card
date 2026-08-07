package ua.vald_zx.game.rat.race.card.screen.board.deck

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import ua.vald_zx.game.rat.race.card.shared.CardInteractionKinds
import ua.vald_zx.game.rat.race.card.shared.InteractionKindId
import ua.vald_zx.game.rat.race.card.shared.PendingInteraction
import ua.vald_zx.game.rat.race.card.shared.StandardInteractionKinds

class CardInteractionRendererRegistryTest {
    @Test
    fun migratedCardInteractionsUseSpecializedRenderers() {
        listOf(
            CardInteractionKinds.BusinessPurchase,
            CardInteractionKinds.ShoppingPurchase,
            CardInteractionKinds.RandomJobConfirmation,
            CardInteractionKinds.ExpenseConfirmation,
        ).forEach { kind ->
            assertTrue(cardInteractionRendererRegistry.hasSpecializedRenderer(interaction(kind = kind)))
        }
    }

    @Test
    fun standardAndUnknownInteractionsUseGenericRenderer() {
        assertFalse(
            cardInteractionRendererRegistry.hasSpecializedRenderer(
                interaction(kind = StandardInteractionKinds.Purchase),
            ),
        )
        assertFalse(
            cardInteractionRendererRegistry.hasSpecializedRenderer(
                interaction(kind = InteractionKindId("feature.custom"), cardDefinitionId = "shopping-2"),
            ),
        )
    }

    @Test
    fun interactionsSavedBeforeRendererKindsStillUseSpecializedRenderers() {
        listOf(
            "smallbusiness-1" to StandardInteractionKinds.Purchase,
            "mediumbusiness-2" to StandardInteractionKinds.Purchase,
            "bigbusiness-3" to StandardInteractionKinds.Purchase,
            "shopping-4" to StandardInteractionKinds.Purchase,
            "chance-5" to StandardInteractionKinds.Purchase,
            "expenses-6" to StandardInteractionKinds.Choice,
        ).forEach { (definitionId, kind) ->
            assertTrue(
                cardInteractionRendererRegistry.hasSpecializedRenderer(
                    interaction(kind = kind, cardDefinitionId = definitionId),
                ),
            )
        }
    }

    private fun interaction(
        kind: InteractionKindId,
        cardDefinitionId: String = "custom-1",
    ) = PendingInteraction(
        id = "interaction",
        cardDefinitionId = cardDefinitionId,
        playerId = "player",
        kind = kind,
        title = "Title",
        fields = emptyList(),
        branches = emptyMap(),
    )
}
