package ua.vald_zx.game.rat.race.card.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.vald_zx.game.rat.race.card.logic.BoardUiAction.*
import ua.vald_zx.game.rat.race.card.shared.*

val players = MutableStateFlow(emptyList<Player>())

data class BoardState(
    val board: Board,
    val player: Player,
) {
    val layer: BoardLayer = player.location.level.toLayer()
    val color: Long = player.attrs.color
    val currentPlayerIsActive: Boolean by lazy { player.id == board.activePlayer }
    val canRoll: Boolean by lazy { board.canRoll && currentPlayerIsActive }

    fun canPay(price: Long): Boolean {
        return (board.loanLimit + player.balance() - player.loan - price) > 0
    }

    fun canBuyBusiness(): Boolean {
        return player.businesses.size <= board.businessLimit
    }
}

sealed class BoardUiAction {
    data class ConfirmDismissal(val business: Business) : BoardUiAction()
    data class BankruptBusiness(val business: Business) : BoardUiAction()
    data class ConfirmSellingAllBusiness(val business: Business) : BoardUiAction()
    data class DepositWithdraw(val balance: Long) : BoardUiAction()
    data class LoanAdded(val balance: Long) : BoardUiAction()
    data class ReceivedCash(val receiverId: String, val amount: Long) : BoardUiAction()
    data class AddCash(val amount: Long) : BoardUiAction()
    data class SubCash(val amount: Long) : BoardUiAction()
    data class PlayerDivorced(val playerName: String) : BoardUiAction()
    data class PlayerMarried(val playerName: String) : BoardUiAction()
    data class PlayerHadBaby(val playerName: String, val babies: Long) : BoardUiAction()
    data object YouDivorced : BoardUiAction()
    data object CongratulationsWithBaby : BoardUiAction()
    data object CongratulationsWithMarriage : BoardUiAction()
    data object LoanOverlimited : BoardUiAction()
    data class Resignation(val business: Business) : BoardUiAction()
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

    fun init() {
        viewModelScope.launch {
            players.value = service.getPlayers()
            _uiState.update { it.copy(board = service.getBoard()) }
            service.eventsObserve().collect { event ->
                when (event) {
                    is Event.MoneyIncome -> {
                        _actions.send(ReceivedCash(event.playerId, event.amount))
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
                        if (changedPlayer.id == _uiState.value.player.id) {
                            _uiState.update { it.copy(player = changedPlayer) }
                        }
                    }

                    is Event.BoardChanged -> {
                        _uiState.update { it.copy(board = event.board) }
                        invalidatePlayers(event.board.playerIds)
                    }

                    is Event.ConfirmDismissal -> {
                        _actions.send(ConfirmDismissal(event.business))
                    }

                    is Event.ConfirmSellingAllBusiness -> {
                        _actions.send(ConfirmSellingAllBusiness(event.business))
                    }

                    is Event.DepositWithdraw -> {
                        _actions.send(DepositWithdraw(event.balance))
                    }

                    is Event.LoanAdded -> {
                        _actions.send(LoanAdded(event.balance))
                    }

                    is Event.AddCash -> {
                        _actions.send(AddCash(event.amount))
                    }

                    is Event.SubCash -> {
                        _actions.send(SubCash(event.amount))
                    }

                    is Event.BankruptBusiness -> {
                        _actions.send(BankruptBusiness(event.business))
                    }

                    is Event.PlayerDivorced -> {
                        if (event.playerId == _uiState.value.player.id) {
                            _actions.send(YouDivorced)
                        } else {
                            players.value.find { it.id == event.playerId }?.let { player ->
                                _actions.send(PlayerDivorced(player.card.name))
                            }
                        }
                    }

                    is Event.PlayerHadBaby -> {
                        if (event.playerId == _uiState.value.player.id) {
                            _actions.send(CongratulationsWithBaby)
                        } else {
                            players.value.find { it.id == event.playerId }?.let { player ->
                                _actions.send(PlayerHadBaby(player.card.name, event.babies))
                            }
                        }
                    }

                    is Event.PlayerMarried -> {
                        if (event.playerId == _uiState.value.player.id) {
                            _actions.send(CongratulationsWithMarriage)
                        } else {
                            players.value.find { it.id == event.playerId }?.let { player ->
                                _actions.send(PlayerMarried(player.card.name))
                            }
                        }
                    }

                    is Event.Resignation -> {
                        _actions.send(Resignation(event.business))
                    }

                    Event.LoanOverlimited -> {
                        _actions.send(LoanOverlimited)
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

    fun passLand() {
        viewModelScope.launch {
            service.passLand()
        }
    }

    fun passEstate() {
        viewModelScope.launch {
            service.passEstate()
        }
    }

    fun passShares(sharesType: SharesType) {
        viewModelScope.launch {
            service.passShares(sharesType)
        }
    }

    fun rollDice() {
        viewModelScope.launch {
            service.rollDice()
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

    fun buy(card: BoardCard.Chance.Estate) {
        viewModelScope.launch {
            service.buyEstate(card)
        }
    }

    fun buy(card: BoardCard.Chance.Land) {
        viewModelScope.launch {
            service.buyLand(card)
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
        if (uiState.value.currentPlayerIsActive) {
            viewModelScope.launch {
                service.changePosition(position)
            }
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

    fun sendMoney(playerId: String, amount: Long) {
        viewModelScope.launch {
            service.sendMoney(playerId, amount)
        }
    }

    fun randomJob(card: BoardCard.Chance.RandomJob) {
        viewModelScope.launch {
            service.randomJob(card)
        }
    }

    fun buyShares(card: BoardCard.Chance.Shares, count: Long) {
        viewModelScope.launch {
            service.buyShares(card, count)
        }
    }

    fun extendBusiness(business: Business, card: BoardCard.EventStore.BusinessExtending) {
        viewModelScope.launch {
            service.extendBusiness(business, card)
        }
    }

    fun sellShares(card: BoardCard.EventStore.Shares, count: Long) {
        viewModelScope.launch {
            service.sellShares(card, count)
        }
    }

    fun sellEstates(estateList: List<Estate>, price: Long) {
        viewModelScope.launch {
            service.sellEstate(estateList, price)
        }
    }

    fun sellLands(area: Long, price: Long) {
        viewModelScope.launch {
            service.sellLands(area, price)
        }
    }

    fun selectCardByNo(cardNo: Int) {
        viewModelScope.launch {
            service.selectCardByNo(cardNo)
        }
    }

    fun toDeposit(amount: Long) {
        viewModelScope.launch {
            service.toDeposit(amount)
        }
    }

    fun repayLoan(amount: Long) {
        viewModelScope.launch {
            service.repayLoan(amount)
        }
    }
}
