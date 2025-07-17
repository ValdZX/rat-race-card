package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.raceRate2KStore
import ua.vald_zx.game.rat.race.card.raceRate2store
import ua.vald_zx.game.rat.race.card.screen.second.PersonCard2Screen
import ua.vald_zx.game.rat.race.card.screen.second.RaceRate2Screen

class SelectTypeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
        ) {
            val coroutineScope = rememberCoroutineScope()
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button("Офлайн картка") {
                    coroutineScope.launch {
                        val ratRace2CardState = raceRate2store.observeState().value
                        runCatching { raceRate2KStore.get() }.onSuccess {
                            if (ratRace2CardState.playerCard.profession.isNotEmpty()) {
                                navigator.push(RaceRate2Screen())
                            } else {
                                navigator.push(PersonCard2Screen())
                            }
                        }.onFailure {
                            navigator.push(PersonCard2Screen())
                        }
                    }
                }
                Button("Онлайн версія (Прототип)") {
                    navigator.push(LoadOnlineScreen())
                }
            }
        }
    }
}