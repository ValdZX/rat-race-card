package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.design.DesignMessageDialog
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.Dream
import ua.vald_zx.game.rat.race.card.splitDecimal

@Composable
internal fun LegacyDreamDialog(
    vm: BoardViewModel,
    dream: Dream?,
    canPay: Boolean,
    onDone: () -> Unit,
) {
    val message = dream?.let {
        listOf(
            stringResource(
                Res.string.dream_offer_message,
                it.name,
                it.price.splitDecimal(),
            ),
            it.description,
        ).filter { part -> part.isNotBlank() }.joinToString("\n\n")
    }.orEmpty()
    DesignMessageDialog(
        title = stringResource(Res.string.dream_offer_title),
        message = message,
        onDismissRequest = {},
        dismissOnBackOrOutside = false,
        confirmLabel = stringResource(Res.string.buy),
        confirmEnabled = canPay,
        confirmDisabledReason = stringResource(Res.string.not_enough_money),
        onConfirm = {
            vm.buyDream()
            onDone()
        },
        dismissLabel = stringResource(Res.string.pass),
        onDismissAction = {
            vm.pass()
            onDone()
        },
    )
}
