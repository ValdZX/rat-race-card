package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.clickableSingle
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.Player

/**
 * «Це ти» — подвійне кільце й підпис, без accent і без руху: ідентичність не мигає.
 * «Твоя черга» несе окремий носій — пульс обводки, і тільки для активного гравця.
 */
@Composable
fun DesignPlayerToken(
    player: Player,
    isCurrentPlayer: Boolean,
    isActivePlayer: Boolean,
    spotSize: DpSize,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = Design.colors
    val playerColor = remember(player.attrs.color) { Color(player.attrs.color) }
    val ringWidth = min(spotSize.width, spotSize.height) * 0.09f

    val transition = rememberInfiniteTransition(label = "ActiveTurn")
    val turnRing by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "TurnRing",
    )
    val activeRing = if (isActivePlayer) (1f + turnRing * 0.35f) else 1f

    Box(
        modifier = modifier
            .size(spotSize)
            .optionalModifier(onClick != null) { clickableSingle(onClick = onClick!!) },
        contentAlignment = Alignment.Center,
    ) {
        if (isActivePlayer) {
            Box(
                Modifier
                    .size(spotSize * activeRing)
                    .clip(DesignShapes.full)
                    .border(ringWidth * 0.6f, colors.scaffold.accent.copy(alpha = 1f - turnRing), DesignShapes.full)
            )
        }
        Box(
            modifier = Modifier
                .size(spotSize * 0.82f)
                .clip(DesignShapes.full)
                .background(playerColor)
                .border(
                    width = ringWidth,
                    color = if (player.isInactive) colors.scaffold.outline else colors.scaffold.onSurface,
                    shape = DesignShapes.full,
                )
                .then(
                    if (isCurrentPlayer) {
                        Modifier.padding(ringWidth * 0.6f)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isCurrentPlayer) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(DesignShapes.full)
                        .border(ringWidth * 0.7f, colors.scaffold.onSurface, DesignShapes.full)
                )
            }
        }
    }
}

@Composable
fun PlayerTokenLabel(
    player: Player,
    isCurrentPlayer: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = Design.colors
    val you = stringResource(Res.string.you)
    Box(
        modifier = modifier
            .clip(DesignShapes.xs)
            .background(if (isCurrentPlayer) colors.scaffold.onSurface else colors.scaffold.surface2)
            .padding(horizontal = 4.dp, vertical = 1.dp),
    ) {
        Text(
            text = if (isCurrentPlayer) you else player.card.name,
            style = Design.type.micro,
            color = if (isCurrentPlayer) colors.scaffold.background else colors.scaffold.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
