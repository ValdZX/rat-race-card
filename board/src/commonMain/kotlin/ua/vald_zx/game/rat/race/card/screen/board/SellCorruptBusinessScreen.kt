package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.ClosableBottomSheetContainer
import ua.vald_zx.game.rat.race.card.formatAmount
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.corrupt_business_sale
import ua.vald_zx.game.rat.race.card.resources.market_pays_percentage
import ua.vald_zx.game.rat.race.card.resources.profit
import ua.vald_zx.game.rat.race.card.resources.sale_amount
import ua.vald_zx.game.rat.race.card.resources.sell
import ua.vald_zx.game.rat.race.card.shared.BusinessType

class SellCorruptBusinessScreen(
    private val vm: BoardViewModel,
    private val salePercentage: Long,
) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        ClosableBottomSheetContainer {
            Text(stringResource(Res.string.corrupt_business_sale))
            Text(stringResource(Res.string.market_pays_percentage, salePercentage))
            state.player.businesses.filter { it.type == BusinessType.CORRUPTION }.forEach { business ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(business.name)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("${stringResource(Res.string.profit)}: ${business.profit.formatAmount()}")
                            Text(
                                "${stringResource(Res.string.sale_amount)}: " +
                                        (business.price * salePercentage / 100).formatAmount()
                            )
                        }
                        Button(text = stringResource(Res.string.sell)) {
                            vm.sellCorruptBusiness(business, salePercentage)
                            bottomSheetNavigator.hide()
                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}
