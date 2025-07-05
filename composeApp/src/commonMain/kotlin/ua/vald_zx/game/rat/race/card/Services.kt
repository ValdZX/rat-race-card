package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import cafe.adriel.voyager.navigator.Navigator
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.url
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import ua.vald_zx.game.rat.race.card.logic.BoardAction.DiceRolled
import ua.vald_zx.game.rat.race.card.logic.BoardAction.UpdateBoard
import ua.vald_zx.game.rat.race.card.logic.BoardAction.UpdateCurrentPlayer
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction.ReceivedCash
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.logic.total
import ua.vald_zx.game.rat.race.card.screen.BoardListScreen
import ua.vald_zx.game.rat.race.card.screen.board.Board2Screen
import ua.vald_zx.game.rat.race.card.screen.board.InitPlayerScreen
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Event
import ua.vald_zx.game.rat.race.card.shared.PlayerState
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import ua.vald_zx.game.rat.race.card.shared.replaceItem


val currentPlayerId: String
    get() = settings["currentPlayerId", ""]
val client by lazy {
    HttpClient {
        installKrpc()
        install(HttpRequestRetry) {
            retryOnExceptionIf(maxRetries = Int.MAX_VALUE) { request, cause ->
                players.value = emptyList()
                true
            }
        }
    }
}


@Composable
fun StartServices(navigator: Navigator) {
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(Unit) {
        coroutineScope.launch(CoroutineExceptionHandler { _, t ->
            Napier.e("Invalid server", t)
            invalidServer.value = true
        }) {
            service = client.rpc {
                url("wss://race-rat-online-1033277102369.us-central1.run.app/api")
                rpcConfig {
                    serialization {
                        json()
                    }
                }
            }.withService()
            val instance = service?.hello(currentPlayerId) ?: return@launch
            settings["currentPlayerId"] = instance.id
            if (instance.boardId.isEmpty()) {
                navigator.push(BoardListScreen())
            } else {
                service?.selectBoard(instance.boardId)?.let { board ->
                    loadBoard(board, navigator)
                }
            }
            service?.eventsObserve()?.onEach { event ->
                when (event) {
                    is Event.MoneyIncome -> {
                        raceRate2store.dispatch(
                            ReceivedCash(
                                payerId = event.playerId,
                                amount = event.amount
                            )
                        )
                    }

                    is Event.PlayerChanged -> {
                        val playersList = players.value
                        val changedPlayer = event.player
                        val oldPlayer = playersList.find { it.id == changedPlayer.id }
                        if (oldPlayer != null) {
                            players.value = playersList.replaceItem(oldPlayer, event.player)
                        } else {
                            players.value = players.value + event.player
                        }
                        if (changedPlayer.id == currentPlayerId) {
                            raceRate2BoardStore.dispatch(UpdateCurrentPlayer(changedPlayer))
                        }
                    }

                    is Event.RollDice -> {
                        raceRate2BoardStore.dispatch(DiceRolled(event.dice))
                    }

                    is Event.BoardChanged -> {
                        raceRate2BoardStore.dispatch(UpdateBoard(event.board))
                        service?.updatePlayers(event.board.players)
                    }
                }
            }?.launchIn(this)
            val state = raceRate2KStore.get()
            val professionCard = state?.playerCard
            if (professionCard?.profession?.isNotEmpty() == true) {
                service?.updatePlayerCard(professionCard)
                service?.updateState(state.toState())
            }
        }
        onDispose {
            coroutineScope.launch {
                service?.closeSession()
                client.close()
            }
        }
    }
}

suspend fun loadBoard(board: Board, navigator: Navigator) {
    raceRate2BoardStore.dispatch(UpdateBoard(board))
    val player = service?.getPlayer(currentPlayerId)
    if (player == null || player.playerCard.name.isEmpty()) {
        val players = service?.makePlayerOnBoard().orEmpty()
        service?.updatePlayers(players)
        navigator.replace(InitPlayerScreen())
    } else {
        service?.updatePlayers(board.players)
        navigator.replace(Board2Screen())
    }
}


var service: RaceRatService? = null

fun RatRace2CardState.toState(): PlayerState {
    return PlayerState(
        totalExpenses = total(),
        cashFlow = cashFlow()
    )
}

suspend fun RaceRatService.updatePlayers(actualIds: Set<String>) {
    val oldList = players.value
    players.value = actualIds.mapNotNull { id ->
        oldList.find { id == it.id } ?: getPlayer(id)?.apply {
            if (this.id == currentPlayerId) {
                raceRate2BoardStore.dispatch(UpdateCurrentPlayer(this))
            }
        }
    }
}