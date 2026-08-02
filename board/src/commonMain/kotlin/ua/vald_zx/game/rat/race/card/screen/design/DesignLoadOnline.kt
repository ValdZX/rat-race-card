package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.resources.*

@Composable
fun DesignLoadOnline(
    failed: Boolean,
    onBack: () -> Unit = {},
    onRetry: () -> Unit,
) {
    val colors = Design.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.scaffold.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Icon(Images.Back, contentDescription = null)
        }
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .clip(DesignShapes.lg)
                .background(colors.scaffold.surface1)
                .border(1.dp, colors.scaffold.outline, DesignShapes.lg)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (failed) {
                Text(
                    text = stringResource(Res.string.connection_failed),
                    style = Design.type.subtitle,
                    color = Design.semantic.negative.edge,
                    textAlign = TextAlign.Center,
                )
                DesignButton(
                    text = stringResource(Res.string.retry_connection),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRetry,
                )
            } else {
                CircularProgressIndicator(color = colors.scaffold.accent)
                Text(
                    text = stringResource(Res.string.connecting_to_server),
                    style = Design.type.body,
                    color = colors.scaffold.onSurfaceMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
