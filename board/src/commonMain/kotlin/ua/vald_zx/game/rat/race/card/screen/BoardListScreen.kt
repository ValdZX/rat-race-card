package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.getKoin
import org.koin.compose.koinInject
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.screen.design.DesignBoardList
import ua.vald_zx.game.rat.race.card.screen.design.DesignNewBoardDialog
import ua.vald_zx.game.rat.race.card.launchWithHandler
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.board.BoardScreen
import ua.vald_zx.game.rat.race.card.screen.board.InitPlayerScreen
import ua.vald_zx.game.rat.race.card.screen.board.cards.decks
import ua.vald_zx.game.rat.race.card.shared.BoardId
import ua.vald_zx.game.rat.race.card.shared.OuterCircleConditions
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import ua.vald_zx.game.rat.race.card.shared.VictoryConditions

class BoardListScreen : Screen {
    @Composable
    override fun Content() {
        val service = koinInject<RaceRatService>()
        val navigator = LocalNavigator.currentOrThrow
        var boardList by remember { mutableStateOf(emptyList<BoardId>()) }
        var isProgressVisible by remember { mutableStateOf(true) }
        var newBoardDialog by remember { mutableStateOf(false) }
        val koin = getKoin()
        LaunchedEffect(Unit) {
            boardList = service.getBoards()
            isProgressVisible = false
            service.observeBoards().onEach {
                boardList = it
            }.launchIn(this)
        }
        val openBoard: (BoardId) -> Unit = { board ->
            launchWithHandler({ navigator.push(LoadOnlineScreen()) }) {
                val service = koin.get<RaceRatService>()
                val helloUuid = appKStore.get()?.clientUuid.orEmpty()
                val instance = service.hello(helloUuid, board.id)
                val instBoard = instance.board
                val player = instance.player
                if (player == null) {
                    navigator.push(InitPlayerScreen(instBoard))
                } else {
                    navigator.push(BoardScreen(instBoard, player))
                }
            }
        }
        val createBoard: (String, Long, Long, Boolean, OuterCircleConditions, VictoryConditions) -> Unit =
            { name, loanLimit, businessLimit, transportBonus, outerCircle, victory ->
                launchWithHandler({ navigator.push(LoadOnlineScreen()) }) {
                    val board = service.createBoard(
                        name = name,
                        loanLimit = loanLimit,
                        businessLimit = businessLimit,
                        decks = decks.map { (type, map) -> type to map.size }.toMap(),
                        outerCircleConditions = outerCircle,
                        victoryConditions = victory,
                        transportMovementBonusEnabled = transportBonus,
                    )
                    navigator.push(InitPlayerScreen(board))
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
