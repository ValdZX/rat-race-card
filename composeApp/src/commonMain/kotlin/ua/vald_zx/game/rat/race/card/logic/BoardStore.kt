package ua.vald_zx.game.rat.race.card.logic

import com.russhwolf.settings.set
import io.github.aakira.napier.Napier
import io.ktor.client.request.url
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import kotlinx.serialization.Serializable
import ua.vald_zx.game.rat.race.card.boardCard
import ua.vald_zx.game.rat.race.card.client
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.invalidServerState
import ua.vald_zx.game.rat.race.card.logic.BoardAction.BackLastMove
import ua.vald_zx.game.rat.race.card.logic.BoardAction.CanTakeSalary
import ua.vald_zx.game.rat.race.card.logic.BoardAction.ChangeColor
import ua.vald_zx.game.rat.race.card.logic.BoardAction.CloseSession
import ua.vald_zx.game.rat.race.card.logic.BoardAction.CreateBoard
import ua.vald_zx.game.rat.race.card.logic.BoardAction.DiceRolled
import ua.vald_zx.game.rat.race.card.logic.BoardAction.HighlightCard
import ua.vald_zx.game.rat.race.card.logic.BoardAction.LoadState
import ua.vald_zx.game.rat.race.card.logic.BoardAction.Move
import ua.vald_zx.game.rat.race.card.logic.BoardAction.RollDice
import ua.vald_zx.game.rat.race.card.logic.BoardAction.SelectBoard
import ua.vald_zx.game.rat.race.card.logic.BoardAction.SelectedCardType
import ua.vald_zx.game.rat.race.card.logic.BoardAction.SendCash
import ua.vald_zx.game.rat.race.card.logic.BoardAction.StartServices
import ua.vald_zx.game.rat.race.card.logic.BoardAction.SwitchLayer
import ua.vald_zx.game.rat.race.card.logic.BoardAction.TakeSalary
import ua.vald_zx.game.rat.race.card.logic.BoardAction.ToDiscardPile
import ua.vald_zx.game.rat.race.card.logic.BoardAction.UpdateBoard
import ua.vald_zx.game.rat.race.card.logic.BoardAction.UpdateCurrentPlayer
import ua.vald_zx.game.rat.race.card.logic.BoardSideEffect.ShowDice
import ua.vald_zx.game.rat.race.card.needStartServerState
import ua.vald_zx.game.rat.race.card.raceRate2BoardStore
import ua.vald_zx.game.rat.race.card.raceRate2KStore
import ua.vald_zx.game.rat.race.card.screen.board.PlaceType
import ua.vald_zx.game.rat.race.card.screen.board.boardLayers
import ua.vald_zx.game.rat.race.card.service
import ua.vald_zx.game.rat.race.card.settings
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.Event
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.shared.PlayerState
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import ua.vald_zx.game.rat.race.card.shared.replaceItem

val players = MutableStateFlow(emptyList<Player>())

enum class BoardLayer(val cellCount: Int, val level: Int) {
    INNER(78, 0),
    OUTER(74, 1),
}

fun Int.toLayer(): BoardLayer {
    return BoardLayer.entries.find { it.level == this } ?: BoardLayer.INNER
}

@Serializable
data class BoardState(
    val positionsHistory: List<Int> = listOf(1),
    val currentPlayer: Player? = players.value.find { it.id == currentPlayerId },
    val highlightedCard: BoardCardType? = null,
    val board: Board? = null,
    val canRoll: Boolean = false,
    val takeSalaryPosition: Int? = null,
) : State {
    val layer: BoardLayer
        get() = currentPlayer?.state?.level?.toLayer() ?: BoardLayer.INNER
    val color: Long
        get() = currentPlayer?.attrs?.color ?: 0
    val currentPlayerIsActive: Boolean
        get() = currentPlayerId == board?.activePlayer
}

