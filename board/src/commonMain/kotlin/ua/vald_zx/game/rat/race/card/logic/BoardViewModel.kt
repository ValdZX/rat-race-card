package ua.vald_zx.game.rat.race.card.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import ua.vald_zx.game.rat.race.card.logic.BoardUiAction.*
import ua.vald_zx.game.rat.race.card.GameSound
import ua.vald_zx.game.rat.race.card.play
import ua.vald_zx.game.rat.race.card.shared.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

val players = MutableStateFlow(emptyList<Player>())
private val SERVER_REQUEST_TIMEOUT = 20.seconds
private val PING_TIMEOUT = 10.seconds

data class BoardState(
    val isProgress: Boolean,
    val board: Board,
    val player: Player,
    val connectionState: BoardConnectionState = BoardConnectionState.Connected,
) {
    val layer: BoardLayer = player.location.level.toLayer()
    val places: List<PlaceType> = board.placesOf(layer)
    val color: Long = player.attrs.color
    val currentPlayerIsConnected: Boolean by lazy { player.isActiveOn(board) }
    val currentPlayerIsActive: Boolean by lazy {
        currentPlayerIsConnected && board.isActivePlayer(player)
    }
    val canRoll: Boolean by lazy {
        board.canRoll && currentPlayerIsActive && connectionState == BoardConnectionState.Connected
    }
    val canEnterOuterCircle: Boolean by lazy {
        player.canEnterOuterCircle(canRoll,board.outerCircleConditions)
    }
    val currentDream: Dream? by lazy {
        val place = places.getOrNull(player.location.position) as? PlaceType.Desire
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

    val hasBusinessSlot: Boolean
        get() = player.businesses.size <= board.businessLimit

    fun canBuy(price: Long): Boolean {
        return !isProgress && currentPlayerIsActive && canPay(price)
    }

    fun canBuyBusiness(price: Long): Boolean {
        return canBuy(price) && hasBusinessSlot
    }

    fun canBuyCorrupt(price: Long, deputies: Int): Boolean {
        return canBuy(price) && player.deputies >= deputies
    }

    val canSkipDeputies: Boolean
        get() = currentPlayerIsActive &&
                !isProgress &&
                board.takenCard == null &&
                board.canTakeCard == listOf(BoardCardType.Deputy)

    fun canMakeBid(): Boolean {
        if (!currentPlayerIsConnected || connectionState != BoardConnectionState.Connected) return false
        val auction = board.auction ?: return false
        val minimumBid = auction.minimumBid(board.bidList)
        return when (auction) {
            is Auction.BusinessAuction -> {
                canPay(minimumBid) && hasBusinessSlot
            }

            is Auction.EstateAuction -> {
                canPay(minimumBid)
            }

            is Auction.LandAuction -> {
                canPay(minimumBid)
            }

            is Auction.SharesAuction -> {
                canPay(minimumBid)
            }
        }
    }
}

private class PlayerSessionLost : IllegalStateException("Player session was not restored")

sealed interface BoardConnectionState {
    data object Connected : BoardConnectionState
    data class Reconnecting(val attempt: Int) : BoardConnectionState
}

data class PlayerMessage(
    val id: Long,
    val text: String,
)

const val PLAYER_MESSAGE_LOG_SIZE = 3

internal fun List<PlayerMessage>.plusSpeech(text: String, expiresAtEpochMs: Long): List<PlayerMessage> {
    if (text.isBlank() || lastOrNull()?.id == expiresAtEpochMs) return this
    return (this + PlayerMessage(expiresAtEpochMs, text)).takeLast(PLAYER_MESSAGE_LOG_SIZE)
}

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
    data class HighRiskPlayed(val outcome: InvestmentOutcome, val guess: Int) : BoardUiAction()
    data class MediumRiskPlayed(val outcome: InvestmentOutcome, val even: Boolean) : BoardUiAction()
    data class FundsCapitalized(val profit: Long) : BoardUiAction()
    data class Resignation(val business: Business) : BoardUiAction()
    data object DreamOffered : BoardUiAction()
    data object ServerRequestFailed : BoardUiAction()
    data class PlayerWon(val playerName: String, val isCurrentPlayer: Boolean) : BoardUiAction()
}

