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
                service = object : RaceRatService {
                    override suspend fun hello(helloUuid: String): Instance {
                        TODO("Not yet implemented")
                    }

                    override suspend fun closeSession() {
                        TODO("Not yet implemented")
                    }

                    override suspend fun getBoards(): List<BoardId> {
                        TODO("Not yet implemented")
                    }

                    override fun observeBoards(): Flow<List<BoardId>> {
                        TODO("Not yet implemented")
                    }

                    override suspend fun createBoard(
                        name: String,
                        decks: Map<BoardCardType, Int>
                    ): Board {
                        TODO("Not yet implemented")
                    }

                    override suspend fun selectBoard(boardId: String): Board {
                        TODO("Not yet implemented")
                    }

                    override suspend fun updateAttributes(attrs: PlayerAttributes) {
                        TODO("Not yet implemented")
                    }

                    override suspend fun getPlayer(): Player {
                        TODO("Not yet implemented")
                    }

                    override suspend fun makePlayer(
                        color: Long,
                        card: PlayerCard
                    ): Player {
                        TODO("Not yet implemented")
                    }

                    override fun eventsObserve(): Flow<Event> = MutableSharedFlow()

                    override suspend fun getPlayers(): List<Player> = emptyList()

                    override suspend fun getBoard(): Board {
                        TODO("Not yet implemented")
                    }

                    override suspend fun sendMoney(receiverId: String, amount: Long) {
                        TODO("Not yet implemented")
                    }

                    override suspend fun rollDice() {
                        TODO("Not yet implemented")
                    }

                    override suspend fun move() {
                        TODO("Not yet implemented")
                    }

                    override suspend fun next() {
                        TODO("Not yet implemented")
                    }

                    override suspend fun takeCard(cardType: BoardCardType) {
                        TODO("Not yet implemented")
                    }

                    override suspend fun takeSalary() {
                        TODO("Not yet implemented")
                    }

                    override suspend fun buyBusiness(business: Business) {
                        TODO("Not yet implemented")
                    }

                    override suspend fun dismissalConfirmed(business: Business) {
                        TODO("Not yet implemented")
                    }

                    override suspend fun sellingAllBusinessConfirmed(business: Business) {
                        TODO("Not yet implemented")
                    }

                    override suspend fun minusCash(price: Long) {
                        TODO("Not yet implemented")
                    }

                    override suspend fun buyThing(card: BoardCard.Shopping) {
                        TODO("Not yet implemented")
                    }

                    override suspend fun changePosition(position: Int) {
                        TODO("Not yet implemented")
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
                        createDateTime = Clock.System.now().toLocalDateTime(TimeZone.UTC),
                        lastCheckTime = Clock.System.now().toLocalDateTime(TimeZone.UTC),
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