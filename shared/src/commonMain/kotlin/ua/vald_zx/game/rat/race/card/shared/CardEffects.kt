package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.longOrNull

interface EffectHandler {
    val type: EffectTypeId

    fun validate(parameters: JsonObject): ValidationResult = ValidationResult.Valid

    fun canApply(context: TurnContext, effect: EffectSpec, input: JsonObject): Boolean = true

    fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext
}

class EffectHandlerRegistry(handlers: Iterable<EffectHandler>) {
    private val handlersByType: Map<EffectTypeId, EffectHandler>

    init {
        val grouped = handlers.groupBy(EffectHandler::type)
        val duplicateTypes = grouped.filterValues { it.size > 1 }.keys
        require(duplicateTypes.isEmpty()) { "Duplicate effect handlers: ${duplicateTypes.joinToString()}" }
        handlersByType = grouped.mapValues { it.value.single() }
    }

    fun validate(effects: Iterable<EffectSpec>): ValidationResult {
        val errors = effects.flatMap { effect ->
            val handler = handlersByType[effect.type]
                ?: return@flatMap listOf("Effect handler is not registered: ${effect.type.value}")
            when (val validation = handler.validate(effect.parameters)) {
                ValidationResult.Valid -> emptyList()
                is ValidationResult.Invalid -> validation.errors
            }
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    fun execute(context: TurnContext, effects: List<EffectSpec>, input: JsonObject): TurnContext {
        return effects.fold(context) { current, effect ->
            val handler = handlersByType[effect.type]
                ?: error("Effect handler is not registered: ${effect.type.value}")
            handler.apply(current, effect, input)
        }
    }

    fun canApply(context: TurnContext, effects: List<EffectSpec>, input: JsonObject): Boolean {
        return effects.all { effect ->
            handlersByType[effect.type]?.canApply(context, effect, input) == true
        }
    }

    operator fun plus(handler: EffectHandler): EffectHandlerRegistry =
        EffectHandlerRegistry(handlersByType.values + handler)

    val registeredTypes: Set<EffectTypeId>
        get() = handlersByType.keys
}

fun standardEffectHandlerRegistry(): EffectHandlerRegistry = EffectHandlerRegistry(standardEffectHandlers())

internal fun standardEffectHandlers(): List<EffectHandler> {
    lateinit var registry: EffectHandlerRegistry
    val handlers = listOf(
        ChangeCashEffectHandler,
        PayAmountEffectHandler,
        AcquireBusinessEffectHandler,
        AcquireShoppingEffectHandler,
        PayExpenseEffectHandler,
        EndTurnEffectHandler,
        RemoveAssetEffectHandler,
        ChangeRecurringIncomeEffectHandler,
        ChangeRecurringExpenseEffectHandler,
        OfferPurchaseEffectHandler,
        OfferSaleEffectHandler,
        RequirePlayerPredicateEffectHandler,
        RequireResourceEffectHandler,
        SpendResourceEffectHandler,
        SetCounterEffectHandler,
        DrawCardEffectHandler,
        StartAuctionEffectHandler,
        EmitNoticeEffectHandler,
        ForEachEligiblePlayerEffectHandler { registry },
    )
    registry = EffectHandlerRegistry(handlers)
    return handlers
}

class CardDefinitionEngine(
    private val effects: EffectHandlerRegistry,
) {
    fun validate(definition: CardDefinition): ValidationResult {
        val structuralErrors = buildList {
            if (definition.id.isBlank()) add("Card id must not be blank")
            if (definition.interactions.map(InteractionSpec::id).distinct().size != definition.interactions.size) {
                add("Interaction ids must be unique")
            }
            definition.interactions.forEach { interaction ->
                if (interaction.id.isBlank()) add("Interaction id must not be blank")
                if (interaction.fields.isEmpty()) add("${interaction.id}: fields must not be empty")
                if (interaction.branches.isEmpty()) add("${interaction.id}: branches must not be empty")
                interaction.fields.forEach { field ->
                    if (field.id.isBlank()) add("${interaction.id}: field id must not be blank")
                    if (field.type != InteractionFieldType.AMOUNT && field.options.isEmpty()) {
                        add("${interaction.id}/${field.id}: options must not be empty")
                    }
                }
            }
        }
        val allEffects = definition.effects + definition.interactions.flatMap { interaction ->
            interaction.branches.values.flatten()
        }
        val effectErrors = when (val validation = effects.validate(allEffects)) {
            ValidationResult.Valid -> emptyList()
            is ValidationResult.Invalid -> validation.errors
        }
        val errors = structuralErrors + effectErrors
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    fun start(
        snapshot: GameSnapshot,
        playerId: String,
        definition: CardDefinition,
        random: GameRandom,
        moneyService: MoneyService,
    ): CardResolution {
        val player = snapshot.players.first { it.id == playerId }
        if (!definition.availability.matches(player)) return CardResolution.NotAvailable
        if (validate(definition) is ValidationResult.Invalid) return CardResolution.InvalidDefinition

        val base = TurnContext(
            result = RuleResult(snapshot),
            playerId = playerId,
            cellIndex = player.location.position,
            isLanding = true,
            random = random,
            moneyService = moneyService,
        )
        val applied = effects.execute(base, definition.effects, JsonObject(emptyMap()))
        if (applied.result.turnDirective == TurnDirective.END_TURN) {
            return CardResolution.Applied(applied.result)
        }
        val pending = definition.interactions.map { interaction ->
            PendingInteraction(
                id = interaction.id,
                cardDefinitionId = definition.id,
                playerId = playerId,
                kind = interaction.kind,
                title = interaction.title,
                description = definition.presentation.description,
                fields = interaction.fields,
                branches = interaction.branches,
            )
        }
        return CardResolution.Applied(
            applied.result.copy(
                snapshot = applied.snapshot.copy(
                    board = applied.board.copy(
                        activeCardDefinitionId = definition.id,
                        pendingInteractions = pending,
                    ),
                ),
            ),
        )
    }

    fun choose(
        snapshot: GameSnapshot,
        playerId: String,
        interactionId: String,
        input: JsonObject,
        random: GameRandom,
        moneyService: MoneyService,
    ): CardResolution {
        val interaction = snapshot.board.pendingInteractions.firstOrNull { it.id == interactionId }
            ?: return CardResolution.InteractionNotFound
        if (interaction.playerId != playerId) return CardResolution.InteractionNotOwned
        if (!interaction.fields.all { it.accepts(input) }) return CardResolution.InvalidInput
        val branchKey = (input[CHOICE_INPUT] as? JsonPrimitive)?.content
            ?: interaction.fields.asSequence()
                .filter { it.type != InteractionFieldType.AMOUNT }
                .mapNotNull { field -> (input[field.id] as? JsonPrimitive)?.content }
                .firstOrNull(interaction.branches::containsKey)
            ?: interaction.branches.keys.singleOrNull()
            ?: return CardResolution.InvalidInput
        val branch = interaction.branches[branchKey] ?: return CardResolution.InvalidInput
        if (effects.validate(branch) is ValidationResult.Invalid) return CardResolution.InvalidDefinition

        val unresolvedContext = TurnContext(
            result = RuleResult(snapshot),
            playerId = playerId,
            cellIndex = snapshot.players.first { it.id == playerId }.location.position,
            isLanding = true,
            random = random,
            moneyService = moneyService,
        )
        if (!effects.canApply(unresolvedContext, branch, input)) return CardResolution.NotAvailable

        val remaining = snapshot.board.pendingInteractions.filterNot { it.id == interactionId }
        val prepared = snapshot.copy(
            board = snapshot.board.copy(
                pendingInteractions = remaining,
                activeCardDefinitionId = snapshot.board.activeCardDefinitionId.takeIf { remaining.isNotEmpty() },
            ),
        )
        val context = TurnContext(
            result = RuleResult(prepared),
            playerId = playerId,
            cellIndex = snapshot.players.first { it.id == playerId }.location.position,
            isLanding = true,
            random = random,
            moneyService = moneyService,
        )
        return CardResolution.Applied(effects.execute(context, branch, input).result)
    }
}

sealed interface CardResolution {
    data class Applied(val result: RuleResult) : CardResolution
    data object NotAvailable : CardResolution
    data object InvalidDefinition : CardResolution
    data object InteractionNotFound : CardResolution
    data object InteractionNotOwned : CardResolution
    data object InvalidInput : CardResolution
}

private object ChangeCashEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.ChangeCash

    override fun validate(parameters: JsonObject): ValidationResult = parameters.requireLong("amount")

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext {
        val amount = effect.parameters.long("amount")
        return context.updatePlayer { it.copy(cash = it.cash + amount) }
    }
}

private object PayAmountEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.PayAmount

    override fun validate(parameters: JsonObject): ValidationResult = parameters.requireNonNegativeLong("amount")

    override fun canApply(context: TurnContext, effect: EffectSpec, input: JsonObject): Boolean {
        return context.player.financialAccount().canAffordVoluntaryPurchase(
            price = effect.parameters.long("amount"),
            loanLimit = context.board.loanLimit,
            useFunds = context.player.config.hasFunds,
        )
    }

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext {
        return context.pay(effect.parameters.long("amount"))
    }
}

private object AcquireBusinessEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.AcquireBusiness

