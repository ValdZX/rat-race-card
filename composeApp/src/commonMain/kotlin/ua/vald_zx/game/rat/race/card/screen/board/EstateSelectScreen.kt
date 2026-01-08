package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.sell
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.screen.board.page.EstateItem

class EstateSelectScreen(private val vm: BoardViewModel, private val price: Long) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val checkedEstates = remember { state.player.estateList.toMutableStateList() }
        BottomSheetContainer {
            state.player.estateList.forEach { estate ->
                Row {
                    Box(Modifier.weight(1f)) {
                        EstateItem(estate)
                    }
                    Checkbox(
                        checked = checkedEstates.contains(estate),
                        onCheckedChange = { checked ->
                            if (checked) {
                                checkedEstates.add(estate)
                            } else {
                                checkedEstates.remove(estate)
                            }
                        })
                }
            }
            Text("Прибуток: ${price * checkedEstates.size}")
            Button(text = stringResource(Res.string.sell), enabled = checkedEstates.isNotEmpty()) {
                vm.sellEstates(checkedEstates, price)
                bottomSheetNavigator.hide()
            }
        }
    }
}