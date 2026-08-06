@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.server.data.Storage
import ua.vald_zx.game.rat.race.server.data.generateStableDbId
import ua.vald_zx.game.rat.race.server.generation.BoardGenerationCoordinator
import ua.vald_zx.game.rat.race.server.generation.playerConfig
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal val LOGGER = KtorSimpleLogger("RaceRatService")

private const val CORRUPT_NAME_LENGTH = 48
private const val SPEECH_LIFETIME_MS = 8_000
private val LEGACY_OUTER_ONLY_CARD_IDS = mapOf(
    BoardCardType.Chance to (121..138).toSet(),
    BoardCardType.EventStore to setOf(107) + (111..124),
)

private val boardMutexes = ConcurrentHashMap<String, Mutex>()
private val playerMutexes = ConcurrentHashMap<String, Mutex>()
internal fun boardMutex(boardId: String): Mutex = boardMutexes.getOrPut(boardId) { Mutex() }
private fun playerMutex(playerId: String): Mutex = playerMutexes.getOrPut(playerId) { Mutex() }

class RaceRatServiceImpl(
    private val uuidStateProvider: MutableStateFlow<String>,
    private val scope: CoroutineScope,
    private val connectionIdentified: (String) -> Unit = {},
    private val random: GameRandom = DefaultGameRandom,
    private val clock: GameClock = SystemGameClock,
) : RaceRatService, CoroutineScope by scope {

    private var boardIdState = MutableStateFlow("")
    private val eventBus = MutableSharedFlow<Event>()
    private val boardsFlow = MutableSharedFlow<List<BoardId>>(replay = 1)
    private val globalEventBus: MutableSharedFlow<GlobalEvent>
        get() = getGlobalEventBus(boardIdState.value)
    private var boardStateSubJob: Job? = null
    private var globalEventStateSubJob: Job? = null
    private val gameApplicationService = GameApplicationService(
        repository = StorageGameRepository,
        engine = GameEngine(random),
        transactionMutex = ::boardMutex,
    )

    private val playerId: String
        get() = generateStableDbId(boardIdState.value, uuidStateProvider.value)

    private suspend fun player() = Storage.getPlayer(playerId)
    private suspend fun board() = Storage.getBoard(boardIdState.value)

    init {
        checkStatusFlow
            .onEach { eventBus.emit(Event.CheckState) }
            .launchIn(this)

        Storage.observeBoards()
            .onEach { boardsFlow.emit(getBoards()) }
            .launchIn(this)

        boardIdState
            .onEach { boardId -> if (boardId.isNotBlank()) subscribeToBoard(boardId) }
            .launchIn(this)
    }

    private suspend fun subscribeToBoard(boardId: String) {
        boardStateSubJob?.cancel()
        boardStateSubJob = Storage.observeBoard(boardId)
            .distinctUntilChanged { previous, board -> board.differsOnlyByGenerationProgress(previous) }
            .onEach { board -> eventBus.emit(Event.BoardChanged(board)) }
            .launchIn(this)

        globalEventStateSubJob?.cancel()
        globalEventStateSubJob = globalEventBus
            .onEach { event -> handleGlobalEvent(event) }
            .launchIn(this)
    }

    private suspend fun handleGlobalEvent(event: GlobalEvent) {
        when (event) {
            is GlobalEvent.SendMoney -> {
                if (event.receiverId == playerId) {
                    eventBus.emit(Event.MoneyIncome(event.playerId, event.amount))
                    updatePlayer { plusCash(event.amount) }
                }
            }

            is GlobalEvent.PlayerChanged -> eventBus.emit(Event.PlayerChanged(event.player))
            is GlobalEvent.PlayerHadBaby -> eventBus.emit(Event.PlayerHadBaby(event.playerId, event.babies))
            is GlobalEvent.PlayerMarried -> eventBus.emit(Event.PlayerMarried(event.playerId))
            is GlobalEvent.PlayerDivorced -> eventBus.emit(Event.PlayerDivorced(event.playerId))
            is GlobalEvent.PlayerWon -> eventBus.emit(Event.PlayerWon(event.playerId, event.playerName))
            is GlobalEvent.BidSelled -> {
                if (event.bid.playerId == playerId) {
                    buyLot(event.auction, event.bid)
                }
            }
        }
    }

    override suspend fun hello(helloUuid: String, boardId: String): Instance {
        val board = Storage.getBoard(boardId)
        boardSelected(board)
        uuidStateProvider.value = helloUuid
        connectionIdentified(helloUuid)
        val storedPlayer = Storage.getPlayerOrNull(playerId)
        val belongsToBoard = storedPlayer?.boardId == board.id
        val boardIsPlayable = !board.generation.enabled || board.generationProgress.isReady
        if (boardIsPlayable && storedPlayer != null && (board.playerIds.contains(playerId) || belongsToBoard)) {
            if (!board.playerIds.contains(playerId)) {
                LOGGER.warn("Restoring missing player ${storedPlayer.id} in persisted board ${board.id}")
                updateBoard {
                    copy(
                        playerIds = playerIds + storedPlayer.id,
                        activePlayerId = activePlayerId.ifBlank { storedPlayer.id },
                    )
                }
            }
            checkStatusJobs[playerId]?.cancel()
            updatePlayer { copy(isInactive = false) }
            val restoredBoard = board()
            invalidateNextPlayer(restoredBoard.activePlayerId)
            return Instance(board(), player())
        }
        return Instance(board, null)
    }

    private fun boardSelected(board: Board) {
        boardIdState.value = board.id
    }

    override suspend fun ping() {
        checkStatusJobs[playerId]?.cancel()
        if (player().isInactive) {
            updatePlayer { copy(isInactive = false) }
        }
    }

    override suspend fun connectionIsValid() {
        checkStatusJobs[playerId]?.cancel()
    }

    override suspend fun getBoards(): List<BoardId> {
        val now = clock.nowEpochMilliseconds()
        return Storage.boards().map { board ->
            val players = Storage.players(board.id)
            val active = players.count { !it.isInactive }
            val inactiveSince = when {
                active == 0 && board.allInactiveSinceEpochMs == null -> now.also { timestamp ->
                    Storage.updateBoard(board.copy(allInactiveSinceEpochMs = timestamp))
                }

                active > 0 && board.allInactiveSinceEpochMs != null -> null.also {
                    Storage.updateBoard(board.copy(allInactiveSinceEpochMs = null))
                }

                else -> board.allInactiveSinceEpochMs
            }
            val deletableAfter = inactiveSince?.plus(BOARD_DELETION_INACTIVITY.inWholeMilliseconds)
            BoardId(
                id = board.id,
                name = board.name,
                createDateTime = board.createDateTime,
                activePlayerCount = active,
                inactivePlayerCount = players.size - active,
                deletableAfterEpochMs = deletableAfter,
                canDelete = active == 0 && deletableAfter != null && now >= deletableAfter,
            )
        }
    }

    override fun observeBoards(): Flow<List<BoardId>> = boardsFlow

    override suspend fun deleteBoard(boardId: String) {
        boardMutex(boardId).withLock {
            val board = Storage.getBoardOrNull(boardId) ?: return
            val players = Storage.players(boardId)
            check(players.none { !it.isInactive }) { "Board has active players" }
            val inactiveSince = board.allInactiveSinceEpochMs ?: error("Board is still active")
            val inactiveFor = clock.nowEpochMilliseconds() - inactiveSince
            check(inactiveFor >= BOARD_DELETION_INACTIVITY.inWholeMilliseconds) {
                "Board has not been inactive long enough"
            }
            BoardGenerationCoordinator.cancelGeneration(boardId)
            Storage.removeBoard(boardId)
        }
    }

    override suspend fun createBoard(
        name: String,
        loanLimit: Long,
        businessLimit: Long,
        decks: Map<BoardCardType, Int>,
        outerCircleConditions: OuterCircleConditions,
        victoryConditions: VictoryConditions,
        transportMovementBonusEnabled: Boolean,
        generation: BoardGeneration,
    ): Board {
        require(decks.keys.containsAll(BoardCardType.entries)) { "All card decks are required" }
        require(decks.values.all { it in 1..500 }) { "Deck size must be between 1 and 500" }
        val world = generation.copy(
            theme = generation.theme.sanitizedWorldField(),
            locality = generation.locality.sanitizedWorldField(),
            epoch = generation.epoch.sanitizedWorldField(),
            seed = if (generation.seed != 0L) generation.seed else clock.nowEpochMilliseconds()
        )
        val board = Board(
            name = name,
            loanLimit = loanLimit,
            businessLimit = businessLimit,
            createDateTime = kotlin.time.Instant.fromEpochMilliseconds(clock.nowEpochMilliseconds())
                .toLocalDateTime(TimeZone.currentSystemDefault()),
            id = Uuid.random().toString(),
            cards = decks.mapValues { (_, size) -> (1..size).toList() },
            outerCircleConditions = outerCircleConditions,
            victoryConditions = victoryConditions,
            transportMovementBonusEnabled = transportMovementBonusEnabled,
            generation = world,
            generationProgress = if (world.enabled) {
                BoardGenerationProgress(
                    stage = BoardGenerationStage.PREPARING,
                    completed = 0,
                    total = 1,
                )
            } else {
                BoardGenerationProgress()
            },
        )
        Storage.newBoard(board)
        boardSelected(board)
        if (world.enabled) {
            BoardGenerationCoordinator.continueGeneration(board.id)
        }
        return board
    }

    override fun observeGeneration(): Flow<BoardGenerationProgress> = flow {
        Storage.observeBoard(boardIdState.value)
            .map { it.generationProgress }
            .distinctUntilChanged()
            .collect(::emit)
    }

    override suspend fun continueGeneration() {
        val board = board()
        if (!board.generation.enabled || board.generationProgress.isReady) return
        BoardGenerationCoordinator.continueGeneration(board.id)
    }

    override suspend fun restartGeneration() {
        val board = board()
        if (!board.generation.enabled) return
        BoardGenerationCoordinator.restartGeneration(board.id)
    }

    override suspend fun makePlayer(
        uuid: String,
        color: Long,
        card: PlayerCard,
    ): Player {
        val board = board()
        check(board.generationProgress.isReady) { "Board generation is not complete" }
        if (board.generation.enabled) {
            check(board.generatedProfessions.any { profession -> board.matches(profession, card) }) {
                "Unknown generated profession"
            }
        }
        uuidStateProvider.value = uuid
        connectionIdentified(uuid)
        val newPlayer = Player(
            id = playerId,
            boardId = board.id,
            attrs = PlayerAttributes(color = color),
            card = card,
            config = board.generatedBalance?.playerConfig() ?: Config(),
            businesses = listOf(
                Business(
                    type = BusinessType.WORK,
                    name = card.profession,
                    price = 0,
                    profit = card.salary
                )
            )
        )
        Storage.newPlayer(newPlayer)
        getGlobalEventBus(board.id).emit(GlobalEvent.PlayerChanged(newPlayer))
        updateBoard { copy(playerIds = playerIds + playerId) }
        takeSalary()
        invalidateNextPlayer(board.activePlayerId)
        return newPlayer
    }

    override suspend fun getPlayer(): Player = player()

    override suspend fun executeCommand(envelope: GameCommandEnvelope): GameCommandResponse {
        val currentSnapshot = StorageGameRepository.load(boardIdState.value)
            ?: error("Board not found: ${boardIdState.value}")
        if (envelope.boardId != currentSnapshot.board.id ||
            envelope.playerId != playerId ||
            envelope.command == GameCommand.CompleteRoll ||
            envelope.command == GameCommand.AdvanceTurn ||
            envelope.command is GameCommand.MoveTo
        ) {
            return GameCommandResponse(
                status = GameCommandStatus.REJECTED,
                snapshot = currentSnapshot,
                rejection = GameCommandRejection.COMMAND_NOT_AVAILABLE,
            )
        }

        val execution = gameApplicationService.execute(envelope)
            ?: error("Board not found: ${envelope.boardId}")
        publish(execution)
        if (envelope.command !is GameCommand.RollDice || execution is GameExecution.Rejected) {
            return execution.toResponse()
        }

        if (!execution.snapshot.board.diceRolling) return execution.toResponse()
        delay(4.seconds)
        val completed = gameApplicationService.execute(
            envelope.copy(
                commandId = "${envelope.commandId}:complete",
                expectedRevision = execution.snapshot.board.revision,
                command = GameCommand.CompleteRoll,
            ),
        ) ?: error("Board not found: ${envelope.boardId}")
        publish(completed)
        return completed.toResponse()
    }

    override suspend fun updateAttributes(attrs: PlayerAttributes) {
        updatePlayer { copy(attrs = attrs) }
    }

    override fun eventsObserve(): Flow<Event> = eventBus

    override suspend fun getPlayers(): List<Player> = Storage.players(boardIdState.value)

    override suspend fun getBoard(): Board = board()

    override suspend fun sendMoney(receiverId: String, amount: Long) {
        globalEventBus.emit(GlobalEvent.SendMoney(playerId, receiverId, amount))
        updatePlayer { minusCash(amount) }
    }

    override suspend fun sendMessage(text: String) {
        val message = text.trim().take(160)
        if (message.isNotEmpty()) {
            val speech = PlayerSpeech(message, clock.nowEpochMilliseconds() + SPEECH_LIFETIME_MS)
            updatePlayer { copy(speech = speech) }
        }
    }

    override suspend fun rollDice() {
        executeCommand(
            compatibilityEnvelope(
                GameCommand.RollDice(Uuid.random().toString()),
            ),
        )
    }

    override suspend fun takeCard(cardType: BoardCardType) {
        if (cardType == BoardCardType.Deputy) {
            buyDeputy()
            return
        }
        val layer = player().location.level.toLayer()
        val currentBoard = board()
        val preparedBoard = currentBoard.prepareCardDeck(cardType, layer)
        if (preparedBoard != currentBoard) {
            updateBoard { prepareCardDeck(cardType, layer) }
        }
        val card = random.choose(preparedBoard.availableCardIds(cardType, layer)) ?: return
        selectCard(card, cardType)
    }

    override suspend fun buyCorruptBusiness(card: BoardCard.Chance.CorruptBusiness) {
        val player = player()
        val board = board()
        val canAfford = if (card.oneTimeProfit > 0) {
            board.canMakeVoluntaryPurchase(player, card.price)
        } else {
            board.canBuyBusiness(player, card.price)
        }
        if (!board.isResolvingCard(BoardCardType.Chance) ||
            player.deputies < card.deputies ||
            !canAfford
        ) return
        updatePlayer {
            val hired = copy(deputies = deputies - card.deputies)
            if (card.oneTimeProfit > 0) {
                hired.minusCash(card.price).plusCash(card.oneTimeProfit)
            } else {
                hired.copy(
                    businesses = businesses + Business(
                        type = BusinessType.CORRUPTION,
                        name = card.description.take(CORRUPT_NAME_LENGTH),
                        price = card.price,
                        profit = card.profit,
                    )
                ).minusCash(card.price)
            }
        }
        nextPlayer()
    }

    override suspend fun buyCorruptLand(card: BoardCard.Chance.CorruptLand) {
        val player = player()
        val board = board()
        if (!board.isResolvingCard(BoardCardType.Chance) ||
            player.deputies < card.deputies ||
            !board.canMakeVoluntaryPurchase(player, card.price)
        ) return
        updatePlayer {
            copy(
                deputies = deputies - card.deputies,
                landList = landList + Land(
                    name = card.description.take(CORRUPT_NAME_LENGTH),
                    area = card.area,
                    price = card.price,
                    corrupt = true,
                ),
            ).minusCash(card.price)
        }
        nextPlayer()
    }

    override suspend fun reelection() {
        val board = board()
        board.players().filter { it.deputies > 0 }.forEach { player ->
            val released = player.copy(deputies = 0)
            Storage.updatePlayer(released)
            globalEventBus.emit(GlobalEvent.PlayerChanged(released))
        }
        nextPlayer()
    }

    override suspend fun skipDeputies() {
        if (board().activePlayerId != playerId) return
        nextPlayer()
    }

    override suspend fun buyDeputy() {
        val board = board()
        if (!board.isResolvingCard(BoardCardType.Deputy) &&
            BoardCardType.Deputy !in board.canTakeCard
        ) return
        val player = player()
        if (!board.canMakeVoluntaryPurchase(player, player.config.deputyCardPrice)) return
        updateBoard { discardPileB() }
        val preparedBoard = board()
        val cardId = random.choose(preparedBoard.cards[BoardCardType.Deputy].orEmpty()) ?: return
        updatePlayer {
            val hired = if (preparedBoard.deputyIsCorrupt(cardId)) deputies + 1 else deputies
            copy(deputies = hired).minusCash(config.deputyCardPrice)
        }
        selectCard(cardId, BoardCardType.Deputy)
    }

    private suspend fun selectCard(cardId: Int, cardType: BoardCardType) {
        updateBoard { takeFromDeck(cardId, cardType) }
    }

    override suspend fun selectCardByNo(cardId: Int, cardType: BoardCardType) {
        val board = board()
        if (board.containsCard(cardId, cardType)) {
            selectCard(cardId, cardType)
        }
    }

    override suspend fun debugMoveToAndSelectCard(cardId: Int, cardType: BoardCardType) {
        val board = board()
        if (!board.canRoll || board.activePlayerId != playerId) return
        if (!board.containsCard(cardId, cardType)) return
        val currentPlayer = player()
        val position = board.placesOf(currentPlayer.location).nearestPlacePosition(
            currentPosition = currentPlayer.location.position,
            cardType = cardType,
        ) ?: return
        processNewPosition(position)
        selectCard(cardId, cardType)
    }

    override suspend fun next() {
        executeCommand(compatibilityEnvelope(GameCommand.EndTurn("legacy.next")))
    }

    private suspend fun compatibilityEnvelope(command: GameCommand): GameCommandEnvelope {
        val currentBoard = board()
        return GameCommandEnvelope(
            commandId = Uuid.random().toString(),
            boardId = currentBoard.id,
            playerId = playerId,
            expectedRevision = currentBoard.revision,
            command = command,
        )
    }

    private suspend fun publish(execution: GameExecution) {
        if (execution is GameExecution.Applied) publish(execution.result)
    }

    private suspend fun publish(result: RuleResult) {
        result.events.forEach { domainEvent ->
            when (domainEvent) {
                is DomainEvent.PlayerChanged -> {
                    globalEventBus.emit(GlobalEvent.PlayerChanged(domainEvent.player))
                    checkVictory(domainEvent.player)
                }

                is DomainEvent.CardOptionsOpened,
                is DomainEvent.DiceRolled,
                is DomainEvent.PaymentApplied,
                is DomainEvent.PlayerMoved,
                is DomainEvent.TurnAdvanced -> Unit
            }
        }
        result.notices.forEach { notice ->
            when (notice) {
                is PresentationNotice.BankruptBusiness -> eventBus.emit(Event.BankruptBusiness(notice.business))
                is PresentationNotice.PlayerHadBaby ->
                    globalEventBus.emit(GlobalEvent.PlayerHadBaby(notice.playerId, notice.babies))

                is PresentationNotice.PlayerDivorced ->
                    globalEventBus.emit(GlobalEvent.PlayerDivorced(notice.playerId))

                is PresentationNotice.PlayerMarried ->
                    globalEventBus.emit(GlobalEvent.PlayerMarried(notice.playerId))

                is PresentationNotice.Resignation -> eventBus.emit(Event.Resignation(notice.business))
                PresentationNotice.DreamOffered -> eventBus.emit(Event.DreamOffered)
                is PresentationNotice.TaxInspectionPaid -> eventBus.emit(Event.TaxInspectionPaid(notice.amount))
                is PresentationNotice.CashSubtracted -> eventBus.emit(Event.SubCash(notice.amount))
                is PresentationNotice.DepositWithdrawn -> eventBus.emit(Event.DepositWithdraw(notice.amount))
                is PresentationNotice.LoanAdded -> eventBus.emit(Event.LoanAdded(notice.amount))
                PresentationNotice.LoanLimitExceeded -> eventBus.emit(Event.LoanOverlimited)
            }
        }
    }

    private fun GameExecution.toResponse(): GameCommandResponse = when (this) {
        is GameExecution.Applied -> GameCommandResponse(GameCommandStatus.APPLIED, snapshot)
        is GameExecution.Duplicate -> GameCommandResponse(GameCommandStatus.DUPLICATE, snapshot)
        is GameExecution.Rejected -> GameCommandResponse(GameCommandStatus.REJECTED, snapshot, reason)
    }

    private suspend fun invalidateNextPlayer(activePlayerId: String) {
        val board = board()
        val active = Storage.getPlayerOrNull(activePlayerId)
        if (activePlayerId.isEmpty() || active == null || !active.isActiveOn(board)) {
            nextPlayer()
        }
    }

    private suspend fun nextPlayer() {
        val execution = gameApplicationService.execute(
            compatibilityEnvelope(GameCommand.AdvanceTurn),
        ) ?: return
        publish(execution)
    }

    override suspend fun takeSalary() {
        updatePlayer {
            val cashFlow = cashFlow()
            (if (cashFlow >= 0) {
                plusCash(cashFlow)
            } else {
                minusCash(cashFlow.absoluteValue)
            }).copy(salaryPosition = null)
        }
    }

    private suspend fun buyBusiness(business: Business, doNext: suspend () -> Unit = {}) {
        val currentBusiness = player().businesses
        if (business.type == BusinessType.SMALL
            && currentBusiness.any { it.type == BusinessType.WORK }
            && currentBusiness.count { it.type == BusinessType.SMALL } == 1
        ) {
            if (business.fromAuction) {
                updatePlayer {
                    copy(businesses = currentBusiness.filter { it.type == BusinessType.WORK } + business)
                        .minusCash(business.price)
                }
                currentBusiness.find { it.type == BusinessType.WORK }?.let { work ->
                    eventBus.emit(Event.Fired(work))
                }
                doNext()
            } else {
                eventBus.emit(Event.ConfirmDismissal(business))
            }
        } else if (currentBusiness.isNotEmpty()
            && currentBusiness.first().type.klass != business.type.klass
            && currentBusiness.none { it.type == BusinessType.WORK }
        ) {
            eventBus.emit(Event.ConfirmSellingAllBusiness(business))
        } else {
            updatePlayer {
                copy(businesses = currentBusiness + business)
                    .minusCash(business.price)
            }
            doNext()
        }
    }

    override suspend fun buyBusiness(business: Business) {
        val board = board()
        if (!board.isResolvingCard(
                BoardCardType.SmallBusiness,
                BoardCardType.MediumBusiness,
                BoardCardType.BigBusiness,
            ) || !board.canBuyBusiness(player(), business.price)
        ) return
        buyBusiness(business) { nextPlayer() }
    }

    override suspend fun dismissalConfirmed(business: Business) {
        val board = board()
        if (!board.isResolvingCard(
                BoardCardType.SmallBusiness,
                BoardCardType.MediumBusiness,
                BoardCardType.BigBusiness,
            ) || !board.canBuyBusiness(player(), business.price)
        ) return
        updatePlayer {
            val newBusinesses = businesses.filter { it.type != BusinessType.WORK } + business
            copy(businesses = newBusinesses).minusCash(business.price)
        }
        nextPlayer()
    }

    override suspend fun sellingAllBusinessConfirmed(business: Business) {
        val board = board()
        if (!board.isResolvingCard(
                BoardCardType.SmallBusiness,
                BoardCardType.MediumBusiness,
                BoardCardType.BigBusiness,
            ) || !board.canBuyBusiness(player(), business.price)
        ) return
        updatePlayer {
            val refund = businesses.sumOf { it.price }
            copy(businesses = listOf(business))
                .plusCash(refund)
                .minusCash(business.price)
        }
        nextPlayer()
    }

    private suspend fun processNewPosition(newPosition: Int) {
        val execution = gameApplicationService.execute(
            compatibilityEnvelope(GameCommand.MoveTo(newPosition)),
        ) ?: return
        publish(execution)
    }

    override suspend fun minusCash(price: Long) {
        updatePlayer { minusCash(price) }
    }

    override suspend fun payExpenses(card: BoardCard.Expenses) {
        val currentBoard = board()
        val currentPlayer = player()
        if (!currentBoard.isActivePlayer(currentPlayer) || !currentBoard.isResolvingCard(BoardCardType.Expenses)) return
        if (!currentPlayer.mustPay(card)) {
            nextPlayer()
            return
        }
        updatePlayer {
            val paid = minusCash(card.price)
            if (card.grantsAnimal) paid.copy(animal = paid.animal + 1) else paid
        }
        nextPlayer()
    }

    private suspend fun updateBoard(change: suspend Board.() -> Board) {
        val id = boardIdState.value
        boardMutex(id).withLock {
            Storage.updateBoard(board().change())
        }
    }

    private suspend fun updatePlayer(change: suspend Player.() -> Player) {
        val id = playerId
        var updatedPlayer: Player? = null
        playerMutex(id).withLock {
            val previousPlayer = player()
            val changed = previousPlayer.change()
            val newPlayer = changed.withRecentChanges(previousPlayer)
            Storage.updatePlayer(newPlayer)
            globalEventBus.emit(GlobalEvent.PlayerChanged(newPlayer))
            updatedPlayer = newPlayer
        }
        updatedPlayer?.let { checkVictory(it) }
    }

    private suspend fun checkVictory(player: Player) {
        val board = board()
        if (!player.hasMetVictoryConditions(board.victoryConditions)) return
        if (board.winnerId != null) return
        var becameWinner = false
        updateBoard {
            if (winnerId == null) {
                becameWinner = true
                copy(winnerId = player.id)
            } else {
                this
            }
        }
        if (becameWinner) {
            globalEventBus.emit(GlobalEvent.PlayerWon(player.id, player.card.name))
        }
    }

    private fun Player.plusCash(value: Long): Player {
        return withFinancialAccount(sharedMoneyService.addCash(financialAccount(), value))
    }

    private suspend fun Player.minusCash(
        value: Long,
        isFundBuy: Boolean = false
    ): Player {
        val board = board()
        if (value == 0L) return this
        eventBus.emit(Event.SubCash(value))
        val result = sharedMoneyService.pay(
            account = financialAccount(),
            amount = value,
            policy = PaymentPolicy(
                useFunds = !isFundBuy && config.hasFunds,
                loanLimit = board.loanLimit,
            ),
        )
        val usedFunds = result.events.any { it is PaymentEvent.FundsWithdrawn }
        result.events.forEach { paymentEvent ->
            when (paymentEvent) {
                is PaymentEvent.DepositWithdrawn -> if (!usedFunds && result.account.loan == loan) {
                    eventBus.emit(Event.DepositWithdraw(paymentEvent.amount))
                }

                is PaymentEvent.LoanAdded -> if (!usedFunds) {
                    eventBus.emit(Event.LoanAdded(paymentEvent.amount))
                }

                PaymentEvent.LoanLimitExceeded -> eventBus.emit(Event.LoanOverlimited)
                is PaymentEvent.FundsWithdrawn -> Unit
            }
        }
        return withFinancialAccount(result.account)
    }

    override suspend fun buyThing(card: BoardCard.Shopping) {
        val board = board()
        if (!board.isResolvingCard(BoardCardType.Shopping) ||
            !board.canMakeVoluntaryPurchase(player(), card.price)
        ) return
        updatePlayer {
            val updated = when (card.shopType) {
                ShopType.AUTO -> copy(cars = cars + 1)
                ShopType.HOUSE -> copy(cottage = cottage + 1)
                ShopType.APARTMENT -> copy(apartment = apartment + 1)
                ShopType.YACHT -> copy(yacht = yacht + 1)
                ShopType.FLY -> copy(flight = flight + 1)
                ShopType.ANIMAL -> copy(animal = animal + 1)
            }
            updated.minusCash(card.price)
        }
        nextPlayer()
    }

    override suspend fun changePosition(position: Int) {
        processNewPosition(position)
    }

    override suspend fun debugChangePosition(location: PlayerLocation) {
        val layer = location.level.toLayer()
        val position = location.position.coerceIn(0, board().placesOf(layer).lastIndex)
        if (layer.level != player().location.level) {
            updatePlayer {
                copy(location = PlayerLocation(position = position, level = layer.level))
            }
        }
        processNewPosition(position)
    }

    override suspend fun debugUpdatePlayer(values: DebugPlayerValues) {
        updatePlayer {
            withDebugValues(values)
        }
    }

    private suspend fun buyEstate(estate: Estate, doNext: suspend () -> Unit = {}) {
        updatePlayer {
            copy(estateList = estateList + estate).minusCash(estate.price)
        }
        doNext()
    }

    override suspend fun buyEstate(estate: Estate) {
        val board = board()
        if (!board.isResolvingCard(BoardCardType.Chance) ||
            !board.canMakeVoluntaryPurchase(player(), estate.price)
        ) return
        buyEstate(estate) { nextPlayer() }
    }

    private suspend fun buyLand(land: Land, doNext: suspend () -> Unit = {}) {
        updatePlayer {
            copy(landList = landList + land).minusCash(land.price)
        }
        doNext()
    }

    override suspend fun buyLand(land: Land) {
        val board = board()
        if (!board.isResolvingCard(BoardCardType.Chance) ||
            !board.canMakeVoluntaryPurchase(player(), land.price)
        ) return
        buyLand(land) { nextPlayer() }
    }

    override suspend fun randomJob(card: BoardCard.Chance.RandomJob) {
        updatePlayer { plusCash(card.profit) }
        nextPlayer()
    }

    private suspend fun purchaseShares(shares: Shares) {
        updatePlayer {
            copy(sharesList = sharesList + shares).minusCash(shares.price)
        }
    }

    override suspend fun buyShares(shares: Shares, totalCount: Long) {
        val currentBoard = board()
        val currentPlayer = player()
        require(currentBoard.activePlayerId == playerId) { "Only the active player can buy shares" }
        require(currentBoard.auction == null) { "Shares are currently being auctioned" }
        require(currentBoard.isResolvingCard(BoardCardType.Chance)) { "No share purchase is active" }
        val availableCount = currentBoard.sharesCount ?: totalCount
        require(shares.count in 1..availableCount) { "Invalid shares count" }
        require(currentBoard.canMakeVoluntaryPurchase(currentPlayer, shares.price)) { "Not enough available credit" }
        purchaseShares(shares)
        val remainingCount = availableCount - shares.count
        if (remainingCount == 0L) {
            nextPlayer()
            return
        }
        updateBoard {
            copy(sharesCount = remainingCount)
        }
    }

    override suspend fun extendBusiness(
        business: Business,
        card: BoardCard.EventStore.BusinessExtending
    ) {
        updatePlayer {
            val extendedBusiness = business.copy(extentions = business.extentions + card.profit)
            copy(businesses = businesses.replace(business, extendedBusiness))
        }
        nextPlayer()
    }

    override suspend fun sellLands(area: Long, priceOfUnit: Long) {
        if (area <= 0 || priceOfUnit <= 0) return
        val currentBoard = board()
        updatePlayer {
            val regularLands = landList.filterNot(currentBoard::isCorruptLand)
            val totalArea = regularLands.sumOf { it.area }
            if (totalArea >= area) {
                var remainder = area
                val updatedLands = landList.toMutableList()
                regularLands.forEach { land ->
                    if (remainder == 0L) return@forEach
                    updatedLands.remove(land)
                    if (land.area <= remainder) {
                        remainder -= land.area
                    } else {
                        updatedLands += land.copy(area = land.area - remainder)
                        remainder = 0
                    }
                }
                copy(landList = updatedLands).plusCash(area * priceOfUnit)
            } else {
                this
            }
        }
        updateBoard {
            copy(processedPlayerIds = processedPlayerIds + playerId)
        }
        passLand()
    }

    override suspend fun sellCorruptBusiness(business: Business, salePercentage: Long) {
        if (salePercentage !in 200..1_000) return
        val currentBoard = board()
        if (currentBoard.takenCard?.type != BoardCardType.EventStore || playerId in currentBoard.processedPlayerIds) return
        val ownedBusiness = player().businesses.firstOrNull { it == business && it.type == BusinessType.CORRUPTION }
            ?: return
        updatePlayer {
            val updatedBusinesses = businesses.toMutableList().apply { remove(ownedBusiness) }
            copy(businesses = updatedBusinesses)
                .plusCash(ownedBusiness.price * salePercentage / 100)
        }
        updateBoard { copy(processedPlayerIds = processedPlayerIds + playerId) }
        passCorruptBusiness()
    }

    override suspend fun sellCorruptLands(area: Long, priceOfUnit: Long) {
        if (area <= 0 || priceOfUnit <= 0) return
        val currentBoard = board()
        if (currentBoard.takenCard?.type != BoardCardType.EventStore || playerId in currentBoard.processedPlayerIds) return
        val corruptLands = player().landList.filter(currentBoard::isCorruptLand)
        val corruptArea = corruptLands.sumOf { it.area }
        if (area > corruptArea) return
        updatePlayer {
            var remainder = area
            val updatedLands = landList.toMutableList()
            corruptLands.forEach { land ->
                if (remainder == 0L) return@forEach
                updatedLands.remove(land)
                if (land.area <= remainder) {
                    remainder -= land.area
                } else {
                    updatedLands += land.copy(area = land.area - remainder, corrupt = true)
                    remainder = 0
                }
            }
            copy(landList = updatedLands).plusCash(area * priceOfUnit)
        }
        updateBoard { copy(processedPlayerIds = processedPlayerIds + playerId) }
        passCorruptLand()
    }

    override suspend fun sellShares(
        card: BoardCard.EventStore.Shares,
        count: Long
    ) {
        val board = board()
        val activeCard = board.generatedShareEventOrNull()
        if (activeCard != null && activeCard.sharesType != card.sharesType) return
        val resolvedCard = activeCard ?: card
        val ownedCount = player().sharesList
            .filter { it.type == resolvedCard.sharesType }
            .sumOf { it.count }
        if (count <= 0 || count > ownedCount) return
        if (resolvedCard.forcedSale && count != ownedCount) return
        updatePlayer {
            var resultList = sharesList.toMutableList()
            val sharesByType = resultList.filter { it.type == resolvedCard.sharesType }
            var needToSell = count
            var index = 0
            while (needToSell != 0L && index < sharesByType.size) {
                val shares = sharesByType[index]
                if (needToSell < shares.count) {
                    resultList = resultList.replace(
                        shares,
                        shares.copy(count = shares.count - needToSell)
                    ).toMutableList()
                    break
                } else if (needToSell == shares.count) {
                    resultList.remove(shares)
                    break
                } else {
                    resultList.remove(shares)
                    needToSell -= shares.count
                    index += 1
                }
            }
            copy(sharesList = resultList).plusCash(count * resolvedCard.price)
        }
        updateBoard {
            copy(processedPlayerIds = processedPlayerIds + playerId)
        }
        passShares(resolvedCard.sharesType)
    }

    override suspend fun sellEstate(
        card: List<Estate>,
        price: Long
    ) {
        updatePlayer {
            copy(estateList = estateList - card.toSet()).plusCash(card.size * price)
        }
        updateBoard {
            copy(processedPlayerIds = processedPlayerIds + playerId)
        }
        passEstate()
    }

    override suspend fun passLand() {
        val initialBoard = board()
        if (player().landList.any { !initialBoard.isCorruptLand(it) }) {
            updateBoard { copy(processedPlayerIds = processedPlayerIds + playerId) }
        }
        val currentBoard = board()
        val owners = currentBoard.activePlayers(currentBoard.players())
            .filter { current -> current.landList.any { !currentBoard.isCorruptLand(it) } }
            .map { it.id }
            .toSet()
        if (marketEventIsComplete(currentBoard.processedPlayerIds, owners)) {
            nextPlayer()
        }
    }

    override suspend fun passCorruptBusiness() {
        completeOptionalMarketEvent { current ->
            current.businesses.any { it.type == BusinessType.CORRUPTION }
        }
    }

    override suspend fun passCorruptLand() {
        val currentBoard = board()
        completeOptionalMarketEvent { current -> current.landList.any(currentBoard::isCorruptLand) }
    }

    private suspend fun completeOptionalMarketEvent(ownsRelevantAsset: (Player) -> Boolean) {
        if (ownsRelevantAsset(player())) {
            updateBoard { copy(processedPlayerIds = processedPlayerIds + playerId) }
        }
        val currentBoard = board()
        val owners = currentBoard.activePlayers(currentBoard.players())
            .filter(ownsRelevantAsset)
            .map { it.id }
            .toSet()
        if (marketEventIsComplete(currentBoard.processedPlayerIds, owners)) {
            nextPlayer()
        }
    }

    override suspend fun passShares(sharesType: String) {
        val initialBoard = board()
        val activeCard = initialBoard.generatedShareEventOrNull()
        if (activeCard != null && activeCard.sharesType != sharesType) return
        val playerHasShares = player().sharesList.any { it.type == sharesType }
        if (activeCard?.forcedSale == true &&
            activeCard.sharesType == sharesType &&
            playerHasShares
        ) return
        if (playerHasShares) {
            updateBoard { copy(processedPlayerIds = processedPlayerIds + playerId) }
        }
        val currentBoard = board()
        val owners = currentBoard.activePlayers(currentBoard.players())
            .filter { current -> current.sharesList.any { it.type == sharesType } }
            .map { it.id }
            .toSet()
        if (marketEventIsComplete(currentBoard.processedPlayerIds, owners)) {
            nextPlayer()
        }
    }

    override suspend fun passEstate() {
        if (player().estateList.isNotEmpty()) {
            updateBoard { copy(processedPlayerIds = processedPlayerIds + playerId) }
        }
        val currentBoard = board()
        val owners = currentBoard.activePlayers(currentBoard.players()).filter { it.estateList.isNotEmpty() }
            .map { it.id }.toSet()
        if (marketEventIsComplete(currentBoard.processedPlayerIds, owners)) {
            nextPlayer()
        }
    }

    override suspend fun playHighRiskInvestment(stake: Long, guess: Int) {
        if (guess !in 1..6) return
        playInvestmentGame(stake, player().config.highRiskMultiplier, { dice -> dice == guess }) { outcome ->
            Event.HighRiskPlayed(outcome, guess)
        }
    }

    override suspend fun playMediumRiskInvestment(stake: Long, even: Boolean) {
        playInvestmentGame(stake, player().config.mediumRiskMultiplier, { dice -> (dice % 2 == 0) == even }) { outcome ->
            Event.MediumRiskPlayed(outcome, even)
        }
    }

    private suspend fun playInvestmentGame(
        stake: Long,
        multiplier: Long,
        isWin: (Int) -> Boolean,
        event: (InvestmentOutcome) -> Event,
    ) {
        val player = player()
        if (player.investmentPosition == null || !board().canMakeVoluntaryPurchase(player, stake)) return
        val dice = random.nextInt(1, 7)
        val payout = if (isWin(dice)) stake * multiplier else 0L
        updatePlayer {
            val afterStake = minusCash(stake)
            if (payout > 0) afterStake.plusCash(payout) else afterStake
        }
        consumeInvestment()
        eventBus.emit(event(InvestmentOutcome(dice = dice, stake = stake, payout = payout)))
    }

    private suspend fun consumeInvestment() {
        if (player().salaryPosition != null) takeSalary()
        updatePlayer { copy(investmentPosition = null) }
    }

    override suspend fun investInFund(amount: Long) {
        val player = player()
        val salaryPosition = player.investmentPosition ?: return
        if (!board().canBuyWithCashAndDeposit(player, amount)) return
        val rate = board().placesOf(player.location).fundRateAtSalary(
            salaryPosition,
            player.config.salaryFundRates,
        )
        updatePlayer {
            val sameRate = funds.find { it.rate == rate }
            val newFunds = if (sameRate != null) {
                funds.replace(sameRate, sameRate.copy(amount = sameRate.amount + amount))
            } else {
                funds + Fund(rate = rate, amount = amount)
            }
            copy(funds = newFunds).minusCash(amount, isFundBuy = true)
        }
        consumeInvestment()
    }

    override suspend fun capitalizeFunds() {
        val player = player()
        val capitalization = player.startCapitalization ?: return
        val rateOverride = if (capitalization.landed) player.config.fundStartRate else null
        val (newFunds, profit) = player.funds.capitalize(rateOverride, player.config.fundBaseRate)
        updatePlayer {
            copy(funds = newFunds, startCapitalization = null)
        }
        eventBus.emit(Event.FundsCapitalized(profit))
    }

    override suspend fun toDeposit(amount: Long) {
        updatePlayer {
            copy(deposit = deposit + amount).minusCash(amount)
        }
    }

    override suspend fun repayLoan(amount: Long) {
        updatePlayer {
            require(amount > 0) { "Repayment must be positive" }
            require(amount <= loan) { "Repayment exceeds the loan" }
            require(amount <= balance()) { "Not enough money for repayment" }
            val paid = minusCash(amount)
            require(paid.loan == loan) { "Repayment cannot be financed with a new loan" }
            paid.copy(loan = loan - amount)
        }
    }

    override suspend fun advertiseAuction(auction: Auction) {
        if (board().activePlayerId != playerId) return
        updateBoard {
            copy(auction = auction, bidList = emptyList())
        }
    }

    override suspend fun sellBid(bid: Bid) {
        val board = board()
        if (board.activePlayerId != playerId) return
        val auction = board.auction ?: return
        if (board.bidList.none { it.playerId == bid.playerId && it.bid == bid.bid && it.count == bid.count }) return
        if (auction is Auction.SharesAuction && auction.shares.count < bid.count) return
        val profit = auction.getProfit(bid)
        updatePlayer { plusCash(profit) }
        if (auction !is Auction.SharesAuction) {
            updateBoard {
                copy(auction = null, bidList = emptyList())
            }
            nextPlayer()
        } else {
            val remainingCount = auction.shares.count - bid.count
            if (remainingCount == 0L) {
                nextPlayer()
            } else {
                updateBoard {
                    copy(
                        auction = auction.copy(
                            shares = auction.shares.copy(count = remainingCount)
                        ),
                        bidList = bidList.filter { it.playerId != bid.playerId }
                    )
                }
            }
        }
        globalEventBus.emit(GlobalEvent.BidSelled(bid, auction))
    }

    override suspend fun makeBid(price: Long, count: Long) {
        val board = board()
        val auction = board.auction ?: return
        if (board.activePlayerId == playerId) return
        if (price <= 0) return
        if (auction is Auction.SharesAuction) {
            if (count <= 0 || count > auction.shares.count) return
        }
        val minBid = auction.minimumBid(board.bidList)
        if (price < minBid) return
        val player = player()
        if (auction is Auction.SharesAuction && count > Long.MAX_VALUE / price) return
        val totalPrice = if (auction is Auction.SharesAuction) price * count else price
        if (!player.isActiveOn(board) || !board.hasAvailableCredit(player, totalPrice)) return
        updateBoard {
            copy(bidList = bidList.filter { it.playerId != playerId } + Bid(playerId, price, count))
        }
    }

    override suspend fun enterOuterCircle() {
        val currentPlayer = player()
        val conditions = board().outerCircleConditions
        if (!currentPlayer.canEnterOuterCircle(true, conditions)) return
        updatePlayer {
            copy(
                location = PlayerLocation(
                    position = 1,
                    level = BoardLayer.OUTER.level,
                ),
                salaryPosition = null,
            )
        }
    }

    override suspend fun buyDream() {
        val currentBoard = board()
        val currentPlayer = player()
        val dreamPlace = currentBoard.placesOf(currentPlayer.location)
            .getOrNull(currentPlayer.location.position) as? PlaceType.Desire ?: return
        val dream = currentBoard.dreamById(dreamPlace.dreamId) ?: return
        val canBuy = dream.id !in currentBoard.purchasedDreamIds &&
                currentBoard.canMakeVoluntaryPurchase(currentPlayer, dream.price)
        if (!canBuy) return
        var dreamClaimed = false
        updateBoard {
            if (dream.id !in purchasedDreamIds) {
                dreamClaimed = true
                copy(purchasedDreamIds = purchasedDreamIds + dream.id)
            } else {
                this
            }
        }
        if (!dreamClaimed) return
        updatePlayer {
            copy(purchasedDreamIds = purchasedDreamIds + dream.id).minusCash(dream.price)
        }
        nextPlayer()
    }

    override suspend fun selectDream(dreamId: String) {
        val currentBoard = board()
        val allowed = currentBoard.canSelectDream(
            dreamId = dreamId,
            playerId = playerId,
            players = Storage.players(currentBoard.id),
        )
        if (!allowed) return
        updatePlayer {
            copy(selectedDreamId = dreamId)
        }
    }

    private suspend fun buyLot(
        auction: Auction,
        bid: Bid
    ) {
        when (auction) {
            is Auction.BusinessAuction -> {
                val business = auction.business.copy(price = bid.bid, fromAuction = true)
                buyBusiness(business) {
                    LOGGER.info("Change ${this@RaceRatServiceImpl.hashCode()} auction is buying business")
                }
                eventBus.emit(Event.BidBusinessAuctionSuccessBuy)
            }

            is Auction.EstateAuction -> {
                buyEstate(auction.estate.copy(price = bid.bid)) {
                    LOGGER.info("Change ${this@RaceRatServiceImpl.hashCode()} auction is buying estate")
                }
                eventBus.emit(Event.BidEstateAuctionSuccessBuy)
            }

            is Auction.LandAuction -> {
                buyLand(auction.land.copy(price = bid.bid)) {
                    LOGGER.info("Change ${this@RaceRatServiceImpl.hashCode()} auction is buying land")
                }
                eventBus.emit(Event.BidLandAuctionSuccessBuy)
            }

            is Auction.SharesAuction -> {
                purchaseShares(auction.shares.copy(count = bid.count, buyPrice = bid.bid))
                eventBus.emit(Event.BidSharesAuctionSuccessBuy)
            }
        }
    }

    private suspend fun Board.players(): List<Player> {
        return playerIds.mapNotNull { playerId -> Storage.getPlayerOrNull(playerId) }
    }
}

