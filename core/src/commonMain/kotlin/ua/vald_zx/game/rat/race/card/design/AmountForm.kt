package ua.vald_zx.game.rat.race.card.design

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import ua.vald_zx.game.rat.race.card.splitDecimal

private const val MAX_DIGITS = 12
private const val BACKSPACE = "backspace"

/**
 * Форма вводу суми з макета C. Власна цифрова клавіатура замість системної:
 * пристрій лежить на столі, і системна клавіатура з'їдає пів екрана.
 */
@Composable
fun DesignAmountForm(
    title: String,
    confirmLabel: (Long) -> String,
    onConfirm: (Long) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    initial: Long = 0,
    quickSteps: List<Long> = listOf(1_000, 5_000),
    maxAmount: Long? = null,
    maxLabel: String? = null,
    hint: (Long) -> String? = { null },
    validate: (Long) -> Boolean = { it > 0 },
    errorFor: (Long) -> String? = { null },
    onCancel: (() -> Unit)? = null,
    cancelLabel: String? = null,
    extraContent: @Composable ColumnScope.() -> Unit = {},
) {
    val colors = Design.colors
    val type = Design.type
    var digits by remember { mutableStateOf(if (initial > 0) initial.toString() else "") }
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

        AmountDisplay(amount = amount, empty = digits.isEmpty(), error = error != null)

        val caption = error ?: hint(amount)
        if (caption != null) {
            Text(
                text = caption,
                style = type.monoMeta,
                color = if (error != null) colors.semantic.negative.edge else colors.scaffold.onSurfaceMuted,
            )
        }

        if (maxAmount != null || quickSteps.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                quickSteps.forEach { step ->
                    QuickChip("+${step.splitDecimal()}") { digits = (amount + step).toString() }
                }
                QuickChip("×2") { if (amount > 0) digits = (amount * 2).toString() }
                if (maxAmount != null && maxLabel != null) {
                    QuickChip(maxLabel) { digits = maxAmount.toString() }
                }
            }
        }

        extraContent()

        Keypad(
            onDigit = { d -> if (digits.length + d.length <= MAX_DIGITS) digits = (digits + d).trimStart('0').ifEmpty { "" } },
            onBackspace = { digits = digits.dropLast(1) },
        )

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
private fun AmountDisplay(amount: Long, empty: Boolean, error: Boolean) {
    val colors = Design.colors
    val depth by animateDpAsState(if (empty) 0.dp else 4.dp, tween(120), label = "FieldPlinth")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (empty) Modifier
                else Modifier.plinth(
                    if (error) colors.semantic.negative.edge else colors.scaffold.accentDim,
                    depth,
                    DesignShapes.md,
                )
            )
            .height(60.dp)
            .clip(DesignShapes.md)
            .background(colors.scaffold.surface2)
            .border(
                width = 1.5.dp,
                color = when {
                    error -> colors.semantic.negative.edge
                    empty -> colors.scaffold.outline
                    else -> colors.scaffold.outlineStrong
                },
                shape = DesignShapes.md,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (empty) "0" else amount.splitDecimal(),
            style = Design.type.amountLg,
            color = if (empty) colors.scaffold.onSurfaceMuted else colors.scaffold.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            softWrap = false,
        )
        Text("₴", style = Design.type.subtitle, color = colors.scaffold.onSurfaceMuted)
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

@Composable
private fun Keypad(onDigit: (String) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("000", "0", BACKSPACE),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    Key(key, modifier = Modifier.weight(1f)) {
                        if (key == BACKSPACE) onBackspace() else onDigit(key)
                    }
                }
            }
        }
    }
}

@Composable
private fun Key(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = Design.colors
    Box(
        modifier = modifier
            .testTag("key_$label")
            .plinth(colors.scaffold.outlineStrong, 3.dp, DesignShapes.md)
            .height(56.dp)
            .clip(DesignShapes.md)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.md)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (label == BACKSPACE) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = BACKSPACE,
                tint = colors.scaffold.onSurface,
            )
        } else {
            Text(
                text = label,
                style = Design.type.amountMd,
                color = colors.scaffold.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}
