package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.CashFlowField
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Rat
import ua.vald_zx.game.rat.race.card.resource.images.Send
import ua.vald_zx.game.rat.race.card.screen.board.SendScreen
import ua.vald_zx.game.rat.race.card.shared.OfflinePlayer
import ua.vald_zx.game.rat.race.card.splitDecimal

val offlinePlayers = MutableStateFlow(emptyList<OfflinePlayer>())

class PlayersScreen : Screen {
    @Composable
    override fun Content() {

        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            val raceRate2store = koinInject<RatRace2CardStore>()
            val state by raceRate2store.observeState().collectAsState()
            if (state.connected) {
                val players by offlinePlayers.collectAsState()
                players.filter { it.id != currentPlayerId }.forEach { player ->
                    Card {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Images.Rat, contentDescription = null)
                            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                                Text(player.name)
                                CashFlowField(
                                    name = "Статки",
                                    rainbow = GoldRainbow,
                                    value = player.total.splitDecimal(),
                                    fontSize = 12.sp
                                )
                                CashFlowField(
                                    name = "Cash Flow",
                                    value = player.cashFlow.splitDecimal(),
                                    fontSize = 12.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    bottomSheetNavigator.replace(SendScreen(player.id, player.name) { id, money ->
                                        //
                                    })
                                },
                                content = {
                                    Icon(Images.Send, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            } else {
                var room by mutableStateOf("")
                OutlinedTextField(
                    value = room,
                    onValueChange = {
                        room = it
                    },
                    label = { Text("IP") }
                )
                Button(
                    text = "Connect",
                    onClick = {
                        raceRate2store.dispatch(RatRace2CardAction.Connect(room))
                    })
            }

        }
    }
}