private val BOARD_DELETION_INACTIVITY = 7.days

suspend fun nextPlayer(board: Board) {
    val activePlayers = board.activePlayers(Storage.players(board.id))
    if (activePlayers.isEmpty()) return
    val nextPlayerId = board.nextActivePlayer(activePlayers)?.id ?: return
    val updatedBoard = board.discardPileB().copy(
        activePlayerId = nextPlayerId,
        moveCount = board.moveCount + 1,
        canRoll = true,
        diceRolling = false,
        takenCard = null,
        sharesCount = null,
        canTakeCard = emptyList(),
        auction = null,
        bidList = emptyList()
    )
    Storage.updateBoard(updatedBoard)
    val nextPlayer = activePlayers.find { it.id == nextPlayerId }
    if ((nextPlayer?.inRest ?: 0) > 0) {
        val player = Storage.getPlayer(nextPlayerId)
        Storage.updatePlayer(player.copy(inRest = player.inRest - 1))
        nextPlayer(updatedBoard)
    }
}

internal fun Board.takeFromDeck(cardId: Int, cardType: BoardCardType): Board {
    val newCards = cards.toMutableMap()
    newCards[cardType] = newCards[cardType].orEmpty() - cardId
    return copy(
        cards = newCards,
        takenCard = CardLink(cardType, cardId),
        sharesCount = null,
        canTakeCard = emptyList(),
        processedPlayerIds = emptySet(),
    )
}

