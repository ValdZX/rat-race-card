package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val errors: List<String>) : ValidationResult
}

interface CellRule {
    val type: CellTypeId

    fun validate(parameters: JsonObject): ValidationResult = ValidationResult.Valid

    fun onPass(context: TurnContext, cell: CellInstance): RuleResult = context.result

    fun onLand(context: TurnContext, cell: CellInstance): RuleResult = context.result
}

class CellRuleRegistry(rules: Iterable<CellRule>) {
    private val rulesByType: Map<CellTypeId, CellRule>

    init {
        val grouped = rules.groupBy(CellRule::type)
        val duplicateTypes = grouped.filterValues { it.size > 1 }.keys
        require(duplicateTypes.isEmpty()) { "Duplicate cell rules: ${duplicateTypes.joinToString()}" }
        rulesByType = grouped.mapValues { it.value.single() }
    }

    fun rule(type: CellTypeId): CellRule = rulesByType[type]
        ?: error("Cell rule is not registered: $type")

    fun validate(cells: Iterable<CellInstance>): ValidationResult {
        val errors = cells.flatMap { cell ->
            val rule = rulesByType[cell.type]
                ?: return@flatMap listOf("${cell.id}: rule ${cell.type} is not registered")
            when (val result = rule.validate(cell.parameters)) {
                ValidationResult.Valid -> emptyList()
                is ValidationResult.Invalid -> result.errors.map { "${cell.id}: $it" }
            }
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    operator fun plus(rule: CellRule): CellRuleRegistry = CellRuleRegistry(rulesByType.values + rule)

    val registeredTypes: Set<CellTypeId>
        get() = rulesByType.keys
}

data class TurnContext(
    val result: RuleResult,
    val playerId: String,
    val cellIndex: Int,
    val isLanding: Boolean,
    val random: GameRandom,
    val moneyService: MoneyService,
) {
    val snapshot: GameSnapshot
        get() = result.snapshot

    val board: Board
        get() = snapshot.board

    val player: Player
        get() = snapshot.players.first { it.id == playerId }

    fun updatePlayer(transform: (Player) -> Player): TurnContext {
        val previous = player
        val changed = transform(previous)
        if (previous == changed) return this
        val updated = changed.withTrackedFinancialChanges(previous)
        return copy(
            result = result.copy(
                snapshot = snapshot.copy(
                    players = snapshot.players.map { if (it.id == updated.id) updated else it },
                ),
                events = result.events + DomainEvent.PlayerChanged(updated),
            ),
        )
    }

    fun pay(amount: Long): TurnContext {
        if (amount == 0L) return this
        val payer = player
        val payment = moneyService.pay(
            account = payer.financialAccount(),
            amount = amount,
            policy = PaymentPolicy(
                useFunds = payer.config.hasFunds,
                loanLimit = board.loanLimit,
            ),
        )
        val usedFunds = payment.events.any { it is PaymentEvent.FundsWithdrawn }
        val paymentNotices = buildList {
            add(PresentationNotice.CashSubtracted(amount))
            payment.events.forEach { event ->
                when (event) {
                    is PaymentEvent.DepositWithdrawn -> if (!usedFunds && payment.account.loan == payer.loan) {
                        add(PresentationNotice.DepositWithdrawn(event.amount))
                    }

                    is PaymentEvent.LoanAdded -> if (!usedFunds) add(PresentationNotice.LoanAdded(event.amount))
                    PaymentEvent.LoanLimitExceeded -> add(PresentationNotice.LoanLimitExceeded)
                    is PaymentEvent.FundsWithdrawn -> Unit
                }
            }
        }
        return updatePlayer { it.withFinancialAccount(payment.account) }.let { updated ->
            updated.copy(
                result = updated.result.copy(
                    events = updated.result.events + DomainEvent.PaymentApplied(playerId, amount, payment.events),
                    notices = updated.result.notices + paymentNotices,
                ),
            )
        }
    }

    fun openCards(types: List<BoardCardType>): TurnContext = copy(
        result = result.copy(
            snapshot = snapshot.copy(board = board.copy(canTakeCard = types)),
            events = result.events + DomainEvent.CardOptionsOpened(types),
        ),
    )

    fun emit(notice: PresentationNotice?): TurnContext {
        return if (notice == null) this else copy(result = result.copy(notices = result.notices + notice))
    }

    fun endTurn(): TurnContext = copy(result = result.copy(turnDirective = TurnDirective.END_TURN))
}

fun Player.withTrackedFinancialChanges(previous: Player): Player = copy(
    lastTotals = appendRecentChange(lastTotals, previous.total(), total()),
    lastCashFlows = appendRecentChange(lastCashFlows, previous.cashFlow(), cashFlow()),
    lastLoans = appendRecentChange(lastLoans, previous.loan, loan),
)

fun legacyCellRuleRegistry(): CellRuleRegistry = CellRuleRegistry(
    listOf(
        StartCellRule,
        SalaryCellRule,
        CardCellRule(CoreCellTypes.Business, ::businessCardOptions),
        CardCellRule(CoreCellTypes.BigBusiness) { listOf(BoardCardType.BigBusiness) },
        CardCellRule(CoreCellTypes.Shopping) { listOf(BoardCardType.Shopping) },
        CardCellRule(CoreCellTypes.Chance) { listOf(BoardCardType.Chance) },
        CardCellRule(CoreCellTypes.Expenses) { listOf(BoardCardType.Expenses) },
        CardCellRule(CoreCellTypes.Store) { listOf(BoardCardType.EventStore) },
        CardCellRule(CoreCellTypes.Deputy) { listOf(BoardCardType.Deputy) },
        BankruptcyCellRule,
        ChildCellRule,
        LoveCellRule,
        RestCellRule,
        DivorceCellRule,
        DesireCellRule,
        TaxInspectionCellRule,
        ResignationCellRule,
    ),
)

private object StartCellRule : CellRule {
    override val type = CoreCellTypes.Start

    override fun onPass(context: TurnContext, cell: CellInstance): RuleResult {
        return context.updatePlayer { player ->
            if (player.funds.isEmpty()) player else player.copy(
                startCapitalization = StartCapitalization(
                    position = context.cellIndex,
                    landed = context.isLanding,
                ),
            )
        }.result
    }

    override fun onLand(context: TurnContext, cell: CellInstance): RuleResult = context.endTurn().result
}

private object SalaryCellRule : CellRule {
    override val type = CoreCellTypes.Salary

    override fun onPass(context: TurnContext, cell: CellInstance): RuleResult {
        val cashFlow = context.player.cashFlow()
        return if (cashFlow > 0) {
            context.updatePlayer { it.copy(salaryPosition = context.cellIndex) }.result
        } else {
            context.pay(kotlin.math.abs(cashFlow)).updatePlayer { it.copy(salaryPosition = null) }.result
        }
    }

    override fun onLand(context: TurnContext, cell: CellInstance): RuleResult {
        return context.updatePlayer { it.copy(investmentPosition = context.cellIndex) }.endTurn().result
    }
}

private class CardCellRule(
    override val type: CellTypeId,
    private val options: (Player) -> List<BoardCardType>,
) : CellRule {
    override fun onLand(context: TurnContext, cell: CellInstance): RuleResult {
        return context.openCards(options(context.player)).result
    }
}

private object BankruptcyCellRule : CellRule {
    override val type = CoreCellTypes.Bankruptcy

    override fun onLand(context: TurnContext, cell: CellInstance): RuleResult {
        val business = context.random.choose(context.player.businesses.filter { it.type != BusinessType.WORK })
        return context.updatePlayer { player ->
            business?.let { player.copy(businesses = player.businesses - it) } ?: player
        }.emit(business?.let(PresentationNotice::BankruptBusiness)).endTurn().result
    }
}

private object ChildCellRule : CellRule {
    override val type = CoreCellTypes.Child

    override fun onLand(context: TurnContext, cell: CellInstance): RuleResult {
        val player = context.player
        val eligible = player.card.gender == Gender.FEMALE || player.card.gender == Gender.MALE && player.isMarried
        return context.updatePlayer {
            if (!eligible) it else it.copy(
                babies = it.babies + 1,
                cash = it.cash + it.config.childBenefit,
            )
        }.let { updated ->
            updated.emit(
                if (eligible) PresentationNotice.PlayerHadBaby(updated.player.id, updated.player.babies) else null,
            )
        }.endTurn().result
    }
}

private object DivorceCellRule : CellRule {
    override val type = CoreCellTypes.Divorce

    override fun onLand(context: TurnContext, cell: CellInstance): RuleResult {
        val married = context.player.isMarried
        return context.updatePlayer(Player::afterDivorce)
            .emit(if (married) PresentationNotice.PlayerDivorced(context.playerId) else null)
            .endTurn().result
    }
}

private object ResignationCellRule : CellRule {
    override val type = CoreCellTypes.Resignation

    override fun onLand(context: TurnContext, cell: CellInstance): RuleResult {
        val work = context.player.businesses.firstOrNull { it.type == BusinessType.WORK }
        return context.updatePlayer { player ->
            work?.let { player.copy(businesses = player.businesses - it) } ?: player
        }.emit(work?.let(PresentationNotice::Resignation)).endTurn().result
    }
}

private object LoveCellRule : CellRule {
    override val type = CoreCellTypes.Love

    override fun onLand(context: TurnContext, cell: CellInstance): RuleResult {
        val married = !context.player.isMarried
        val gender = context.player.card.gender
        val marriageCost = context.player.config.marriageCost
        val updated = context.updatePlayer { if (married) it.copy(isMarried = true) else it }
            .emit(if (married) PresentationNotice.PlayerMarried(context.playerId) else null)
        return if (married && gender == Gender.MALE) updated.pay(marriageCost).endTurn().result
        else updated.endTurn().result
    }
}

private object RestCellRule : CellRule {
    override val type = CoreCellTypes.Rest

    override fun onLand(context: TurnContext, cell: CellInstance): RuleResult {
        return context.updatePlayer { it.copy(inRest = it.config.restTurnCount) }.endTurn().result
    }
}

private object DesireCellRule : CellRule {
    override val type = CoreCellTypes.Desire

    override fun validate(parameters: JsonObject): ValidationResult {
        return if ((parameters[DREAM_ID_PARAMETER] as? JsonPrimitive)?.content.orEmpty().isNotBlank()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(listOf("$DREAM_ID_PARAMETER is required"))
        }
    }

    override fun onLand(context: TurnContext, cell: CellInstance): RuleResult {
        val dreamId = (cell.parameters[DREAM_ID_PARAMETER] as? JsonPrimitive)?.content
        val dream = context.board.dreamById(dreamId)
        return if (dream != null && dream.id !in context.board.purchasedDreamIds) {
            context.emit(PresentationNotice.DreamOffered).result
        } else context.endTurn().result
    }
}

private object TaxInspectionCellRule : CellRule {
    override val type = CoreCellTypes.TaxInspection

    override fun onLand(context: TurnContext, cell: CellInstance): RuleResult {
        val bribe = context.player.taxInspectionBribe(context.board)
        val paid = if (bribe > 0) context.pay(bribe) else context
        return paid.emit(bribe.takeIf { it > 0 }?.let(PresentationNotice::TaxInspectionPaid)).endTurn().result
    }
}

private fun businessCardOptions(player: Player): List<BoardCardType> = when {
    player.businesses.any { it.type == BusinessType.LARGE } -> listOf(BoardCardType.BigBusiness)
    player.businesses.any { it.type == BusinessType.MEDIUM } ->
        listOf(BoardCardType.BigBusiness, BoardCardType.MediumBusiness)

    player.businesses.any { it.type == BusinessType.SMALL } ->
        listOf(BoardCardType.SmallBusiness, BoardCardType.MediumBusiness)

    else -> listOf(BoardCardType.SmallBusiness)
}

private fun Player.afterDivorce(): Player {
    if (!isMarried) return this
    if (card.gender == Gender.FEMALE) return copy(isMarried = false)
    val retained = config.divorceAssetRetentionPercentage
    return copy(
        isMarried = false,
        babies = 0,
        cash = cash * retained / 100,
        deposit = deposit * retained / 100,
    )
}

data class CellPresentation(
    val type: CellTypeId,
    val titleKey: String,
    val iconKey: String,
    val colorToken: String,
    val soundId: String? = null,
)

class CellPresentationRegistry(presentations: Iterable<CellPresentation>) {
    private val presentationsByType: Map<CellTypeId, CellPresentation>

    init {
        val presentationList = presentations.toList()
        presentationsByType = presentationList.associateBy(CellPresentation::type).also { mapped ->
            require(mapped.size == presentationList.size) { "Duplicate cell presentations" }
        }
    }

    fun presentation(type: CellTypeId): CellPresentation = presentationsByType[type]
        ?: error("Cell presentation is not registered: $type")

    operator fun plus(presentation: CellPresentation): CellPresentationRegistry =
        CellPresentationRegistry(presentationsByType.values + presentation)
}

val legacyCellPresentationRegistry = CellPresentationRegistry(
    CoreCellTypes.all.map { type ->
        CellPresentation(
            type = type,
            titleKey = type.value,
            iconKey = "cell_${type.value.substringAfter('.')}",
            colorToken = type.value,
            soundId = if (type == CoreCellTypes.Salary) "cash" else null,
        )
    },
)
