@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.shared.Event.MoneyIncome
import ua.vald_zx.game.rat.race.card.shared.Event.PlayerChanged
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal val LOGGER = KtorSimpleLogger("RaceRatService")

val globalEventBusMap = mutableMapOf<String, MutableSharedFlow<GlobalEvent>>()
fun getGlobalEventBus(boardId: String): MutableSharedFlow<GlobalEvent> {
    return globalEventBusMap.getOrPut(boardId) { MutableSharedFlow() }
}

class RaceRatServiceImpl(
    private val uuid: String,
) : RaceRatService, CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private val eventBus = MutableSharedFlow<Event>()
    private val boardId: String
        get() = boardState?.value?.id.orEmpty()
    private val globalEventBus: MutableSharedFlow<GlobalEvent>
        get() = getGlobalEventBus(boardId)
    private var boardStateSubJob: Job? = null
    private var playerStateSubJob: Job? = null
    private var boardGlobalEventsJob: Job? = null

    private val player: Player
        get() = players.value[uuid]?.value ?: error("No player found for $uuid")
    private val board: Board
        get() = boardState?.value ?: error("No board found")
    private var boardState: MutableStateFlow<Board>? = null
        set(value) {
            field = value
            if (value != null) {
                boardStateSubJob?.cancel()
                boardStateSubJob = launch {
                    value.collect { boardState ->
                        eventBus.emit(Event.BoardChanged(boardState))
                    }
                }
                boardGlobalEventsJob?.cancel()
                boardGlobalEventsJob = launch {
                    globalEventBus.collect { event ->
                        when (event) {
                            is GlobalEvent.MoneyIncome -> {
                                if (event.receiverId == uuid) {
                                    eventBus.emit(MoneyIncome(event.playerId, event.amount))
                                }
                            }

                            is GlobalEvent.PlayerChanged -> {
                                eventBus.emit(PlayerChanged(event.player))
                            }
                        }
                    }
                }
            }
        }

    private fun Board.players(): List<Player> {
        return playerIds.map { playerId ->
            players[playerId]
        }
    }

    override suspend fun hello(id: String): Instance {
        boards.value.forEach { boardState ->
            val oldBoard = boardState.value
            if (oldBoard.playerIds.contains(id)) {
                this@RaceRatServiceImpl.boardState = boardState
                val playerState = players.value[id] ?: error("No player found for $id")
                playerState.update { player ->
                    player.copy(id = uuid)
                }
                players.value = players.value.toMutableMap().apply {
                    this.remove(id)
                    this[uuid] = playerState
                }
                changeBoard {
                    copy(playerIds = playerIds - id + uuid)
                }
                playerStateSubJob?.cancel()
                playerStateSubJob = launch {
                    playerState.collect { player ->
                        globalEventBus.emit(GlobalEvent.PlayerChanged(player))
                    }
                }
                invalidateNextPlayer(oldBoard.activePlayer)
                return Instance(uuid, oldBoard, player)
            }
        }
        return Instance(uuid, null, null)
    }

    override suspend fun getBoards(): List<BoardId> {
        return boards.value.map { boardState ->
            val board = boardState.value
            BoardId(board.id, board.name, board.createDateTime)
        }
    }

    override fun observeBoards(): Flow<List<BoardId>> = boards.mapNotNull { list ->
        list.map { board ->
            board.value.toBoardId()
        }
    }

    override suspend fun createBoard(name: String, decks: Map<BoardCardType, Int>): Board {
        val board = Board(
            name = name,
            createDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            id = Uuid.random().toString(),
            cards = decks.map { (type, size) ->
                type to (1..size).toMutableList()
            }.toMap()
        )
        val boardFlow = MutableStateFlow(board)
        boards.value = boards.value.toMutableList().apply { add(boardFlow) }
        boardState = boardFlow
        return board
    }

    override suspend fun selectBoard(boardId: String): Board {
        return boards.value.find { it.value.id == boardId }
            ?.apply { boardState = this }
            ?.value ?: error("No board found for $boardId")
    }

    override suspend fun makePlayer(
        name: String,
        gender: Gender,
        color: Long
    ): Player {
        val newPlayer = Player(
            id = uuid,
            boardId = board.id,
            attrs = PlayerAttributes(color = color),
            card = PlayerCard(name = name, gender = gender),
            businesses = listOf(
                Business(
                    type = BusinessType.WORK,
                    name = "",
                    price = 0,
                    profit = 1234
                )
            )//TODO profession card
        )
        players.value = players.value.toMutableMap().apply {
            this[uuid] = MutableStateFlow(newPlayer)
        }
        changeBoard {
            copy(playerIds = playerIds + uuid)
        }
        invalidateNextPlayer(boardState?.value?.activePlayer.orEmpty())
        return newPlayer
    }

    override suspend fun getPlayer(): Player {
        return player
    }

    override suspend fun updateAttributes(attrs: PlayerAttributes) = changePlayer {
        copy(attrs = attrs)
    }

    override fun eventsObserve(): Flow<Event> = eventBus

    override suspend fun getPlayers(): List<Player> {
        return boardState?.value?.playerIds?.map { playerId ->
            players.value[playerId]?.value ?: error("No player found for $playerId")
        }?.toList().orEmpty()
    }

    override suspend fun getBoard(): Board {
        return board
    }

    override suspend fun sendMoney(receiverId: String, amount: Long) {
        globalEventBus.emit(GlobalEvent.MoneyIncome(uuid, receiverId, amount))
    }

    override suspend fun rollDice() {
        changeBoard {
            val dice = (1..6).random()
            copy(dice = dice)
        }
    }

    override suspend fun takeCard(cardType: BoardCardType) {
        val card = board.cards[cardType]?.random() ?: return
        changeBoard {
            copy(
                cards = cards.apply {
                    this[cardType]?.remove(card)
                },
                takenCard = CardLink(cardType, card)
            )
        }
    }

    override suspend fun discardPile() {
        val boardState = boardState ?: return
        val oldState = boardState.value
        val takenCard = oldState.takenCard ?: return
        val discard = oldState.discard.toMutableMap()
        discard[takenCard.type] = oldState.discard[takenCard.type].orEmpty() + takenCard.id
        boardState.value = oldState.copy(discard = discard, takenCard = null)
    }

    private suspend fun invalidateNextPlayer(activePlayer: String) {
        val playerIds = boardState?.value?.playerIds.orEmpty()
        if (activePlayer.isEmpty() || !playerIds.contains(activePlayer) || players[activePlayer].isInactive) {
            nextPlayer()
        }
    }

    suspend fun nextPlayer() {
        val activePlayers = board.players().filter { player -> !player.isInactive }
        if (activePlayers.isEmpty()) return
        val playerIds = activePlayers.map { it.id }
        val activePlayerIndex = playerIds.indexOf(board.activePlayer)
        val nextPlayer = if (activePlayerIndex + 1 == playerIds.size) {
            playerIds.first()
        } else {
            playerIds[activePlayerIndex + 1]
        }
        changeBoard {
            copy(activePlayer = nextPlayer, moveCount = moveCount + 1)
        }
        if (board.takenCard != null) {
            discardPile()
        }
    }

    override suspend fun closeSession() {
        changePlayer {
            if (boardState?.value?.activePlayer == uuid) {
                nextPlayer()
            }
            copy(isInactive = true)
        }
        boardState?.value?.let { board ->
            validateBoard(board.id)
        }
    }

    override suspend fun takeSalary() {
        changeBoard {
            copy(salaryPosition = null)
        }
        changePlayer {
            val cashFlow = cashFlow()
            if (cashFlow >= 0) {
                plusCash(cashFlow)
            } else {
                minusCash(cashFlow.absoluteValue)
            }
        }
    }

    override suspend fun buyBusiness(business: Business) {
        val currentBusiness = player.businesses
        if (business.type == BusinessType.SMALL
            && currentBusiness.any { it.type == BusinessType.WORK }
            && currentBusiness.count { it.type == BusinessType.SMALL } == 1
        ) {
            eventBus.emit(Event.ConfirmDismissal(business))
        } else if (currentBusiness.isNotEmpty()
            && currentBusiness.first().type.klass != business.type.klass
            && !currentBusiness.any { it.type == BusinessType.WORK }
        ) {
            eventBus.emit(Event.ConfirmSellingAllBusiness(business))
        } else {
            changePlayer {
                copy(businesses = currentBusiness + business).minusCash(business.price)
            }
        }
    }

    override suspend fun dismissalConfirmed(business: Business) {
        changePlayer {
            val businesses = player.businesses.filter { it.type != BusinessType.WORK } + business
            copy(businesses = businesses).minusCash(business.price)
        }
    }

    override suspend fun sellingAllBusinessConfirmed(business: Business) {
        changePlayer {
            copy(businesses = listOf(business))
                .plusCash(player.businesses.sumOf { it.price })
                .minusCash(business.price)
        }
    }

    override suspend fun move() {
        val layer = player.location.level.toLayer()
        val cellCount = layer.cellCount
        val currentPosition = player.location.position
        val newPosition = moveTo(currentPosition, cellCount, board.dice)
        val list = if (currentPosition > newPosition) {
            layer.places.subList(currentPosition + 1, layer.places.size) + layer.places.subList(0, newPosition + 1)
        } else {
            layer.places.subList(currentPosition + 1, newPosition + 1)
        }
        val salaryPosition = if (list.contains(PlaceType.Salary)) {
            var salaryPosition =
                currentPosition + list.indexOf(PlaceType.Salary) + 1
            val placeCount = layer.places.size
            if (salaryPosition >= placeCount) {
                salaryPosition -= placeCount
            }
            salaryPosition
        } else null
        changePlayer {
            copy(location = location.copy(position = newPosition))
        }
        val place = layer.places[newPosition]
        changeBoard {
            copy(
                moveCount = moveCount + 1,
                salaryPosition = salaryPosition,
                canTakeCard = getTakeCardType(place)
            )
        }
    }

    override suspend fun minusCash(price: Long) {
        changePlayer {
            minusCash(price)
        }
    }

    private fun getTakeCardType(place: PlaceType): BoardCardType? {
        return when (place) {
            PlaceType.BigBusiness -> BoardCardType.BigBusiness
            PlaceType.Business -> BoardCardType.SmallBusiness
            PlaceType.Chance -> BoardCardType.Chance
            PlaceType.Deputy -> BoardCardType.Deputy
            PlaceType.Expenses -> BoardCardType.Expenses
            PlaceType.Shopping -> BoardCardType.Shopping
            PlaceType.Store -> BoardCardType.EventStore
            else -> null
        }
    }

    private suspend fun changeBoard(todo: suspend Board.() -> Board) {
        boardState?.value?.let { oldBoard ->
            boardState?.value = oldBoard.todo()
        }
    }

    private suspend fun changePlayer(todo: suspend Player.() -> Player) {
        players.value[uuid]?.let { playerState ->
            val player = playerState.value.todo()
            playerState.value = player
            globalEventBus.emit(GlobalEvent.PlayerChanged(player))
        }
    }

    private fun Player.plusCash(value: Long): Player {
        return copy(cash = cash + value)
    }

    private fun Player.minusCash(
        value: Long,
        isFundBuy: Boolean = false
    ): Player {
        if (value == 0L) return this
        launch { eventBus.emit(Event.SubCash(value)) }
        return if (cash > value) {
            copy(cash = cash - value)
        } else if ((cash + deposit) > value) {
            launch { eventBus.emit(Event.DepositWithdraw(value - cash)) }
            copy(cash = 0, deposit = (deposit + cash) - value)
        } else if (!isFundBuy && config.hasFunds && funds.isNotEmpty()) {
            var stub = cash + deposit
            var newFunds = funds.toList()
            funds.sortedBy { it.rate }.firstOrNull { fund ->
                if (stub + fund.amount > value) {
                    newFunds = funds.replace(fund, fund.copy(amount = stub + fund.amount - value))
                    true
                } else {
                    stub += fund.amount
                    newFunds = newFunds.remove(fund)
                    false
                }
            }
            if (newFunds.isEmpty() && stub < value) {
                copy(cash = 0, deposit = 0, funds = emptyList(), loan = loan + (value - stub))
            } else {
                copy(cash = 0, deposit = 0, funds = newFunds)
            }
        } else {
            launch { eventBus.emit(Event.LoanAdded(value - (cash + deposit))) }
            copy(cash = 0, deposit = 0, loan = loan + (value - (deposit + cash)))
        }
    }

    override suspend fun buy(card: BoardCard.Shopping) {
        when (card.shopType) {
            ShopType.AUTO -> {
                changePlayer {
                    copy(cars = cars + 1).minusCash(card.price)
                }
            }

            ShopType.HOUSE -> {
                changePlayer {
                    copy(cottage = cottage + 1).minusCash(card.price)
                }
            }

            ShopType.APARTMENT -> {
                changePlayer {
                    copy(apartment = apartment + 1).minusCash(card.price)
                }
            }

            ShopType.YACHT -> {
                changePlayer {
                    copy(yacht = yacht + 1).minusCash(card.price)
                }
            }

            ShopType.FLY -> {
                changePlayer {
                    copy(flight = flight + 1).minusCash(card.price)
                }
            }
        }
    }
}