    override fun validate(parameters: JsonObject): ValidationResult = runCatching {
        Json.decodeFromJsonElement(Business.serializer(), parameters)
    }.fold(
        onSuccess = { ValidationResult.Valid },
        onFailure = { ValidationResult.Invalid(listOf("business is invalid")) },
    )

    override fun canApply(context: TurnContext, effect: EffectSpec, input: JsonObject): Boolean {
        return context.player.businesses.size.toLong() <= context.board.businessLimit
    }

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext {
        val business = Json.decodeFromJsonElement(Business.serializer(), effect.parameters)
        return context.updatePlayer { player ->
            val businesses = player.businesses
            when {
                business.type == BusinessType.SMALL &&
                        businesses.any { it.type == BusinessType.WORK } &&
                        businesses.count { it.type == BusinessType.SMALL } == 1 ->
                    player.copy(businesses = businesses.filter { it.type != BusinessType.WORK } + business)

                businesses.isNotEmpty() &&
                        businesses.first().type.klass != business.type.klass &&
                        businesses.none { it.type == BusinessType.WORK } ->
                    player.copy(
                        cash = player.cash + businesses.sumOf(Business::price),
                        businesses = listOf(business),
                    )

                else -> player.copy(businesses = businesses + business)
            }
        }
    }
}

