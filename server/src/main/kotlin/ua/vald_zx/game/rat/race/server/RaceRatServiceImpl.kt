@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.server.data.Storage
import ua.vald_zx.game.rat.race.server.data.generateStableDbId
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal val LOGGER = KtorSimpleLogger("RaceRatService")

private val boardMutexes = ConcurrentHashMap<String, Mutex>()
private val playerMutexes = ConcurrentHashMap<String, Mutex>()
private fun boardMutex(boardId: String): Mutex = boardMutexes.getOrPut(boardId) { Mutex() }
private fun playerMutex(playerId: String): Mutex = playerMutexes.getOrPut(playerId) { Mutex() }

class RaceRatServiceImpl(
    private val uuidStateProvider: MutableStateFlow<String>,
    private val scope: CoroutineScope,
) : RaceRatService, CoroutineScope by scope {

    private var boardIdState = MutableStateFlow("")
    private val eventBus = MutableSharedFlow<Event>()
    private val boardsFlow = MutableSharedFlow<List<BoardId>>()
    private val globalEventBus: MutableSharedFlow<GlobalEvent>
        get() = getGlobalEventBus(boardIdState.value)
    private var boardStateSubJob: Job? = null
    private var globalEventStateSubJob: Job? = null

    private val playerId: String
        get() = generateStableDbId(boardIdState.value, uuidStateProvider.value)

    private suspend fun player() = Storage.getPlayer(playerId)
    private suspend fun board() = Storage.getBoard(boardIdState.value)

    init {
        checkStatusFlow
            .onEach { eventBus.emit(Event.CheckState) }
            .launchIn(this)

        Storage.observeBoards()
            .onEach { boardsFlow.emit(getBoards()) }
            .launchIn(this)

        boardIdState
            .onEach { boardId -> if (boardId.isNotBlank()) subscribeToBoard(boardId) }
            .launchIn(this)
    }

    private suspend fun subscribeToBoard(boardId: String) {
        boardStateSubJob?.cancel()
        boardStateSubJob = Storage.observeBoard(boardId)
            .onEach { board -> eventBus.emit(Event.BoardChanged(board)) }
            .launchIn(this)

        globalEventStateSubJob?.cancel()
        globalEventStateSubJob = globalEventBus
            .onEach { event -> handleGlobalEvent(event) }
            .launchIn(this)
    }

    private suspend fun handleGlobalEvent(event: GlobalEvent) {
        when (event) {
            is GlobalEvent.SendMoney -> {
                if (event.receiverId == playerId) {
                    eventBus.emit(Event.MoneyIncome(event.playerId, event.amount))
                    updatePlayer { plusCash(event.amount) }
                }
            }

            is GlobalEvent.PlayerChanged -> eventBus.emit(Event.PlayerChanged(event.player))
            is GlobalEvent.PlayerHadBaby -> eventBus.emit(Event.PlayerHadBaby(event.playerId, event.babies))
            is GlobalEvent.PlayerMarried -> eventBus.emit(Event.PlayerMarried(event.playerId))
            is GlobalEvent.PlayerDivorced -> eventBus.emit(Event.PlayerDivorced(event.playerId))
            is GlobalEvent.BidSelled -> {
                if (event.bid.playerId == playerId) {
                    buyLot(event.auction, event.bid)
                }
            }
        }
    }

    override suspend fun hello(helloUuid: String, boardId: String): Instance {
        val board = Storage.getBoard(boardId)
        boardSelected(board)
        uuidStateProvider.value = helloUuid
        if (board.playerIds.contains(playerId)) {
            checkStatusJobs[playerId]?.cancel()
            updatePlayer { copy(isInactive = false) }
            invalidateNextPlayer(board.activePlayerId)
            return Instance(board, player())
        }
        return Instance(board, null)
    }

    private fun boardSelected(board: Board) {
        boardIdState.value = board.id
    }

    override suspend fun ping() {
        checkStatusJobs[playerId]?.cancel()
        if (player().isInactive) {
            updatePlayer { copy(isInactive = false) }
        }
    }

    override suspend fun connectionIsValid() {
        checkStatusJobs[playerId]?.cancel()
    }

    override suspend fun getBoards(): List<BoardId> {
        return Storage.boards().map { board -> board.toBoardId() }
    }

    override fun observeBoards(): Flow<List<BoardId>> = boardsFlow

    override suspend fun createBoard(
        name: String,
        loanLimit: Long,
        businessLimit: Long,
        decks: Map<BoardCardType, Int>
    ): Board {
        val board = Board(
            name = name,
            loanLimit = loanLimit,
            businessLimit = businessLimit,
            createDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            id = Uuid.random().toString(),
            cards = decks.mapValues { (_, size) -> (1..size).toList() }
        )
        Storage.newBoard(board)
        boardSelected(board)
        return board
    }

    override suspend fun makePlayer(
        uuid: String,
        color: Long,
        card: PlayerCard,
    ): Player {
        val board = board()
        uuidStateProvider.value = uuid
        val newPlayer = Player(
            id = playerId,
            boardId = board.id,
            attrs = PlayerAttributes(color = color),
            card = card,
            businesses = listOf(
                Business(
                    type = BusinessType.WORK,
                    name = card.profession,
                    price = 0,
                    profit = card.salary
                )
            )
        )
        Storage.newPlayer(newPlayer)
        getGlobalEventBus(board.id).emit(GlobalEvent.PlayerChanged(newPlayer))
        updateBoard { copy(playerIds = playerIds + playerId) }
        takeSalary()
        invalidateNextPlayer(board.activePlayerId)
        return newPlayer
    }

    override suspend fun getPlayer(): Player = player()

    override suspend fun updateAttributes(attrs: PlayerAttributes) {
        updatePlayer { copy(attrs = attrs) }
    }

    override fun eventsObserve(): Flow<Event> = eventBus

    override suspend fun getPlayers(): List<Player> = Storage.players(boardIdState.value)

    override suspend fun getBoard(): Board = board()

    override suspend fun sendMoney(receiverId: String, amount: Long) {
        globalEventBus.emit(GlobalEvent.SendMoney(playerId, receiverId, amount))
        updatePlayer { minusCash(amount) }
    }

    override suspend fun rollDice() {
        updateBoard {
            val dice = (1..6).random()
            copy(dice = dice, canRoll = false, diceRolling = true)
        }
        delay(4.seconds)
        updateBoard {
            copy(diceRolling = false)
        }
        move()
    }

    override suspend fun takeCard(cardType: BoardCardType) {
        val card = board().cards[cardType]?.randomOrNull() ?: return
        selectCard(card, cardType)
    }

    private suspend fun selectCard(cardId: Int, cardType: BoardCardType) {
        updateBoard {
            val newCards = cards.toMutableMap()
            newCards[cardType] = newCards[cardType].orEmpty() - cardId
            copy(
                cards = newCards,
                takenCard = CardLink(cardType, cardId),
                sharesCount = null,
                canTakeCard = emptyList(),
                processedPlayerIds = emptySet(),
            )
        }
    }

    override suspend fun selectCardByNo(cardId: Int, cardType: BoardCardType) {
        if (board().canTakeCard.contains(cardType)) {
            selectCard(cardId, cardType)
        }
    }

    override suspend fun next() {
        nextPlayer()
    }

    private suspend fun invalidateNextPlayer(activePlayerId: String) {
        val playerIds = board().playerIds
        val active = Storage.getPlayerOrNull(activePlayerId)
        if (activePlayerId.isEmpty() || !playerIds.contains(activePlayerId) || active == null || active.isInactive) {
            nextPlayer()
        }
    }

    private suspend fun nextPlayer() {
        nextPlayer(board())
    }

    override suspend fun takeSalary() {
        updatePlayer {
            val cashFlow = cashFlow()
            (if (cashFlow >= 0) {
                plusCash(cashFlow)
            } else {
                minusCash(cashFlow.absoluteValue)
            }).copy(salaryPosition = null)
        }
    }

    private suspend fun buyBusiness(business: Business, doNext: suspend () -> Unit = {}) {
        val currentBusiness = player().businesses
        if (business.type == BusinessType.SMALL
            && currentBusiness.any { it.type == BusinessType.WORK }
            && currentBusiness.count { it.type == BusinessType.SMALL } == 1
        ) {
            if (business.fromAuction) {
                updatePlayer {
                    copy(businesses = currentBusiness.filter { it.type == BusinessType.WORK } + business)
                        .minusCash(business.price)
                }
                currentBusiness.find { it.type == BusinessType.WORK }?.let { work ->
                    eventBus.emit(Event.Fired(work))
                }
                doNext()
            } else {
                eventBus.emit(Event.ConfirmDismissal(business))
            }
        } else if (currentBusiness.isNotEmpty()
            && currentBusiness.first().type.klass != business.type.klass
            && currentBusiness.none { it.type == BusinessType.WORK }
        ) {
            eventBus.emit(Event.ConfirmSellingAllBusiness(business))
        } else {
            updatePlayer {
                copy(businesses = currentBusiness + business)
                    .minusCash(business.price)
            }
            doNext()
        }
    }

    override suspend fun buyBusiness(business: Business) {
        buyBusiness(business) { nextPlayer() }
    }

    override suspend fun dismissalConfirmed(business: Business) {
        updatePlayer {
            val newBusinesses = businesses.filter { it.type != BusinessType.WORK } + business
            copy(businesses = newBusinesses).minusCash(business.price)
        }
        nextPlayer()
    }

    override suspend fun sellingAllBusinessConfirmed(business: Business) {
        updatePlayer {
            val refund = businesses.sumOf { it.price }
            copy(businesses = listOf(business))
                .plusCash(refund)
                .minusCash(business.price)
        }
        nextPlayer()
    }

    private suspend fun move() {
        val player = player()
        val layer = player.location.level.toLayer()
        val cellCount = layer.cellCount
        val currentPosition = player.location.position
        val newPosition = moveTo(currentPosition, cellCount, board().dice)
        processNewPosition(newPosition)
    }

    private suspend fun processNewPosition(newPosition: Int) {
        val player = player()
        val layer = player.location.level.toLayer()
        val placeCount = layer.places.size
        val currentPosition = player.location.position
        val safeNewPosition = newPosition.coerceIn(0, placeCount - 1)
        val safeCurrent = currentPosition.coerceIn(0, placeCount - 1)

        val passedPlaces = if (safeCurrent > safeNewPosition) {
            layer.places.subList(safeCurrent + 1, placeCount) + layer.places.subList(0, safeNewPosition + 1)
        } else {
            layer.places.subList(safeCurrent + 1, safeNewPosition + 1)
        }
        val salaryPosition = if (passedPlaces.contains(PlaceType.Salary)) {
            if (player.cashFlow() > 0) {
                var pos = safeCurrent + passedPlaces.indexOfLast { it == PlaceType.Salary } + 1
                if (pos >= placeCount) pos -= placeCount
                pos
            } else {
                takeSalary()
                null
            }
        } else null

        updateBoard {
            copy(moveCount = moveCount + 1, canRoll = false)
        }
        updatePlayer {
            copy(location = location.copy(position = safeNewPosition), salaryPosition = salaryPosition)
        }

        when (layer.places[safeNewPosition]) {
            PlaceType.BigBusiness -> updateBoard {
                copy(canTakeCard = listOf(BoardCardType.BigBusiness))
            }

            PlaceType.Business -> updateBoard {
                copy(canTakeCard = businessCardOptions(player))
            }

            PlaceType.Chance -> updateBoard { copy(canTakeCard = listOf(BoardCardType.Chance)) }
            PlaceType.Deputy -> updateBoard { copy(canTakeCard = listOf(BoardCardType.Deputy)) }
            PlaceType.Expenses -> updateBoard { copy(canTakeCard = listOf(BoardCardType.Expenses)) }
            PlaceType.Shopping -> updateBoard { copy(canTakeCard = listOf(BoardCardType.Shopping)) }
            PlaceType.Store -> updateBoard { copy(canTakeCard = listOf(BoardCardType.EventStore)) }

            PlaceType.Bankruptcy -> {
                updatePlayer {
                    val sellable = businesses.filter { it.type != BusinessType.WORK }
                    if (sellable.isNotEmpty()) {
                        val random = sellable.random()
                        eventBus.emit(Event.BankruptBusiness(random))
                        copy(businesses = businesses - random)
                    } else this
                }
                nextPlayer()
            }

            PlaceType.Child -> {
                if (player.card.gender == Gender.FEMALE || (player.card.gender == Gender.MALE && player.isMarried)) {
                    updatePlayer {
                        val totalBabies = babies + 1
                        copy(babies = totalBabies).plusCash(1000).also {
                            globalEventBus.emit(GlobalEvent.PlayerHadBaby(playerId, totalBabies))
                        }
                    }
                }
                nextPlayer()
            }

            PlaceType.Divorce -> {
                if (player.isMarried) {
                    updatePlayer {
                        (if (card.gender == Gender.MALE) {
                            copy(isMarried = false, babies = 0, cash = cash / 2, deposit = deposit / 2)
                        } else {
                            copy(isMarried = false)
                        }).also {
                            globalEventBus.emit(GlobalEvent.PlayerDivorced(playerId))
                        }
                    }
                }
                nextPlayer()
            }

            PlaceType.Resignation -> {
                val business = player.businesses.find { it.type == BusinessType.WORK }
                if (business != null) {
                    updatePlayer {
                        copy(businesses = businesses - business).also {
                            eventBus.emit(Event.Resignation(business))
                        }
                    }
                }
                nextPlayer()
            }

            PlaceType.Love -> {
                if (!player.isMarried) {
                    updatePlayer {
                        copy(isMarried = true).also {
                            globalEventBus.emit(GlobalEvent.PlayerMarried(playerId))
                        }
                    }
                    if (player.card.gender == Gender.MALE) {
                        updatePlayer { minusCash(config.marriageCost) }
                    }
                }
                nextPlayer()
            }

            PlaceType.Rest -> {
                updatePlayer { copy(inRest = 2) }
                nextPlayer()
            }

            PlaceType.Salary,
            PlaceType.Start,
            PlaceType.TaxInspection,
            PlaceType.Desire -> nextPlayer()
        }
    }

    private fun businessCardOptions(player: Player): List<BoardCardType> {
        return when {
            player.businesses.any { it.type == BusinessType.LARGE } ->
                listOf(BoardCardType.BigBusiness)

            player.businesses.any { it.type == BusinessType.MEDIUM } ->
                listOf(BoardCardType.BigBusiness, BoardCardType.MediumBusiness)

            player.businesses.any { it.type == BusinessType.SMALL } ->
                listOf(BoardCardType.SmallBusiness, BoardCardType.MediumBusiness)

            else -> listOf(BoardCardType.SmallBusiness)
        }
    }

    override suspend fun minusCash(price: Long) {
        updatePlayer { minusCash(price) }
    }

    private suspend fun updateBoard(change: suspend Board.() -> Board) {
        val id = boardIdState.value
        boardMutex(id).withLock {
            Storage.updateBoard(board().change())
        }
    }

    private suspend fun updatePlayer(change: suspend Player.() -> Player) {
        val id = playerId
        playerMutex(id).withLock {
            val previousPlayer = player()
            val changed = previousPlayer.change()
            val newTotal = changed.total()
            val previousTotal = previousPlayer.total()
            val totals = if (newTotal != previousTotal) {
                (changed.lastTotals + (newTotal - previousTotal)).takeLast(3)
            } else changed.lastTotals
            val newCashFlow = changed.cashFlow()
            val previousCashFlow = previousPlayer.cashFlow()
            val cashFlows = if (newCashFlow != previousCashFlow) {
                (changed.lastCashFlows + (newCashFlow - previousCashFlow)).takeLast(3)
            } else changed.lastCashFlows
            val newPlayer = changed.copy(lastTotals = totals, lastCashFlows = cashFlows)
            Storage.updatePlayer(newPlayer)
            globalEventBus.emit(GlobalEvent.PlayerChanged(newPlayer))
        }
    }

    private fun Player.plusCash(value: Long): Player {
        return copy(cash = cash + value)
    }

    private suspend fun Player.minusCash(
        value: Long,
        isFundBuy: Boolean = false
    ): Player {
        val board = board()
        if (value == 0L) return this
        eventBus.emit(Event.SubCash(value))
        return if (cash >= value) {
            copy(cash = cash - value)
        } else if ((cash + deposit) >= value) {
            eventBus.emit(Event.DepositWithdraw(value - cash))
            copy(cash = 0, deposit = (deposit + cash) - value)
        } else if (!isFundBuy && config.hasFunds && funds.isNotEmpty()) {
            var stub = cash + deposit
            var newFunds = funds.toList()
            funds.sortedBy { it.rate }.firstOrNull { fund ->
                if (stub + fund.amount >= value) {
                    newFunds = newFunds.replace(fund, fund.copy(amount = stub + fund.amount - value))
                    true
                } else {
                    stub += fund.amount
                    newFunds = newFunds.remove(fund)
                    false
                }
            }
            if (stub < value) {
                val newLoan = loan + (value - stub)
                if (newLoan > board.loanLimit) {
                    eventBus.emit(Event.LoanOverlimited)
                }
                copy(cash = 0, deposit = 0, funds = emptyList(), loan = newLoan)
            } else {
                copy(cash = 0, deposit = 0, funds = newFunds)
            }
        } else {
            eventBus.emit(Event.LoanAdded(value - (cash + deposit)))
            val newLoan = loan + (value - (deposit + cash))
            if (newLoan > board.loanLimit) {
                eventBus.emit(Event.LoanOverlimited)
            }
            copy(cash = 0, deposit = 0, loan = newLoan)
        }
    }

    override suspend fun buyThing(card: BoardCard.Shopping) {
        updatePlayer {
            val updated = when (card.shopType) {
                ShopType.AUTO -> copy(cars = cars + 1)
                ShopType.HOUSE -> copy(cottage = cottage + 1)
                ShopType.APARTMENT -> copy(apartment = apartment + 1)
                ShopType.YACHT -> copy(yacht = yacht + 1)
                ShopType.FLY -> copy(flight = flight + 1)
            }
            updated.minusCash(card.price)
        }
        nextPlayer()
    }

    override suspend fun changePosition(position: Int) {
        processNewPosition(position)
    }

    private suspend fun buyEstate(estate: Estate, doNext: suspend () -> Unit = {}) {
        updatePlayer {
            copy(estateList = estateList + estate).minusCash(estate.price)
        }
        doNext()
    }

    override suspend fun buyEstate(estate: Estate) {
        buyEstate(estate) { nextPlayer() }
    }

    private suspend fun buyLand(land: Land, doNext: suspend () -> Unit = {}) {
        updatePlayer {
            copy(landList = landList + land).minusCash(land.price)
        }
        doNext()
    }

    override suspend fun buyLand(land: Land) {
        buyLand(land) { nextPlayer() }
    }

    override suspend fun randomJob(card: BoardCard.Chance.RandomJob) {
        updatePlayer { plusCash(card.profit) }
        nextPlayer()
    }

    private suspend fun buyShares(totalCount: Long, shares: Shares) {
        updatePlayer {
            copy(sharesList = sharesList + shares).minusCash(shares.price)
        }
        val auction = board().auction
        if (auction is Auction.SharesAuction) {
            updateBoard {
                copy(
                    sharesCount = null,
                    auction = auction.copy(shares = auction.shares.copy(count = auction.shares.count - shares.count))
                )
            }
        } else {
            updateBoard {
                copy(sharesCount = totalCount - shares.count)
            }
        }
    }

    override suspend fun buyShares(shares: Shares, totalCount: Long) {
        buyShares(totalCount, shares)
    }

    override suspend fun extendBusiness(
        business: Business,
        card: BoardCard.EventStore.BusinessExtending
    ) {
        updatePlayer {
            val extendedBusiness = business.copy(extentions = business.extentions + card.profit)
            copy(businesses = businesses.replace(business, extendedBusiness))
        }
        nextPlayer()
    }

    override suspend fun sellLands(area: Long, priceOfUnit: Long) {
        updatePlayer {
            val totalArea = landList.sumOf { it.area }
            if (totalArea >= area) {
                val updatedLands = if (totalArea == area) {
                    emptyList()
                } else {
                    var remainder = area
                    val newLands = landList.toMutableList()
                    landList.forEach { land ->
                        if (remainder == 0L) return@forEach
                        newLands -= land
                        if (land.area <= remainder) {
                            remainder -= land.area
                        } else {
                            newLands += land.copy(area = land.area - remainder)
                            remainder = 0
                        }
                    }
                    newLands
                }
                copy(landList = updatedLands).plusCash(area * priceOfUnit)
            } else {
                this
            }
        }
        updateBoard {
            copy(processedPlayerIds = processedPlayerIds + playerId)
        }
        passLand()
    }

    override suspend fun sellShares(
        card: BoardCard.EventStore.Shares,
        count: Long
    ) {
        updatePlayer {
            var resultList = sharesList.toMutableList()
            val sharesByType = resultList.filter { it.type == card.sharesType }
            var needToSell = count
            var index = 0
            while (needToSell != 0L && index < sharesByType.size) {
                val shares = sharesByType[index]
                if (needToSell < shares.count) {
                    resultList = resultList.replace(
                        shares,
                        shares.copy(count = shares.count - needToSell)
                    ).toMutableList()
                    break
                } else if (needToSell == shares.count) {
                    resultList.remove(shares)
                    break
                } else {
                    resultList.remove(shares)
                    needToSell -= shares.count
                    index += 1
                }
            }
            copy(sharesList = resultList).plusCash(count * card.price)
        }
        updateBoard {
            copy(processedPlayerIds = processedPlayerIds + playerId)
        }
        passShares(card.sharesType)
    }

    override suspend fun sellEstate(
        card: List<Estate>,
        price: Long
    ) {
        updatePlayer {
            copy(estateList = estateList - card.toSet()).plusCash(card.size * price)
        }
        updateBoard {
            copy(processedPlayerIds = processedPlayerIds + playerId)
        }
        passEstate()
    }

    override suspend fun passLand() {
        val board = board()
        val playersWithLands = board.players().filter { it.landList.isNotEmpty() }.map { it.id }.toSet()
        if (playersWithLands.isEmpty() || playersWithLands.size == 1 || playersWithLands == board.processedPlayerIds) {
            nextPlayer()
        }
    }

    override suspend fun passShares(sharesType: SharesType) {
        val board = board()
        val playersWithShares =
            board.players().filter { player -> player.sharesList.any { it.type == sharesType } }.map { it.id }.toSet()
        if (playersWithShares.isEmpty() || playersWithShares.size == 1 || playersWithShares == board.processedPlayerIds) {
            nextPlayer()
        }
    }

    override suspend fun passEstate() {
        val board = board()
        val playersWithEstate = board.players().filter { it.estateList.isNotEmpty() }.map { it.id }.toSet()
        if (playersWithEstate.isEmpty() || playersWithEstate.size == 1 || playersWithEstate == board.processedPlayerIds) {
            nextPlayer()
        }
    }

    override suspend fun toDeposit(amount: Long) {
        updatePlayer {
            copy(deposit = deposit + amount).minusCash(amount)
        }
    }

    override suspend fun repayLoan(amount: Long) {
        updatePlayer {
            copy(loan = loan - amount).minusCash(amount)
        }
    }

    override suspend fun advertiseAuction(auction: Auction) {
        updateBoard {
            copy(auction = auction)
        }
    }

    override suspend fun sellBid(bid: Bid) {
        val auction = board().auction ?: return
        val profit = auction.getProfit(bid)
        updatePlayer { plusCash(profit) }
        if (auction !is Auction.SharesAuction) {
            updateBoard {
                copy(auction = null, bidList = emptyList())
            }
            nextPlayer()
        } else {
            updateBoard {
                copy(
                    auction = auction.copy(
                        shares = auction.shares.copy(
                            count = auction.shares.count - bid.count
                        )
                    )
                )
            }
        }
        globalEventBus.emit(GlobalEvent.BidSelled(bid, auction))
    }

    override suspend fun makeBid(price: Long, count: Long) {
        updateBoard {
            copy(bidList = bidList.filter { it.playerId != playerId } + Bid(playerId, price, count))
        }
    }

    private suspend fun buyLot(
        auction: Auction,
        bid: Bid
    ) {
        when (auction) {
            is Auction.BusinessAuction -> {
                val business = auction.business.copy(price = bid.bid, fromAuction = true)
                buyBusiness(business) {
                    LOGGER.info("Change ${this@RaceRatServiceImpl.hashCode()} auction is buying business")
                }
                eventBus.emit(Event.BidBusinessAuctionSuccessBuy)
            }

            is Auction.EstateAuction -> {
                buyEstate(auction.estate.copy(price = bid.bid)) {
                    LOGGER.info("Change ${this@RaceRatServiceImpl.hashCode()} auction is buying estate")
                }
                eventBus.emit(Event.BidEstateAuctionSuccessBuy)
            }

            is Auction.LandAuction -> {
                buyLand(auction.land.copy(price = bid.bid)) {
                    LOGGER.info("Change ${this@RaceRatServiceImpl.hashCode()} auction is buying land")
                }
                eventBus.emit(Event.BidLandAuctionSuccessBuy)
            }

            is Auction.SharesAuction -> {
                buyShares(bid.count, auction.shares.copy(count = bid.count, buyPrice = bid.bid))
                eventBus.emit(Event.BidSharesAuctionSuccessBuy)
            }
        }
    }

    private suspend fun Board.players(): List<Player> {
        return playerIds.mapNotNull { playerId -> Storage.getPlayerOrNull(playerId) }
    }
}