internal fun Board.availableCardIds(cardType: BoardCardType, layer: BoardLayer): List<Int> {
    return cards[cardType].orEmpty().filter { cardId -> cardIsAvailable(cardType, cardId, layer) }
}

internal fun Board.prepareCardDeck(cardType: BoardCardType, layer: BoardLayer): Board {
    if (availableCardIds(cardType, layer).isNotEmpty()) return this
    val recyclable = discard[cardType].orEmpty().filter { cardId ->
        cardIsAvailable(cardType, cardId, layer)
    }
    if (recyclable.isEmpty()) return this
    val recyclableIds = recyclable.toSet()
    return copy(
        cards = cards + (cardType to (cards[cardType].orEmpty() + recyclable)),
        discard = discard + (cardType to discard[cardType].orEmpty().filterNot(recyclableIds::contains)),
    )
}

private fun Board.cardIsAvailable(cardType: BoardCardType, cardId: Int, layer: BoardLayer): Boolean {
    if (layer != BoardLayer.INNER || cardType !in LEGACY_OUTER_ONLY_CARD_IDS) return true
    val generatedDeck = generatedCards[cardType]
    return when (generatedDeck?.get(cardId)) {
        is BoardCard.Chance.CorruptBusiness,
        is BoardCard.Chance.CorruptLand,
        is BoardCard.EventStore.Reelection,
        is BoardCard.EventStore.CorruptBusiness,
        is BoardCard.EventStore.CorruptLand -> false

        null -> generatedDeck != null || cardId !in LEGACY_OUTER_ONLY_CARD_IDS.getValue(cardType)
        else -> true
    }
}

