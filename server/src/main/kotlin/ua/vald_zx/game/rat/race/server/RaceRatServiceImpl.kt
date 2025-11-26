package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.shared.Event.MoneyIncome
import ua.vald_zx.game.rat.race.card.shared.Event.PlayerChanged

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
        get() = boardState?.value?.id.orEmpty()
    private val globalEventBus: MutableSharedFlow<GlobalEvent>
        get() = getGlobalEventBus(boardId)
    private var boardStateSubJob: Job? = null
    private var boardGlobalEventsJob: Job? = null
    private var boardState: MutableStateFlow<BoardState>? = null
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
                this@RaceRatServiceImpl.boardState = boardState
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

    override suspend fun createBoard(name: String, decks: Map<BoardCardType, Int>): Board {
        val boardState = BoardState(name, cards = decks.map { (type, size) ->
            type to (1..size).toMutableList()
        }.toMap())
        val boardFlow = MutableStateFlow(boardState)
        boards.value = boards.value.toMutableList().apply { add(boardFlow) }
        this@RaceRatServiceImpl.boardState = boardFlow
        return boardState.toBoard()
    }

    override suspend fun selectBoard(boardId: String): Board? {
        return boards.value.find { it.value.id == boardId }
            ?.apply { boardState = this }
            ?.value?.toBoard()
    }

    override suspend fun makePlayerOnBoard() {
        boardState?.value?.let { oldBoard ->
            boardState?.value = oldBoard.copy(
                players = oldBoard.players.toMutableMap()
                    .apply {
                        this[uuid] = Player(uuid, PlayerAttributes(color = getRandomColor()))
                    })
        }
        invalidateNextPlayer(boardState?.value?.activePlayer.orEmpty())
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
        return boardState?.value?.players?.get(id)
    }

    override suspend fun getPlayers(ids: Set<String>): List<Player> {
        return ids.mapNotNull { id -> boardState?.value?.players?.get(id) }
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
        val boardState = boardState ?: return
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
        val boardState = boardState ?: return
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
        val players = boardState?.value?.players.orEmpty()
        if (activePlayer.isEmpty() || !players.containsKey(activePlayer) || players[activePlayer]?.isInactive == true) {
            nextPlayer()
        }
    }

    override suspend fun nextPlayer() {
        val boardState = boardState ?: return
        val oldState = boardState.value
        val activePlayers = oldState.players.filter { (_, player) -> !player.isInactive }
        if (activePlayers.isEmpty()) return
        val playerIds = oldState.players.filter { (_, player) -> !player.isInactive }.keys.toList()
        val activePlayerIndex = playerIds.indexOf(oldState.activePlayer)
        val nextPlayer = if (activePlayerIndex + 1 == playerIds.size) {
            playerIds.first()
        } else {
            playerIds[activePlayerIndex + 1]
        }
        boardState.value =
            oldState.copy(activePlayer = nextPlayer, moveCount = oldState.moveCount + 1)
        if(oldState.takenCard != null) {
            discardPile()
        }
    }

    override suspend fun closeSession() {
        changeCurrentPlayer {
            if (boardState?.value?.activePlayer == uuid) {
                nextPlayer()
            }
            copy(isInactive = true)
        }
        boardState?.value?.let { board ->
            validateBoard(board.id)
        }
    }

    private suspend fun changeCurrentPlayer(todo: suspend Player.() -> Player) {
        val player = boardState?.value?.players[uuid] ?: return
        boardState?.value?.let { oldBoard ->
            boardState?.value = oldBoard.copy(
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
        val otherPlayersColors = boardState?.value
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
        moveCount = moveCount,
    )
}

private fun BoardState.toBoardId(): BoardId {
    return BoardId(
        id = id,
        createDateTime = createDateTime,
        name = name,
    )
}