package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lennartegb.shadows.boxShadow

@Composable
fun LegacyDiceGlow(rollSize: Dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteTransition")
    val spreadRadius by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = rollSize * 0.3f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "FloatAnimation",
        typeConverter = TwoWayConverter({ AnimationVector(it.value) }, { it.value.dp })
    )
    Box(
        modifier = Modifier
            .size(rollSize * 0.2f)
            .padding(top = rollSize * 0.3f)
            .boxShadow(
                blurRadius = rollSize * 0.3f,
                spreadRadius = spreadRadius,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onBackground,
            )
    )
}
