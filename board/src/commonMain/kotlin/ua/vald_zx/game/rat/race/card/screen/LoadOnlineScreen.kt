package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds
import org.koin.compose.koinInject
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.AppRoute
import ua.vald_zx.game.rat.race.card.RoutedScreen
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.di.RaceRatConnection
import ua.vald_zx.game.rat.race.card.screen.board.BoardScreen
import ua.vald_zx.game.rat.race.card.screen.board.InitPlayerScreen
import ua.vald_zx.game.rat.race.card.screen.design.DesignLoadOnline

class LoadOnlineScreen(private val boardId: String? = null) : Screen, RoutedScreen {

    override val appRoute: AppRoute get() = boardId?.let(AppRoute::Board) ?: AppRoute.Online

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val invalidServerState = remember { mutableStateOf(false) }
        val connection = koinInject<RaceRatConnection>()
        var retryKey by remember { mutableIntStateOf(0) }
        LaunchedEffect(retryKey) {
            invalidServerState.value = false
            try {
                val service = withTimeout(20.seconds) { connection.reconnect().also { it.getBoards() } }
                if (boardId == null) {
                    navigator.replace(BoardListScreen())
                } else {
                    val helloUuid = appKStore.get()?.clientUuid.orEmpty()
                    val instance = withTimeout(20.seconds) { service.hello(helloUuid, boardId) }
                    val player = instance.player
                    navigator.replace(BoardListScreen())
                    if (player == null) {
                        navigator.push(InitPlayerScreen(instance.board))
                    } else {
                        navigator.push(BoardScreen(instance.board, player))
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                Napier.e("Server connection failed", error)
                invalidServerState.value = true
            }
        }
        val retry = {
            invalidServerState.value = false
            retryKey += 1
        }
        if (designV2Enabled.value) {
            DesignLoadOnline(
                failed = invalidServerState.value,
                onBack = { navigator.pop() },
                onRetry = retry,
            )
        } else {
            LegacyLoadOnline(
                failed = invalidServerState.value,
                onBack = { navigator.pop() },
                onRetry = retry,
            )
        }
    }
}
