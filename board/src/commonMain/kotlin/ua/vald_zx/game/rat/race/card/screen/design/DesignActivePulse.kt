package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import kotlin.math.floor

private const val PulseDurationMillis = 1800
private const val ColorCycleDurationMillis = 4200
private val PulseOffsets = listOf(0f, 1f / 3f, 2f / 3f)
private val PulseSpectrum = listOf(
    Color(0xFFFF3D57),
    Color(0xFFFF9100),
    Color(0xFFFFE600),
    Color(0xFF24E87A),
    Color(0xFF00CFFF),
    Color(0xFF765CFF),
    Color(0xFFFF43C3),
)

@Composable
internal fun BoxScope.DesignActivePulse(
    shape: Shape,
    anchorColor: Color,
    maxExpansion: Float,
    strokeWidth: Dp,
    modifier: Modifier = Modifier.matchParentSize(),
) {
    val transition = rememberInfiniteTransition(label = "ActivePulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(PulseDurationMillis, easing = LinearEasing)
        ),
        label = "ActivePulseWave",
    )
    val colorPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(ColorCycleDurationMillis, easing = LinearEasing)
        ),
        label = "ActivePulseColor",
    )
    val palette = remember(anchorColor) {
        listOf(anchorColor) + PulseSpectrum
    }
    val brush = remember(palette, colorPhase) {
        Brush.sweepGradient(rotatePulsePalette(palette, colorPhase))
    }

    PulseOffsets.forEach { offset ->
        ActivePulseWave(
            modifier = modifier,
            shape = shape,
            brush = brush,
            progress = (progress + offset) % 1f,
            maxExpansion = maxExpansion,
            strokeWidth = strokeWidth,
        )
    }
}

@Composable
private fun BoxScope.ActivePulseWave(
    modifier: Modifier,
    shape: Shape,
    brush: Brush,
    progress: Float,
    maxExpansion: Float,
    strokeWidth: Dp,
) {
    val strength = (1f - progress).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .scale(1f + progress * maxExpansion)
            .border(strokeWidth * strength, brush, shape)
    )
}

internal fun rotatePulsePalette(colors: List<Color>, phase: Float): List<Color> {
    if (colors.isEmpty()) return emptyList()
    val position = phase.mod(1f) * colors.size
    val offset = floor(position).toInt() % colors.size
    val fraction = position - floor(position)
    val rotated = List(colors.size) { index ->
        val from = colors[(index + offset) % colors.size]
        val to = colors[(index + offset + 1) % colors.size]
        lerp(from, to, fraction)
    }
    return rotated + rotated.first()
}
