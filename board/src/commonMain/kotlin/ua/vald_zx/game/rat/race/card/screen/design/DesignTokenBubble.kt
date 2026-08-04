package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.design.DesignButton
import ua.vald_zx.game.rat.race.card.design.DesignButtonKind
import ua.vald_zx.game.rat.race.card.design.DesignShapes
import ua.vald_zx.game.rat.race.card.design.levelSheet
import ua.vald_zx.game.rat.race.card.logic.PlayerMessage
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.cashFlow
import ua.vald_zx.game.rat.race.card.shared.total
import ua.vald_zx.game.rat.race.card.splitDecimal

internal val tokenBubbleWidth = 220.dp
internal const val tokenBubbleTag = "token-bubble"

@Composable
internal fun DesignTokenBubble(
    player: Player,
    isCurrentPlayer: Boolean,
    isActivePlayer: Boolean,
    messages: List<PlayerMessage>,
    onSendMessage: () -> Unit,
    onSendMoney: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = Design.colors
    Column(
        modifier = modifier
            .testTag(tokenBubbleTag)
            .width(tokenBubbleWidth)
            .levelSheet(colors, DesignShapes.md)
            .clip(DesignShapes.md)
            .background(colors.scaffold.surface1)
            .border(1.dp, colors.scaffold.outline, DesignShapes.md)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BubbleHeader(player, isCurrentPlayer, isActivePlayer)
        BubbleStats(player)
        BubbleMessages(messages)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (onSendMoney != null) {
                DesignButton(
                    text = stringResource(Res.string.send),
                    kind = DesignButtonKind.Filled,
                    height = 38.dp,
                    padding = 10.dp,
                    fontSize = Design.type.label.fontSize,
                    modifier = Modifier.weight(1f),
                    onClick = onSendMoney,
                )
            }
            DesignButton(
                text = stringResource(Res.string.send_message),
                kind = DesignButtonKind.Tonal,
                height = 38.dp,
                padding = 10.dp,
                fontSize = Design.type.label.fontSize,
                modifier = Modifier.weight(1f),
                onClick = onSendMessage,
            )
        }
    }
}

@Composable
private fun BubbleHeader(player: Player, isCurrentPlayer: Boolean, isActivePlayer: Boolean) {
    val colors = Design.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(DesignShapes.full)
                .background(Color(player.attrs.color))
                .border(1.5.dp, colors.scaffold.onSurface, DesignShapes.full)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isCurrentPlayer) stringResource(Res.string.you) else player.card.name,
                style = Design.type.label,
                color = colors.scaffold.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = player.card.profession,
                style = Design.type.micro,
                color = colors.scaffold.onSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (isActivePlayer) {
            Text(
                text = stringResource(Res.string.player_turn_now),
                style = Design.type.micro,
                color = colors.scaffold.accent,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BubbleStats(player: Player) {
    val colors = Design.colors
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        BubbleStat(stringResource(Res.string.total_assets), player.total(), colors.scaffold.brass)
        BubbleStat(
            label = stringResource(Res.string.cash_flow),
            amount = player.cashFlow(),
            color = if (player.cashFlow() >= 0) {
                Design.semantic.positive.edge
            } else {
                Design.semantic.negative.edge
            },
        )
    }
}

@Composable
private fun BubbleStat(label: String, amount: Long, color: Color) {
    Column {
        Text(label, style = Design.type.micro, color = Design.scaffold.onSurfaceMuted, maxLines = 1)
        Text(
            text = amount.splitDecimal(),
            style = Design.type.amountMd,
            color = color,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun BubbleMessages(messages: List<PlayerMessage>) {
    if (messages.isEmpty()) return
    val colors = Design.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(DesignShapes.sm)
            .background(colors.scaffold.surface2)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        messages.forEach { message ->
            Text(
                text = message.text,
                style = Design.type.micro,
                color = colors.scaffold.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
