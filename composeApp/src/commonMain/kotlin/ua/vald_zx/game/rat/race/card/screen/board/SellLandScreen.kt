package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.area
import rat_race_card.composeapp.generated.resources.sell
import rat_race_card.composeapp.generated.resources.total
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel

class SellLandScreen(private val vm: BoardViewModel, private val price: Long) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        BottomSheetContainer {
            val totalArea = state.player.landList.sumOf { it.area }
            val inputArea = remember {
                mutableStateOf(
                    TextFieldValue(
                        totalArea.toString()
                    )
                )
            }
            val area = inputArea.value.text
            Text(
                stringResource(Res.string.total) + ": ${(area.toLongOrNull() ?: 0) * price}",
                style = MaterialTheme.typography.titleSmall
            )
            NumberTextField(
                input = inputArea,
                inputLabel = stringResource(Res.string.area),
            )
            ElevatedButton(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp),
                onClick = {
                    vm.sellLands(inputArea.value.text.toLong(), price)
                    bottomSheetNavigator.hide()
                },
                enabled = area.isNotEmpty()
                        && area.toLong() <= totalArea,
                content = {
                    Text(stringResource(Res.string.sell))
                }
            )
        }
    }
}