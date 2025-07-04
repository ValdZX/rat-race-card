package ua.vald_zx.game.rat.race.card

import com.russhwolf.settings.get
import com.russhwolf.settings.set
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.url
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.logic.total
import ua.vald_zx.game.rat.race.card.shared.PlayerState
import ua.vald_zx.game.rat.race.card.shared.RaceRatService


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


var service: RaceRatService? = null
suspend fun startService() {
    service = client.rpc {
        url("wss://race-rat-online-1033277102369.us-central1.run.app/api")
        rpcConfig {
            serialization {
                json()
            }
        }
    }.withService()
    settings["currentPlayerId"] = service?.hello(currentPlayerId)
    val state = raceRate2KStore.get()
    val professionCard = state?.playerCard
    if (professionCard?.profession?.isNotEmpty() == true) {
        service?.updatePlayerCard(professionCard)
        service?.updateState(state.toState())
    }
}

fun RatRace2CardState.toState(): PlayerState {
    return PlayerState(
        totalExpenses = total(),
        cashFlow = cashFlow()
    )
}

suspend fun RaceRatService.updatePlayers(actualIds: Set<String>) {
    val oldList = players.value
    players.value = actualIds.mapNotNull { id ->
        oldList.find { id == it.id } ?: getPlayer(id)
    }
}