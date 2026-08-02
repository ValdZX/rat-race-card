package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds
import io.github.aakira.napier.Napier
import org.koin.compose.koinInject
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.di.RaceRatConnection
import ua.vald_zx.game.rat.race.card.screen.design.DesignBoardList
import ua.vald_zx.game.rat.race.card.screen.design.DesignNewBoardDialog
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.BoardScreen
import ua.vald_zx.game.rat.race.card.screen.board.InitPlayerScreen
import ua.vald_zx.game.rat.race.card.screen.board.cards.decks
import ua.vald_zx.game.rat.race.card.shared.BoardId
import ua.vald_zx.game.rat.race.card.shared.OuterCircleConditions
import ua.vald_zx.game.rat.race.card.shared.VictoryConditions

class BoardListScreen : Screen {
    @Composable
    override fun Content() {
        val connection = koinInject<RaceRatConnection>()
        val navigator = LocalNavigator.currentOrThrow
        var boardList by remember { mutableStateOf(emptyList<BoardId>()) }
        var isProgressVisible by remember { mutableStateOf(true) }
        var newBoardDialog by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            try {
                val service = connection.service()
                boardList = withTimeout(20.seconds) { service.getBoards() }
                isProgressVisible = false
                service.observeBoards().collect {
                    boardList = it
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                Napier.e("Loading boards failed", error)
                navigator.replace(LoadOnlineScreen())
            }
        }
        val openBoard: (BoardId) -> Unit = { board ->
            coroutineScope.launch {
                isProgressVisible = true
                try {
                    val helloUuid = appKStore.get()?.clientUuid.orEmpty()
                    val instance = withTimeout(20.seconds) {
                        connection.service().hello(helloUuid, board.id)
                    }
                    val instBoard = instance.board
                    val player = instance.player
                    if (player == null) {
                        navigator.push(InitPlayerScreen(instBoard))
                    } else {
                        navigator.push(BoardScreen(instBoard, player))
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Throwable) {
                    Napier.e("Opening board failed", error)
                    navigator.replace(LoadOnlineScreen())
                } finally {
                    isProgressVisible = false
                }
            }
        }
        val createBoard: (String, Long, Long, Boolean, OuterCircleConditions, VictoryConditions) -> Unit =
            { name, loanLimit, businessLimit, transportBonus, outerCircle, victory ->
                coroutineScope.launch {
                    isProgressVisible = true
                    try {
                        val board = withTimeout(20.seconds) {
                            connection.service().createBoard(
                                name = name,
                                loanLimit = loanLimit,
                                businessLimit = businessLimit,
                                decks = decks.map { (type, map) -> type to map.size }.toMap(),
                                outerCircleConditions = outerCircle,
                                victoryConditions = victory,
                                transportMovementBonusEnabled = transportBonus,
                            )
                        }
                        navigator.push(InitPlayerScreen(board))
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (error: Throwable) {
                        Napier.e("Creating board failed", error)
                        navigator.replace(LoadOnlineScreen())
                    } finally {
                        isProgressVisible = false
                    }
                }
            }
        val designV2 = designV2Enabled.value
        if (designV2) {
            DesignBoardList(
                boards = boardList,
                isLoading = isProgressVisible,
                onBack = { navigator.pop() },
                onCreate = { newBoardDialog = true },
                onOpen = openBoard,
            )
        } else {
            LegacyBoardList(
                boards = boardList,
                isLoading = isProgressVisible,
                onBack = { navigator.pop() },
                onCreate = { newBoardDialog = true },
                onOpen = openBoard,
            )
        }
        if (newBoardDialog) {
            if (designV2) {
                DesignNewBoardDialog(onDismiss = { newBoardDialog = false }, onCreate = createBoard)
            } else {
                LegacyNewBoardDialog(onDismiss = { newBoardDialog = false }, onCreate = createBoard)
            }
        }
    }
}
