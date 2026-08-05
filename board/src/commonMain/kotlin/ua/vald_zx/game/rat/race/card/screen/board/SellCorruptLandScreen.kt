package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.ClosableBottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.design.proportionalAmountOptions
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.all_in
import ua.vald_zx.game.rat.race.card.resources.area
import ua.vald_zx.game.rat.race.card.resources.corrupt_land_sale
import ua.vald_zx.game.rat.race.card.resources.sale_amount
import ua.vald_zx.game.rat.race.card.resources.sell
import ua.vald_zx.game.rat.race.card.shared.isCorruptLand

class SellCorruptLandScreen(
    private val vm: BoardViewModel,
    private val price: Long,
) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val totalArea = state.player.landList.filter(state.board::isCorruptLand).sumOf { it.area }
        val inputArea = remember(totalArea) { mutableStateOf(TextFieldValue(totalArea.toString())) }
        val area = inputArea.value.text.toLongOrNull() ?: 0
        ClosableBottomSheetContainer {
            Text(stringResource(Res.string.corrupt_land_sale))
            Text(
                "${stringResource(Res.string.sale_amount)}: ${(area * price).formatAmount()}",
                style = MaterialTheme.typography.titleSmall,
            )
            NumberTextField(
                input = inputArea,
                inputLabel = stringResource(Res.string.area),
                quickOptions = proportionalAmountOptions(totalArea, stringResource(Res.string.all_in)),
            )
            ElevatedButton(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).widthIn(min = 200.dp),
                onClick = {
                    vm.sellCorruptLands(area, price)
                    bottomSheetNavigator.hide()
                },
                enabled = area in 1..totalArea,
            ) {
                Text(stringResource(Res.string.sell))
            }
        }
    }
}
