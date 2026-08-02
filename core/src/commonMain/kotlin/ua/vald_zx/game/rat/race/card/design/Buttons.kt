package ua.vald_zx.game.rat.race.card.design

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.vald_zx.game.rat.race.card.components.clickableSingle

enum class DesignButtonKind { Filled, Tonal, Brass, Text }

private const val PRESS_MS = 90

@Composable
fun DesignButton(
    text: String,
    modifier: Modifier = Modifier,
    kind: DesignButtonKind = DesignButtonKind.Filled,
    enabled: Boolean = true,
    disabledReason: String? = null,
    height: Dp = 48.dp,
    fontSize: TextUnit = 15.sp,
    padding: Dp = 20.dp,
    onClick: () -> Unit,
) {
    val colors = Design.colors
    val type = Design.type
    val shape: Shape = when (kind) {
        DesignButtonKind.Brass -> androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
        DesignButtonKind.Text -> androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
        else -> DesignShapes.md
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val depth by animateDpAsState(
        targetValue = if (!enabled) 0.dp else if (pressed) 1.dp else 4.dp,
        animationSpec = tween(PRESS_MS),
        label = "PlinthDepth",
    )
    val shift by animateDpAsState(
        targetValue = if (enabled && pressed) 3.dp else 0.dp,
        animationSpec = tween(PRESS_MS),
        label = "PressShift",
    )

    if (!enabled && disabledReason != null) {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            ButtonSurface(Modifier.fillMaxWidth(), text, kind, colors, type, enabled, depth, shift, shape, height, fontSize, padding, onClick)
            Text(
                text = disabledReason,
                style = type.micro,
                color = colors.scaffold.onSurfaceMuted,
                modifier = Modifier.padding(top = 6.dp),
                textAlign = TextAlign.Center,
            )
        }
    } else {
        ButtonSurface(modifier, text, kind, colors, type, enabled, depth, shift, shape, height, fontSize, padding, onClick)
    }
}

@Composable
private fun ButtonSurface(
    modifier: Modifier,
    text: String,
    kind: DesignButtonKind,
    colors: DesignColors,
    type: DesignTypography,
    enabled: Boolean,
    depth: Dp,
    shift: Dp,
    shape: Shape,
    height: Dp,
    fontSize: TextUnit,
    padding: Dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .offset(y = shift)
            .plinthFor(kind, colors, enabled, depth, shape)
            .clip(shape)
            .fillFor(kind, colors, enabled)
            .borderFor(kind, colors, enabled, shape)
            .height(height)
            .clickableSingle(enabled = enabled, onClick = onClick)
            .padding(horizontal = padding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = type.subtitle,
            color = contentColorFor(kind, colors, enabled),
            textAlign = TextAlign.Center,
            maxLines = 2,
            fontSize = fontSize,
            lineHeight = fontSize * 1.33f,
        )
    }
}

private fun Modifier.plinthFor(
    kind: DesignButtonKind,
    colors: DesignColors,
    enabled: Boolean,
    depth: Dp,
    shape: Shape,
): Modifier {
    if (!enabled || kind == DesignButtonKind.Text) return this
    val color = when (kind) {
        DesignButtonKind.Filled -> colors.scaffold.accentDim
        DesignButtonKind.Brass -> colors.scaffold.brass.darken()
        else -> colors.scaffold.outlineStrong
    }
    return plinth(color, depth, shape)
}

private fun Modifier.fillFor(
    kind: DesignButtonKind,
    colors: DesignColors,
    enabled: Boolean,
): Modifier {
    if (!enabled) return background(colors.scaffold.surface3)
    return when (kind) {
        DesignButtonKind.Filled -> background(colors.scaffold.accent)
        DesignButtonKind.Brass -> background(
            Brush.verticalGradient(listOf(colors.scaffold.brass, colors.scaffold.brass.darken()))
        )

        DesignButtonKind.Tonal -> background(colors.scaffold.surface2)
        DesignButtonKind.Text -> this
    }
}

private fun Modifier.borderFor(
    kind: DesignButtonKind,
    colors: DesignColors,
    enabled: Boolean,
    shape: Shape,
): Modifier = when {
    !enabled -> border(BorderStroke(1.5.dp, colors.scaffold.outlineStrong), shape)
    kind == DesignButtonKind.Tonal -> border(BorderStroke(1.5.dp, colors.scaffold.outlineStrong), shape)
    else -> this
}

private fun contentColorFor(
    kind: DesignButtonKind,
    colors: DesignColors,
    enabled: Boolean,
): Color = when {
    !enabled -> colors.scaffold.onSurfaceMuted
    kind == DesignButtonKind.Filled -> colors.scaffold.onAccent
    kind == DesignButtonKind.Brass -> colors.scaffold.brassInk
    else -> colors.scaffold.onSurface
}

private fun Color.darken(factor: Float = 0.72f) =
    Color(red * factor, green * factor, blue * factor, alpha)
