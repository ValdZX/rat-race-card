package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.aakira.napier.Napier
import io.ktor.client.*
import kotlinx.coroutines.*
import org.koin.compose.getKoin
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.di.getRaceRatService
import ua.vald_zx.game.rat.race.card.screen.design.DesignLoadOnline

class LoadOnlineScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val invalidServerState = remember { mutableStateOf(false) }
        val koin = getKoin()
        fun connectToService() {
            val handler = CoroutineExceptionHandler { _, t ->
                Napier.e("Invalid server", t)
                val service = koin.get<HttpClient>().getRaceRatService()
                koin.declare(service, allowOverride = true)
                invalidServerState.value = true
            }
            CoroutineScope(Dispatchers.Main + SupervisorJob()).launch(handler) {
                navigator.push(BoardListScreen())
            }
        }
        LaunchedEffect(Unit) {
            connectToService()
        }
        val retry = {
            connectToService()
            invalidServerState.value = false
        }
        if (designV2Enabled.value) {
            DesignLoadOnline(failed = invalidServerState.value, onRetry = retry)
        } else {
            LegacyLoadOnline(failed = invalidServerState.value, onRetry = retry)
        }
    }
}
