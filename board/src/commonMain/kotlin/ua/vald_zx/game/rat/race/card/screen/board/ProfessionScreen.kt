package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.AppDataStorageBean
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.clientVersion
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.design.DesignMessageDialog
import ua.vald_zx.game.rat.race.card.di.RaceRatConnection
import ua.vald_zx.game.rat.race.card.screen.design.DesignProfessionContent
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.PlayerCard
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import org.koin.compose.koinInject
import kotlin.uuid.Uuid
import kotlin.time.Duration.Companion.seconds

class ProfessionScreen(
    private val board: Board,
    private val card: ProfessionCard,
    private val playerName: String,
    private val color: Long,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val connection = koinInject<RaceRatConnection>()
        val coroutineScope = rememberCoroutineScope()
        var isJoining by remember { mutableStateOf(false) }
        var joinFailed by remember { mutableStateOf(false) }
        val join = {
            if (!isJoining) {
                val shouldReconnect = joinFailed
                isJoining = true
                joinFailed = false
                coroutineScope.launch {
                    try {
                        val helloUuid = appKStore.get()?.clientUuid.orEmpty().ifEmpty {
                            Uuid.random().toString().apply {
                                appKStore.update { stored ->
                                    (stored ?: AppDataStorageBean("", null)).copy(clientUuid = this)
                                }
                            }
                        }
                        val player = withTimeout(20.seconds) {
                            val service = if (shouldReconnect) connection.reconnect() else connection.service()
                            val instance = service.hello(helloUuid, board.id, clientVersion.label)
                            instance.player ?: service.makePlayer(
                                uuid = helloUuid,
                                color = color,
                                card = PlayerCard(
                                    name = playerName,
                                    gender = card.gender,
                                    profession = card.name,
                                    salary = card.salary,
                                    rent = card.rent,
                                    food = card.food,
                                    cloth = card.cloth,
                                    transport = card.transport,
                                    phone = card.phone,
                                ),
                            )
                        }
                        navigator.replace(BoardScreen(board, player))
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (error: Throwable) {
                        Napier.e("Creating player failed", error)
                        joinFailed = true
                    } finally {
                        isJoining = false
                    }
                }
            }
        }
        if (designV2Enabled.value) {
            DesignProfessionContent(
                card = card,
                isLoading = isJoining,
                onBack = { navigator.pop() },
                onNext = join,
            )
        } else {
            LegacyProfessionContent(
                card = card,
                isLoading = isJoining,
                onBack = { navigator.pop() },
                onNext = join,
            )
        }
        if (joinFailed) {
            DesignMessageDialog(
                onDismissRequest = { joinFailed = false },
                title = stringResource(Res.string.connection_failed),
                message = stringResource(Res.string.server_request_failed),
                confirmLabel = stringResource(Res.string.retry_connection),
                onConfirm = join,
                dismissLabel = stringResource(Res.string.cancel),
                onDismissAction = { joinFailed = false },
            )
        }
    }
}
