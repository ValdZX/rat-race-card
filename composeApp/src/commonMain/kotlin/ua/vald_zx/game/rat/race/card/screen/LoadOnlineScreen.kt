package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.screen.board.BoardScreen
import ua.vald_zx.game.rat.race.card.shared.RaceRatService

class LoadOnlineScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val invalidServerState = mutableStateOf(false)
        val service = koinInject<RaceRatService>()
        val coroutineScope = rememberCoroutineScope()
        fun connectToService() {
            val handler = CoroutineExceptionHandler { _, t ->
                Napier.e("Invalid server", t)
                invalidServerState.value = true
            }
            coroutineScope.launch(handler) {
                val instance = service.hello(currentPlayerId)
                currentPlayerId = instance.playerId
                val board = instance.board
                val player = instance.player
                if (board == null || player == null) {
                    navigator.push(BoardListScreen())
                } else {
                    navigator.replace(BoardScreen(board, player))
                }
            }
        }
        DisposableEffect(Unit) {
            connectToService()
            onDispose {
                coroutineScope.launch { service.closeSession() }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
        ) {
            val invalidServer by invalidServerState
            if (invalidServer) {
                Column(modifier = Modifier.align(Alignment.Center)) {
                    Text("Невдалося підключитися до сервера")
                    Button("Спробувати підключития ще раз") {
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