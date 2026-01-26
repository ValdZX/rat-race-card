package ua.vald_zx.game.rat.race.card.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import ua.vald_zx.game.rat.race.card.logic.BoardUiAction.*
import ua.vald_zx.game.rat.race.card.shared.*
import kotlin.coroutines.CoroutineContext

val players = MutableStateFlow(emptyList<Player>())

data class BoardState(
    val board: Board,
    val player: Player,
) {
    val layer: BoardLayer = player.location.level.toLayer()
    val color: Long = player.attrs.color
    val currentPlayerIsActive: Boolean by lazy { player.id == board.activePlayerId }
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
    data object BidBusinessAuctionSuccessBuy : BoardUiAction()
    data object BidEstateAuctionSuccessBuy : BoardUiAction()
    data object BidLandAuctionSuccessBuy : BoardUiAction()
    data object BidSharesAuctionSuccessBuy : BoardUiAction()
    data object ConnectionLost : BoardUiAction()
    data class Resignation(val business: Business) : BoardUiAction()
}

class BoardViewModel(
    board: Board,
    player: Player,
    private val serviceProvider: () -> RaceRatService
) : ViewModel() {


    private val _uiState = MutableStateFlow(BoardState(board, player))
    val uiState: StateFlow<BoardState> = _uiState.asStateFlow()

    private val _actions = Channel<BoardUiAction>()
    val actions = _actions.receiveAsFlow()

    private fun safeLaunch(block: suspend RaceRatService.(CoroutineContext) -> Unit): Job {
        return viewModelScope.launch(viewModelScope.coroutineContext + SupervisorJob() + CoroutineExceptionHandler { _, t ->
            Napier.e("Invalid server", t)
            viewModelScope.launch {
                _actions.send(ConnectionLost)
            }
        }, block = { serviceProvider().block(coroutineContext) })
    }

    fun init(player: Player) {
        safeLaunch {
            val actualPlayers = getPlayers()
            players.value = actualPlayers
            _uiState.update { it.copy(board = getBoard(), player = player) }
            eventsObserve().collect { event ->
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

                    Event.BidBusinessAuctionSuccessBuy -> {
                        _actions.send(BidBusinessAuctionSuccessBuy)
                    }

                    Event.BidEstateAuctionSuccessBuy -> {
                        _actions.send(BidEstateAuctionSuccessBuy)
                    }

                    Event.BidLandAuctionSuccessBuy -> {
                        _actions.send(BidLandAuctionSuccessBuy)
                    }

                    Event.BidSharesAuctionSuccessBuy -> {
                        _actions.send(BidSharesAuctionSuccessBuy)
                    }
                }
            }
        }
        safeLaunch {
            ping()
            pong().collect {
                delay(3000)
                ping()
            }
        }
    }

    private fun invalidatePlayers(playerIds: Set<String>) {
        safeLaunch {
            val localIds = players.value.map { player -> player.id }.toSet()
            if (localIds != playerIds) {
                players.value = getPlayers()
            }
        }
    }

    fun pass() {
        safeLaunch {
            next()
        }
    }

    fun passLand() {
        safeLaunch {
            passLand()
        }
    }

    fun passEstate() {
        safeLaunch {
            passEstate()
        }
    }

    fun passShares(sharesType: SharesType) {
        safeLaunch {
            passShares(sharesType)
        }
    }

    fun rollDice() {
        safeLaunch {
            rollDice()
        }
    }

    fun buyBusiness(business: Business) {
        safeLaunch {
            buyBusiness(business)
        }
    }

    fun sideExpenses(price: Long) {
        safeLaunch {
            minusCash(price)
            next()
        }
    }

    fun buy(card: BoardCard.Shopping) {
        safeLaunch {
            buyThing(card)
        }
    }

    fun buy(card: BoardCard.Chance.Estate) {
        safeLaunch {
            buyEstate(Estate(name = card.name, card.price))
        }
    }

    fun buy(card: BoardCard.Chance.Land) {
        safeLaunch {
            buyLand(Land(name = card.name, card.area, card.price))
        }
    }

    fun changePlayerColor(value: Long) {
        safeLaunch {
            updateAttributes(uiState.value.player.attrs.copy(color = value))
        }
    }

    fun selectCard(cardType: BoardCardType) {
        safeLaunch {
            takeCard(cardType)
        }
    }

    fun takeSalary() {
        safeLaunch {
            takeSalary()
        }
    }

    fun changePosition(position: Int) {
        if (uiState.value.currentPlayerIsActive) {
            safeLaunch {
                changePosition(position)
            }
        }
    }

    fun dismissalConfirmed(business: Business) {
        safeLaunch {
            dismissalConfirmed(business)
        }
    }

    fun sellingAllBusinessConfirmed(business: Business) {
        safeLaunch {
            sellingAllBusinessConfirmed(business)
        }
    }

    fun sendMoney(playerId: String, amount: Long) {
        safeLaunch {
            sendMoney(playerId, amount)
        }
    }

    fun randomJob(card: BoardCard.Chance.RandomJob) {
        safeLaunch {
            randomJob(card)
        }
    }

    fun buyShares(card: BoardCard.Chance.Shares, count: Long) {
        safeLaunch {
            buyShares(Shares(card.sharesType, count, card.price))
        }
    }

    fun extendBusiness(business: Business, card: BoardCard.EventStore.BusinessExtending) {
        safeLaunch {
            extendBusiness(business, card)
        }
    }

    fun sellShares(card: BoardCard.EventStore.Shares, count: Long) {
        safeLaunch {
            sellShares(card, count)
        }
    }

    fun sellEstates(estateList: List<Estate>, price: Long) {
        safeLaunch {
            sellEstate(estateList, price)
        }
    }

    fun sellLands(area: Long, price: Long) {
        safeLaunch {
            sellLands(area, price)
        }
    }

    fun selectCardByNo(cardNo: Int) {
        safeLaunch {
            selectCardByNo(cardNo)
        }
    }

    fun toDeposit(amount: Long) {
        safeLaunch {
            toDeposit(amount)
        }
    }

    fun repayLoan(amount: Long) {
        safeLaunch {
            repayLoan(amount)
        }
    }

    fun advertiseAuction(auction: Auction) {
        safeLaunch {
            advertiseAuction(auction)
        }
    }

    fun sellBid(bid: Bid) {
        safeLaunch {
            sellBid(bid)
        }
    }

    fun makeBid(price: Long, count: Long) {
        safeLaunch {
            makeBid(price, count)
        }
    }
}
