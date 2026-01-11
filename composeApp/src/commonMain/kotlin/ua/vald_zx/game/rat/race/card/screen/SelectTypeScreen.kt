package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.offline_mode
import rat_race_card.composeapp.generated.resources.online_mode
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.raceRate2KStore
import ua.vald_zx.game.rat.race.card.screen.second.PersonCard2Screen
import ua.vald_zx.game.rat.race.card.screen.second.RaceRate2Screen

class SelectTypeScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val raceRate2store = koinInject<RatRace2CardStore>()
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
                var kStoreLoaded by remember { mutableStateOf(false) }
                if (kStoreLoaded) {
                    Button(stringResource(Res.string.offline_mode)) {
                        coroutineScope.launch {
                            val ratRace2CardState = raceRate2store.observeState().value
                            runCatching { appKStore.get() }.onSuccess {
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
                } else {
                    LaunchedEffect(Unit) {
                        val state2 = runCatching { raceRate2KStore.get() }.getOrNull()
                        if (state2 != null) {
                            raceRate2store.dispatch(RatRace2CardAction.LoadState(state2))
                        }
                        kStoreLoaded = true
                    }
                }
                Button(stringResource(Res.string.online_mode)) {
                    navigator.push(LoadOnlineScreen())
                }
            }
        }
    }
}