class BoardViewModel(
    board: Board,
    player: Player,
    private val serviceProvider: () -> RaceRatService,
    private val reconnectService: () -> RaceRatService = serviceProvider,
    private val clientUuidProvider: suspend () -> String = { "" },
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoardState(false, board, player))
    val uiState: StateFlow<BoardState> = _uiState.asStateFlow()

    private val _actions = Channel<BoardUiAction>()
    val actions = _actions.receiveAsFlow()

    private val _inspectedDreamId = MutableStateFlow<String?>(null)
    val inspectedDreamId: StateFlow<String?> = _inspectedDreamId.asStateFlow()

    private val _playerMessages = MutableStateFlow<Map<String, PlayerMessage>>(emptyMap())
    val playerMessages: StateFlow<Map<String, PlayerMessage>> = _playerMessages.asStateFlow()
    private val _playerMessageLog = MutableStateFlow<Map<String, List<PlayerMessage>>>(emptyMap())
    val playerMessageLog: StateFlow<Map<String, List<PlayerMessage>>> = _playerMessageLog.asStateFlow()
    private var observationJob: Job? = null
    private var pingJob: Job? = null
    private var reconnectJob: Job? = null

    private fun safeLaunch(
        needProgress: Boolean = true,
        notifyFailure: Boolean = true,
        useTimeout: Boolean = true,
        block: suspend RaceRatService.(CoroutineContext) -> Unit
    ): Job {
        return viewModelScope.launch(Dispatchers.Default + CoroutineExceptionHandler { _, t ->
            Napier.e("Invalid server", t)
            if (notifyFailure) {
                viewModelScope.launch { _actions.send(ServerRequestFailed) }
            }
            beginReconnect()
        }, block = {
            try {
                if (needProgress) _uiState.update { it.copy(isProgress = true) }
                val service = serviceProvider()
                if (useTimeout) {
                    withTimeout(SERVER_REQUEST_TIMEOUT) { service.block(coroutineContext) }
                } else {
                    service.block(coroutineContext)
                }
            } finally {
                if (needProgress) _uiState.update { it.copy(isProgress = false) }
            }
        })
    }

    fun init(player: Player) {
        observeGame(player)
        startPing()
    }

    private fun observeGame(player: Player) {
        observationJob?.cancel()
        observationJob = safeLaunch(
            needProgress = false,
            notifyFailure = false,
            useTimeout = false,
        ) {
            val actualPlayers = getPlayers()
            players.value = actualPlayers
            val actualBoard = getBoard()
            val actualPlayer = actualPlayers.find { it.id == player.id } ?: getPlayer()
            _uiState.update {
                it.copy(
                    board = actualBoard,
                    player = actualPlayer,
                    connectionState = BoardConnectionState.Connected,
                    isProgress = false,
                )
            }
            actualBoard.winnerId?.let { winnerId ->
                actualPlayers.find { it.id == winnerId }?.let { winner ->
                    _actions.send(
                        PlayerWon(
                            playerName = winner.card.name,
                            isCurrentPlayer = winner.id == actualPlayer.id,
                        )
                    )
                }
            }
            eventsObserve().collect { event ->
                when (event) {
                    is Event.MoneyIncome -> {
                        play(GameSound.Coin)
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
                        moveSound(old = oldPlayer, changed = changedPlayer)?.let(::play)
                        changedPlayer.speech?.let { showSpeech(changedPlayer.id, it) }
                    }

                    is Event.BoardChanged -> {
                        cardTakenSound(_uiState.value.board, event.board)?.let(::play)
                        _uiState.update { it.copy(board = event.board) }
                        invalidatePlayers(event.board.playerIds)
                    }

                    is Event.ConfirmDismissal -> {
                        _actions.send(ConfirmDismissal(event.business))
                    }

                    is Event.Fired -> {
                        play(GameSound.PlaceLoss)
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
                        play(GameSound.Coin)
                        _actions.send(AddCash(event.amount))
                    }

                    is Event.SubCash -> {
                        play(GameSound.PlaceLoss)
                        _actions.send(SubCash(event.amount))
                    }

                    is Event.BankruptBusiness -> {
                        play(GameSound.PlaceLoss)
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

                    is Event.HighRiskPlayed -> {
                        _actions.send(HighRiskPlayed(event.outcome, event.guess))
                    }

                    is Event.MediumRiskPlayed -> {
                        _actions.send(MediumRiskPlayed(event.outcome, event.even))
                    }

                    is Event.FundsCapitalized -> {
                        play(GameSound.Coin)
                        _actions.send(FundsCapitalized(event.profit))
                    }

                    is Event.PlayerHadBaby -> {
                        play(GameSound.PlaceLife)
                        if (event.playerId == _uiState.value.player.id) {
                            _actions.send(CongratulationsWithBaby)
                        } else {
                            players.value.find { it.id == event.playerId }?.let { player ->
                                _actions.send(PlayerHadBaby(player.card.name, event.babies))
                            }
                        }
                    }

                    is Event.PlayerMarried -> {
                        play(GameSound.PlaceLife)
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
                        play(GameSound.PlaceAsset)
                        _actions.send(BidBusinessAuctionSuccessBuy)
                    }

                    Event.BidEstateAuctionSuccessBuy -> {
                        play(GameSound.PlaceAsset)
                        _actions.send(BidEstateAuctionSuccessBuy)
                    }

                    Event.BidLandAuctionSuccessBuy -> {
                        play(GameSound.PlaceAsset)
                        _actions.send(BidLandAuctionSuccessBuy)
                    }

                    Event.BidSharesAuctionSuccessBuy -> {
                        play(GameSound.PlaceAsset)
                        _actions.send(BidSharesAuctionSuccessBuy)
                    }

                    Event.CheckState -> {
                        connectionIsValid()
                    }

                    Event.DreamOffered -> {
                        _actions.send(DreamOffered)
                    }

                    is Event.PlayerWon -> {
                        play(GameSound.PlaceLife)
                        _actions.send(
                            PlayerWon(
                                playerName = event.playerName,
                                isCurrentPlayer = event.playerId == _uiState.value.player.id,
                            )
                        )
                    }
                }
            }
            error("Server event stream completed")
        }
    }

    private fun startPing() {
        if (pingJob?.isActive == true) return
        pingJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(10000.milliseconds)
                if (_uiState.value.connectionState == BoardConnectionState.Connected) {
                    try {
                        withTimeout(PING_TIMEOUT) { serviceProvider().ping() }
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (error: Throwable) {
                        Napier.e("Server ping failed", error)
                        beginReconnect()
                    }
                }
            }
        }
    }

    private fun beginReconnect() {
        viewModelScope.launch {
            if (reconnectJob?.isActive == true) return@launch
            observationJob?.cancel()
            pingJob?.cancel()
            reconnectJob = viewModelScope.launch(Dispatchers.Default) reconnect@{
                var attempt = 1
                while (isActive) {
                    _uiState.update {
                        it.copy(
                            isProgress = true,
                            connectionState = BoardConnectionState.Reconnecting(attempt),
                        )
                    }
                    if (attempt > 1) {
                        delay(reconnectDelay(attempt).milliseconds)
                    }
                    try {
                        val (recoveredPlayer, actualBoard, actualPlayers) = withTimeout(SERVER_REQUEST_TIMEOUT) {
                            val service = reconnectService()
                            val instance = service.hello(
                                helloUuid = clientUuidProvider(),
                                boardId = _uiState.value.board.id,
                            )
                            val recoveredPlayer = instance.player ?: throw PlayerSessionLost()
                            Triple(recoveredPlayer, service.getBoard(), service.getPlayers())
                        }
                        players.value = actualPlayers
                        _uiState.update {
                            it.copy(
                                board = actualBoard,
                                player = actualPlayers.find { player -> player.id == recoveredPlayer.id }
                                    ?: recoveredPlayer,
                                connectionState = BoardConnectionState.Connected,
                                isProgress = false,
                            )
                        }
                        reconnectJob = null
                        observeGame(recoveredPlayer)
                        startPing()
                        return@reconnect
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (sessionLost: PlayerSessionLost) {
                        Napier.w("The server has no player session for this board, reconnect stopped")
                        _uiState.update { it.copy(isProgress = false) }
                        _actions.send(ServerRequestFailed)
                        reconnectJob = null
                        return@reconnect
                    } catch (error: Throwable) {
                        Napier.e("Reconnect attempt $attempt failed", error)
                        attempt += 1
                    }
                }
            }
        }
    }

    private fun reconnectDelay(attempt: Int): Long {
        val multiplier = 1 shl (attempt - 2).coerceIn(0, 4)
        return minOf(1_000L * multiplier, 15_000L)
    }

    private fun invalidatePlayers(playerIds: Set<String>) {
        safeLaunch {
            val localIds = players.value.map { player -> player.id }.toSet()
            if (localIds != playerIds) {
                players.value = getPlayers()
            }
        }
    }

    internal fun showSpeech(playerId: String, speech: PlayerSpeech) {
        val expiresAtEpochMs = speech.expiresAtEpochMs
        if (expiresAtEpochMs <= Clock.System.now().toEpochMilliseconds()) return
        val log = _playerMessageLog.value[playerId].orEmpty()
        val updated = log.plusSpeech(speech.text, expiresAtEpochMs)
        if (updated == log) return
        val message = updated.last()
        _playerMessages.update { it + (playerId to message) }
        _playerMessageLog.update { it + (playerId to updated) }
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

    fun passShares(sharesType: String) {
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

    fun sideExpenses(card: BoardCard.Expenses) {
        safeLaunch {
            payExpenses(card)
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

    fun buyDeputy() {
        safeLaunch {
            buyDeputy()
        }
    }

    fun buyCorrupt(card: BoardCard.Chance.CorruptBusiness) {
        safeLaunch {
            buyCorruptBusiness(card)
        }
    }

    fun buyCorrupt(card: BoardCard.Chance.CorruptLand) {
        safeLaunch {
            buyCorruptLand(card)
        }
    }

    fun reelection() {
        safeLaunch {
            reelection()
        }
    }

    fun skipDeputies() {
        safeLaunch {
            skipDeputies()
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

    fun playHighRiskInvestment(stake: Long, guess: Int) {
        safeLaunch {
            playHighRiskInvestment(stake, guess)
        }
    }

    fun playMediumRiskInvestment(stake: Long, even: Boolean) {
        safeLaunch {
            playMediumRiskInvestment(stake, even)
        }
    }

    fun investInFund(amount: Long) {
        safeLaunch {
            investInFund(amount)
        }
    }

    fun capitalizeFunds() {
        safeLaunch {
            capitalizeFunds()
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

    fun inspectDream(dreamId: String) {
        _inspectedDreamId.value = dreamId
    }

    fun closeInspectedDream() {
        _inspectedDreamId.value = null
    }

    fun selectDream(dreamId: String) {
        _inspectedDreamId.value = null
        safeLaunch {
            selectDream(dreamId)
        }
    }
}
