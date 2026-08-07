package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.longOrNull

internal object RemoveAssetEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.RemoveAsset

    override fun validate(parameters: JsonObject): ValidationResult =
        parameters.requireEnum<AssetKind>("asset") + parameters.optionalEnum<AssetSelector>("selector")

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext {
        val asset = effect.parameters.enum<AssetKind>("asset")
        val selector = effect.parameters.enumOrNull<AssetSelector>("selector") ?: AssetSelector.RANDOM
        return context.updatePlayer { player ->
            when (asset) {
                AssetKind.BUSINESS -> {
                    val removable = player.businesses.filter { it.type != BusinessType.WORK }
                    val victims = context.select(removable, selector)
                    player.copy(businesses = player.businesses - victims.toSet())
                }

                AssetKind.SHARES -> player.copy(
                    sharesList = player.sharesList - context.select(player.sharesList, selector).toSet(),
                )

                AssetKind.LAND -> player.copy(
                    landList = player.landList - context.select(player.landList, selector).toSet(),
                )

                AssetKind.ESTATE -> player.copy(
                    estateList = player.estateList - context.select(player.estateList, selector).toSet(),
                )
            }
        }
    }
}

internal object ChangeRecurringIncomeEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.ChangeRecurringIncome

    override fun validate(parameters: JsonObject): ValidationResult = parameters.requireLong("amount")

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext {
        val amount = effect.parameters.long("amount")
        val onlyType = effect.parameters.enumOrNull<BusinessType>("businessType")
        return context.updatePlayer { player ->
            val target = player.businesses.firstOrNull { business ->
                business.type != BusinessType.WORK && (onlyType == null || business.type == onlyType)
            } ?: return@updatePlayer player
            player.copy(
                businesses = player.businesses.map { business ->
                    if (business === target) {
                        business.copy(extentions = business.extentions + amount)
                    } else {
                        business
                    }
                },
            )
        }
    }
}

internal object ChangeRecurringExpenseEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.ChangeRecurringExpense

    override fun validate(parameters: JsonObject): ValidationResult =
        parameters.requireEnum<PlayerResource>("resource") + parameters.requireLong("delta")

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext {
        val resource = effect.parameters.enum<PlayerResource>("resource")
        if (!resource.isDirectlySettable) return context
        val delta = effect.parameters.long("delta")
        return context.updatePlayer { it.withResource(resource, it.resource(resource) + delta) }
    }
}

internal object OfferPurchaseEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.OfferPurchase

    override fun validate(parameters: JsonObject): ValidationResult = parameters.requireInteraction()

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext =
        context.offerFrom(effect, StandardInteractionKinds.Purchase)
}

internal object OfferSaleEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.OfferSale

    override fun validate(parameters: JsonObject): ValidationResult = parameters.requireInteraction()

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext =
        context.offerFrom(effect, StandardInteractionKinds.Sell)
}

internal object RequirePlayerPredicateEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.RequirePlayerPredicate

    override fun validate(parameters: JsonObject): ValidationResult = parameters.requireEnum<PlayerPredicate>("predicate")

    override fun canApply(context: TurnContext, effect: EffectSpec, input: JsonObject): Boolean =
        context.player.matches(effect.parameters.enum<PlayerPredicate>("predicate"), context.board)

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext = context
}

internal object RequireResourceEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.RequireResource

    override fun validate(parameters: JsonObject): ValidationResult =
        parameters.requireEnum<PlayerResource>("resource") + parameters.requireNonNegativeLong("amount")

    override fun canApply(context: TurnContext, effect: EffectSpec, input: JsonObject): Boolean {
        val resource = effect.parameters.enum<PlayerResource>("resource")
        return context.player.resource(resource) >= effect.parameters.long("amount")
    }

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext = context
}

internal object SpendResourceEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.SpendResource

    override fun validate(parameters: JsonObject): ValidationResult =
        parameters.requireEnum<PlayerResource>("resource") + parameters.requireNonNegativeLong("amount")

    override fun canApply(context: TurnContext, effect: EffectSpec, input: JsonObject): Boolean {
        val resource = effect.parameters.enum<PlayerResource>("resource")
        return resource.isDirectlySettable && context.player.resource(resource) >= effect.parameters.long("amount")
    }

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext {
        val resource = effect.parameters.enum<PlayerResource>("resource")
        if (!resource.isDirectlySettable) return context
        val amount = effect.parameters.long("amount")
        return context.updatePlayer { it.withResource(resource, it.resource(resource) - amount) }
    }
}

internal object SetCounterEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.SetCounter

    override fun validate(parameters: JsonObject): ValidationResult =
        parameters.requireEnum<PlayerResource>("resource") + parameters.requireNonNegativeLong("value")

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext {
        val resource = effect.parameters.enum<PlayerResource>("resource")
        if (!resource.isDirectlySettable) return context
        return context.updatePlayer { it.withResource(resource, effect.parameters.long("value")) }
    }
}

internal object DrawCardEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.DrawCard

    override fun validate(parameters: JsonObject): ValidationResult = parameters.requireEnum<BoardCardType>("deck")

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext =
        context.openCards(listOf(effect.parameters.enum<BoardCardType>("deck")))
}

