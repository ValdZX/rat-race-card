package ua.vald_zx.game.rat.race.card.shared

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc
import kotlinx.serialization.Serializable

@Serializable
sealed class GlobalEvent {

    @Serializable
    data class SendMoney(val playerId: String, val receiverId: String, val amount: Long) : GlobalEvent()

    @Serializable
    data class PlayerChanged(val player: Player) : GlobalEvent()

    @Serializable
    data class PlayerHadBaby(val playerId: String, val babies: Long) : GlobalEvent()

    @Serializable
    data class PlayerMarried(val playerId: String) : GlobalEvent()

    @Serializable
    data class PlayerDivorced(val playerId: String) : GlobalEvent()

    @Serializable
    data class BidSelled(val bid: Bid, val auction: Auction) : GlobalEvent()

    @Serializable
    data class PlayerWon(val playerId: String, val playerName: String) : GlobalEvent()

    @Serializable
    data class EconomyPeriodAdvanced(val index: EconomyIndex) : GlobalEvent()

    @Serializable
    data class MarketCrashed(val playerId: String, val sector: String, val lostValue: Long) : GlobalEvent()
}

@Serializable
sealed class Event {
    @Serializable
    data class MoneyIncome(val playerId: String, val amount: Long) : Event()

    @Serializable
    data class PlayerHadBaby(val playerId: String, val babies: Long) : Event()

    @Serializable
    data class PlayerMarried(val playerId: String) : Event()

    @Serializable
    data class PlayerDivorced(val playerId: String) : Event()

    @Serializable
    data class PlayerChanged(val player: Player) : Event()

    @Serializable
    data class BoardChanged(val board: Board) : Event()

    @Serializable
    data class LoanAdded(val balance: Long) : Event()

    @Serializable
    data class DepositWithdraw(val balance: Long) : Event()

    @Serializable
    data class AddCash(val amount: Long) : Event()

    @Serializable
    data class SubCash(val amount: Long) : Event()

    @Serializable
    data class ConfirmDismissal(val business: Business) : Event()

    @Serializable
    data class Fired(val business: Business) : Event()

    @Serializable
    data class ConfirmSellingAllBusiness(val business: Business) : Event()

    @Serializable
    data class BankruptBusiness(val business: Business) : Event()

    @Serializable
    data class Resignation(val business: Business) : Event()

    @Serializable
    data object LoanOverlimited : Event()

    @Serializable
    data object BidBusinessAuctionSuccessBuy : Event()

    @Serializable
    data object BidEstateAuctionSuccessBuy : Event()

    @Serializable
    data object BidLandAuctionSuccessBuy : Event()

    @Serializable
    data object BidSharesAuctionSuccessBuy : Event()

    @Serializable
    data class HighRiskPlayed(val outcome: InvestmentOutcome, val guess: Int) : Event()

    @Serializable
    data class MediumRiskPlayed(val outcome: InvestmentOutcome, val even: Boolean) : Event()

    @Serializable
    data class FundsCapitalized(val profit: Long) : Event()

    @Serializable
    data class TaxInspectionPaid(val amount: Long) : Event()

    @Serializable
    data object CheckState : Event()

    @Serializable
    data object DreamOffered : Event()

    @Serializable
    data class PlayerWon(val playerId: String, val playerName: String) : Event()

    @Serializable
    data class EconomyPeriodAdvanced(val index: EconomyIndex) : Event()

    @Serializable
    data class MarketCrashed(val sector: String, val lostValue: Long) : Event()

    @Serializable
    data class ScamResolved(val paid: Long, val received: Long) : Event()

    @Serializable
    data class PaydayLoanTaken(val amount: Long) : Event()
}

@Serializable
data class Instance(val board: Board, val player: Player?)