internal fun Board.discardPileB(): Board {
    val card = takenCard ?: return this
    val newDiscard = discard.toMutableMap()
    newDiscard[card.type] = newDiscard[card.type].orEmpty() + card.id
    val newCards = cards.toMutableMap()
    newCards[card.type] = newCards[card.type].orEmpty() - card.id
    return copy(
        discard = newDiscard,
        cards = newCards,
        takenCard = null,
        sharesCount = null,
    ).invalidateDecks()
}

private fun Board.invalidateDecks(): Board {
    val newDiscard = discard.toMutableMap()
    val newCards = cards.mapValues { (type, list) ->
        list.ifEmpty {
            val recycled = newDiscard[type].orEmpty()
            newDiscard[type] = emptyList()
            recycled
        }
    }
    return copy(cards = newCards, discard = newDiscard)
}

internal fun Player.withRecentChanges(previous: Player): Player {
    return copy(
        lastTotals = appendRecentChange(lastTotals, previous.total(), total()),
        lastCashFlows = appendRecentChange(lastCashFlows, previous.cashFlow(), cashFlow()),
        lastLoans = appendRecentChange(lastLoans, previous.loan, loan),
    )
}

private const val MAX_WORLD_FIELD_LENGTH = 60

private fun String.sanitizedWorldField(): String =
    filterNot(Char::isISOControl).trim().take(MAX_WORLD_FIELD_LENGTH).trim()

