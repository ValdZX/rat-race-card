package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.InputScreen

/** Стара мова поповнення депозиту. Нова — DesignAmountSheet. */
@Composable
internal fun LegacyToDepositScreen(vm: BoardViewModel, cash: Long) {
    InputScreen(
        inputLabel = stringResource(Res.string.deposit_amount),
        buttonText = stringResource(Res.string.deposit_action),
        validation = { amount -> amount.isNotEmpty() },
        onClick = { amount -> vm.toDeposit(amount = amount.toLong()) },
        value = cash.toString(),
    )
}
