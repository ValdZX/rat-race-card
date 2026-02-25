package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.aakira.napier.Napier
import io.ktor.client.*
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.getKoin
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.connection_failed
import rat_race_card.composeapp.generated.resources.retry_connection
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.di.getRaceRatService

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
        ) {
            if (invalidServerState.value) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text(stringResource(Res.string.connection_failed))
                    Button(stringResource(Res.string.retry_connection)) {
                        connectToService()
                        invalidServerState.value = false
                    }
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}