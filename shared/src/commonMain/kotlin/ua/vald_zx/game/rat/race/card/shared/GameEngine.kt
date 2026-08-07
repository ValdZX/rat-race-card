package ua.vald_zx.game.rat.race.card.shared

class GameEngine(
    private val random: GameRandom = DefaultGameRandom,
    private val moneyService: MoneyService = sharedMoneyService,
    private val cellRules: CellRuleRegistry = legacyCellRuleRegistry(),
    effectHandlers: EffectHandlerRegistry = standardEffectHandlerRegistry(),
) {
    private val cardDefinitions = CardDefinitionEngine(effectHandlers)

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
            is GameCommand.MoveTo -> moveTo(snapshot, envelope.playerId, envelope.command.position)
            is GameCommand.EnterTransition -> enterTransition(
                snapshot,
                envelope.playerId,
                envelope.command.transitionId,
            )
            is GameCommand.StartCard -> startCard(snapshot, envelope.playerId, envelope.command.definition)
            is GameCommand.ChooseInteraction -> chooseInteraction(
                snapshot,
                envelope.playerId,
                envelope.command.interactionId,
                envelope.command.input,
            )
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
        if (snapshot.board.validateTracks(snapshot.players) is ValidationResult.Invalid) {
            return GameCommandRejection.INVALID_BOARD_DEFINITION
        }
        val cells = snapshot.board.resolvedTracks().flatMap(TrackDefinition::cells)
        if (cellRules.validate(cells) is ValidationResult.Invalid) {
            return GameCommandRejection.INVALID_BOARD_DEFINITION
        }
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

        val cells = snapshot.board.cellsOf(player.location)
        val movementSteps = player.movementSteps(
            dice = snapshot.board.dice,
            transportMovementBonusEnabled = snapshot.board.transportMovementBonusEnabled,
        )
        val newPosition = moveTo(player.location.position, cells.size, movementSteps)
        return Transition.Applied(moveAndResolve(snapshot, player, newPosition))
    }

    private fun moveTo(snapshot: GameSnapshot, playerId: String, position: Int): Transition {
        val player = snapshot.player(playerId)
        if (!snapshot.board.isActivePlayer(player)) return Transition.Rejected(GameCommandRejection.PLAYER_NOT_ACTIVE)
        return Transition.Applied(moveAndResolve(snapshot, player, position))
    }

    private fun enterTransition(snapshot: GameSnapshot, playerId: String, transitionId: String): Transition {
        val player = snapshot.player(playerId)
        if (!snapshot.board.isActivePlayer(player)) return Transition.Rejected(GameCommandRejection.PLAYER_NOT_ACTIVE)
        val transition = snapshot.board.availableTransition(player, snapshot.board.canRoll)
            ?.takeIf { it.id == transitionId }
            ?: return Transition.Rejected(GameCommandRejection.TRANSITION_NOT_AVAILABLE)
        val target = snapshot.board.track(transition.to)
        if (transition.entryCellIndex !in target.cells.indices) {
            return Transition.Rejected(GameCommandRejection.INVALID_BOARD_DEFINITION)
        }
        val updated = player.copy(
            location = PlayerLocation(transition.entryCellIndex, transition.to),
            salaryPosition = null,
            investmentPosition = null,
        ).withTrackedFinancialChanges(player)
        return Transition.Applied(
            RuleResult(
                snapshot.copy(players = snapshot.players.map { if (it.id == playerId) updated else it }),
                events = listOf(DomainEvent.PlayerChanged(updated)),
            ),
        )
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

    private fun startCard(snapshot: GameSnapshot, playerId: String, definition: CardDefinition): Transition {
        val player = snapshot.player(playerId)
        if (!snapshot.board.isActivePlayer(player)) return Transition.Rejected(GameCommandRejection.PLAYER_NOT_ACTIVE)
        return cardTransition(cardDefinitions.start(snapshot, playerId, definition, random, moneyService))
    }

    private fun chooseInteraction(
        snapshot: GameSnapshot,
        playerId: String,
        interactionId: String,
        input: kotlinx.serialization.json.JsonObject,
    ): Transition {
        val player = snapshot.player(playerId)
        if (!snapshot.board.isActivePlayer(player)) return Transition.Rejected(GameCommandRejection.PLAYER_NOT_ACTIVE)
        return cardTransition(
            cardDefinitions.choose(snapshot, playerId, interactionId, input, random, moneyService),
        )
    }

    private fun cardTransition(resolution: CardResolution): Transition = when (resolution) {
        is CardResolution.Applied -> {
            val result = resolution.result
            if (result.turnDirective != TurnDirective.END_TURN) {
                Transition.Applied(result)
            } else {
                val advanced = advance(result.snapshot)?.result
                    ?: return Transition.Rejected(GameCommandRejection.NO_ACTIVE_PLAYERS)
                Transition.Applied(
                    advanced.copy(
                        events = result.events + advanced.events,
                        notices = result.notices + advanced.notices,
                    ),
                )
            }
        }

        CardResolution.NotAvailable -> Transition.Rejected(GameCommandRejection.CARD_NOT_AVAILABLE)
        CardResolution.InvalidDefinition -> Transition.Rejected(GameCommandRejection.INVALID_CARD_DEFINITION)
        CardResolution.InteractionNotFound -> Transition.Rejected(GameCommandRejection.INTERACTION_NOT_FOUND)
        CardResolution.InteractionNotOwned -> Transition.Rejected(GameCommandRejection.INTERACTION_NOT_OWNED)
        CardResolution.InvalidInput -> Transition.Rejected(GameCommandRejection.INVALID_INTERACTION_INPUT)
    }

    private fun moveAndResolve(snapshot: GameSnapshot, player: Player, newPosition: Int): RuleResult {
        val board = snapshot.board
        val cells = board.cellsOf(player.location)
        val cellCount = cells.size
        val currentPosition = player.location.position.coerceIn(0, cellCount - 1)
        val safeNewPosition = newPosition.coerceIn(0, cellCount - 1)
        val passedIndices = if (currentPosition > safeNewPosition) {
            (currentPosition + 1 until cellCount).toList() + (0..safeNewPosition).toList()
        } else {
            (currentPosition + 1..safeNewPosition).toList()
        }

        val initial = RuleResult(
            snapshot = snapshot.copy(
                board = board.copy(
                    moveCount = board.moveCount + 1,
                    canRoll = false,
                    diceRolling = false,
                ),
            ),
            events = listOf(DomainEvent.PlayerMoved(player.id, currentPosition, safeNewPosition)),
        )
        var result = TurnContext(
            result = initial,
            playerId = player.id,
            cellIndex = safeNewPosition,
            isLanding = true,
            random = random,
            moneyService = moneyService,
        ).updatePlayer { it.copy(location = it.location.copy(position = safeNewPosition)) }.result

        passedIndices.forEach { index ->
            val cell = cells[index]
            val context = TurnContext(
                result = result,
                playerId = player.id,
                cellIndex = index,
                isLanding = index == safeNewPosition,
                random = random,
                moneyService = moneyService,
            )
            result = cellRules.rule(cell.type).onPass(context, cell)
        }

        val landedCell = cells[safeNewPosition]
        result = cellRules.rule(landedCell.type).onLand(
            TurnContext(
                result = result,
                playerId = player.id,
                cellIndex = safeNewPosition,
                isLanding = true,
                random = random,
                moneyService = moneyService,
            ),
            landedCell,
        )
        if (result.turnDirective != TurnDirective.END_TURN) return result
        val advanced = advance(result.snapshot)?.result ?: return result
        return advanced.copy(
            events = result.events + advanced.events,
            notices = result.notices + advanced.notices,
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
                activeCardDefinitionId = null,
                pendingInteractions = emptyList(),
            )
            val outgoing = current.players.firstOrNull { it.id == previousId }
            val clearsMarkers = outgoing != null &&
                    (outgoing.salaryPosition != null || outgoing.investmentPosition != null)
            current = current.copy(
                board = updatedBoard,
                players = current.players.map { player ->
                    if (player.id == previousId && clearsMarkers) {
                        player.copy(salaryPosition = null, investmentPosition = null)
                    } else {
                        player
                    }
                },
            )
            events += DomainEvent.TurnAdvanced(previousId, next.id)
            if (clearsMarkers) {
                events += DomainEvent.PlayerChanged(current.players.first { it.id == previousId })
            }
            val upcoming = current.players.first { it.id == next.id }
            if (upcoming.inRest <= 0) break
            val rested = upcoming.copy(inRest = upcoming.inRest - 1).withTrackedFinancialChanges(upcoming)
            current = current.copy(players = current.players.map { if (it.id == rested.id) rested else it })
            events += DomainEvent.PlayerChanged(rested)
        }
        return Transition.Applied(RuleResult(current, events))
    }


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
