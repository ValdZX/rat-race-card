package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
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
            AlertDialog(
                title = { Text(stringResource(Res.string.dream_offer_title)) },
                text = {
                    Column {
                        Text(
                            dream?.let {
                                stringResource(
                                    Res.string.dream_offer_message,
                                    it.name,
                                    it.price.splitDecimal(),
                                )
                            }.orEmpty()
                        )
                        dream?.description?.let { Text(it) }
                    }
                },
                onDismissRequest = {},
                confirmButton = {
                    TextButton(
                        enabled = canPay,
                        onClick = {
                            vm.buyDream()
                            onDone()
                        },
                    ) {
                        Text(stringResource(Res.string.buy))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            vm.pass()
                            onDone()
                        },
                    ) {
                        Text(stringResource(Res.string.pass))
                    }
                },
            )
}
