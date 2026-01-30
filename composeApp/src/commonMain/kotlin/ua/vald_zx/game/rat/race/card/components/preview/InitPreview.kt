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
                        override suspend fun init() {
                            //nop
                        }

                        override suspend fun hello(helloUuid: String): Instance {
                            error("Not for preview")
                        }

                        override suspend fun eventReceived(event: Event) {
                            //nop
                        }

                        override suspend fun ping() {
                            //nop
                        }

                        override suspend fun connectionIsValid() {
                            //nop
                        }

                        override suspend fun getBoards(): List<BoardId> = emptyList()

                        override fun observeBoards(): Flow<List<BoardId>> {
                            error("Not for preview")
                        }

                        override suspend fun createBoard(
                            name: String,
                            loanLimit: Long,
                            businessLimit: Long,
                            decks: Map<BoardCardType, Int>
                        ): Board {
                            error("Not for preview")
                        }

                        override suspend fun selectBoard(id: String): Board {
                            error("Not for preview")
                        }

                        override suspend fun updateAttributes(attrs: PlayerAttributes) {
                            error("Not for preview")
                        }

                        override suspend fun getPlayer(): Player {
                            error("Not for preview")
                        }

                        override suspend fun makePlayer(
                            color: Long,
                            card: PlayerCard
                        ): Player {
                            error("Not for preview")
                        }

                        override fun eventsObserve(): Flow<Event> = MutableSharedFlow()

                        override suspend fun getPlayers(): List<Player> = emptyList()

                        override suspend fun getBoard(): Board {
                            error("Not for preview")
                        }

                        override suspend fun sendMoney(receiverId: String, amount: Long) {
                            //NOP
                        }

                        override suspend fun rollDice() {
                            //NOP
                        }

                        override suspend fun next() {
                            //NOP
                        }

                        override suspend fun takeCard(cardType: BoardCardType) {
                            //NOP
                        }

                        override suspend fun takeSalary() {
                            //NOP
                        }

                        override suspend fun buyBusiness(business: Business) {
                            //NOP
                        }

                        override suspend fun dismissalConfirmed(business: Business) {
                            //NOP
                        }

                        override suspend fun sellingAllBusinessConfirmed(business: Business) {
                            //NOP
                        }

                        override suspend fun minusCash(price: Long) {
                            //NOP
                        }

                        override suspend fun buyThing(card: BoardCard.Shopping) {
                            //NOP
                        }

                        override suspend fun changePosition(position: Int) {
                            //NOP
                        }

                        override suspend fun buyEstate(card: Estate) {
                            //NOP
                        }

                        override suspend fun buyLand(land: Land) {
                            //NOP
                        }

                        override suspend fun randomJob(card: BoardCard.Chance.RandomJob) {
                            //NOP
                        }

                        override suspend fun buyShares(shares: Shares) {
                            //NOP
                        }

                        override suspend fun selectCardByNo(cardId: Int) {
                            //NOP
                        }

                        override suspend fun extendBusiness(
                            business: Business,
                            card: BoardCard.EventStore.BusinessExtending
                        ) {
                            //NOP
                        }

                        override suspend fun sellLands(area: Long, priceOfUnit: Long) {
                            //NOP
                        }

                        override suspend fun sellShares(
                            card: BoardCard.EventStore.Shares,
                            count: Long
                        ) {
                            //NOP
                        }

                        override suspend fun sellEstate(
                            card: List<Estate>,
                            price: Long
                        ) {
                            //NOP
                        }

                        override suspend fun passLand() {
                            //NOP
                        }

                        override suspend fun passShares(sharesType: SharesType) {
                            //NOP
                        }

                        override suspend fun passEstate() {
                            //NOP
                        }

                        override suspend fun toDeposit(amount: Long) {
                            //NOP
                        }

                        override suspend fun repayLoan(amount: Long) {
                            //NOP
                        }

                        override suspend fun advertiseAuction(auction: Auction) {
                            //NOP
                        }

                        override suspend fun sellBid(bid: Bid) {
                            //NOP
                        }

                        override suspend fun makeBid(price: Long, count: Long) {
                            //NOP
                        }
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
                        canTakeCard = null,
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