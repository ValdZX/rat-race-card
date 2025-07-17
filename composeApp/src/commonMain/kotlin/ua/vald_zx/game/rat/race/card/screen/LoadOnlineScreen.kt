package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.invalidServerState
import ua.vald_zx.game.rat.race.card.needStartServerState

class LoadOnlineScreen : Screen {
    @Composable
    override fun Content() {
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
                        invalidServerState.value = false
                        needStartServerState.value = true
                    }
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        LaunchedEffect(Unit) {
            needStartServerState.value = true
        }
    }
}