package ua.vald_zx.game.rat.race.card.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private const val MAX_DIGITS = 12

data class AmountQuickOption(
    val label: String,
    val amount: Long,
)

fun proportionalAmountOptions(maxAmount: Long, allLabel: String): List<AmountQuickOption> = listOf(
    AmountQuickOption("25%", maxAmount / 4),
    AmountQuickOption("50%", maxAmount / 2),
    AmountQuickOption(allLabel, maxAmount),
).filter { it.amount > 0 }.distinctBy { it.amount }

@Composable
fun DesignAmountForm(
    title: String,
    confirmLabel: (Long) -> String,
    onConfirm: (Long) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    initial: Long = 0,
    quickOptions: List<AmountQuickOption> = emptyList(),
    maxAmount: Long? = null,
    hint: (Long) -> String? = { null },
    validate: (Long) -> Boolean = { it > 0 },
    errorFor: (Long) -> String? = { null },
    onCancel: (() -> Unit)? = null,
    cancelLabel: String? = null,
    extraContent: @Composable ColumnScope.() -> Unit = {},
) {
    val colors = Design.colors
    val type = Design.type
    var digits by remember(initial) { mutableStateOf(if (initial > 0) initial.toString() else "") }
    val amount = digits.toLongOrNull() ?: 0
    val error = errorFor(amount)
    val valid = validate(amount) && error == null

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = type.title, color = colors.scaffold.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (subtitle != null) {
            Text(subtitle, style = type.body, color = colors.scaffold.onSurfaceMuted, maxLines = 2)
        }

        DesignTextField(
            value = digits,
            onValueChange = { input ->
                val filtered = input.filter(Char::isDigit).take(MAX_DIGITS)
                digits = filtered.trimStart('0').ifEmpty { "" }
            },
            placeholder = "0",
            suffix = "₴",
            textStyle = type.amountLg,
            inputTestTag = "system-amount-field",
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
        )

        val caption = error ?: hint(amount)
        if (caption != null) {
            Text(
                text = caption,
                style = type.monoMeta,
                color = if (error != null) colors.semantic.negative.edge else colors.scaffold.onSurfaceMuted,
            )
        }

        val visibleQuickOptions = quickOptions
            .filter { it.amount > 0 && (maxAmount == null || it.amount <= maxAmount) }
            .distinctBy { it.amount }
        if (visibleQuickOptions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                visibleQuickOptions.forEach { option ->
                    QuickChip(option.label) {
                        digits = option.amount.toString()
                    }
                }
            }
        }

        extraContent()

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            if (onCancel != null && cancelLabel != null) {
                DesignButton(
                    text = cancelLabel,
                    kind = DesignButtonKind.Tonal,
                    modifier = Modifier.weight(1f),
                    onClick = onCancel,
                )
            }
            DesignButton(
                text = confirmLabel(amount),
                enabled = valid,
                modifier = Modifier.weight(1.4f),
                onClick = { onConfirm(amount) },
            )
        }
    }
}

@Composable
private fun QuickChip(text: String, onClick: () -> Unit) {
    val colors = Design.colors
    Box(
        modifier = Modifier
            .clip(DesignShapes.sm)
            .background(colors.scaffold.surface3)
            .border(1.dp, colors.scaffold.outline, DesignShapes.sm)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(text, style = Design.type.label, color = colors.scaffold.onSurface, maxLines = 1)
    }
}
