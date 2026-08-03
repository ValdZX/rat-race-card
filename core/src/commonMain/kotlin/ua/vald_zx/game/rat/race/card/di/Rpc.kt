package ua.vald_zx.game.rat.race.card.di

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.dsl.module
import ua.vald_zx.game.rat.race.card.shared.RaceRatCardService
import ua.vald_zx.game.rat.race.card.shared.RaceRatService

private const val NORTHFLANK_API = "wss://p01--rat-race--8zcqpqq8ysrd.code.run/api"
private const val CLOUD_RUN_API = "wss://race-rat-online-1033277102369.us-central1.run.app/api"
private const val LAN_API = "ws://10.51.71.82:8080/api"

private val apiUrl = NORTHFLANK_API

val coreModule = module {
    single {
        HttpClient {
            installKrpc()
        }
    }
    single { RaceRatConnection(get()) }
    single<RaceRatService> { get<RaceRatConnection>().service() }
    single {
        get<HttpClient>().getRaceRatCardService()
    }
}

class RaceRatConnection(private val client: HttpClient) {
    private val currentService = MutableStateFlow(client.getRaceRatService())

    fun service(): RaceRatService = currentService.value

    fun reconnect(): RaceRatService {
        return client.getRaceRatService().also { currentService.value = it }
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
