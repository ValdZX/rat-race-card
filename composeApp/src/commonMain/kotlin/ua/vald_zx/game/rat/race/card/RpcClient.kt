package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.navigator.Navigator
import com.russhwolf.settings.get
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.rpc.krpc.ktor.client.installKrpc
import ua.vald_zx.game.rat.race.card.logic.BoardAction
import ua.vald_zx.game.rat.race.card.logic.BoardSideEffect
import ua.vald_zx.game.rat.race.card.screen.BoardListScreen
import ua.vald_zx.game.rat.race.card.screen.board.Board2Screen
import ua.vald_zx.game.rat.race.card.screen.board.InitPlayerScreen
import ua.vald_zx.game.rat.race.card.shared.RaceRatService


val currentPlayerId: String
    get() = settings["currentPlayerId", ""]
val client by lazy {
    HttpClient {
        installKrpc()
    }
}

var service: RaceRatService? = null

@Composable
fun StartServices(navigator: Navigator) {
    DisposableEffect(Unit) {
        raceRate2BoardStore.dispatch(BoardAction.StartServices)
        onDispose {
            raceRate2BoardStore.dispatch(BoardAction.CloseSession)
        }
    }
    LaunchedEffect(Unit){
        raceRate2BoardStore.observeSideEffect().onEach { effect ->
            when (effect) {
                is BoardSideEffect.GotoBoardList -> {
                    navigator.push(BoardListScreen())
                }
                is BoardSideEffect.GotoBoard -> {
                    navigator.replace(Board2Screen())
                }
                is BoardSideEffect.GotoInitPlayer -> {
                    navigator.replace(InitPlayerScreen())
                }

                else -> {
                    //nop
                }
            }
        }.launchIn(this)
    }
}