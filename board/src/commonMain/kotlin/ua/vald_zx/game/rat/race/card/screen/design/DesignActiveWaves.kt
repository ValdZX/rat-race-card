package ua.vald_zx.game.rat.race.card.screen.design

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

internal const val WAVE_COUNT = 3

internal fun waveRadiusFraction(progress: Float) = pulseProgress(progress)

internal fun waveAlpha(progress: Float) = 1f - pulseProgress(progress)

internal fun wavePhase(progress: Float, index: Int) =
    (progress + index.toFloat() / WAVE_COUNT) % 1f

@Composable
internal fun BoxScope.DesignActiveWaves(
    color: Color,
    modifier: Modifier = Modifier.matchParentSize(),
) {
    val transition = rememberInfiniteTransition(label = "ActiveWaves")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(WaveDurationMillis, easing = LinearEasing)),
        label = "ActiveWavesTravel",
    )
    Box(
        modifier = modifier.drawBehind {
            repeat(WAVE_COUNT) { index ->
                drawWave(color, wavePhase(progress, index))
            }
        }
    )
}

private const val WaveDurationMillis = 2400

private fun DrawScope.drawWave(color: Color, phase: Float) {
    val alpha = waveAlpha(phase)
    if (alpha <= 0f) return
    val reach = size.minDimension / 2f
    val radius = waveRadiusFraction(phase) * reach
    if (radius <= 0f) return
    drawCircle(
        color = color,
        radius = radius,
        alpha = alpha,
        style = Stroke(width = reach * 0.09f),
    )
}
