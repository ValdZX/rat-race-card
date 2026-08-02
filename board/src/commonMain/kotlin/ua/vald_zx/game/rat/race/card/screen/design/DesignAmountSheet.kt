package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.design.DesignAmountForm
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.splitDecimal

/**
 * Обгортка над DesignAmountForm для онлайн-шторок: тримає локалізацію
 * і підказку «доступно / лишиться», однакову для всіх грошових форм.
 */
@Composable
fun DesignAmountSheet(
    title: String,
    confirmLabel: (Long) -> String,
    onConfirm: (Long) -> Unit,
    onCancel: () -> Unit,
    subtitle: String? = null,
    available: Long? = null,
    initial: Long = 0,
    validate: (Long) -> Boolean = { it > 0 },
    errorFor: (Long) -> String? = { null },
) {
    val availableWord = stringResource(Res.string.available)
    val remainderWord = stringResource(Res.string.remainder)
    BottomSheetContainer(verticalScrollState = null) {
        DesignAmountForm(
            title = title,
            subtitle = subtitle,
            confirmLabel = confirmLabel,
            onConfirm = onConfirm,
            onCancel = onCancel,
            cancelLabel = stringResource(Res.string.cancel),
            initial = initial,
            maxAmount = available,
            maxLabel = stringResource(Res.string.all_in),
            validate = validate,
            errorFor = errorFor,
            hint = { amount ->
                available?.let {
                    val left = it - amount
                    val leftText = if (left < 0) "−${(-left).splitDecimal()}" else left.splitDecimal()
                    "$availableWord ${it.splitDecimal()} · $remainderWord $leftText"
                }
            },
        )
    }
}
