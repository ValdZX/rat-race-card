package ua.vald_zx.game.rat.race.card.screen.board.deck

import ua.vald_zx.game.rat.race.card.shared.CardInteractionKinds
import ua.vald_zx.game.rat.race.card.shared.InteractionKindId
import ua.vald_zx.game.rat.race.card.shared.PendingInteraction
import ua.vald_zx.game.rat.race.card.shared.StandardInteractionKinds

internal class CardInteractionRendererRegistry(
    private val specializedKinds: Set<InteractionKindId>,
    private val legacyDefinitionPrefixes: Set<String>,
) {
    fun hasSpecializedRenderer(interaction: PendingInteraction): Boolean {
        if (interaction.kind in specializedKinds) return true
        if (interaction.kind !in legacyInteractionKinds) return false
        return legacyDefinitionPrefixes.any { prefix ->
            interaction.cardDefinitionId.startsWith("$prefix-")
        }
    }

    private companion object {
        val legacyInteractionKinds = setOf(
            StandardInteractionKinds.Purchase,
            StandardInteractionKinds.Choice,
        )
    }
}

internal val cardInteractionRendererRegistry = CardInteractionRendererRegistry(
    specializedKinds = setOf(
        CardInteractionKinds.BusinessPurchase,
        CardInteractionKinds.ShoppingPurchase,
        CardInteractionKinds.RandomJobConfirmation,
        CardInteractionKinds.ExpenseConfirmation,
    ),
    legacyDefinitionPrefixes = setOf(
        "smallbusiness",
        "mediumbusiness",
        "bigbusiness",
        "shopping",
        "chance",
        "expenses",
    ),
)
