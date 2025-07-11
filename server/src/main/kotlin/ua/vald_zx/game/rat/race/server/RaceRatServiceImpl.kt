package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.BoardId
import ua.vald_zx.game.rat.race.card.shared.CardLink
import ua.vald_zx.game.rat.race.card.shared.Event
import ua.vald_zx.game.rat.race.card.shared.Event.MoneyIncome
import ua.vald_zx.game.rat.race.card.shared.Event.PlayerChanged
import ua.vald_zx.game.rat.race.card.shared.GlobalEvent
import ua.vald_zx.game.rat.race.card.shared.Instance
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.shared.PlayerCard
import ua.vald_zx.game.rat.race.card.shared.PlayerState
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import ua.vald_zx.game.rat.race.card.shared.pointerColors

internal val LOGGER = KtorSimpleLogger("RaceRatService")

private val globalEventBusMap = mutableMapOf<String, MutableSharedFlow<GlobalEvent>>()
fun getGlobalEventBus(boardId: String): MutableSharedFlow<GlobalEvent> {
    return globalEventBusMap.getOrPut(boardId) { MutableSharedFlow() }
}

class RaceRatServiceImpl(
    private val uuid: String,
) : RaceRatService, CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private val eventBus = MutableSharedFlow<Event>()
    private val boardId: String
        get() = currentBoardState?.value?.id.orEmpty()
    private val globalEventBus: MutableSharedFlow<GlobalEvent>
        get() = getGlobalEventBus(boardId)
    private var boardStateSubJob: Job? = null
    private var boardGlobalEventsJob: Job? = null
    private var currentBoardState: MutableStateFlow<BoardState>? = null
        set(value) {
            field = value
            if (value != null) {
                boardStateSubJob?.cancel()
                boardStateSubJob = launch {
                    value.collect { boardState ->
                        eventBus.emit(Event.BoardChanged(boardState.toBoard()))
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

                            is GlobalEvent.RollDice -> {
                                eventBus.emit(Event.RollDice(event.dice))
                            }
                        }
                    }
                }
            }
        }

    override suspend fun hello(id: String): Instance {
        boards.value.forEach { boardState ->
            val oldBoard = boardState.value
            if (oldBoard.players.containsKey(id)) {
                currentBoardState = boardState
                oldBoard.players[id]?.let { oldPlayer ->
                    boardState.value =
                        oldBoard.copy(players = oldBoard.players.toMutableMap().apply {
                            this.remove(id)
                            this[uuid] = oldPlayer.copy(id = uuid, isInactive = false)
                        })
                }
                invalidateNextPlayer(oldBoard.activePlayer)
                return Instance(uuid, oldBoard.id)
            }
        }
        return Instance(uuid, "")
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

    override suspend fun createBoard(name: String): Board {
        val boardState = BoardState(name)
        val boardFlow = MutableStateFlow(boardState)
        boards.value = boards.value.toMutableList().apply { add(boardFlow) }
        currentBoardState = boardFlow
        return boardState.toBoard()
    }

    override suspend fun selectBoard(boardId: String): Board? {
        return boards.value.find { it.value.id == boardId }
            ?.apply { currentBoardState = this }
            ?.value?.toBoard()
    }

    override suspend fun makePlayerOnBoard(): Set<String> {
        currentBoardState?.value?.let { oldBoard ->
            currentBoardState?.value = oldBoard.copy(
                players = oldBoard.players.toMutableMap()
                    .apply {
                        this[uuid] = Player(uuid, PlayerAttributes(color = getRandomColor()))
                    })
        }
        invalidateNextPlayer(currentBoardState?.value?.activePlayer.orEmpty())
        return currentBoardState?.value?.players?.keys.orEmpty()
    }

    override suspend fun updatePlayerCard(playerCard: PlayerCard) = changeCurrentPlayer {
        copy(playerCard = playerCard)
    }

    override suspend fun updateState(state: PlayerState) = changeCurrentPlayer {
        copy(state = state)
    }

    override suspend fun updateAttributes(attrs: PlayerAttributes) = changeCurrentPlayer {
        copy(attrs = attrs)
    }

    override fun eventsObserve(): Flow<Event> = eventBus

    override suspend fun getPlayer(id: String): Player? {
        return currentBoardState?.value?.players?.get(id)
    }

    override suspend fun sendMoney(receiverId: String, amount: Long) {
        globalEventBus.emit(GlobalEvent.MoneyIncome(uuid, receiverId, amount))
    }

    override suspend fun rollDice(): Int {
        val dice = (1..6).random()
        globalEventBus.emit(GlobalEvent.RollDice(uuid, dice))
        return dice
    }

    override suspend fun changePosition(position: Int, level: Int) = changeCurrentPlayer {
        copy(state = state.copy(position = position, level = level))
    }

    override suspend fun takeCard(cardType: BoardCardType) {
        val boardState = currentBoardState ?: return
        val oldState = boardState.value
        val someCard = oldState.cards[cardType]?.random() ?: return
        boardState.value = oldState.copy(
            cards = oldState.cards.apply {
                this[cardType]?.remove(someCard)
            },
            takenCard = CardLink(cardType, someCard)
        )
    }

    override suspend fun discardPile() {
        val boardState = currentBoardState ?: return
        val oldState = boardState.value
        val takenCard = oldState.takenCard ?: return
        boardState.value = oldState.copy(
            discard = oldState.discard.apply {
                this.getOrPut(takenCard.type, { mutableListOf() }).add(takenCard.id)
            },
            takenCard = null
        )
    }

    private suspend fun invalidateNextPlayer(activePlayer: String) {
        val players = currentBoardState?.value?.players.orEmpty()
        if (activePlayer.isEmpty() || !players.containsKey(activePlayer)) {
            nextPlayer()
        }
    }

    override suspend fun nextPlayer() {
        val boardState = currentBoardState ?: return
        val oldState = boardState.value
        val activePlayers = oldState.players.filter { (_, player) -> !player.isInactive }
        if (activePlayers.isEmpty()) return
        val playerIds = oldState.players.keys.toList()
        val activePlayerIndex = playerIds.indexOf(oldState.activePlayer)
        val nextPlayer = if (activePlayerIndex + 1 == playerIds.size) {
            playerIds.first()
        } else {
            playerIds[activePlayerIndex + 1]
        }
        if (oldState.players[nextPlayer]?.isInactive == true) {
            nextPlayer()
        } else {
            boardState.value = boardState.value.copy(activePlayer = nextPlayer)
        }
    }

    override suspend fun closeSession() {
        changeCurrentPlayer {
            if (currentBoardState?.value?.activePlayer == uuid) {
                nextPlayer()
            }
            copy(isInactive = true)
        }
        currentBoardState?.value?.let { board ->
            validateBoard(board.id)
        }
    }

    private suspend fun changeCurrentPlayer(todo: suspend Player.() -> Player) {
        val player = currentBoardState?.value?.players[uuid] ?: return
        currentBoardState?.value?.let { oldBoard ->
            currentBoardState?.value = oldBoard.copy(
                players = oldBoard.players.toMutableMap()
                    .apply {
                        val updatedPlayer = player.todo()
                        this[uuid] = updatedPlayer
                        globalEventBus.emit(GlobalEvent.PlayerChanged(updatedPlayer))
                    }
            )
        }
    }

    private fun getRandomColor(): Long {
        val otherPlayersColors = currentBoardState?.value
            ?.players?.values
            ?.map { player -> player.attrs.color }
            ?.toSet().orEmpty()
        return (pointerColors - otherPlayersColors).random()
    }
}

private fun BoardState.toBoard(): Board {
    return Board(
        id = id,
        createDateTime = createDateTime,
        name = name,
        cards = cards,
        discard = discard,
        players = players.keys,
        activePlayer = activePlayer,
        takenCard = takenCard,
    )
}

private fun BoardState.toBoardId(): BoardId {
    return BoardId(
        id = id,
        createDateTime = createDateTime,
        name = name,
    )
}