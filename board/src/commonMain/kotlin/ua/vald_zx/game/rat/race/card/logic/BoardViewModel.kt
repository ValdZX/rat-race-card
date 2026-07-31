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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

val players = MutableStateFlow(emptyList<Player>())

data class BoardState(
    val isProgress: Boolean,
    val board: Board,
    val player: Player,
) {
    val layer: BoardLayer = player.location.level.toLayer()
    val color: Long = player.attrs.color
    val currentPlayerIsActive: Boolean by lazy { player.id == board.activePlayerId }
    val canRoll: Boolean by lazy { board.canRoll && currentPlayerIsActive }
    val canEnterOuterCircle: Boolean by lazy {
        player.canEnterOuterCircle(canRoll,board.outerCircleConditions)
    }
    val currentDream: Dream? by lazy {
        val place = player.location.level.toLayer().places
            .getOrNull(player.location.position) as? PlaceType.Desire
        board.dreamById(place?.dreamId)
    }
    val canBuyDream: Boolean by lazy {
        currentPlayerIsActive &&
                currentDream != null &&
                currentDream?.id?.let { it !in board.purchasedDreamIds } == true
    }

    fun canPay(price: Long): Boolean {
        return (board.loanLimit + player.balance() - player.loan - price) > 0
    }

    fun canBuyBusiness(): Boolean {
        return player.businesses.size <= board.businessLimit
    }

    fun canMakeBid(): Boolean {
        val auction = board.auction ?: return false
        return when (auction) {
            is Auction.BusinessAuction -> {
                canPay(auction.firstBid) && canBuyBusiness()
            }

            is Auction.EstateAuction -> {
                canPay(auction.firstBid)
            }

            is Auction.LandAuction -> {
                canPay(auction.firstBid)
            }

            is Auction.SharesAuction -> {
                canPay(auction.firstBid)
            }
        }
    }
}

data class PlayerMessage(
    val id: Long,
    val text: String,
)

sealed class BoardUiAction {
    data class ConfirmDismissal(val business: Business) : BoardUiAction()
    data class Fired(val business: Business) : BoardUiAction()
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
    data object DreamOffered : BoardUiAction()
    data class PlayerWon(val playerName: String, val isCurrentPlayer: Boolean) : BoardUiAction()
}

class BoardViewModel(
    board: Board,
    player: Player,
    private val serviceProvider: () -> RaceRatService
) : ViewModel() {


    private val _uiState = MutableStateFlow(BoardState(false, board, player))
    val uiState: StateFlow<BoardState> = _uiState.asStateFlow()

    private val _actions = Channel<BoardUiAction>()
    val actions = _actions.receiveAsFlow()

    private val _playerMessages = MutableStateFlow<Map<String, PlayerMessage>>(emptyMap())
    val playerMessages: StateFlow<Map<String, PlayerMessage>> = _playerMessages.asStateFlow()
    private var nextPlayerMessageId = 0L

    private fun safeLaunch(
        needProgress: Boolean = true,
        block: suspend RaceRatService.(CoroutineContext) -> Unit
    ): Job {
        return viewModelScope.launch(Dispatchers.Default + SupervisorJob() + CoroutineExceptionHandler { _, t ->
            Napier.e("Invalid server", t)
            viewModelScope.launch {
                _actions.send(ConnectionLost)
            }
        }, block = {
            if (needProgress) _uiState.update { it.copy(isProgress = true) }
            serviceProvider().block(coroutineContext)
            if (needProgress) _uiState.update { it.copy(isProgress = false) }
        })
    }

    fun init(player: Player) {
        safeLaunch(false) {
            val actualPlayers = getPlayers()
            players.value = actualPlayers
            val actualBoard = getBoard()
            _uiState.update { it.copy(board = actualBoard, player = player) }
            actualBoard.winnerId?.let { winnerId ->
                actualPlayers.find { it.id == winnerId }?.let { winner ->
                    _actions.send(
                        PlayerWon(
                            playerName = winner.card.name,
                            isCurrentPlayer = winner.id == player.id,
                        )
                    )
                }
            }
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
                        changedPlayer.speech
                            ?.takeIf {
                                it.text.isNotBlank() &&
                                        it.expiresAtEpochMs > Clock.System.now().toEpochMilliseconds()
                            }
                            ?.let { showPlayerMessage(changedPlayer.id, it.text, it.expiresAtEpochMs) }
                    }

                    is Event.BoardChanged -> {
                        _uiState.update { it.copy(board = event.board) }
                        invalidatePlayers(event.board.playerIds)
                    }

                    is Event.ConfirmDismissal -> {
                        _actions.send(ConfirmDismissal(event.business))
                    }

                    is Event.Fired -> {
                        _actions.send(Fired(event.business))
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

                    is Event.PlayerMessage -> showPlayerMessage(event.playerId, event.text)

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

                    Event.CheckState -> {
                        connectionIsValid()
                    }

                    Event.DreamOffered -> {
                        _actions.send(DreamOffered)
                    }

                    is Event.PlayerWon -> {
                        _actions.send(
                            PlayerWon(
                                playerName = event.playerName,
                                isCurrentPlayer = event.playerId == _uiState.value.player.id,
                            )
                        )
                    }
                }
            }
        }
        safeLaunch(false) {
            while (true) {
                delay(10000.milliseconds)
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

    private fun showPlayerMessage(
        playerId: String,
        text: String,
        expiresAtEpochMs: Long = Clock.System.now().toEpochMilliseconds() + 8_000,
    ) {
        if (text.isBlank()) return
        val message = PlayerMessage(++nextPlayerMessageId, text)
        _playerMessages.update { it + (playerId to message) }
        viewModelScope.launch {
            delay((expiresAtEpochMs - Clock.System.now().toEpochMilliseconds()).coerceAtLeast(0).milliseconds)
            _playerMessages.update {
                if (it[playerId]?.id == message.id) it - playerId else it
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
        safeLaunch(false) {
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

    fun debugChangePosition(layer: BoardLayer, position: Int) {
        if (uiState.value.currentPlayerIsActive) {
            safeLaunch {
                debugChangePosition(PlayerLocation(position = position, level = layer.level))
            }
        }
    }

    fun debugUpdatePlayer(values: DebugPlayerValues) {
        if (uiState.value.currentPlayerIsActive) {
            safeLaunch {
                debugUpdatePlayer(values)
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

    fun sendMessage(text: String) {
        safeLaunch(false) {
            sendMessage(text)
        }
    }

    fun randomJob(card: BoardCard.Chance.RandomJob) {
        safeLaunch {
            randomJob(card)
        }
    }

    fun buyShares(card: BoardCard.Chance.Shares, count: Long) {
        safeLaunch {
            buyShares(Shares(card.sharesType, count, card.price), uiState.value.board.sharesCount ?: card.maxCount)
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

    fun selectCardByNo(cardNo: Int, cardType: BoardCardType) {
        val moveToCard = uiState.value.canRoll
        safeLaunch {
            if (moveToCard) {
                debugMoveToAndSelectCard(cardNo, cardType)
            } else {
                selectCardByNo(cardNo, cardType)
            }
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

    fun enterOuterCircle() {
        safeLaunch {
            enterOuterCircle()
        }
    }

    fun buyDream() {
        safeLaunch {
            buyDream()
        }
    }

    fun selectDream(dreamId: String) {
        safeLaunch {
            selectDream(dreamId)
        }
    }
}
