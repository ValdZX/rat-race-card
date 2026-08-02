package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope

private const val PulseDurationMillis = 1800
private const val PulseVisiblePart = 0.7f
internal const val PulseSpreadFraction = 8f / 76f
internal const val TokenPulseSpreadFraction = 0.4f

private val PulseEasing = CubicBezierEasing(0f, 0f, 0.58f, 1f)

internal fun pulseSpreadFraction(progress: Float, maxFraction: Float = PulseSpreadFraction): Float {
    if (progress >= PulseVisiblePart) return 0f
    return PulseEasing.transform(progress / PulseVisiblePart) * maxFraction
}

internal fun pulseAlpha(progress: Float): Float {
    if (progress >= PulseVisiblePart) return 0f
    return 1f - PulseEasing.transform(progress / PulseVisiblePart)
}

@Composable
internal fun BoxScope.DesignActivePulse(
    shape: Shape,
    color: Color,
    modifier: Modifier = Modifier.matchParentSize(),
    spreadFraction: Float = PulseSpreadFraction,
) {
    val transition = rememberInfiniteTransition(label = "ActivePulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PulseDurationMillis, easing = LinearEasing)),
        label = "ActivePulseWave",
    )
    Box(
        modifier = modifier.drawBehind {
            drawPulseRing(
                shape = shape,
                color = color,
                spread = size.minDimension * pulseSpreadFraction(progress, spreadFraction),
                alpha = pulseAlpha(progress),
            )
        }
    )
}

private fun DrawScope.drawPulseRing(shape: Shape, color: Color, spread: Float, alpha: Float) {
    if (spread <= 0f || alpha <= 0f) return
    val grown = Path().apply {
        addOutline(
            shape.createOutline(
                Size(size.width + spread * 2, size.height + spread * 2),
                layoutDirection,
                this@drawPulseRing,
            )
        )
        translate(Offset(-spread, -spread))
    }
    val core = Path().apply {
        addOutline(shape.createOutline(size, layoutDirection, this@drawPulseRing))
    }
    drawPath(
        path = Path().apply { op(grown, core, PathOperation.Difference) },
        color = color,
        alpha = alpha,
    )
}
