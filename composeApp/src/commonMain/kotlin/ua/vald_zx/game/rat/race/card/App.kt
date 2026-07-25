@file:OptIn(InternalCompottieApi::class)

package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.github.alexzhirkevich.compottie.*
import io.github.sudarshanmhasrup.localina.api.LocalinaApp
import kotlinx.coroutines.withContext
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.dsl.koinConfiguration
import ua.vald_zx.game.rat.race.card.di.boardModule
import ua.vald_zx.game.rat.race.card.di.cardModule
import ua.vald_zx.game.rat.race.card.di.coreModule
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.screen.SelectTypeScreen
import ua.vald_zx.game.rat.race.card.screen.second.PersonCard2Screen
import ua.vald_zx.game.rat.race.card.screen.second.RaceRate2Screen
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark

@Composable
internal fun App() {
    KoinApplication(
        configuration = koinConfiguration(declaration = { modules(coreModule, cardModule, boardModule) }),
        content = {
            val lottieCache = LocalLottieCache.current
            LaunchedEffect(Unit) {
                lottieDiceAnimations = (1..6).associate { side ->
                    withContext(Compottie.ioDispatcher()) {
                        val specInstance = LottieCompositionSpec.JsonString(
                            Res.readBytes("files/cube_$side.json").decodeToString()
                        )
                        side to lottieCache.getOrPut(specInstance.key, specInstance::load)
                    }
                }
            }
            if (BuildConfig.CARD_ONLY_MODE) {
                CardOnlyApp()
            } else {
                FullApp()
            }
        })
}

@Composable
private fun FullApp() {
    Navigator(SelectTypeScreen()) {
        LocalinaApp {
            AppTheme {
                var isDarkTheme by LocalThemeIsDark.current
                LaunchedEffect(Unit) {
                    Napier.base(DebugAntilog())
                    val storage = appKStore.get()
                    val systemIsDark = storage?.theme ?: isDarkTheme
                    isDarkTheme = systemIsDark
                }
                CurrentScreen()
            }
        }
    }
}

@Composable
private fun CardOnlyApp() {
    val raceRate2store = koinInject<RatRace2CardStore>()
    var kStoreLoaded by remember { mutableStateOf(false) }
    if (kStoreLoaded) {
        val raceRate2State by raceRate2store.observeState().collectAsState()
        val hasProfession = raceRate2State.playerCard.profession.isNotEmpty()
        val startScreen = if (hasProfession) RaceRate2Screen() else PersonCard2Screen()
        Navigator(startScreen) {
            LocalinaApp {
                AppTheme {
                    var isDarkTheme by LocalThemeIsDark.current
                    LaunchedEffect(Unit) {
                        Napier.base(DebugAntilog())
                        val storage = appKStore.get()
                        isDarkTheme = storage?.theme ?: isDarkTheme
                    }
                    CurrentScreen()
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
}
