package ua.vald_zx.game.rat.race.card.components.preview

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.KoinApplicationPreview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import kotlin.time.Clock

@Composable
fun InitPreview(
    content: @Composable () -> Unit
) {
    val previewModule = module {
        viewModel { parameters ->
            BoardViewModel(
                board = parameters.get<Board>(),
                player = parameters.get<Player>(),
                serviceProvider = {
                    object : RaceRatService {

                        override suspend fun hello(helloUuid: String, boardId: String): Instance {
                            error("Not for preview")
                        }

                        override suspend fun ping() {
                        }

                        override suspend fun connectionIsValid() {
                        }

                        override suspend fun getBoards(): List<BoardId> = emptyList()

                        override fun observeBoards(): Flow<List<BoardId>> {
                            error("Not for preview")
                        }

                        override suspend fun deleteBoard(boardId: String) {
                        }

                        override suspend fun createBoard(
                            name: String,
                            loanLimit: Long,
                            businessLimit: Long,
                            decks: Map<BoardCardType, Int>,
                            outerCircleConditions: OuterCircleConditions,
                            victoryConditions: VictoryConditions,
                            transportMovementBonusEnabled: Boolean,
                            generation: BoardGeneration,
                        ): Board {
                            error("Not for preview")
                        }

                        override suspend fun updateAttributes(attrs: PlayerAttributes) {
                            error("Not for preview")
                        }

                        override suspend fun getPlayer(): Player {
                            error("Not for preview")
                        }

                        override fun observeGeneration(): Flow<BoardGenerationProgress> = MutableSharedFlow()

                        override suspend fun continueGeneration() {
                        }

                        override suspend fun restartGeneration() {
                        }

                        override suspend fun makePlayer(
                            uuid: String,
                            color: Long,
                            card: PlayerCard,
                        ): Player {
                            error("Not for preview")
                        }

                        override fun eventsObserve(): Flow<Event> = MutableSharedFlow()

                        override suspend fun getPlayers(): List<Player> = emptyList()

                        override suspend fun getBoard(): Board {
                            error("Not for preview")
                        }

                        override suspend fun sendMoney(receiverId: String, amount: Long) {
                        }

                        override suspend fun sendMessage(text: String) {
                        }

                        override suspend fun rollDice() {
                        }

                        override suspend fun next() {
                        }

                        override suspend fun takeCard(cardType: BoardCardType) {
                        }

                        override suspend fun buyDeputy() {
                        }

                        override suspend fun buyCorruptBusiness(card: BoardCard.Chance.CorruptBusiness) {
                        }

                        override suspend fun buyCorruptLand(card: BoardCard.Chance.CorruptLand) {
                        }

                        override suspend fun reelection() {
                        }

                        override suspend fun skipDeputies() {
                        }

                        override suspend fun takeSalary() {
                        }

                        override suspend fun buyBusiness(business: Business) {
                        }

                        override suspend fun dismissalConfirmed(business: Business) {
                        }

                        override suspend fun sellingAllBusinessConfirmed(business: Business) {
                        }

                        override suspend fun minusCash(price: Long) {
                        }

                        override suspend fun payExpenses(card: BoardCard.Expenses) {
                        }

                        override suspend fun buyThing(card: BoardCard.Shopping) {
                        }

                        override suspend fun changePosition(position: Int) {
                        }

                        override suspend fun debugChangePosition(location: PlayerLocation) {
                        }

                        override suspend fun debugUpdatePlayer(values: DebugPlayerValues) {
                        }

                        override suspend fun buyEstate(card: Estate) {
                        }

                        override suspend fun buyLand(land: Land) {
                        }

                        override suspend fun randomJob(card: BoardCard.Chance.RandomJob) {
                        }

                        override suspend fun buyShares(shares: Shares, totalCount: Long) {
                        }

                        override suspend fun selectCardByNo(cardId: Int, cardType: BoardCardType) {
                        }

                        override suspend fun debugMoveToAndSelectCard(
                            cardId: Int,
                            cardType: BoardCardType
                        ) {
                        }

                        override suspend fun extendBusiness(
                            business: Business,
                            card: BoardCard.EventStore.BusinessExtending
                        ) {
                        }

                        override suspend fun sellLands(area: Long, priceOfUnit: Long) {
                        }

                        override suspend fun sellCorruptBusiness(business: Business, salePercentage: Long) {
                        }

                        override suspend fun sellCorruptLands(area: Long, priceOfUnit: Long) {
                        }

                        override suspend fun sellShares(
                            card: BoardCard.EventStore.Shares,
                            count: Long
                        ) {
                        }

                        override suspend fun sellEstate(
                            card: List<Estate>,
                            price: Long
                        ) {
                        }

                        override suspend fun passLand() {
                        }

                        override suspend fun passCorruptBusiness() {
                        }

                        override suspend fun passCorruptLand() {
                        }

                        override suspend fun passShares(sharesType: String) {
                        }

                        override suspend fun passEstate() {
                        }

                        override suspend fun playHighRiskInvestment(stake: Long, guess: Int) {
                        }

                        override suspend fun playMediumRiskInvestment(stake: Long, even: Boolean) {
                        }

                        override suspend fun investInFund(amount: Long) {
                        }

                        override suspend fun capitalizeFunds() {
                        }

                        override suspend fun toDeposit(amount: Long) {
                        }

                        override suspend fun repayLoan(amount: Long) {
                        }

                        override suspend fun advertiseAuction(auction: Auction) {
                        }

                        override suspend fun sellBid(bid: Bid) {
                        }

                        override suspend fun makeBid(price: Long, count: Long) {
                        }

                        override suspend fun enterOuterCircle() = Unit

                        override suspend fun buyDream() = Unit

                        override suspend fun selectDream(dreamId: String) = Unit
                    }
                }
            )
        }
    }
    KoinApplicationPreview(application = { modules(previewModule) }) {
        AppTheme {
            content()
        }
    }
}

@Composable
fun InitPreviewWithVm(
    content: @Composable (BoardViewModel) -> Unit
) {
    InitPreview {
        val vm: BoardViewModel = koinViewModel<BoardViewModel>(
            parameters = {
                parametersOf(
                    Board(
                        name = "",
                        loanLimit = 0,
                        businessLimit = 0,
                        createDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC),
                        id = "",
                        cards = emptyMap(),
                        canTakeCard = emptyList(),
                    ), Player(
                        id = "",
                        boardId = "",
                        attrs = PlayerAttributes(0, 0),
                    )
                )
            }
        )
        content(vm)
    }
}
