@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package ua.vald_zx.game.rat.race.server

import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.server.utils.savedMutableStateFlow
import kotlin.math.absoluteValue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal val LOGGER = KtorSimpleLogger("RaceRatService")

val globalEventBusMap = mutableMapOf<String, MutableSharedFlow<GlobalEvent>>()
fun getGlobalEventBus(boardId: String): MutableSharedFlow<GlobalEvent> {
    return globalEventBusMap.getOrPut(boardId) { MutableSharedFlow() }
}

class RaceRatServiceImpl(invokeOnCompletionFlow: MutableSharedFlow<Boolean>) : RaceRatService,
    CoroutineScope by CoroutineScope(Dispatchers.Default) {
    private var uuid: String = ""
    private val eventBus = MutableSharedFlow<Event>()
    private val boardId: String
        get() = boardState?.value?.id.orEmpty()
    private val globalEventBus: MutableSharedFlow<GlobalEvent>
        get() = getGlobalEventBus(boardId)
    private var boardStateSubJob: Job? = null
    private var playerStateSubJob: Job? = null
    private var boardGlobalEventsJob: Job? = null

    private val player: Player
        get() = getPlayerState(uuid)?.value ?: error("No player found for $uuid")
    private val board: Board
        get() = boardState?.value ?: error("No board found")
    private var boardState: MutableStateFlow<Board>? = null
        set(value) {
            field = value
            if (value != null) {
                boardStateSubJob?.cancel()
                boardStateSubJob = launch {
                    value.collect { boardState ->
                        eventBus.emit(Event.BoardChanged(boardState))
                    }
                }
                boardGlobalEventsJob?.cancel()
                boardGlobalEventsJob = launch {
                    globalEventBus.collect { event ->
                        when (event) {
                            is GlobalEvent.SendMoney -> {
                                if (event.receiverId == uuid) {
                                    eventBus.emit(Event.MoneyIncome(event.playerId, event.amount))
                                    changePlayer {
                                        this.plusCash(event.amount)
                                    }
                                }
                            }

                            is GlobalEvent.PlayerChanged -> {
                                eventBus.emit(Event.PlayerChanged(event.player))
                            }

                            is GlobalEvent.PlayerHadBaby -> {
                                eventBus.emit(Event.PlayerHadBaby(event.playerId, event.babies))
                            }

                            is GlobalEvent.PlayerMarried -> {
                                eventBus.emit(Event.PlayerMarried(event.playerId))
                            }

                            is GlobalEvent.PlayerDivorced -> {
                                eventBus.emit(Event.PlayerDivorced(event.playerId))
                            }

                            is GlobalEvent.BidSelled -> {
                                if (event.bid.playerId == uuid) {
                                    buyLot(event.auction, event.bid)
                                }
                            }
                        }
                    }
                }
            }
        }

    init {
        invokeOnCompletionFlow.onEach {
            closeSession()
        }.launchIn(this)
    }

    override suspend fun hello(helloUuid: String): Instance {
        boards.value.forEach { boardId ->
            boardState = getBoardState(boardId)
            val board = boardState?.value
            if (board?.playerIds?.contains(helloUuid) == true) {
                uuid = helloUuid
                invalidateNextPlayer(board.activePlayer)
                invalidateBoard(board)
                playerStateSubJob?.cancel()
                playerStateSubJob = launch {
                    getPlayerState(uuid)?.collect { player ->
                        globalEventBus.emit(GlobalEvent.PlayerChanged(player))
                    }
                }
                return Instance(uuid, board, player)
            } else {
                uuid = Uuid.random().toString()
            }
        }
        return Instance(uuid, null, null)
    }

    override suspend fun ping() {
        LOGGER.info("Pong $uuid")
    }

    override suspend fun getBoards(): List<BoardId> {
        return boards.value.mapNotNull { boardId ->
            val boardState = getBoardState(boardId) ?: return@mapNotNull null
            val board = boardState.value
            BoardId(board.id, board.name, board.createDateTime)
        }
    }

    override fun observeBoards(): Flow<List<BoardId>> = boards.mapNotNull { list ->
        list.map { board ->
            val boardState = getBoardState(board) ?: error("No board found for $board")
            boardState.value.toBoardId()
        }
    }

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
            cards = decks.map { (type, size) ->
                type to (1..size).toMutableList()
            }.toMap()
        )
        val boardFlow = savedMutableStateFlow({ board }, board.id)
        boards.value = boards.value.toMutableList().apply { add(board.id) }
        boardMap[board.id] = boardFlow
        boardState = boardFlow
        return board
    }

    override suspend fun selectBoard(boardId: String): Board {
        boardState = getBoardState(boardId)
        return boardState?.value ?: error("No board found for $boardId")
    }

    override suspend fun makePlayer(
        color: Long,
        card: PlayerCard,
    ): Player {
        val newPlayer = Player(
            id = uuid,
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
        players.value = players.value.toMutableList().apply {
            add(uuid)
            playerMap[uuid] = savedMutableStateFlow({ newPlayer }, uuid)
        }
        changeBoard {
            copy(playerIds = playerIds + uuid)
        }
        takeSalary()
        invalidateNextPlayer(boardState?.value?.activePlayer.orEmpty())
        return player
    }

    override suspend fun getPlayer(): Player {
        return player
    }

    override suspend fun updateAttributes(attrs: PlayerAttributes) = changePlayer {
        copy(attrs = attrs)
    }

    override fun eventsObserve(): Flow<Event> = eventBus

    override suspend fun getPlayers(): List<Player> {
        return boardState?.value?.playerIds?.map { playerId ->
            getPlayerState(playerId)?.value ?: error("No player found for $playerId")
        }?.toList().orEmpty()
    }

    override suspend fun getBoard(): Board {
        return board
    }

    override suspend fun sendMoney(receiverId: String, amount: Long) {
        globalEventBus.emit(GlobalEvent.SendMoney(uuid, receiverId, amount))
        changePlayer {
            this.minusCash(amount)
        }
    }

    override suspend fun rollDice() {
        changeBoard {
            val dice = (1..6).random()
            copy(dice = dice, canRoll = false, diceRolling = true)
        }
        delay(4000)
        changeBoard {
            copy(diceRolling = false)
        }
        move()
    }

    override suspend fun takeCard(cardType: BoardCardType) {
        val card = board.cards[cardType]?.random() ?: return
        selectCard(card, cardType)
    }

    private suspend fun selectCard(cardId: Int, cardType: BoardCardType) {
        changeBoard {
            copy(
                cards = cards.apply {
                    this[cardType]?.remove(cardId)
                },
                takenCard = CardLink(cardType, cardId),
                canTakeCard = null,
                processedPlayerIds = emptySet(),
            )
        }
    }

    override suspend fun selectCardByNo(cardId: Int) {
        board.canTakeCard?.let { cardType ->
            selectCard(cardId, cardType)
        }
    }

    override suspend fun next() {
        nextPlayer()
    }

    private suspend fun invalidateNextPlayer(activePlayer: String) {
        val playerIds = boardState?.value?.playerIds.orEmpty()
        if (activePlayer.isEmpty() || !playerIds.contains(activePlayer) || getPlayerState(activePlayer)?.value?.isInactive.let { it == null || it }) {
            nextPlayer()
        }
    }

    private suspend fun invalidateBoard(board: Board) {
        if (!board.canRoll && !board.diceRolling && board.canTakeCard == null && board.takenCard == null) {
            nextPlayer()
        }
    }

    private suspend fun nextPlayer() {
        boardState.nextPlayer()
    }

    override suspend fun closeSession() {
        changePlayer {
            if (boardState?.value?.activePlayer == uuid) {
                nextPlayer()
            }
            copy(isInactive = true)
        }
        boardState?.value?.let { board ->
            validateBoard(board.id)
        }
        boardStateSubJob?.cancel()
        playerStateSubJob?.cancel()
        boardGlobalEventsJob?.cancel()
        coroutineContext.cancel()
    }

    override suspend fun takeSalary() {
        changePlayer {
            val cashFlow = cashFlow()
            if (cashFlow >= 0) {
                plusCash(cashFlow)
            } else {
                minusCash(cashFlow.absoluteValue)
            }.copy(salaryPosition = null)
        }
    }

    override suspend fun buyBusiness(business: Business, needGoNextPlayer: Boolean) {
        val currentBusiness = player.businesses
        if (business.type == BusinessType.SMALL
            && currentBusiness.any { it.type == BusinessType.WORK }
            && currentBusiness.count { it.type == BusinessType.SMALL } == 1
        ) {
            eventBus.emit(Event.ConfirmDismissal(business))
        } else if (currentBusiness.isNotEmpty()
            && currentBusiness.first().type.klass != business.type.klass
            && !currentBusiness.any { it.type == BusinessType.WORK }
        ) {
            eventBus.emit(Event.ConfirmSellingAllBusiness(business))
        } else {
            changePlayer {
                copy(businesses = currentBusiness + business)
                    .minusCash(business.price)
            }
            if (needGoNextPlayer) nextPlayer()
        }
    }

    override suspend fun dismissalConfirmed(business: Business) {
        changePlayer {
            val businesses = player.businesses.filter { it.type != BusinessType.WORK } + business
            copy(businesses = businesses).minusCash(business.price)
        }
        nextPlayer()
    }

    override suspend fun sellingAllBusinessConfirmed(business: Business) {
        changePlayer {
            copy(businesses = listOf(business))
                .plusCash(player.businesses.sumOf { it.price })
                .minusCash(business.price)
        }
        nextPlayer()
    }

    private suspend fun move() {
        val layer = player.location.level.toLayer()
        val cellCount = layer.cellCount
        val currentPosition = player.location.position
        val newPosition = moveTo(currentPosition, cellCount, board.dice)
        processNewPosition(newPosition)
    }

    private suspend fun processNewPosition(newPosition: Int) {
        val layer = player.location.level.toLayer()
        val currentPosition = player.location.position
        val list = if (currentPosition > newPosition) {
            layer.places.subList(currentPosition + 1, layer.places.size) + layer.places.subList(0, newPosition + 1)
        } else {
            layer.places.subList(currentPosition + 1, newPosition + 1)
        }
        val salaryPosition = if (list.contains(PlaceType.Salary)) {
            if (player.cashFlow() > 0) {
                var salaryPosition =
                    currentPosition + list.indexOfLast { it == PlaceType.Salary } + 1
                val placeCount = layer.places.size
                if (salaryPosition >= placeCount) {
                    salaryPosition -= placeCount
                }
                salaryPosition
            } else {
                takeSalary()
                null
            }
        } else null
        changeBoard {
            copy(moveCount = moveCount + 1, canRoll = false)
        }
        changePlayer {
            copy(location = location.copy(position = newPosition), salaryPosition = salaryPosition)
        }
        val place = layer.places[newPosition]
        when (place) {
            PlaceType.BigBusiness -> {
                changeBoard {
                    copy(canTakeCard = BoardCardType.BigBusiness)
                }
            }

            PlaceType.Business -> {
                changeBoard {
                    copy(canTakeCard = BoardCardType.SmallBusiness)
                }
            }

            PlaceType.Chance -> {
                changeBoard {
                    copy(canTakeCard = BoardCardType.Chance)
                }
            }

            PlaceType.Deputy -> {
                changeBoard {
                    copy(canTakeCard = BoardCardType.Deputy)
                }
            }

            PlaceType.Expenses -> {
                changeBoard {
                    copy(canTakeCard = BoardCardType.Expenses)
                }
            }

            PlaceType.Shopping -> {
                changeBoard {
                    copy(canTakeCard = BoardCardType.Shopping)
                }
            }

            PlaceType.Store -> {
                changeBoard {
                    copy(canTakeCard = BoardCardType.EventStore)
                }
            }

            PlaceType.Bankruptcy -> {
                changePlayer {
                    if (businesses.isNotEmpty() && businesses.any { it.type != BusinessType.WORK }) {
                        val random = businesses.filter { it.type != BusinessType.WORK }.random()
                        eventBus.emit(Event.BankruptBusiness(random))
                        copy(businesses = businesses - random)
                    } else this
                }
                nextPlayer()
            }

            PlaceType.Child -> {
                changePlayer {
                    val totalBabies = babies + 1
                    copy(babies = totalBabies).plusCash(1000).apply {
                        globalEventBus.emit(GlobalEvent.PlayerHadBaby(uuid, totalBabies))
                    }
                }
                nextPlayer()
            }

            PlaceType.Divorce -> {
                if (player.isMarried) {
                    changePlayer {
                        if (player.card.gender == Gender.MALE) {
                            copy(isMarried = false, babies = 0, cash = cash / 2, deposit = deposit / 2)
                        } else {
                            copy(isMarried = false)
                        }.apply {
                            globalEventBus.emit(GlobalEvent.PlayerDivorced(uuid))
                        }
                    }
                }
                nextPlayer()
            }

            PlaceType.Resignation -> {
                val business = player.businesses.find { it.type == BusinessType.WORK }
                if (business != null) {
                    changePlayer {
                        copy(businesses = businesses - business).apply {
                            eventBus.emit(Event.Resignation(business))
                        }
                    }
                }
                nextPlayer()
            }

            PlaceType.Love -> {
                if (!player.isMarried) {
                    changePlayer {
                        copy(isMarried = true).apply {
                            globalEventBus.emit(GlobalEvent.PlayerMarried(uuid))
                        }
                    }
                    if (player.card.gender == Gender.MALE) {
                        changePlayer {
                            this.minusCash(player.config.marriageCost)
                        }
                    }
                }
                nextPlayer()
            }

            PlaceType.Rest -> {
                changePlayer {
                    copy(inRest = 2)
                }
                nextPlayer()
            }

            PlaceType.Salary -> {
                nextPlayer()
            }

            PlaceType.Start -> {
                nextPlayer()
            }

            PlaceType.TaxInspection -> {
                nextPlayer()
            }

            PlaceType.Desire -> {
                nextPlayer()
            }
        }
    }

    override suspend fun minusCash(price: Long) {
        changePlayer {
            minusCash(price)
        }
    }

    private suspend fun changeBoard(todo: suspend Board.() -> Board) {
        boardState.changeBoard(todo)
    }

    private suspend fun changePlayer(todo: suspend Player.() -> Player) {
        getPlayerState(uuid)?.let { playerState ->
            playerState.value = playerState.value.todo().let { player ->
                val newTotal = player.total()
                val previous = playerState.value.total()
                if (newTotal != previous) {
                    val delta = newTotal - previous
                    player.copy(lastTotals = (player.lastTotals + delta).takeLast(3))
                } else player
            }.let {
                val newCashFlow = player.cashFlow()
                val previews = playerState.value.cashFlow()
                if (newCashFlow != previews) {
                    val delta = newCashFlow - previews
                    it.copy(lastCashFlows = (it.lastCashFlows + delta).takeLast(3))
                } else it
            }
            globalEventBus.emit(GlobalEvent.PlayerChanged(playerState.value))
        }
    }

    private fun Player.plusCash(value: Long): Player {
        return copy(cash = cash + value)
    }

    private fun Player.minusCash(
        value: Long,
        isFundBuy: Boolean = false
    ): Player {
        if (value == 0L) return this
        launch { eventBus.emit(Event.SubCash(value)) }
        return if (cash > value) {
            copy(cash = cash - value)
        } else if ((cash + deposit) > value) {
            launch { eventBus.emit(Event.DepositWithdraw(value - cash)) }
            copy(cash = 0, deposit = (deposit + cash) - value)
        } else if (!isFundBuy && config.hasFunds && funds.isNotEmpty()) {
            var stub = cash + deposit
            var newFunds = funds.toList()
            funds.sortedBy { it.rate }.firstOrNull { fund ->
                if (stub + fund.amount > value) {
                    newFunds = funds.replace(fund, fund.copy(amount = stub + fund.amount - value))
                    true
                } else {
                    stub += fund.amount
                    newFunds = newFunds.remove(fund)
                    false
                }
            }
            if (newFunds.isEmpty() && stub < value) {
                val newLoan = loan + (value - stub)
                if (newLoan > board.loanLimit) {
                    launch { eventBus.emit(Event.LoanOverlimited) }
                }
                copy(cash = 0, deposit = 0, funds = emptyList(), loan = newLoan)
            } else {
                copy(cash = 0, deposit = 0, funds = newFunds)
            }
        } else {
            launch { eventBus.emit(Event.LoanAdded(value - (cash + deposit))) }
            val newLoan = loan + (value - (deposit + cash))
            if (newLoan > board.loanLimit) {
                launch { eventBus.emit(Event.LoanOverlimited) }
            }
            copy(cash = 0, deposit = 0, loan = newLoan)
        }
    }

    override suspend fun buyThing(card: BoardCard.Shopping) {
        when (card.shopType) {
            ShopType.AUTO -> {
                changePlayer {
                    copy(cars = cars + 1).minusCash(card.price)
                }
            }

            ShopType.HOUSE -> {
                changePlayer {
                    copy(cottage = cottage + 1).minusCash(card.price)
                }
            }

            ShopType.APARTMENT -> {
                changePlayer {
                    copy(apartment = apartment + 1).minusCash(card.price)
                }
            }

            ShopType.YACHT -> {
                changePlayer {
                    copy(yacht = yacht + 1).minusCash(card.price)
                }
            }

            ShopType.FLY -> {
                changePlayer {
                    copy(flight = flight + 1).minusCash(card.price)
                }
            }
        }
        nextPlayer()
    }

    override suspend fun changePosition(position: Int) {
        processNewPosition(position)
    }

    override suspend fun buyEstate(estate: Estate, needGoNextPlayer: Boolean) {
        changePlayer {
            copy(estateList = estateList + estate).minusCash(estate.price)
        }
        if (needGoNextPlayer) nextPlayer()
    }

    override suspend fun buyLand(land: Land, needGoNextPlayer: Boolean) {
        changePlayer {
            copy(landList = landList + land).minusCash(land.price)
        }
        if (needGoNextPlayer) nextPlayer()
    }

    override suspend fun randomJob(card: BoardCard.Chance.RandomJob) {
        changePlayer {
            plusCash(card.profit)
        }
        nextPlayer()
    }

    override suspend fun buyShares(shares: Shares, needGoNextPlayer: Boolean) {
        changePlayer {
            copy(sharesList = sharesList + shares).minusCash(shares.price)
        }
        if (needGoNextPlayer) nextPlayer()
    }

    override suspend fun extendBusiness(
        business: Business,
        card: BoardCard.EventStore.BusinessExtending
    ) {
        changePlayer {
            val extendedBusiness = business.copy(extentions = business.extentions + card.profit)
            val updatedBusiness = businesses.replace(business, extendedBusiness)
            copy(businesses = updatedBusiness)
        }
        nextPlayer()
    }

    override suspend fun sellLands(area: Long, priceOfUnit: Long) {
        changePlayer {
            val lands = landList.toMutableList()
            val totalArea = lands.sumOf { it.area }
            if (totalArea >= area) {
                val updatedLands = if (totalArea == area) {
                    emptyList()
                } else {
                    var remainder = area
                    val newLands = lands.toMutableList()
                    lands.forEach { land ->
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
        changeBoard {
            copy(processedPlayerIds = processedPlayerIds + uuid)
        }
        passLand()
    }

    override suspend fun sellShares(
        card: BoardCard.EventStore.Shares,
        count: Long
    ) {
        changePlayer {
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
        changeBoard {
            copy(processedPlayerIds = processedPlayerIds + uuid)
        }
        passShares(card.sharesType)
    }

    override suspend fun sellEstate(
        card: List<Estate>,
        price: Long
    ) {
        changePlayer {
            copy(estateList = estateList - card).plusCash(card.size * price)
        }
        changeBoard {
            copy(processedPlayerIds = processedPlayerIds + uuid)
        }
        passEstate()
    }

    override suspend fun passLand() {
        val playersWithLands = board.players().filter { it.landList.isNotEmpty() }.map { it.id }.toSet()
        if (playersWithLands.isEmpty() || playersWithLands == board.processedPlayerIds) {
            nextPlayer()
        }
    }

    override suspend fun passShares(sharesType: SharesType) {
        val playersWithShares =
            board.players().filter { it.sharesList.any { it.type == sharesType } }.map { it.id }.toSet()
        if (playersWithShares.isEmpty() || playersWithShares == board.processedPlayerIds) {
            nextPlayer()
        }
    }

    override suspend fun passEstate() {
        val playersWithEstate = board.players().filter { it.estateList.isNotEmpty() }.map { it.id }.toSet()
        if (playersWithEstate.isEmpty() || playersWithEstate == board.processedPlayerIds) {
            nextPlayer()
        }
    }

    override suspend fun toDeposit(amount: Long) {
        changePlayer {
            copy(deposit = deposit + amount).minusCash(amount)
        }
    }

    override suspend fun repayLoan(amount: Long) {
        changePlayer {
            copy(loan = loan - amount).minusCash(amount)
        }
    }

    override suspend fun advertiseAuction(auction: Auction) {
        changeBoard {
            copy(auction = auction)
        }
    }

    override suspend fun sellBid(bid: Bid) {
        val auction = board.auction ?: return
        val profit = auction.getProfit(bid)
        changePlayer {
            plusCash(profit)
        }
        globalEventBus.emit(GlobalEvent.BidSelled(bid, auction))
        if (auction !is Auction.SharesAuction) {
            changeBoard {
                copy(auction = null, bidList = emptyList())
            }
            nextPlayer()
        } else {
            changeBoard {
                copy(
                    auction = auction.copy(
                        shares = auction.shares.copy(
                            count = auction.shares.count - bid.count
                        )
                    )
                )
            }
        }
    }

    override suspend fun makeBid(price: Long, count: Long) {
        changeBoard {
            copy(bidList = bidList.filter { it.playerId != uuid } + Bid(uuid, price, count))
        }
    }

    private suspend fun buyLot(
        auction: Auction,
        bid: Bid
    ) {
        when (auction) {
            is Auction.BusinessAuction -> {
                buyBusiness(auction.business.copy(price = bid.bid), needGoNextPlayer = false)
                eventBus.emit(Event.BidBusinessAuctionSuccessBuy)
            }

            is Auction.EstateAuction -> {
                buyEstate(auction.estate.copy(price = bid.bid), needGoNextPlayer = false)
                eventBus.emit(Event.BidEstateAuctionSuccessBuy)
            }

            is Auction.LandAuction -> {
                buyLand(auction.land.copy(price = bid.bid), needGoNextPlayer = false)
                eventBus.emit(Event.BidLandAuctionSuccessBuy)
            }

            is Auction.SharesAuction -> {
                buyShares(auction.shares.copy(count = bid.count, buyPrice = bid.bid), needGoNextPlayer = false)
                eventBus.emit(Event.BidSharesAuctionSuccessBuy)
            }
        }
    }
}