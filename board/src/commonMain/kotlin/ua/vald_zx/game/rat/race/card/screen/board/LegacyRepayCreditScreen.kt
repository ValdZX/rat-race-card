package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.InputScreen
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.balance

/** Стара мова погашення кредиту. Нова — DesignAmountSheet. */
@Composable
internal fun LegacyRepayCreditScreen(vm: BoardViewModel, player: Player, repayable: Long) {
    InputScreen(
        inputLabel = stringResource(Res.string.repay_amount),
        buttonText = stringResource(Res.string.repay_action),
        validation = { amount -> amount.isNotEmpty() && player.balance() >= amount.toInt() },
        onClick = { amount -> vm.repayLoan(amount = amount.toLong()) },
        value = repayable.toString(),
    )
}
