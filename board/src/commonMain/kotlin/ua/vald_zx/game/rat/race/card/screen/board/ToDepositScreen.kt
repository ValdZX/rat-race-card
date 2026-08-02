package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.design.DesignAmountSheet
import ua.vald_zx.game.rat.race.card.splitDecimal

class ToDepositScreen(private val vm: BoardViewModel) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        if (!designV2Enabled.value) {
            LegacyToDepositScreen(vm, state.player.cash)
            return
        }
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val action = stringResource(Res.string.deposit_action)
        DesignAmountSheet(
            title = stringResource(Res.string.deposit_amount),
            available = state.player.cash,
            confirmLabel = { amount -> "$action ${amount.splitDecimal()}" },
            onConfirm = { amount ->
                bottomSheetNavigator.hide()
                vm.toDeposit(amount = amount)
            },
            onCancel = { bottomSheetNavigator.hide() },
        )
    }
}
