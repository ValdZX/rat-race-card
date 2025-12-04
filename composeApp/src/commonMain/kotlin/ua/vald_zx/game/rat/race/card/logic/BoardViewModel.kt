package ua.vald_zx.game.rat.race.card.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.shared.*

val players = MutableStateFlow(emptyList<Player>())

data class BoardState(
    val board: Board,
    val player: Player,
) {
    val layer: BoardLayer = player.location.level.toLayer()
    val color: Long = player.attrs.color
    val currentPlayerIsActive: Boolean = currentPlayerId == board.activePlayer
    val canRoll: Boolean = board.canRoll && currentPlayerIsActive
}

sealed class BoardUiAction {
    data class ShowDice(val dice: Int) : BoardUiAction()
}

class BoardViewModel(
    board: Board,
    player: Player,
    private val service: RaceRatService
) : ViewModel() {


    private val _uiState = MutableStateFlow(BoardState(board, player))
    val uiState: StateFlow<BoardState> = _uiState.asStateFlow()

    private val _actions = Channel<BoardUiAction>()
    val actions = _actions.receiveAsFlow()

    init {
        viewModelScope.launch {
            players.value = service.getPlayers()
            _uiState.update { it.copy(board = service.getBoard()) }
            service.eventsObserve().collect { event ->
                when (event) {
                    is Event.MoneyIncome -> {
//                    card.dispatch(
//                        ReceivedCash(
//                            payerId = event.playerId,
//                            amount = event.amount
//                        )
//                    )
                    }

                    is Event.PlayerChanged -> {
                        val playersList = players.value
                        val changedPlayer = event.player
                        val oldPlayer = playersList.find { it.id == changedPlayer.id }
                        if (oldPlayer != null) {
                            players.value = playersList.replaceItem(oldPlayer, event.player)
                        } else {
                            players.value += event.player
                        }
                        if (changedPlayer.id == currentPlayerId) {
                            _uiState.update { it.copy(player = changedPlayer) }
                        }
                    }

                    is Event.BoardChanged -> {
                        _uiState.update { it.copy(board = event.board) }
                    }

                    is Event.ConfirmDismissal -> TODO()
                    is Event.ConfirmSellingAllBusiness -> TODO()
                    is Event.DepositWithdraw -> TODO()
                    is Event.LoanAdded -> TODO()
                    is Event.SubCash -> TODO()
                }
            }
        }
    }

    fun discardPile() {
        viewModelScope.launch {
            service.discardPile()
        }
    }

    fun rollDice() {
        viewModelScope.launch {
            service.rollDice()
        }
    }

    fun move() {
        viewModelScope.launch {
            service.move()
        }
    }

    fun buyBusiness(business: Business) {
        viewModelScope.launch {
            service.buyBusiness(business)
        }
    }

    fun sideExpenses(price: Long) {
        viewModelScope.launch {
            service.minusCash(price)
        }
    }

    fun buy(card: BoardCard.Shopping) {
        viewModelScope.launch {
            service.buy(card)
        }
    }

    fun changePlayerColor(value: Long) {
        viewModelScope.launch {
            service.updateAttributes(uiState.value.player.attrs.copy(color = value))
        }
    }

    fun selectCard(cardType: BoardCardType) {
        viewModelScope.launch {
            service.takeCard(cardType)
        }
    }

    fun takeSalary() {
        viewModelScope.launch {
            service.takeSalary()
        }
    }
}
