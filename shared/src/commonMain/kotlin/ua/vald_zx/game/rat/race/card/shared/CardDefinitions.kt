package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
value class DeckId(val value: String)

@JvmInline
@Serializable
value class CardKindId(val value: String)

@JvmInline
@Serializable
value class EffectTypeId(val value: String)

@JvmInline
@Serializable
value class InteractionKindId(val value: String)

@Serializable
data class CardPresentation(
    val title: String,
    val description: String,
    val imageKey: String? = null,
    val soundId: String? = null,
)

@Serializable
sealed interface AvailabilityRule {
    @Serializable
    @SerialName("always")
    data object Always : AvailabilityRule

    @Serializable
    @SerialName("minimumCash")
    data class MinimumCash(val amount: Long) : AvailabilityRule

    @Serializable
    @SerialName("minimumDeputies")
    data class MinimumDeputies(val count: Int) : AvailabilityRule
}

@Serializable
data class EffectSpec(
    val type: EffectTypeId,
    val parameters: JsonObject = JsonObject(emptyMap()),
)

@Serializable
enum class InteractionFieldType {
    AMOUNT,
    CHOICE,
    CONFIRMATION,
}

@Serializable
data class InteractionOption(
    val value: String,
    val label: String,
)

@Serializable
data class InteractionField(
    val id: String,
    val type: InteractionFieldType,
    val label: String,
    val minimum: Long? = null,
    val maximum: Long? = null,
    val quickValues: List<Long> = emptyList(),
    val options: List<InteractionOption> = emptyList(),
)

@Serializable
data class InteractionSpec(
    val id: String,
    val kind: InteractionKindId,
    val title: String,
    val fields: List<InteractionField>,
    val branches: Map<String, List<EffectSpec>>,
)

@Serializable
data class CardDefinition(
    val id: String,
    val deckId: DeckId,
    val kind: CardKindId,
    val availability: AvailabilityRule = AvailabilityRule.Always,
    val presentation: CardPresentation,
    val interactions: List<InteractionSpec> = emptyList(),
    val effects: List<EffectSpec> = emptyList(),
)

@Serializable
data class PendingInteraction(
    val id: String,
    val cardDefinitionId: String,
    val playerId: String,
    val kind: InteractionKindId,
    val title: String,
    val description: String = "",
    val fields: List<InteractionField>,
    val branches: Map<String, List<EffectSpec>>,
)

object StandardInteractionKinds {
    val Purchase = InteractionKindId("standard.purchase")
    val Sell = InteractionKindId("standard.sell")
    val Amount = InteractionKindId("standard.amount")
    val Choice = InteractionKindId("standard.choice")
}

object CardInteractionKinds {
    val BusinessPurchase = InteractionKindId("card.business.purchase")
    val ShoppingPurchase = InteractionKindId("card.shopping.purchase")
    val RandomJobConfirmation = InteractionKindId("card.random_job.confirmation")
    val ExpenseConfirmation = InteractionKindId("card.expense.confirmation")
}

object StandardEffectTypes {
    val ChangeCash = EffectTypeId("core.change_cash")
    val PayAmount = EffectTypeId("core.pay_amount")
    val AcquireBusiness = EffectTypeId("core.acquire_business")
    val AcquireShopping = EffectTypeId("core.acquire_shopping")
    val PayExpense = EffectTypeId("core.pay_expense")
    val RemoveAsset = EffectTypeId("core.remove_asset")
    val ChangeRecurringIncome = EffectTypeId("core.change_recurring_income")
    val ChangeRecurringExpense = EffectTypeId("core.change_recurring_expense")
    val OfferPurchase = EffectTypeId("core.offer_purchase")
    val OfferSale = EffectTypeId("core.offer_sale")
    val RequirePlayerPredicate = EffectTypeId("core.require_player_predicate")
    val RequireResource = EffectTypeId("core.require_resource")
    val SpendResource = EffectTypeId("core.spend_resource")
    val DrawCard = EffectTypeId("core.draw_card")
    val StartAuction = EffectTypeId("core.start_auction")
    val ForEachEligiblePlayer = EffectTypeId("core.for_each_eligible_player")
    val SetCounter = EffectTypeId("core.set_counter")
    val EmitNotice = EffectTypeId("core.emit_notice")
    val EndTurn = EffectTypeId("core.end_turn")
}

