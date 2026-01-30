package ua.vald_zx.game.rat.race.card.di

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.RaceRatCardService
import ua.vald_zx.game.rat.race.card.shared.RaceRatService


//private val apiUrl = "wss://race-rat-online-1033277102369.us-central1.run.app/api"
private val apiUrl = "ws://192.168.0.159:8080/api"
//private val apiUrl = "ws://10.65.184.61:8080/api"
val baseModule = module {
    single {
        HttpClient {
            installKrpc()
        }
    }
    single {
        get<HttpClient>().getRaceRatService()
    }
    single {
        get<HttpClient>().getRaceRatCardService()
    }

    single {
        RatRace2CardStore(getKoin())
    }

    viewModel { parameters ->
        BoardViewModel(
            board = parameters.get<Board>(),
            player = parameters.get<Player>(),
            serviceProvider = { get() }
        )
    }
}

fun HttpClient.getRaceRatService() = this.rpc {
    url(apiUrl)
    rpcConfig { serialization { json() } }
}.withService<RaceRatService>()

fun HttpClient.getRaceRatCardService() = this.rpc {
    url(apiUrl)
    rpcConfig { serialization { json() } }
}.withService<RaceRatCardService>()