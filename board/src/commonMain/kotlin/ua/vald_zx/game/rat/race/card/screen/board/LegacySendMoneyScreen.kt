package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.screen.InputScreen

/** Стара мова переказу грошей у онлайні. Нова — DesignAmountSheet. */
@Composable
internal fun LegacySendMoneyScreen(vm: BoardViewModel, playerId: String, playerName: String) {
    InputScreen(
        inputLabel = stringResource(Res.string.send_money_to, playerName),
        buttonText = stringResource(Res.string.send),
        validation = { amount -> amount.isNotEmpty() },
        onClick = { amount -> vm.sendMoney(playerId, amount.toLong()) },
    )
}
