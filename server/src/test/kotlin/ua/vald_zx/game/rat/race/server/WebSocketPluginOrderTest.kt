package ua.vald_zx.game.rat.race.server

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.websocket.WebSockets
import kotlinx.rpc.krpc.ktor.server.Krpc
import kotlinx.rpc.krpc.ktor.server.rpc
import kotlinx.rpc.krpc.serialization.json.json
import kotlin.test.Test
import kotlin.test.assertEquals

class WebSocketPluginOrderTest {

    @Test
    fun krpcReusesAnExplicitlyConfiguredWebSocketsPlugin() = testApplication {
        application {
            install(WebSockets) {
                pingPeriodMillis = 15_000
                timeoutMillis = 15_000
            }
            install(Krpc)
            routing {
                get("/") { call.respondText("Race rat RPC services") }
                rpc("/api") {
                    rpcConfig {
                        serialization {
                            json()
                        }
                    }
                }
            }
        }

        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Race rat RPC services", response.bodyAsText())
    }
}