sealed class BoardAction : Action {
    data class LoadState(val state: BoardState) : BoardAction()
    data class Move(val dice: Int) : BoardAction()
    data class ChangePosition(val position: Int) : BoardAction()
    data class ChangeColor(val color: Long) : BoardAction()
    data object BackLastMove : BoardAction()
    data object SwitchLayer : BoardAction()
    data object StartServices : BoardAction()
    data object CloseSession : BoardAction()
    data class CreateBoard(val name: String) : BoardAction()
    data class SelectBoard(val boardId: String) : BoardAction()
    data class CanTakeSalary(val salaryPosition: Int) : BoardAction()
    data object TakeSalary : BoardAction()
    data object RollDice : BoardAction()
    data class UpdateCurrentPlayer(val player: Player) : BoardAction()
    data class HighlightCard(val card: BoardCardType) : BoardAction()
    data class SelectedCardType(val card: BoardCardType) : BoardAction()
    data object ToDiscardPile : BoardAction()
    data class UpdateBoard(val board: Board) : BoardAction()
    data class DiceRolled(val dice: Int) : BoardAction()

    data class SendCash(val id: String, val cash: Long) : BoardAction()
}

sealed class BoardSideEffect : Effect {
    data class ShowDice(val dice: Int) : BoardSideEffect()
    data object GotoBoardList : BoardSideEffect()
    data object GotoInitPlayer : BoardSideEffect()
    data object GotoBoard : BoardSideEffect()
}

