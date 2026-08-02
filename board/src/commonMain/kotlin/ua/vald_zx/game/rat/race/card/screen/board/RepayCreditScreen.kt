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
import ua.vald_zx.game.rat.race.card.shared.balance
import ua.vald_zx.game.rat.race.card.splitDecimal
import kotlin.math.min

class RepayCreditScreen(private val vm: BoardViewModel) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        val repayable = min(state.player.loan, state.player.balance())
        if (!designV2Enabled.value) {
            LegacyRepayCreditScreen(vm, state.player, repayable)
            return
        }
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val action = stringResource(Res.string.repay_action)
        val tooMuch = stringResource(Res.string.not_enough_money)
        DesignAmountSheet(
            title = stringResource(Res.string.repay_amount),
            available = repayable,
            confirmLabel = { amount -> "$action ${amount.splitDecimal()}" },
            errorFor = { amount -> if (amount > repayable) tooMuch else null },
            onConfirm = { amount ->
                bottomSheetNavigator.hide()
                vm.repayLoan(amount = amount)
            },
            onCancel = { bottomSheetNavigator.hide() },
        )
    }
}