internal object StartAuctionEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.StartAuction

    override fun validate(parameters: JsonObject): ValidationResult = runCatching {
        Json.decodeFromJsonElement(Auction.serializer(), parameters)
    }.fold(
        onSuccess = { ValidationResult.Valid },
        onFailure = { ValidationResult.Invalid(listOf("auction is invalid")) },
    )

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext =
        context.startAuction(Json.decodeFromJsonElement(Auction.serializer(), effect.parameters))
}

internal object EmitNoticeEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.EmitNotice

    override fun validate(parameters: JsonObject): ValidationResult = parameters.requireNonNegativeLong("amount")

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext =
        context.emit(PresentationNotice.CashSubtracted(effect.parameters.long("amount")))
}

internal class ForEachEligiblePlayerEffectHandler(
    private val effects: () -> EffectHandlerRegistry,
) : EffectHandler {
    override val type = StandardEffectTypes.ForEachEligiblePlayer

    override fun validate(parameters: JsonObject): ValidationResult {
        val predicate = parameters.optionalEnum<PlayerPredicate>("predicate")
        val nested = parameters["effects"] ?: return predicate + ValidationResult.Invalid(listOf("effects is required"))
        return runCatching {
            Json.decodeFromJsonElement(EffectSpecListSerializer, nested)
        }.fold(
            onSuccess = { predicate },
            onFailure = { predicate + ValidationResult.Invalid(listOf("effects is invalid")) },
        )
    }

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext {
        val predicate = effect.parameters.enumOrNull<PlayerPredicate>("predicate")
        val nested = Json.decodeFromJsonElement(
            EffectSpecListSerializer,
            effect.parameters.getValue("effects"),
        )
        val includeSelf = (effect.parameters["includeSelf"] as? JsonPrimitive)?.content != "false"
        val origin = context.playerId
        val targets = context.board.activePlayers(context.snapshot.players)
            .filter { includeSelf || it.id != origin }
            .filter { predicate == null || it.matches(predicate, context.board) }
            .map { it.id }
        val applied = targets.fold(context) { current, targetId ->
            effects().execute(current.forPlayer(targetId), nested, input)
        }
        return applied.forPlayer(origin)
    }
}

private val EffectSpecListSerializer = kotlinx.serialization.builtins.ListSerializer(EffectSpec.serializer())

private fun TurnContext.offerFrom(effect: EffectSpec, fallbackKind: InteractionKindId): TurnContext {
    val spec = Json.decodeFromJsonElement(InteractionSpec.serializer(), effect.parameters)
    return offer(
        PendingInteraction(
            id = spec.id,
            cardDefinitionId = board.activeCardDefinitionId.orEmpty(),
            playerId = playerId,
            kind = spec.kind.takeIf { it.value.isNotBlank() } ?: fallbackKind,
            title = spec.title,
            fields = spec.fields,
            branches = spec.branches,
        ),
    )
}

private fun <T> TurnContext.select(items: List<T>, selector: AssetSelector): List<T> = when {
    items.isEmpty() -> emptyList()
    selector == AssetSelector.ALL -> items
    selector == AssetSelector.FIRST -> listOf(items.first())
    else -> listOfNotNull(random.choose(items))
}

private fun JsonObject.requireInteraction(): ValidationResult = runCatching {
    Json.decodeFromJsonElement(InteractionSpec.serializer(), this)
}.fold(
    onSuccess = { ValidationResult.Valid },
    onFailure = { ValidationResult.Invalid(listOf("interaction is invalid")) },
)

private inline fun <reified T : Enum<T>> JsonObject.enum(key: String): T =
    enumOrNull<T>(key) ?: error("$key is not a valid ${T::class.simpleName}")

private inline fun <reified T : Enum<T>> JsonObject.enumOrNull(key: String): T? {
    val raw = (get(key) as? JsonPrimitive)?.content ?: return null
    return enumValues<T>().firstOrNull { it.name == raw }
}

private inline fun <reified T : Enum<T>> JsonObject.requireEnum(key: String): ValidationResult =
    if (enumOrNull<T>(key) != null) {
        ValidationResult.Valid
    } else {
        ValidationResult.Invalid(listOf("$key must be one of ${enumValues<T>().joinToString { it.name }}"))
    }

private inline fun <reified T : Enum<T>> JsonObject.optionalEnum(key: String): ValidationResult =
    if (get(key) == null) ValidationResult.Valid else requireEnum<T>(key)

private fun JsonObject.long(key: String): Long = (get(key) as? JsonPrimitive)?.longOrNull
    ?: error("$key must be a number")

private fun JsonObject.requireLong(key: String): ValidationResult =
    if ((get(key) as? JsonPrimitive)?.longOrNull != null) {
        ValidationResult.Valid
    } else {
        ValidationResult.Invalid(listOf("$key must be a number"))
    }

private fun JsonObject.requireNonNegativeLong(key: String): ValidationResult {
    val value = (get(key) as? JsonPrimitive)?.longOrNull
    return when {
        value == null -> ValidationResult.Invalid(listOf("$key must be a number"))
        value < 0 -> ValidationResult.Invalid(listOf("$key must not be negative"))
        else -> ValidationResult.Valid
    }
}

internal operator fun ValidationResult.plus(other: ValidationResult): ValidationResult = when {
    this is ValidationResult.Invalid && other is ValidationResult.Invalid ->
        ValidationResult.Invalid(errors + other.errors)

    this is ValidationResult.Invalid -> this
    other is ValidationResult.Invalid -> other
    else -> ValidationResult.Valid
}