private object AcquireShoppingEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.AcquireShopping

    override fun validate(parameters: JsonObject): ValidationResult {
        val type = parameters.stringOrNull("shopType")
        return if (type != null && ShopType.entries.any { it.name == type }) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(listOf("shopType is invalid"))
        }
    }

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext {
        val shopType = ShopType.valueOf(effect.parameters.string("shopType"))
        return context.updatePlayer { player ->
            when (shopType) {
                ShopType.AUTO -> player.copy(cars = player.cars + 1)
                ShopType.HOUSE -> player.copy(cottage = player.cottage + 1)
                ShopType.APARTMENT -> player.copy(apartment = player.apartment + 1)
                ShopType.YACHT -> player.copy(yacht = player.yacht + 1)
                ShopType.FLY -> player.copy(flight = player.flight + 1)
                ShopType.ANIMAL -> player.copy(animal = player.animal + 1)
            }
        }
    }
}

private object PayExpenseEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.PayExpense

    override fun validate(parameters: JsonObject): ValidationResult {
        val amount = parameters.requireNonNegativeLong("amount")
        val payer = parameters.stringOrNull("payer")
        val payerIsValid = payer != null && PayerType.entries.any { it.name == payer }
        return when {
            amount is ValidationResult.Invalid -> amount
            !payerIsValid -> ValidationResult.Invalid(listOf("payer is invalid"))
            else -> ValidationResult.Valid
        }
    }

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext {
        val payer = PayerType.valueOf(effect.parameters.string("payer"))
        if (!context.player.mustPay(payer)) return context
        val paid = context.pay(effect.parameters.long("amount"))
        val grantsAnimal = (effect.parameters["grantsAnimal"] as? JsonPrimitive)?.booleanOrNull == true
        return if (grantsAnimal) paid.updatePlayer { it.copy(animal = it.animal + 1) } else paid
    }
}

private object EndTurnEffectHandler : EffectHandler {
    override val type = StandardEffectTypes.EndTurn

    override fun apply(context: TurnContext, effect: EffectSpec, input: JsonObject): TurnContext = context.endTurn()
}

private fun AvailabilityRule.matches(player: Player): Boolean = when (this) {
    AvailabilityRule.Always -> true
    is AvailabilityRule.MinimumCash -> player.cash >= amount
    is AvailabilityRule.MinimumDeputies -> player.deputies >= count
}

private fun InteractionField.accepts(input: JsonObject): Boolean {
    val value = input[id] as? JsonPrimitive ?: return false
    return when (type) {
        InteractionFieldType.CHOICE,
        InteractionFieldType.CONFIRMATION -> value.content in options.map(InteractionOption::value)

        InteractionFieldType.AMOUNT -> value.longOrNull?.let { amount ->
            amount >= (minimum ?: Long.MIN_VALUE) && amount <= (maximum ?: Long.MAX_VALUE)
        } == true
    }
}

private fun Player.mustPay(payer: PayerType): Boolean = when (payer) {
    PayerType.ALL -> true
    PayerType.MEN -> card.gender == Gender.MALE
    PayerType.AUTO_OWNER -> cars > 0
    PayerType.PARENT -> babies > 0
    PayerType.MARRIED_M -> card.gender == Gender.MALE && isMarried
    PayerType.APARTMENT_OWNER -> apartment > 0
    PayerType.APARTMENT_OR_HOUSE_OWNER -> apartment > 0 || cottage > 0
    PayerType.ANIMAL_OWNER -> animal > 0
    PayerType.FREE_W_OR_MARRIED_M -> card.gender == Gender.FEMALE && !isMarried ||
            card.gender == Gender.MALE && isMarried
}

private fun JsonObject.long(key: String): Long = (get(key) as? JsonPrimitive)?.longOrNull
    ?: error("$key must be a number")

private fun JsonObject.string(key: String): String = (get(key) as? JsonPrimitive)?.content
    ?: error("$key must be a string")

private fun JsonObject.stringOrNull(key: String): String? = (get(key) as? JsonPrimitive)?.content

private fun JsonObject.requireLong(key: String): ValidationResult =
    if ((get(key) as? JsonPrimitive)?.longOrNull != null) {
        ValidationResult.Valid
    } else {
        ValidationResult.Invalid(listOf("$key must be a number"))
    }

private fun JsonObject.requireNonNegativeLong(key: String): ValidationResult {
    val value = (get(key) as? JsonPrimitive)?.longOrNull
    return if (value != null && value >= 0) {
        ValidationResult.Valid
    } else {
        ValidationResult.Invalid(listOf("$key must be a non-negative number"))
    }
}
