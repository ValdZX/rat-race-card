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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.empty_room
import rat_race_card.composeapp.generated.resources.room
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.CashFlowField
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.currentPlayerId
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
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
            val store = koinInject<RatRace2CardStore>()
            val state by store.observeState().collectAsState()
            if (state.room.isNotEmpty()) {
                IconButton(
                    modifier = Modifier.align(Alignment.Start),
                    onClick = { store.dispatch(RatRace2CardAction.Disconnect) },
                    content = {
                        Icon(Images.Back, contentDescription = null)
                    }
                )
                val players by offlinePlayers.collectAsState()
                if (players.size < 2) {
                    Text(text = stringResource(Res.string.empty_room))
                }
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
                                    lastCashFlows = player.lastTotals,
                                    rainbow = GoldRainbow,
                                    value = player.total.splitDecimal(),
                                    fontSize = 12.sp
                                )
                                CashFlowField(
                                    name = "Cash Flow",
                                    lastCashFlows = player.lastCashFlows,
                                    value = player.cashFlow.splitDecimal(),
                                    fontSize = 12.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    bottomSheetNavigator.replace(SendScreen(player.id, player.name) { id, money ->
                                        store.dispatch(RatRace2CardAction.SendMoney(id, money))
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
                var room by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = room,
                    onValueChange = {
                        room = it
                    },
                    label = { Text(stringResource(Res.string.room)) }
                )
                Button(
                    text = "Connect",
                    onClick = {
                        store.dispatch(RatRace2CardAction.Connect(room))
                    })
            }

        }
    }
}