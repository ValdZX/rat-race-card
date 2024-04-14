package ua.vald_zx.game.rat.race.card.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Button(
    text: String,
    containerColor: Color = ButtonDefaults.elevatedButtonColors().containerColor,
    onClick: () -> Unit
) = ElevatedButton(
    modifier = Modifier
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .widthIn(min = 200.dp),
    onClick = onClick,
    content = {
        Text(text)
    },
    colors = ButtonDefaults.elevatedButtonColors().copy(containerColor = containerColor)
)

@Composable
fun RainbowButton(
    text: String,
    onClick: () -> Unit
) {
    val currentFontSizePx = with(LocalDensity.current) { 50.dp.toPx() }
    val currentFontSizeDoublePx = currentFontSizePx * 2

    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = currentFontSizeDoublePx,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing))
    )
    androidx.compose.material3.Button(
        modifier = Modifier,
        onClick = onClick,
        content = {
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .widthIn(min = 200.dp)
                    .clip(CircleShape)
                    .drawWithCache {
                        val brush = Brush.linearGradient(
                            SkittlesRainbow,
                            start = Offset(offset, offset),
                            end = Offset(offset + currentFontSizePx, offset + currentFontSizePx),
                            tileMode = TileMode.Mirror
                        )

                        onDrawBehind {
                            drawRect(
                                brush = brush,
                                blendMode = BlendMode.SrcIn
                            )
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent)
    )
}