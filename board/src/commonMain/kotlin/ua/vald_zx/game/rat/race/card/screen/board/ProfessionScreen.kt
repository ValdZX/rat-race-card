package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.screen.design.DesignProfessionContent
import ua.vald_zx.game.rat.race.card.components.DetailsField
import ua.vald_zx.game.rat.race.card.launchWithHandler
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.BoardListScreen
import ua.vald_zx.game.rat.race.card.screen.LoadOnlineScreen
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.PlayerCard
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import kotlin.uuid.Uuid

class ProfessionScreen(
    private val board: Board,
    private val card: ProfessionCard,
    private val playerName: String,
    private val color: Long,
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val service = koinInject<RaceRatService>()
        val join = {
            launchWithHandler({
                navigator.popUntil { it is BoardListScreen }
                navigator.replace(LoadOnlineScreen())
            }) {
                val helloUuid = appKStore.get()?.clientUuid.orEmpty().ifEmpty {
                    Uuid.random().toString().apply {
                        appKStore.update { it?.copy(clientUuid = this) }
                    }
                }
                val player = service.makePlayer(
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
                navigator.replace(BoardScreen(board, player))
            }
        }
        if (designV2Enabled.value) {
            DesignProfessionContent(
                card = card,
                onBack = { navigator.pop() },
                onNext = join,
            )
        } else {
            LegacyProfessionContent(
                card = card,
                onBack = { navigator.pop() },
                onNext = join,
            )
        }
    }
}
