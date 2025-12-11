package ua.vald_zx.game.rat.race.card.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
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
    val canRoll: Boolean = board.canRoll &&
            currentPlayerIsActive

    init {
        Napier.d("BoardState: $this")
    }
}

sealed class BoardUiAction {
    data class ConfirmDismissal(val business: Business) : BoardUiAction()
    data class ConfirmSellingAllBusiness(val business: Business) : BoardUiAction()
    data class DepositWithdraw(val balance: Long) : BoardUiAction()
    data class LoanAdded(val balance: Long) : BoardUiAction()
    data class ReceivedCash(val amount: Long) : BoardUiAction()
    data class AddCash(val amount: Long) : BoardUiAction()
    data class SubCash(val amount: Long) : BoardUiAction()
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
                        _actions.send(BoardUiAction.ReceivedCash(event.amount))
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
                        invalidatePlayers(event.board.playerIds)
                    }

                    is Event.ConfirmDismissal -> {
                        _actions.send(BoardUiAction.ConfirmDismissal(event.business))
                    }

                    is Event.ConfirmSellingAllBusiness -> {
                        _actions.send(BoardUiAction.ConfirmSellingAllBusiness(event.business))
                    }

                    is Event.DepositWithdraw -> {
                        _actions.send(BoardUiAction.DepositWithdraw(event.balance))
                    }

                    is Event.LoanAdded -> {
                        _actions.send(BoardUiAction.LoanAdded(event.balance))
                    }

                    is Event.AddCash -> {
                        _actions.send(BoardUiAction.AddCash(event.amount))
                    }

                    is Event.SubCash -> {
                        _actions.send(BoardUiAction.SubCash(event.amount))
                    }

                    is Event.BankruptBusiness -> {
                        //todo
                    }
                }
            }
        }
    }

    private suspend fun invalidatePlayers(playerIds: Set<String>) {
        val localIds = players.value.map { player -> player.id }.toSet()
        if (localIds != playerIds) {
            players.value = service.getPlayers()
        }
    }

    fun pass() {
        viewModelScope.launch {
            service.next()
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
            service.next()
        }
    }

    fun buy(card: BoardCard.Shopping) {
        viewModelScope.launch {
            service.buyThing(card)
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

    fun changePosition(position: Int) {
        viewModelScope.launch {
            service.changePosition(position)
        }
    }

    fun dismissalConfirmed(business: Business) {
        viewModelScope.launch {
            service.dismissalConfirmed(business)
        }
    }

    fun sellingAllBusinessConfirmed(business: Business) {
        viewModelScope.launch {
            service.sellingAllBusinessConfirmed(business)
        }
    }
}
