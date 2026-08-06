package ua.vald_zx.game.rat.race.card.shared

import kotlin.math.absoluteValue

class GameEngine(
    private val random: GameRandom = DefaultGameRandom,
    private val moneyService: MoneyService = sharedMoneyService,
) {
    fun execute(snapshot: GameSnapshot, envelope: GameCommandEnvelope): GameExecution {
        if (envelope.commandId.isBlank()) {
            return GameExecution.Rejected(snapshot, GameCommandRejection.EMPTY_COMMAND_ID)
        }
        if (snapshot.board.id != envelope.boardId) {
            return GameExecution.Rejected(snapshot, GameCommandRejection.BOARD_MISMATCH)
        }
        if (envelope.commandId in snapshot.board.processedCommandIds) {
            return GameExecution.Duplicate(snapshot)
        }
        val rejection = validate(snapshot, envelope)
        if (rejection != null) return GameExecution.Rejected(snapshot, rejection)

        val transition = when (envelope.command) {
            is GameCommand.RollDice -> rollDice(snapshot, envelope.playerId)
            GameCommand.CompleteRoll -> completeRoll(snapshot, envelope.playerId)
            is GameCommand.EndTurn -> endTurn(snapshot, envelope.playerId)
            GameCommand.AdvanceTurn -> advanceTurn(snapshot)
        }
        if (transition is Transition.Rejected) {
            return GameExecution.Rejected(snapshot, transition.reason)
        }

        transition as Transition.Applied
        val board = transition.result.snapshot.board
        val finalized = transition.result.copy(
            snapshot = transition.result.snapshot.copy(
                board = board.copy(
                    revision = board.revision + 1,
                    processedCommandIds = (board.processedCommandIds + envelope.commandId)
                        .takeLast(MAX_PROCESSED_COMMAND_IDS),
                ),
            ),
        )
        return GameExecution.Applied(finalized)
    }

    private fun validate(snapshot: GameSnapshot, envelope: GameCommandEnvelope): GameCommandRejection? {
        if (snapshot.players.none { it.id == envelope.playerId }) return GameCommandRejection.PLAYER_NOT_FOUND
        if (snapshot.board.revision != envelope.expectedRevision) return GameCommandRejection.REVISION_CONFLICT
        return null
    }

    private fun rollDice(snapshot: GameSnapshot, playerId: String): Transition {
        val player = snapshot.player(playerId)
        if (!snapshot.board.isActivePlayer(player)) return Transition.Rejected(GameCommandRejection.PLAYER_NOT_ACTIVE)
        if (!snapshot.board.canRoll || snapshot.board.diceRolling) {
            return Transition.Rejected(GameCommandRejection.ROLL_NOT_ALLOWED)
        }
        val dice = random.nextInt(1, 7)
        return Transition.Applied(
            RuleResult(
                snapshot = snapshot.copy(
                    board = snapshot.board.copy(
                        dice = dice,
                        canRoll = false,
                        diceRolling = true,
                    ),
                ),
                events = listOf(DomainEvent.DiceRolled(playerId, dice)),
            ),
        )
    }

    private fun completeRoll(snapshot: GameSnapshot, playerId: String): Transition {
        val player = snapshot.player(playerId)
        if (!snapshot.board.isActivePlayer(player)) return Transition.Rejected(GameCommandRejection.PLAYER_NOT_ACTIVE)
        if (!snapshot.board.diceRolling) return Transition.Rejected(GameCommandRejection.ROLL_NOT_IN_PROGRESS)

        val places = snapshot.board.placesOf(player.location)
        val movementSteps = player.movementSteps(
            dice = snapshot.board.dice,
            transportMovementBonusEnabled = snapshot.board.transportMovementBonusEnabled,
        )
        val newPosition = moveTo(player.location.position, places.size, movementSteps)
        return Transition.Applied(moveAndResolve(snapshot, player, newPosition))
    }

    private fun endTurn(snapshot: GameSnapshot, playerId: String): Transition {
        val player = snapshot.player(playerId)
        if (!snapshot.board.isActivePlayer(player)) return Transition.Rejected(GameCommandRejection.PLAYER_NOT_ACTIVE)
        return advanceTurn(snapshot)
    }

    private fun advanceTurn(snapshot: GameSnapshot): Transition {
        val advanced = advance(snapshot) ?: return Transition.Rejected(GameCommandRejection.NO_ACTIVE_PLAYERS)
        return advanced
    }

    private fun moveAndResolve(snapshot: GameSnapshot, player: Player, newPosition: Int): RuleResult {
        val board = snapshot.board
        val places = board.placesOf(player.location)
        val placeCount = places.size
        val currentPosition = player.location.position.coerceIn(0, placeCount - 1)
        val safeNewPosition = newPosition.coerceIn(0, placeCount - 1)
        val passedPlaces = if (currentPosition > safeNewPosition) {
            places.subList(currentPosition + 1, placeCount) + places.subList(0, safeNewPosition + 1)
        } else {
            places.subList(currentPosition + 1, safeNewPosition + 1)
        }
        val salaryPosition = passedPosition(
            places = passedPlaces,
            place = PlaceType.Salary,
            currentPosition = currentPosition,
            placeCount = placeCount,
        )
        val startPosition = passedPosition(
            places = passedPlaces,
            place = PlaceType.Start,
            currentPosition = currentPosition,
            placeCount = placeCount,
        )

        var result = RuleResult(
            snapshot = snapshot.copy(
                board = board.copy(
                    moveCount = board.moveCount + 1,
                    canRoll = false,
                    diceRolling = false,
                ),
            ),
            events = listOf(DomainEvent.PlayerMoved(player.id, currentPosition, safeNewPosition)),
        )
        var movedPlayer = player.copy(
            location = player.location.copy(position = safeNewPosition),
            salaryPosition = salaryPosition.takeIf { player.cashFlow() > 0 },
            investmentPosition = safeNewPosition.takeIf { places[it] == PlaceType.Salary },
            startCapitalization = startPosition
                ?.takeIf { player.funds.isNotEmpty() }
                ?.let { StartCapitalization(position = it, landed = it == safeNewPosition) },
        )
        if (salaryPosition != null && player.cashFlow() <= 0) {
            result = result.withPayment(
                player = movedPlayer,
                amount = player.cashFlow().absoluteValue,
            )
            movedPlayer = result.snapshot.player(player.id).copy(salaryPosition = null)
        }
        result = result.withPlayer(movedPlayer)

        return when (val place = places[safeNewPosition]) {
            PlaceType.BigBusiness,
            PlaceType.Business,
            PlaceType.Chance,
            PlaceType.Deputy,
            PlaceType.Expenses,
            PlaceType.Shopping,
            PlaceType.Store -> {
                val options = cardOptions(place, movedPlayer)
                result.copy(
                    snapshot = result.snapshot.copy(
                        board = result.snapshot.board.copy(canTakeCard = options),
                    ),
                    events = result.events + DomainEvent.CardOptionsOpened(options),
                )
            }

            PlaceType.Bankruptcy -> {
                val business = random.choose(movedPlayer.businesses.filter { it.type != BusinessType.WORK })
                val updated = business?.let { movedPlayer.copy(businesses = movedPlayer.businesses - it) } ?: movedPlayer
                result.withPlayer(updated)
                    .withNotice(business?.let(PresentationNotice::BankruptBusiness))
                    .thenAdvance()
            }

            PlaceType.Child -> {
                val eligible = movedPlayer.card.gender == Gender.FEMALE ||
                        movedPlayer.card.gender == Gender.MALE && movedPlayer.isMarried
                val updated = if (eligible) {
                    movedPlayer.copy(
                        babies = movedPlayer.babies + 1,
                        cash = movedPlayer.cash + movedPlayer.config.childBenefit,
                    )
                } else movedPlayer
                result.withPlayer(updated)
                    .withNotice(
                        if (eligible) PresentationNotice.PlayerHadBaby(updated.id, updated.babies) else null,
                    )
                    .thenAdvance()
            }

            PlaceType.Divorce -> {
                val updated = movedPlayer.afterDivorce()
                result.withPlayer(updated)
                    .withNotice(
                        if (updated != movedPlayer) PresentationNotice.PlayerDivorced(updated.id) else null,
                    )
                    .thenAdvance()
            }

            PlaceType.Resignation -> {
                val work = movedPlayer.businesses.firstOrNull { it.type == BusinessType.WORK }
                val updated = work?.let { movedPlayer.copy(businesses = movedPlayer.businesses - it) } ?: movedPlayer
                result.withPlayer(updated)
                    .withNotice(work?.let(PresentationNotice::Resignation))
                    .thenAdvance()
            }

            PlaceType.Love -> {
                val married = !movedPlayer.isMarried
                val updated = if (married) movedPlayer.copy(isMarried = true) else movedPlayer
                val marriedResult = result.withPlayer(updated)
                    .withNotice(if (married) PresentationNotice.PlayerMarried(updated.id) else null)
                val paidResult = if (married && updated.card.gender == Gender.MALE) {
                    marriedResult.withPayment(updated, updated.config.marriageCost)
                } else marriedResult
                paidResult.thenAdvance()
            }

            PlaceType.Rest -> result
                .withPlayer(movedPlayer.copy(inRest = movedPlayer.config.restTurnCount))
                .thenAdvance()

            is PlaceType.Desire -> {
                val dream = board.dreamById(place.dreamId)
                if (dream != null && dream.id !in board.purchasedDreamIds) {
                    result.withNotice(PresentationNotice.DreamOffered)
                } else result.thenAdvance()
            }

            PlaceType.TaxInspection -> {
                val bribe = movedPlayer.taxInspectionBribe(board)
                val paid = if (bribe > 0) result.withPayment(movedPlayer, bribe) else result
                paid.withNotice(bribe.takeIf { it > 0 }?.let(PresentationNotice::TaxInspectionPaid))
                    .thenAdvance()
            }

            PlaceType.Salary,
            PlaceType.Start -> result.thenAdvance()
        }
    }

    private fun RuleResult.withPayment(player: Player, amount: Long): RuleResult {
        if (amount == 0L) return this
        val payment = moneyService.pay(
            account = player.financialAccount(),
            amount = amount,
            policy = PaymentPolicy(
                useFunds = player.config.hasFunds,
                loanLimit = snapshot.board.loanLimit,
            ),
        )
        val updated = player.withFinancialAccount(payment.account)
        val usedFunds = payment.events.any { it is PaymentEvent.FundsWithdrawn }
        val paymentNotices = buildList {
            add(PresentationNotice.CashSubtracted(amount))
            payment.events.forEach { event ->
                when (event) {
                    is PaymentEvent.DepositWithdrawn -> if (!usedFunds && payment.account.loan == player.loan) {
                        add(PresentationNotice.DepositWithdrawn(event.amount))
                    }

                    is PaymentEvent.LoanAdded -> if (!usedFunds) {
                        add(PresentationNotice.LoanAdded(event.amount))
                    }

                    PaymentEvent.LoanLimitExceeded -> add(PresentationNotice.LoanLimitExceeded)
                    is PaymentEvent.FundsWithdrawn -> Unit
                }
            }
        }
        return withPlayer(updated).copy(
            events = events + DomainEvent.PaymentApplied(player.id, amount, payment.events),
            notices = notices + paymentNotices,
        )
    }

    private fun RuleResult.withPlayer(player: Player): RuleResult {
        val previous = snapshot.players.firstOrNull { it.id == player.id } ?: return this
        if (previous == player) return this
        val updated = player.withRecentChanges(previous)
        return copy(
            snapshot = snapshot.copy(
                players = snapshot.players.map { if (it.id == updated.id) updated else it },
            ),
            events = events + DomainEvent.PlayerChanged(updated),
        )
    }

    private fun RuleResult.withNotice(notice: PresentationNotice?): RuleResult {
        return if (notice == null) this else copy(notices = notices + notice)
    }

    private fun RuleResult.thenAdvance(): RuleResult {
        val advanced = advance(snapshot)?.result ?: return this
        return advanced.copy(
            events = events + advanced.events,
            notices = notices + advanced.notices,
        )
    }

    private fun advance(snapshot: GameSnapshot): Transition.Applied? {
        var current = snapshot
        val events = mutableListOf<DomainEvent>()
        while (true) {
            val activePlayers = current.board.activePlayers(current.players)
            if (activePlayers.isEmpty()) return null
            val previousId = current.board.activePlayerId
            val next = current.board.nextActivePlayer(activePlayers) ?: return null
            val updatedBoard = current.board.discardTakenCard().copy(
                activePlayerId = next.id,
                moveCount = current.board.moveCount + 1,
                canRoll = true,
                diceRolling = false,
                takenCard = null,
                sharesCount = null,
                canTakeCard = emptyList(),
                auction = null,
                bidList = emptyList(),
            )
            current = current.copy(board = updatedBoard)
            events += DomainEvent.TurnAdvanced(previousId, next.id)
            if (next.inRest <= 0) break
            val rested = next.copy(inRest = next.inRest - 1).withRecentChanges(next)
            current = current.copy(players = current.players.map { if (it.id == rested.id) rested else it })
            events += DomainEvent.PlayerChanged(rested)
        }
        return Transition.Applied(RuleResult(current, events))
    }

    private fun passedPosition(
        places: List<PlaceType>,
        place: PlaceType,
        currentPosition: Int,
        placeCount: Int,
    ): Int? {
        val index = places.indexOfLast { it == place }
        if (index < 0) return null
        val position = currentPosition + index + 1
        return if (position >= placeCount) position - placeCount else position
    }

    private fun cardOptions(place: PlaceType, player: Player): List<BoardCardType> = when (place) {
        PlaceType.BigBusiness -> listOf(BoardCardType.BigBusiness)
        PlaceType.Business -> when {
            player.businesses.any { it.type == BusinessType.LARGE } -> listOf(BoardCardType.BigBusiness)
            player.businesses.any { it.type == BusinessType.MEDIUM } ->
                listOf(BoardCardType.BigBusiness, BoardCardType.MediumBusiness)

            player.businesses.any { it.type == BusinessType.SMALL } ->
                listOf(BoardCardType.SmallBusiness, BoardCardType.MediumBusiness)

            else -> listOf(BoardCardType.SmallBusiness)
        }

        PlaceType.Chance -> listOf(BoardCardType.Chance)
        PlaceType.Deputy -> listOf(BoardCardType.Deputy)
        PlaceType.Expenses -> listOf(BoardCardType.Expenses)
        PlaceType.Shopping -> listOf(BoardCardType.Shopping)
        PlaceType.Store -> listOf(BoardCardType.EventStore)
        else -> emptyList()
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

    private fun Player.withRecentChanges(previous: Player): Player = copy(
        lastTotals = appendRecentChange(lastTotals, previous.total(), total()),
        lastCashFlows = appendRecentChange(lastCashFlows, previous.cashFlow(), cashFlow()),
        lastLoans = appendRecentChange(lastLoans, previous.loan, loan),
    )

    private fun Board.nextActivePlayer(players: List<Player>): Player? {
        val active = activePlayers(players)
        if (active.isEmpty()) return null
        val currentIndex = active.indexOfFirst { it.id == activePlayerId }
        return active[if (currentIndex < 0 || currentIndex == active.lastIndex) 0 else currentIndex + 1]
    }

    private fun Board.discardTakenCard(): Board {
        val card = takenCard ?: return this
        val updatedDiscard = discard + (card.type to (discard[card.type].orEmpty() + card.id))
        val updatedCards = cards + (card.type to cards[card.type].orEmpty().filterNot { it == card.id })
        val recycledDiscard = updatedDiscard.toMutableMap()
        val recycledCards = updatedCards.mapValues { (type, ids) ->
            ids.ifEmpty {
                recycledDiscard[type].orEmpty().also { recycledDiscard[type] = emptyList() }
            }
        }
        return copy(
            discard = recycledDiscard,
            cards = recycledCards,
            takenCard = null,
            sharesCount = null,
        )
    }

    private fun GameSnapshot.player(playerId: String): Player = players.first { it.id == playerId }

    private sealed interface Transition {
        data class Applied(val result: RuleResult) : Transition
        data class Rejected(val reason: GameCommandRejection) : Transition
    }

    private companion object {
        const val MAX_PROCESSED_COMMAND_IDS = 100
    }
}
