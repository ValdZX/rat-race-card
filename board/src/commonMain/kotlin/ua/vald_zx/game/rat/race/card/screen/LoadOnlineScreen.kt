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
import ua.vald_zx.game.rat.race.card.di.RaceRatConnection
import ua.vald_zx.game.rat.race.card.screen.design.DesignLoadOnline

class LoadOnlineScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val invalidServerState = remember { mutableStateOf(false) }
        val connection = koinInject<RaceRatConnection>()
        var retryKey by remember { mutableIntStateOf(0) }
        LaunchedEffect(retryKey) {
            invalidServerState.value = false
            try {
                withTimeout(20.seconds) { connection.reconnect().getBoards() }
                navigator.replace(BoardListScreen())
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
