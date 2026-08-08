@file:OptIn(InternalCompottieApi::class)

package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
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
import ua.vald_zx.game.rat.race.card.screen.LoadOnlineScreen
import ua.vald_zx.game.rat.race.card.screen.SelectTypeScreen
import ua.vald_zx.game.rat.race.card.screen.board.BoardScreen
import ua.vald_zx.game.rat.race.card.screen.second.PersonCard2Screen
import ua.vald_zx.game.rat.race.card.screen.second.RaceRate2Screen
import ua.vald_zx.game.rat.race.card.theme.AppTheme

@Composable
internal fun App() {
    clientVersion = ClientVersion(
        version = BuildConfig.APP_VERSION,
        commit = BuildConfig.BUILD_COMMIT,
        buildTime = BuildConfig.BUILD_TIME,
    )
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
            WaitForAppSettings {
                if (BuildConfig.CARD_ONLY_MODE) {
                    CardOnlyApp()
                } else {
                    FullApp()
                }
            }
        })
}

@Composable
private fun WaitForAppSettings(content: @Composable () -> Unit) {
    LocalinaApp {
        var settingsLoaded by remember { mutableStateOf(false) }
        var forceDark by remember { mutableStateOf<Boolean?>(null) }

        LaunchedEffect(Unit) {
            Napier.base(DebugAntilog())
            val storage = runCatching { appKStore.get() }.getOrNull()
            forceDark = storage?.theme
            designV2Enabled.value = storage?.designV2 ?: false
            vividPaletteEnabled.value = storage?.vividPalette ?: false
            soundEnabled.value = storage?.sound ?: true
            restoreAppLanguage(storage)
            settingsLoaded = true
        }

        if (settingsLoaded) {
            AppTheme(forceDark = forceDark, content = content)
        } else {
            AppWaiter()
        }
    }
}

@Composable
private fun AppWaiter() {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF101410) else Color(0xFFF7FAF2)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = if (isDark) Color(0xFFA2FF82) else Color(0xFF357A45),
            trackColor = if (isDark) Color(0xFF344033) else Color(0xFFD9E5D7),
        )
    }
}

@Composable
private fun FullApp() {
    val initialRoute = remember { readAppRoute() }
    Navigator(
        screens = initialScreens(initialRoute),
        onBackPressed = { screen ->
            if (screen is BoardScreen) {
                screen.requestExit()
                false
            } else {
                true
            }
        },
    ) { navigator ->
        LaunchedEffect(navigator.lastItem) {
            writeAppRoute((navigator.lastItem as? RoutedScreen)?.appRoute ?: AppRoute.Start)
        }
        CurrentScreen()
    }
}

private fun initialScreens(route: AppRoute): List<Screen> = when (route) {
    AppRoute.Start -> listOf(SelectTypeScreen())
    AppRoute.Card -> listOf(SelectTypeScreen(), PersonCard2Screen())
    AppRoute.Online -> listOf(SelectTypeScreen(), LoadOnlineScreen())
    is AppRoute.Board -> listOf(SelectTypeScreen(), LoadOnlineScreen(route.boardId))
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
            CurrentScreen()
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