@Rpc
interface RaceRatService {
    suspend fun hello(helloUuid: String = "", boardId: String, clientVersion: String? = null): Instance
    suspend fun ping()
    suspend fun connectionIsValid()
    suspend fun getBoards(): List<BoardId>
    fun observeBoards(): Flow<List<BoardId>>
    suspend fun deleteBoard(boardId: String)
    suspend fun createBoard(
        name: String,
        loanLimit: Long,
        businessLimit: Long,
        decks: Map<BoardCardType, Int>,
        outerCircleConditions: OuterCircleConditions = OuterCircleConditions(),
        victoryConditions: VictoryConditions = VictoryConditions(),
        transportMovementBonusEnabled: Boolean = true,
        generation: BoardGeneration = BoardGeneration(),
        contentPackVersions: Map<FeatureId, Int> = standardContentPackVersions(),
        inflation: InflationSettings = InflationSettings(),
    ): Board

    suspend fun updateAttributes(attrs: PlayerAttributes)
    suspend fun getPlayer(): Player
    suspend fun executeCommand(envelope: GameCommandEnvelope): GameCommandResponse
    suspend fun getLedger(): List<LedgerEntry>
    fun observeGeneration(): Flow<BoardGenerationProgress>
    suspend fun continueGeneration()
    suspend fun restartGeneration()
    suspend fun makePlayer(
        uuid: String,
        color: Long,
        card: PlayerCard,
    ): Player

    fun eventsObserve(): Flow<Event>
    suspend fun getPlayers(): List<Player>
    suspend fun getBoard(): Board

    suspend fun sendMoney(receiverId: String, amount: Long)
    suspend fun sendMessage(text: String)

    suspend fun rollDice()
    suspend fun next()
    suspend fun takeCard(cardType: BoardCardType)
    suspend fun buyDeputy()
    suspend fun buyCorruptBusiness(card: BoardCard.Chance.CorruptBusiness)
    suspend fun buyCorruptLand(card: BoardCard.Chance.CorruptLand)
    suspend fun reelection()
    suspend fun skipDeputies()
    suspend fun takeSalary()
    suspend fun buyBusiness(business: Business)

    suspend fun dismissalConfirmed(business: Business)
    suspend fun sellingAllBusinessConfirmed(business: Business)
    suspend fun minusCash(price: Long)
    suspend fun payExpenses(card: BoardCard.Expenses)
    suspend fun buyThing(card: BoardCard.Shopping)
    suspend fun changePosition(position: Int)
    suspend fun debugChangePosition(location: PlayerLocation)
    suspend fun debugUpdatePlayer(values: DebugPlayerValues)
    suspend fun buyEstate(estate: Estate)
    suspend fun buyLand(land: Land)
    suspend fun randomJob(card: BoardCard.Chance.RandomJob)
    suspend fun investInScam(card: BoardCard.Chance.Scam)
    suspend fun declineScam()
    suspend fun applyMarketCrash(card: BoardCard.EventStore.MarketCrash)
    suspend fun buyShares(shares: Shares, totalCount: Long)
    suspend fun selectCardByNo(cardId: Int, cardType: BoardCardType)
    suspend fun debugMoveToAndSelectCard(cardId: Int, cardType: BoardCardType)
    suspend fun extendBusiness(business: Business, card: BoardCard.EventStore.BusinessExtending)
    suspend fun sellLands(area: Long, priceOfUnit: Long)
    suspend fun sellCorruptBusiness(business: Business, salePercentage: Long)
    suspend fun sellCorruptLands(area: Long, priceOfUnit: Long)
    suspend fun sellShares(card: BoardCard.EventStore.Shares, count: Long)
    suspend fun sellEstate(card: List<Estate>, price: Long)
    suspend fun passLand()
    suspend fun passCorruptBusiness()
    suspend fun passCorruptLand()
    suspend fun passShares(sharesType: String)
    suspend fun passEstate()
    suspend fun playHighRiskInvestment(stake: Long, guess: Int)
    suspend fun playMediumRiskInvestment(stake: Long, even: Boolean)
    suspend fun investInFund(amount: Long)
    suspend fun capitalizeFunds()
    suspend fun toDeposit(amount: Long)
    suspend fun repayLoan(amount: Long)
    suspend fun repayDebt(debtId: String, amount: Long)
    suspend fun advertiseAuction(auction: Auction)
    suspend fun sellBid(bid: Bid)
    suspend fun makeBid(price: Long, count: Long)
    suspend fun enterOuterCircle()
    suspend fun buyDream()
    suspend fun selectDream(dreamId: String)
}