internal fun Board.differsOnlyByGenerationProgress(previous: Board): Boolean =
    previous.copy(generationProgress = generationProgress) == this &&
            previous.generationProgress.isReady == generationProgress.isReady &&
            previous.generationProgress.isFailed == generationProgress.isFailed

private fun Board.generatedShareEventOrNull(): BoardCard.EventStore.Shares? {
    val link = takenCard?.takeIf { it.type == BoardCardType.EventStore } ?: return null
    return generatedCards[link.type]?.get(link.id) as? BoardCard.EventStore.Shares
}

private fun Board.containsCard(cardId: Int, cardType: BoardCardType): Boolean {
    val knownCardIds = cards[cardType].orEmpty() +
            discard[cardType].orEmpty() +
            listOfNotNull(takenCard?.takeIf { it.type == cardType }?.id)
    return cardId in knownCardIds
}

private fun Board.matches(profession: ProfessionCard, card: PlayerCard): Boolean {
    val names = generatedTexts.values.mapNotNull { texts -> texts.professions[profession.id] }
    return card.profession in names &&
            profession.gender == card.gender &&
            profession.salary == card.salary &&
            profession.rent == card.rent &&
            profession.food == card.food &&
            profession.cloth == card.cloth &&
            profession.transport == card.transport &&
            profession.phone == card.phone
}
