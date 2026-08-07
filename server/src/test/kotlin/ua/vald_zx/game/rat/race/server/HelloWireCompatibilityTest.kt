@file:OptIn(ExperimentalRpcApi::class, InternalRpcApi::class)

package ua.vald_zx.game.rat.race.server

import kotlinx.coroutines.test.runTest
import kotlinx.rpc.descriptor.RpcCallable
import kotlinx.rpc.descriptor.RpcInvokator
import kotlinx.rpc.descriptor.serviceDescriptorOf
import kotlinx.rpc.internal.utils.ExperimentalRpcApi
import kotlinx.rpc.internal.utils.InternalRpcApi
import kotlinx.rpc.krpc.internal.CallableParametersSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import ua.vald_zx.game.rat.race.card.shared.OfflinePlayer
import ua.vald_zx.game.rat.race.card.shared.RaceRatCardService
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import ua.vald_zx.game.rat.race.card.shared.SendMoneyPack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HelloWireCompatibilityTest {

    private val stalePlayerJson = Json.encodeToString(
        OfflinePlayer.serializer(),
        OfflinePlayer(id = "p-1", name = "n", cashFlow = 0, total = 0, room = "r-1"),
    )

    @Test
    fun boardHelloAcceptsAPayloadWithoutClientVersion() {
        val callable = serviceDescriptorOf<RaceRatService>().getCallable("hello")!!

        val args = decodeArgs(callable, """{"helloUuid":"u-1","boardId":"b-1"}""")

        assertEquals(3, args.size)
        assertEquals("u-1", args[0])
        assertEquals("b-1", args[1])
        assertNull(args[2])
    }

    @Test
    fun cardHelloAcceptsAPayloadWithoutClientVersion() {
        val callable = serviceDescriptorOf<RaceRatCardService>().getCallable("hello")!!

        val args = decodeArgs(callable, """{"player":$stalePlayerJson}""")

        assertEquals(2, args.size)
        assertNull(args[1])
    }

    @Test
    fun aStaleClientCallReachesTheService() = runTest {
        val callable = serviceDescriptorOf<RaceRatCardService>().getCallable("hello")!!
        val args = decodeArgs(callable, """{"player":$stalePlayerJson}""")
        var reportedVersion: String? = "not-called"
        val service = object : RaceRatCardService by NotImplementedCardService {
            override suspend fun hello(player: OfflinePlayer, clientVersion: String?): String {
                reportedVersion = clientVersion
                return player.id
            }
        }

        @Suppress("UNCHECKED_CAST")
        val invokator = callable.invokator as RpcInvokator.Method<RaceRatCardService>
        val result = invokator.call(service, args)

        assertEquals("p-1", result)
        assertNull(reportedVersion)
    }

    private fun decodeArgs(callable: RpcCallable<*>, payload: String): Array<Any?> =
        Json.decodeFromString(CallableParametersSerializer(callable, EmptySerializersModule()), payload)
}

private object NotImplementedCardService : RaceRatCardService {
    override suspend fun hello(player: OfflinePlayer, clientVersion: String?): String = error("stub")
    override suspend fun getPlayers(): List<OfflinePlayer> = error("stub")
    override fun playersObserve() = error("stub")
    override suspend fun updatePlayer(player: OfflinePlayer) = error("stub")
    override fun sendMoneyObserve() = error("stub")
    override suspend fun sendMoney(pack: SendMoneyPack) = error("stub")
}
