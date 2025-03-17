package ua.vald_zx.game.rat.race.card.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

val RainbowRed = Color(0xFFDA034E)
val RainbowOrange = Color(0xFFFF9800)
val RainbowYellow = Color(0xFFFFEB3B)
val RainbowGreen = Color(0xFF4CAF50)
val RainbowBlue = Color(0xFF2196F3)
val RainbowIndigo = Color(0xFF3F51B5)
val RainbowViolet = Color(0xFF9C27B0)

val SkittlesRainbow = listOf(
    RainbowRed,
    RainbowOrange,
    RainbowYellow,
    RainbowGreen,
    RainbowBlue,
    RainbowIndigo,
    RainbowViolet,
    RainbowRed,
)

val GrayRainbow = listOf(
    Color(0xFF444444),
    RainbowBlue,
    Color(0xFF888888),
    RainbowGreen,
    Color.Gray,
    RainbowIndigo
)

@Composable
fun MultiColorSmoothText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = LocalTextStyle.current,
    rainbow: List<Color> = SkittlesRainbow,
    startIndex: Int = 0,
    duration: Int
) {
    val infiniteTransition = rememberInfiniteTransition()
    val interval = duration / rainbow.size
    val color by infiniteTransition.animateColor(
        initialValue = rainbow[0],
        targetValue = rainbow.last(),
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = duration
                delayMillis = startIndex * interval / 2
                var i = 0
                // set the keyframes from the rainbow with code
                for (color in rainbow) { // this is the crux  of setting the keyframes
                    color at i // at is an infix method in the KeyframesSpec class
                    i += interval
                }
            },
            repeatMode = RepeatMode.Restart
        )
    )
    Text(text = text, color = color, style = style, modifier = modifier)
}

@Composable
fun SmoothRainbowText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    rainbow: List<Color> = SkittlesRainbow,
    startColor: Int = 0,
    duration: Int = 1200
) {
    Row(modifier) {
        var index = startColor
        for (letter in text) {
            MultiColorSmoothText(
                text = letter.toString(),
                style = style,
                rainbow = rainbow,
                startIndex = index,
                duration = duration
            )
            index++
            if (index == rainbow.size) index = 0
        }
    }
}