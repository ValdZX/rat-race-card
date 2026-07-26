package ua.vald_zx.game.rat.race.card.di

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import org.koin.dsl.module
import ua.vald_zx.game.rat.race.card.shared.RaceRatCardService
import ua.vald_zx.game.rat.race.card.shared.RaceRatService


//private val apiUrl = "wss://race-rat-online-1033277102369.us-central1.run.app/api"
private val apiUrl = "wss://p01--rat-race--8zcqpqq8ysrd.code.run/api"
//private val apiUrl = "ws://192.168.0.159:8080/api"
//private val apiUrl = "ws://10.194.162.82:8080/api"

val coreModule = module {
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
}

fun HttpClient.getRaceRatService() = this.rpc {
    url(apiUrl)
    rpcConfig { serialization { json() } }
}.withService<RaceRatService>()

fun HttpClient.getRaceRatCardService() = this.rpc {
    url(apiUrl)
    rpcConfig { serialization { json() } }
}.withService<RaceRatCardService>()
