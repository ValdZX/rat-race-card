package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.design.AmountQuickOption
import ua.vald_zx.game.rat.race.card.design.DesignAmountForm
import ua.vald_zx.game.rat.race.card.design.proportionalAmountOptions
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.formatAmount

@Composable
fun DesignAmountSheet(
    title: String,
    confirmLabel: (Long) -> String,
    onConfirm: (Long) -> Unit,
    onCancel: () -> Unit,
    subtitle: String? = null,
    available: Long? = null,
    initial: Long = 0,
    quickOptions: List<AmountQuickOption>? = null,
    validate: (Long) -> Boolean = { it > 0 },
    errorFor: (Long) -> String? = { null },
) {
    val availableWord = stringResource(Res.string.available)
    val remainderWord = stringResource(Res.string.remainder)
    val allLabel = stringResource(Res.string.all_in)
    val resolvedQuickOptions = quickOptions
        ?: available?.let { proportionalAmountOptions(it, allLabel) }
        ?: emptyList()
    BottomSheetContainer(
        onClose = onCancel,
    ) {
        DesignAmountForm(
            title = title,
            subtitle = subtitle,
            confirmLabel = confirmLabel,
            onConfirm = onConfirm,
            onCancel = onCancel,
            cancelLabel = stringResource(Res.string.cancel),
            initial = initial,
            quickOptions = resolvedQuickOptions,
            maxAmount = available,
            validate = validate,
            errorFor = errorFor,
            hint = { amount ->
                available?.let {
                    val left = it - amount
                    val leftText = if (left < 0) "−${(-left).formatAmount()}" else left.formatAmount()
                    "$availableWord ${it.formatAmount()} · $remainderWord $leftText"
                }
            },
        )
    }
}
