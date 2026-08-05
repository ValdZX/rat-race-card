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
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal val LOGGER = KtorSimpleLogger("RaceRatService")

private const val CORRUPT_NAME_LENGTH = 48
private const val SPEECH_LIFETIME_MS = 8_000
private const val LEGACY_CHILD_BENEFIT = 1_000L

private val boardMutexes = ConcurrentHashMap<String, Mutex>()
private val playerMutexes = ConcurrentHashMap<String, Mutex>()
internal fun boardMutex(boardId: String): Mutex = boardMutexes.getOrPut(boardId) { Mutex() }
private fun playerMutex(playerId: String): Mutex = playerMutexes.getOrPut(playerId) { Mutex() }

class RaceRatServiceImpl(
    private val uuidStateProvider: MutableStateFlow<String>,
    private val scope: CoroutineScope,
    private val connectionIdentified: (String) -> Unit = {},
) : RaceRatService, CoroutineScope by scope {

    private var boardIdState = MutableStateFlow("")
    private val eventBus = MutableSharedFlow<Event>()
    private val boardsFlow = MutableSharedFlow<List<BoardId>>(replay = 1)
    private val globalEventBus: MutableSharedFlow<GlobalEvent>
        get() = getGlobalEventBus(boardIdState.value)
    private var boardStateSubJob: Job? = null
    private var globalEventStateSubJob: Job? = null

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
        val now = Clock.System.now().toEpochMilliseconds()
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
            val inactiveFor = Clock.System.now().toEpochMilliseconds() - inactiveSince
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
            seed = if (generation.seed != 0L) generation.seed else Clock.System.now().toEpochMilliseconds()
        )
        val board = Board(
            name = name,
            loanLimit = loanLimit,
            businessLimit = businessLimit,
            createDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
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
            val speech = PlayerSpeech(message, Clock.System.now().toEpochMilliseconds() + SPEECH_LIFETIME_MS)
            updatePlayer { copy(speech = speech) }
        }
    }

    override suspend fun rollDice() {
        updateBoard {
            val dice = (1..6).random()
            copy(dice = dice, canRoll = false, diceRolling = true)
        }
        delay(4.seconds)
        updateBoard {
            copy(diceRolling = false)
        }
        move()
    }

    override suspend fun takeCard(cardType: BoardCardType) {
        if (cardType == BoardCardType.Deputy) {
            buyDeputy()
            return
        }
        val card = board().cards[cardType]?.randomOrNull() ?: return
        selectCard(card, cardType)
    }

    override suspend fun buyCorruptBusiness(card: BoardCard.Chance.CorruptBusiness) {
        if (player().deputies < card.deputies) return
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
        if (player().deputies < card.deputies) return
        updatePlayer {
            copy(
                deputies = deputies - card.deputies,
                landList = landList + Land(
                    name = card.description.take(CORRUPT_NAME_LENGTH),
                    area = card.area,
                    price = card.price,
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
        if (board().activePlayerId != playerId) return
        updateBoard { discardPileB() }
        val board = board()
        val cardId = board.cards[BoardCardType.Deputy]?.randomOrNull() ?: return
        updatePlayer {
            val hired = if (board.deputyIsCorrupt(cardId)) deputies + 1 else deputies
            copy(deputies = hired).minusCash(DEPUTY_CARD_PRICE)
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
        nextPlayer()
    }

    private suspend fun invalidateNextPlayer(activePlayerId: String) {
        val board = board()
        val active = Storage.getPlayerOrNull(activePlayerId)
        if (activePlayerId.isEmpty() || active == null || !active.isActiveOn(board)) {
            nextPlayer()
        }
    }

    private suspend fun nextPlayer() {
        nextPlayer(board())
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
        buyBusiness(business) { nextPlayer() }
    }

    override suspend fun dismissalConfirmed(business: Business) {
        updatePlayer {
            val newBusinesses = businesses.filter { it.type != BusinessType.WORK } + business
            copy(businesses = newBusinesses).minusCash(business.price)
        }
        nextPlayer()
    }

    override suspend fun sellingAllBusinessConfirmed(business: Business) {
        updatePlayer {
            val refund = businesses.sumOf { it.price }
            copy(businesses = listOf(business))
                .plusCash(refund)
                .minusCash(business.price)
        }
        nextPlayer()
    }

    private suspend fun move() {
        val player = player()
        val board = board()
        val cellCount = board.placesOf(player.location).size
        val currentPosition = player.location.position
        val movementSteps = player.movementSteps(
            dice = board.dice,
            transportMovementBonusEnabled = board.transportMovementBonusEnabled,
        )
        val newPosition = moveTo(currentPosition, cellCount, movementSteps)
        processNewPosition(newPosition)
    }

    private suspend fun processNewPosition(newPosition: Int) {
        val player = player()
        val currentBoard = board()
        val places = currentBoard.placesOf(player.location)
        val placeCount = places.size
        val currentPosition = player.location.position
        val safeNewPosition = newPosition.coerceIn(0, placeCount - 1)
        val safeCurrent = currentPosition.coerceIn(0, placeCount - 1)

        val passedPlaces = if (safeCurrent > safeNewPosition) {
            places.subList(safeCurrent + 1, placeCount) + places.subList(0, safeNewPosition + 1)
        } else {
            places.subList(safeCurrent + 1, safeNewPosition + 1)
        }
        val passedSalaryPosition = if (passedPlaces.contains(PlaceType.Salary)) {
            var pos = safeCurrent + passedPlaces.indexOfLast { it == PlaceType.Salary } + 1
            if (pos >= placeCount) pos -= placeCount
            pos
        } else null

        val salaryPosition = if (passedSalaryPosition != null) {
            if (player.cashFlow() > 0) {
                passedSalaryPosition
            } else {
                takeSalary()
                null
            }
        } else null

        val startCapitalization = if (passedPlaces.contains(PlaceType.Start) && player.funds.isNotEmpty()) {
            var pos = safeCurrent + passedPlaces.indexOfLast { it == PlaceType.Start } + 1
            if (pos >= placeCount) pos -= placeCount
            StartCapitalization(position = pos, landed = pos == safeNewPosition)
        } else null

        updateBoard {
            copy(moveCount = moveCount + 1, canRoll = false)
        }
        updatePlayer {
            copy(
                location = location.copy(position = safeNewPosition),
                salaryPosition = salaryPosition,
                investmentPosition = safeNewPosition.takeIf { places[it] == PlaceType.Salary },
                startCapitalization = startCapitalization,
            )
        }

        when (val place = places[safeNewPosition]) {
            PlaceType.BigBusiness -> updateBoard {
                copy(canTakeCard = listOf(BoardCardType.BigBusiness))
            }

            PlaceType.Business -> updateBoard {
                copy(canTakeCard = businessCardOptions(player))
            }

            PlaceType.Chance -> updateBoard { copy(canTakeCard = listOf(BoardCardType.Chance)) }
            PlaceType.Deputy -> updateBoard { copy(canTakeCard = listOf(BoardCardType.Deputy)) }
            PlaceType.Expenses -> updateBoard { copy(canTakeCard = listOf(BoardCardType.Expenses)) }
            PlaceType.Shopping -> updateBoard { copy(canTakeCard = listOf(BoardCardType.Shopping)) }
            PlaceType.Store -> updateBoard { copy(canTakeCard = listOf(BoardCardType.EventStore)) }

            PlaceType.Bankruptcy -> {
                updatePlayer {
                    val sellable = businesses.filter { it.type != BusinessType.WORK }
                    if (sellable.isNotEmpty()) {
                        val random = sellable.random()
                        eventBus.emit(Event.BankruptBusiness(random))
                        copy(businesses = businesses - random)
                    } else this
                }
                nextPlayer()
            }

            PlaceType.Child -> {
                if (player.card.gender == Gender.FEMALE || (player.card.gender == Gender.MALE && player.isMarried)) {
                    updatePlayer {
                        val totalBabies = babies + 1
                        copy(babies = totalBabies)
                            .plusCash(currentBoard.generatedBalance?.childBenefit ?: LEGACY_CHILD_BENEFIT)
                            .also {
                            globalEventBus.emit(GlobalEvent.PlayerHadBaby(playerId, totalBabies))
                        }
                    }
                }
                nextPlayer()
            }

            PlaceType.Divorce -> {
                if (player.isMarried) {
                    updatePlayer {
                        (if (card.gender == Gender.MALE) {
                            copy(isMarried = false, babies = 0, cash = cash / 2, deposit = deposit / 2)
                        } else {
                            copy(isMarried = false)
                        }).also {
                            globalEventBus.emit(GlobalEvent.PlayerDivorced(playerId))
                        }
                    }
                }
                nextPlayer()
            }

            PlaceType.Resignation -> {
                val business = player.businesses.find { it.type == BusinessType.WORK }
                if (business != null) {
                    updatePlayer {
                        copy(businesses = businesses - business).also {
                            eventBus.emit(Event.Resignation(business))
                        }
                    }
                }
                nextPlayer()
            }

            PlaceType.Love -> {
                if (!player.isMarried) {
                    updatePlayer {
                        copy(isMarried = true).also {
                            globalEventBus.emit(GlobalEvent.PlayerMarried(playerId))
                        }
                    }
                    if (player.card.gender == Gender.MALE) {
                        updatePlayer { minusCash(config.marriageCost) }
                    }
                }
                nextPlayer()
            }

            PlaceType.Rest -> {
                updatePlayer { copy(inRest = 2) }
                nextPlayer()
            }

            is PlaceType.Desire -> {
                val currentBoard = board()
                val dream = currentBoard.dreamById(place.dreamId)
                if (dream != null && dream.id !in currentBoard.purchasedDreamIds) {
                    eventBus.emit(Event.DreamOffered)
                } else {
                    nextPlayer()
                }
            }

            PlaceType.Salary,
            PlaceType.Start,
            PlaceType.TaxInspection -> nextPlayer()
        }
    }

    private fun businessCardOptions(player: Player): List<BoardCardType> {
        return when {
            player.businesses.any { it.type == BusinessType.LARGE } ->
                listOf(BoardCardType.BigBusiness)

            player.businesses.any { it.type == BusinessType.MEDIUM } ->
                listOf(BoardCardType.BigBusiness, BoardCardType.MediumBusiness)

            player.businesses.any { it.type == BusinessType.SMALL } ->
                listOf(BoardCardType.SmallBusiness, BoardCardType.MediumBusiness)

            else -> listOf(BoardCardType.SmallBusiness)
        }
    }

    override suspend fun minusCash(price: Long) {
        updatePlayer { minusCash(price) }
    }

    override suspend fun payExpenses(card: BoardCard.Expenses) {
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
            val newTotal = changed.total()
            val previousTotal = previousPlayer.total()
            val totals = if (newTotal != previousTotal) {
                (changed.lastTotals + (newTotal - previousTotal)).takeLast(3)
            } else changed.lastTotals
            val newCashFlow = changed.cashFlow()
            val previousCashFlow = previousPlayer.cashFlow()
            val cashFlows = if (newCashFlow != previousCashFlow) {
                (changed.lastCashFlows + (newCashFlow - previousCashFlow)).takeLast(3)
            } else changed.lastCashFlows
            val newPlayer = changed.copy(lastTotals = totals, lastCashFlows = cashFlows)
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
        return copy(cash = cash + value)
    }

    private suspend fun Player.minusCash(
        value: Long,
        isFundBuy: Boolean = false
    ): Player {
        val board = board()
        if (value == 0L) return this
        eventBus.emit(Event.SubCash(value))
        return if (cash >= value) {
            copy(cash = cash - value)
        } else if ((cash + deposit) >= value) {
            eventBus.emit(Event.DepositWithdraw(value - cash))
            copy(cash = 0, deposit = (deposit + cash) - value)
        } else if (!isFundBuy && config.hasFunds && funds.isNotEmpty()) {
            var stub = cash + deposit
            var newFunds = funds.toList()
            funds.sortedBy { it.rate }.firstOrNull { fund ->
                if (stub + fund.amount >= value) {
                    newFunds = newFunds.replace(fund, fund.copy(amount = stub + fund.amount - value))
                    true
                } else {
                    stub += fund.amount
                    newFunds = newFunds.remove(fund)
                    false
                }
            }
            if (stub < value) {
                val newLoan = loan + (value - stub)
                if (newLoan > board.loanLimit) {
                    eventBus.emit(Event.LoanOverlimited)
                }
                copy(cash = 0, deposit = 0, funds = emptyList(), loan = newLoan)
            } else {
                copy(cash = 0, deposit = 0, funds = newFunds)
            }
        } else {
            eventBus.emit(Event.LoanAdded(value - (cash + deposit)))
            val newLoan = loan + (value - (deposit + cash))
            if (newLoan > board.loanLimit) {
                eventBus.emit(Event.LoanOverlimited)
            }
            copy(cash = 0, deposit = 0, loan = newLoan)
        }
    }

    override suspend fun buyThing(card: BoardCard.Shopping) {
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
        buyEstate(estate) { nextPlayer() }
    }

    private suspend fun buyLand(land: Land, doNext: suspend () -> Unit = {}) {
        updatePlayer {
            copy(landList = landList + land).minusCash(land.price)
        }
        doNext()
    }

    override suspend fun buyLand(land: Land) {
        buyLand(land) { nextPlayer() }
    }

    override suspend fun randomJob(card: BoardCard.Chance.RandomJob) {
        updatePlayer { plusCash(card.profit) }
        nextPlayer()
    }

    private suspend fun buyShares(totalCount: Long, shares: Shares) {
        updatePlayer {
            copy(sharesList = sharesList + shares).minusCash(shares.price)
        }
        val auction = board().auction
        if (auction is Auction.SharesAuction) {
            updateBoard {
                copy(
                    sharesCount = null,
                    auction = auction.copy(shares = auction.shares.copy(count = auction.shares.count - shares.count))
                )
            }
        } else {
            updateBoard {
                copy(sharesCount = totalCount - shares.count)
            }
        }
    }

    override suspend fun buyShares(shares: Shares, totalCount: Long) {
        buyShares(totalCount, shares)
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
        updatePlayer {
            val totalArea = landList.sumOf { it.area }
            if (totalArea >= area) {
                val updatedLands = if (totalArea == area) {
                    emptyList()
                } else {
                    var remainder = area
                    val newLands = landList.toMutableList()
                    landList.forEach { land ->
                        if (remainder == 0L) return@forEach
                        newLands -= land
                        if (land.area <= remainder) {
                            remainder -= land.area
                        } else {
                            newLands += land.copy(area = land.area - remainder)
                            remainder = 0
                        }
                    }
                    newLands
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
        if (player().landList.isNotEmpty()) {
            updateBoard { copy(processedPlayerIds = processedPlayerIds + playerId) }
        }
        val currentBoard = board()
        val owners = currentBoard.activePlayers(currentBoard.players())
            .filter { it.landList.isNotEmpty() }
            .map { it.id }
            .toSet()
        val participants = owners + currentBoard.processedPlayerIds
        if (participants.isEmpty() || currentBoard.processedPlayerIds.containsAll(participants)) {
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
        val participants = owners + currentBoard.processedPlayerIds
        if (participants.isEmpty() || currentBoard.processedPlayerIds.containsAll(participants)) {
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
        val participants = owners + currentBoard.processedPlayerIds
        if (participants.isEmpty() || currentBoard.processedPlayerIds.containsAll(participants)) {
            nextPlayer()
        }
    }

    override suspend fun playHighRiskInvestment(stake: Long, guess: Int) {
        if (guess !in 1..6) return
        playInvestmentGame(stake, HIGH_RISK_MULTIPLIER, { dice -> dice == guess }) { outcome ->
            Event.HighRiskPlayed(outcome, guess)
        }
    }

    override suspend fun playMediumRiskInvestment(stake: Long, even: Boolean) {
        playInvestmentGame(stake, MEDIUM_RISK_MULTIPLIER, { dice -> (dice % 2 == 0) == even }) { outcome ->
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
        if (stake <= 0 || player.investmentPosition == null) return
        val dice = (1..6).random()
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
        if (amount <= 0) return
        val rate = board().placesOf(player.location).fundRateAtSalary(salaryPosition)
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
        val rateOverride = if (capitalization.landed) START_LANDED_RATE else null
        val (newFunds, profit) = player.funds.capitalize(rateOverride)
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
            copy(loan = loan - amount).minusCash(amount)
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
            updateBoard {
                copy(
                    auction = auction.copy(
                        shares = auction.shares.copy(
                            count = auction.shares.count - bid.count
                        )
                    ),
                    bidList = bidList.filter { it.playerId != bid.playerId }
                )
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
        val minBid = board.bidList.maxOfOrNull { it.bid } ?: auction.getBid
        if (price < minBid) return
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
        val canBuy = currentBoard.activePlayerId == playerId &&
                dream.id !in currentBoard.purchasedDreamIds &&
                (currentBoard.loanLimit + currentPlayer.balance() - currentPlayer.loan - dream.price) > 0
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
        if (currentBoard.dreamById(dreamId) == null) return
        if (dreamId in currentBoard.purchasedDreamIds) return
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
                buyShares(bid.count, auction.shares.copy(count = bid.count, buyPrice = bid.bid))
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
    val playerIds = activePlayers.map { it.id }
    val activePlayerIndex = playerIds.indexOf(board.activePlayerId)
    val nextPlayerId = if (activePlayerIndex < 0 || activePlayerIndex + 1 == playerIds.size) {
        playerIds.first()
    } else {
        playerIds[activePlayerIndex + 1]
    }
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
