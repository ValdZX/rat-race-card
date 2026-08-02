package ua.vald_zx.game.rat.race.card.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.splitDecimal

@Composable
fun ValueField(
    label: String,
    amount: Long,
    modifier: Modifier = Modifier,
    tone: SemanticTone? = null,
    signed: Boolean = false,
) {
    val colors = Design.colors
    val type = Design.type
    val accentBar = tone?.edge ?: colors.scaffold.outlineStrong
    val numberColor = tone?.edge ?: colors.scaffold.onSurface
    Row(
        modifier = modifier
            .height(64.dp)
            .clip(DesignShapes.md)
            .background(colors.scaffold.surface2)
            .border(1.dp, colors.scaffold.outline, DesignShapes.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .padding(vertical = 12.dp)
                .padding(start = 4.dp)
                .width(4.dp)
                .fillMaxHeight()
                .clip(DesignShapes.xs)
                .background(accentBar)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Text(
                text = label,
                style = type.micro,
                color = colors.scaffold.onSurfaceMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = amount.formatSigned(signed),
                style = type.amountMd,
                color = numberColor,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

fun Long.formatSigned(signed: Boolean = true): String {
    val body = if (this < 0) "−${(-this).splitDecimal()}" else splitDecimal()
    return if (signed && this > 0) "+$body" else body
}

@Composable
fun DesignSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = Design.type.label,
        color = Design.scaffold.onSurfaceMuted,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
fun BrassToken(
    label: String,
    amount: Long,
    modifier: Modifier = Modifier,
) {
    val colors = Design.colors
    Row(
        modifier = modifier
            .height(24.dp)
            .clip(DesignShapes.sm)
            .background(colors.scaffold.brass)
            .border(1.5.dp, colors.scaffold.onFill, DesignShapes.sm)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, style = Design.type.micro, color = colors.scaffold.brassInk)
        Text("+${amount.splitDecimal()}", style = Design.type.monoMeta, color = colors.scaffold.brassInk)
    }
}

@Composable
fun DesignChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = Design.colors
    val shape = DesignShapes.sm
    Box(
        modifier = modifier
            .then(if (selected) Modifier.plinth(colors.scaffold.accentDim, 3.dp, shape) else Modifier)
            .clip(shape)
            .background(if (selected) colors.scaffold.accent else colors.scaffold.surface3)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.scaffold.accent else colors.scaffold.outline,
                shape = shape,
            )
            .clickableSingle(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = Design.type.label,
            color = if (selected) colors.scaffold.onAccent else colors.scaffold.onSurface,
            maxLines = 1,
        )
    }
}
