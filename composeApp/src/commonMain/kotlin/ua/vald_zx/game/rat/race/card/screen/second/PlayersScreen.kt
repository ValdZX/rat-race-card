package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import io.github.xxfast.kstore.utils.ExperimentalKStoreApi
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.CashFlowField
import ua.vald_zx.game.rat.race.card.components.GoldRainbow
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.raceRate2store
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Rat
import ua.vald_zx.game.rat.race.card.resource.images.Send
import ua.vald_zx.game.rat.race.card.splitDecimal

class PlayersScreen : Screen {
    @OptIn(ExperimentalKStoreApi::class)
    @Composable
    override fun Content() {
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val state by raceRate2store.observeState().collectAsState()
        val players by players
        val items = remember(players) {
            players.toList().filter { player -> player.uuid != state.uuid }
        }
        BottomSheetContainer {
            items.forEach { player ->
                Card {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Images.Rat, contentDescription = null)
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            Text(player.professionCard.profession)
                            CashFlowField(
                                name = "Статки",
                                rainbow = GoldRainbow,
                                value = player.state.totalExpenses.splitDecimal(),
                                fontSize = 12.sp
                            )
                            CashFlowField(
                                name = "Cash Flow",
                                value = player.state.cashFlow.splitDecimal(),
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                bottomSheetNavigator.replace(SendScreen(player))
                            },
                            content = {
                                Icon(Images.Send, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    }
}