suspend fun nextPlayer(board: Board) {
    val activePlayers = Storage.players(board.id).filter { player -> !player.isInactive }
    if (activePlayers.isEmpty()) return
    val playerIds = activePlayers.map { it.id }
    val activePlayerIndex = playerIds.indexOf(board.activePlayerId)
    val nextPlayerId = if (activePlayerIndex < 0 || activePlayerIndex + 1 == playerIds.size) {
        playerIds.first()
    } else {
        playerIds[activePlayerIndex + 1]
    }
    val updatedBoard = board.discardPileB().copy(
        activePlayerId = nextPlayerId,
        moveCount = board.moveCount + 1,
        canRoll = true,
        diceRolling = false,
        takenCard = null,
        sharesCount = null,
        canTakeCard = emptyList(),
        auction = null,
        bidList = emptyList()
    )
    Storage.updateBoard(updatedBoard)
    val nextPlayer = activePlayers.find { it.id == nextPlayerId }
    if ((nextPlayer?.inRest ?: 0) > 0) {
        val player = Storage.getPlayer(nextPlayerId)
        Storage.updatePlayer(player.copy(inRest = player.inRest - 1))
        nextPlayer(updatedBoard)
    }
}

private fun Board.discardPileB(): Board {
    val card = takenCard ?: return this
    val newDiscard = discard.toMutableMap()
    newDiscard[card.type] = newDiscard[card.type].orEmpty() + card.id
    val newCards = cards.toMutableMap()
    newCards[card.type] = newCards[card.type].orEmpty() - card.id
    return copy(
        discard = newDiscard,
        cards = newCards,
        takenCard = null,
        sharesCount = null,
    ).invalidateDecks()
}

private fun Board.invalidateDecks(): Board {
    val newDiscard = discard.toMutableMap()
    val newCards = cards.mapValues { (type, list) ->
        list.ifEmpty {
            val recycled = newDiscard[type].orEmpty()
            newDiscard[type] = emptyList()
            recycled
        }
    }
    return copy(cards = newCards, discard = newDiscard)
}