fun BoardCard.toCardDefinition(link: CardLink): CardDefinition? = when (this) {
    is BoardCard.SmallBusiness -> businessDefinition(link, BusinessType.SMALL, name, description, price, profit)
    is BoardCard.MediumBusiness -> businessDefinition(link, BusinessType.MEDIUM, name, description, price, profit)
    is BoardCard.BigBusiness -> businessDefinition(link, BusinessType.LARGE, name, description, price, profit)
    is BoardCard.Shopping -> choiceDefinition(
        link = link,
        kind = CardKindId("core.shopping"),
        interactionKind = CardInteractionKinds.ShoppingPurchase,
        title = name,
        description = description,
        acceptEffects = listOf(
            EffectSpec(StandardEffectTypes.PayAmount, buildJsonObject { put("amount", price) }),
            EffectSpec(StandardEffectTypes.AcquireShopping, buildJsonObject { put("shopType", shopType.name) }),
            EffectSpec(StandardEffectTypes.EndTurn),
        ),
    )

    is BoardCard.Chance.RandomJob -> choiceDefinition(
        link = link,
        kind = CardKindId("core.random_job"),
        interactionKind = CardInteractionKinds.RandomJobConfirmation,
        title = name,
        description = description,
        acceptEffects = listOf(
            EffectSpec(StandardEffectTypes.ChangeCash, buildJsonObject { put("amount", profit) }),
            EffectSpec(StandardEffectTypes.EndTurn),
        ),
    )

    is BoardCard.Expenses -> CardDefinition(
        id = link.definitionId(),
        deckId = link.type.deckId(),
        kind = CardKindId("core.expense"),
        presentation = CardPresentation(name, description),
        interactions = listOf(
            InteractionSpec(
                id = "${link.definitionId()}:pay",
                kind = CardInteractionKinds.ExpenseConfirmation,
                title = priceTitle,
                fields = listOf(
                    InteractionField(
                        id = CHOICE_INPUT,
                        type = InteractionFieldType.CHOICE,
                        label = priceTitle,
                        options = listOf(InteractionOption(ACCEPT_CHOICE, price.toString())),
                    ),
                ),
                branches = mapOf(
                    ACCEPT_CHOICE to listOf(
                        EffectSpec(
                            StandardEffectTypes.PayExpense,
                            buildJsonObject {
                                put("amount", price)
                                put("payer", payer.name)
                                put("grantsAnimal", grantsAnimal)
                            },
                        ),
                        EffectSpec(StandardEffectTypes.EndTurn),
                    ),
                ),
            ),
        ),
    )

    else -> null
}

private fun businessDefinition(
    link: CardLink,
    type: BusinessType,
    name: String,
    description: String,
    price: Long,
    profit: Long,
): CardDefinition {
    val business = Business(type, name, price, profit)
    return choiceDefinition(
        link = link,
        kind = CardKindId("core.business"),
        interactionKind = CardInteractionKinds.BusinessPurchase,
        title = name,
        description = description,
        acceptEffects = listOf(
            EffectSpec(StandardEffectTypes.PayAmount, buildJsonObject { put("amount", price) }),
            EffectSpec(
                StandardEffectTypes.AcquireBusiness,
                Json.parseToJsonElement(Json.encodeToString(business)) as JsonObject,
            ),
            EffectSpec(StandardEffectTypes.EndTurn),
        ),
    )
}

private fun choiceDefinition(
    link: CardLink,
    kind: CardKindId,
    interactionKind: InteractionKindId,
    title: String,
    description: String,
    acceptEffects: List<EffectSpec>,
): CardDefinition = CardDefinition(
    id = link.definitionId(),
    deckId = link.type.deckId(),
    kind = kind,
    presentation = CardPresentation(title, description),
    interactions = listOf(
        InteractionSpec(
            id = "${link.definitionId()}:choice",
            kind = interactionKind,
            title = title,
            fields = listOf(
                InteractionField(
                    id = CHOICE_INPUT,
                    type = InteractionFieldType.CHOICE,
                    label = title,
                    options = listOf(
                        InteractionOption(ACCEPT_CHOICE, "Accept"),
                        InteractionOption(DECLINE_CHOICE, "Skip"),
                    ),
                ),
            ),
            branches = mapOf(
                ACCEPT_CHOICE to acceptEffects,
                DECLINE_CHOICE to listOf(EffectSpec(StandardEffectTypes.EndTurn)),
            ),
        ),
    ),
)

fun BoardCardType.deckId(): DeckId = DeckId("legacy.${name.lowercase()}")

private fun CardLink.definitionId(): String = "${type.name.lowercase()}-$id"

const val CHOICE_INPUT = "choice"
const val ACCEPT_CHOICE = "accept"
const val DECLINE_CHOICE = "decline"
