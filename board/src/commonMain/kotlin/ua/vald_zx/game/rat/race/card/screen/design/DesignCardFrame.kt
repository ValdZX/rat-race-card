package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.shared.BoardCardType

@Composable
fun DesignCardFrame(
    type: BoardCardType,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = Design.colors
    val tone = type.tone()
    Column(
        modifier = modifier
            .levelCard(colors, DesignShapes.lg)
            .clip(DesignShapes.lg)
            .background(colors.scaffold.surface1)
            .border(2.dp, tone.edge, DesignShapes.lg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(tone.fill)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = type.shortLabel(),
                style = Design.type.label,
                color = colors.scaffold.onFill,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        CompositionLocalProvider(LocalContentColor provides colors.scaffold.onSurface) {
            Box(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}
