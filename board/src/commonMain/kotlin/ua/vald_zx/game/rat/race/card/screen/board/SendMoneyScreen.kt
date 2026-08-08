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
import ua.vald_zx.game.rat.race.card.formatAmount

class SendMoneyScreen(
    private val vm: BoardViewModel,
    private val playerId: String,
    private val playerName: String,
) : Screen {
    @Composable
    override fun Content() {
        val state by vm.uiState.collectAsState()
        if (!designV2Enabled.value) {
            LegacySendMoneyScreen(vm, playerId, playerName, state.player.cash)
            return
        }
        val bottomSheetNavigator = LocalBottomSheetNavigator.current
        val action = stringResource(Res.string.send)
        val notEnough = stringResource(Res.string.not_enough_money)
        DesignAmountSheet(
            title = stringResource(Res.string.send_money_to, playerName),
            available = state.player.cash,
            confirmLabel = { amount -> "$action ${amount.formatAmount()}" },
            validate = { amount -> amount > 0 && amount <= state.player.cash },
            errorFor = { amount -> notEnough.takeIf { amount > state.player.cash } },
            onConfirm = { amount ->
                bottomSheetNavigator.hide()
                vm.sendMoney(playerId, amount)
            },
            onCancel = { bottomSheetNavigator.hide() },
        )
    }
}