class BoardStore : Store<BoardState, BoardAction, BoardSideEffect>,
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private val state = MutableStateFlow(BoardState())
    private val sideEffect = MutableSharedFlow<BoardSideEffect>()

    val card = CardStore(state)

    override fun observeState(): StateFlow<BoardState> = state

    override fun observeSideEffect(): Flow<BoardSideEffect> = sideEffect

    init {
        players.onEach { players ->
            players.forEach { player ->
                if (player.id == currentPlayerId) {
                    dispatch(UpdateCurrentPlayer(player))
                }
            }
        }.launchIn(this)
    }

    override fun dispatch(action: BoardAction) {
        val oldState = state.value
        val newState = when (action) {
            is LoadState -> {
                action.state
            }

            is UpdateCurrentPlayer -> {
                oldState.copy(currentPlayer = action.player)
            }

            BackLastMove -> {
                val moves = oldState.positionsHistory
                val newMoves = moves.subList(0, moves.lastIndex)
                val position = newMoves.last()
                launch { service?.changePosition(position, oldState.layer.level) }
                oldState.copy(
                    positionsHistory = newMoves,
                )
            }

            is HighlightCard -> {
                oldState.copy(highlightedCard = action.card)
            }

            is SelectedCardType -> {
                launch { service?.takeCard(action.card) }
                oldState.copy(highlightedCard = null, takeSalaryPosition = null)
            }

            is BoardAction.ChangePosition -> {
                launch {
                    oldState.changePosition(action.position)
                }
                oldState
            }
            is Move -> {
                val position = moveTo(
                    oldState.currentPlayer?.state?.position ?: 1,
                    oldState.layer.cellCount,
                    action.dice
                )
                launch {
                    oldState.changePosition(position)
                }
                oldState.copy(
                    positionsHistory = oldState.positionsHistory + position,
                )
            }

            is CanTakeSalary -> {
                oldState.copy(takeSalaryPosition = action.salaryPosition)
            }

            is TakeSalary -> {
                card.dispatch(CardAction.GetSalaryApproved)
                launch { service?.nextPlayer() }
                oldState.copy(takeSalaryPosition = null)
            }

            SwitchLayer -> {
                val currentLayer = oldState.layer
                val layer = if (currentLayer == BoardLayer.INNER) {
                    BoardLayer.OUTER
                } else {
                    BoardLayer.INNER
                }
                launch { service?.changePosition(1, layer.level) }
                oldState
            }

            is ChangeColor -> {
                launch { service?.updateAttributes(PlayerAttributes(color = action.color)) }
                oldState
            }

            ToDiscardPile -> {
                launch {
                    service?.discardPile()
                    service?.nextPlayer()
                }
                oldState//todo
            }

            is DiceRolled -> {
                launch { sideEffect.emit(ShowDice(action.dice)) }
                oldState
            }

            RollDice -> {
                launch { service?.rollDice() }
                oldState.copy(canRoll = false)
            }

            is UpdateBoard -> {
                val rollCountChanged =
                    oldState.board?.moveCount != action.board.moveCount || action.board.moveCount == 0
                val canRoll = oldState.canRoll || rollCountChanged &&
                        action.board.activePlayer == currentPlayerId &&
                        action.board.takenCard == null &&
                        oldState.highlightedCard == null
                launch {
                    service?.updatePlayers(action.board.players)
                }
                oldState.copy(board = action.board, canRoll = canRoll)
            }

            is SendCash -> {
                launch {
                    service?.sendMoney(action.id, action.cash)
                    card.dispatch(CardAction.SideExpenses(action.cash))
                }
                oldState
            }

            StartServices -> {
                val handler = CoroutineExceptionHandler { _, t ->
                    Napier.e("Invalid server", t)
                    invalidServerState.value = true
                    needStartServerState.value = false
                }
                CoroutineScope(Dispatchers.Default).launch(handler) {
                    startService()
                }
                oldState
            }

            is CreateBoard -> {
                launch {
                    service?.createBoard(action.name)?.let { board ->
                        loadBoard(board)
                    }
                }
                oldState
            }

            is SelectBoard -> {
                launch {
                    service?.selectBoard(action.boardId)?.let { board ->
                        loadBoard(board)
                    }
                }
                oldState
            }

            CloseSession -> {
                launch {
                    if (!invalidServerState.value) {
                        service?.closeSession()
                    }
                }
                oldState
            }
        }
        if (newState != oldState) {
            state.value = newState
        }
    }

    private suspend fun BoardState.changePosition(position: Int) {
        service?.changePosition(position, layer.level)
        delay(500)
        currentPlayer?.state?.position?.let { currentPosition ->
            val layer = boardLayers.layers[layer]
            val list = if (currentPosition > position) {
                layer?.places?.subList(currentPosition + 1, layer.places.size)
                    .orEmpty() +
                        layer?.places?.subList(0, position + 1).orEmpty()
            } else {
                layer?.places?.subList(currentPosition + 1, position + 1)
            }
            val salaryContains = list?.contains(PlaceType.Salary) ?: false
            if (salaryContains) {
                var salaryPosition =
                    currentPosition + list.indexOf(PlaceType.Salary) + 1
                val placeCount = layer?.places?.size ?: 0
                if (salaryPosition >= placeCount) {
                    salaryPosition = salaryPosition - placeCount
                }
                dispatch(CanTakeSalary(salaryPosition))
            }
        }
        boardLayers.layers[layer]?.places[position]?.let { place ->
            processPlace(place)
        }
    }
    private suspend fun startService() {
        service = client.rpc {
            url("wss://race-rat-online-1033277102369.us-central1.run.app/api")
            rpcConfig {
                serialization {
                    json()
                }
            }
        }.withService()
        val instance = service?.hello(currentPlayerId) ?: return
        settings["currentPlayerId"] = instance.id
        if (instance.boardId.isEmpty()) {
            sideEffect.emit(BoardSideEffect.GotoBoardList)
        } else {
            service?.selectBoard(instance.boardId)?.let { board ->
                loadBoard(board)
            }
        }
        service?.eventsObserve()?.onEach { event ->
            when (event) {
                is Event.MoneyIncome -> {
                    card.dispatch(
                        CardAction.ReceivedCash(
                            payerId = event.playerId,
                            amount = event.amount
                        )
                    )
                }

                is Event.PlayerChanged -> {
                    val playersList = players.value
                    val changedPlayer = event.player
                    val oldPlayer = playersList.find { it.id == changedPlayer.id }
                    if (oldPlayer != null) {
                        players.value = playersList.replaceItem(oldPlayer, event.player)
                    } else {
                        players.value = players.value + event.player
                    }
                    if (changedPlayer.id == currentPlayerId) {
                        raceRate2BoardStore.dispatch(UpdateCurrentPlayer(changedPlayer))
                    }
                }

                is Event.RollDice -> {
                    raceRate2BoardStore.dispatch(DiceRolled(event.dice))
                }

                is Event.BoardChanged -> {
                    raceRate2BoardStore.dispatch(UpdateBoard(event.board))
                }
            }
        }?.launchIn(this)
        val state = raceRate2KStore.get()
        val professionCard = state?.playerCard
        if (professionCard?.profession?.isNotEmpty() == true) {
            service?.updatePlayerCard(professionCard)
            service?.updateState(state.toState())
        }
    }

    private fun processPlace(place: PlaceType) = launch {
        when (place) {
            PlaceType.Bankruptcy -> {
                service?.nextPlayer()
            }

            PlaceType.BigBusiness -> {
                service?.nextPlayer()
            }

            PlaceType.Business -> {
                dispatch(HighlightCard(BoardCardType.SmallBusiness))
            }

            PlaceType.Chance -> {
                dispatch(HighlightCard(BoardCardType.Chance))
            }

            PlaceType.Child -> {
                service?.nextPlayer()
            }

            PlaceType.Deputy -> {
                service?.nextPlayer()
            }

            PlaceType.Desire -> {
                service?.nextPlayer()
            }

            PlaceType.Divorce -> {
                service?.nextPlayer()
            }

            PlaceType.Exaltation -> {
                service?.nextPlayer()
            }

            PlaceType.Expenses -> {
                dispatch(HighlightCard(BoardCardType.Expenses))
            }

            PlaceType.Love -> {
                service?.nextPlayer()
            }

            PlaceType.Rest -> {
                service?.nextPlayer()
            }

            PlaceType.Salary -> {
                //TODO
            }

            PlaceType.Shopping -> {
                dispatch(HighlightCard(BoardCardType.Shopping))
            }

            PlaceType.Start -> {
                //todo
            }

            PlaceType.Store -> {
                dispatch(HighlightCard(BoardCardType.EventStore))
            }

            PlaceType.TaxInspection -> {
                //todo
            }
        }
    }

    suspend fun loadBoard(board: Board) {
        raceRate2BoardStore.dispatch(UpdateBoard(board))
        val storedCard = boardCard(board.id).get()
        raceRate2BoardStore.card.dispatch(CardAction.LoadState(storedCard))
        val player = service?.getPlayer(currentPlayerId)
        if (player == null || player.playerCard.name.isEmpty()) {
            service?.makePlayerOnBoard()
            sideEffect.emit(BoardSideEffect.GotoInitPlayer)
        } else {
            sideEffect.emit(BoardSideEffect.GotoBoard)
        }
    }

    fun RatRace2CardState.toState(): PlayerState {
        return PlayerState(
            totalExpenses = total(),
            cashFlow = cashFlow()
        )
    }

    suspend fun RaceRatService.updatePlayers(actualIds: Set<String>) {
        val oldPlayers = players.value
        val unknownKeys = actualIds - oldPlayers.map { it.id }
        val actualPlayers = oldPlayers.filter { actualIds.contains(it.id) }
        val unknownPlayers = if (unknownKeys.isNotEmpty()) getPlayers(unknownKeys) else emptyList()
        players.value = actualPlayers + unknownPlayers
    }
}

fun moveTo(position: Int, cellCount: Int, toMove: Int): Int {
    val nextPosition = position + toMove
    return if (nextPosition < 0) {
        cellCount + nextPosition
    } else if (cellCount <= nextPosition) {
        nextPosition - cellCount
    } else {
        nextPosition
    }
}
