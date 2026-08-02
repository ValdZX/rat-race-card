package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.connection_failed
import ua.vald_zx.game.rat.race.card.resources.retry_connection

@Composable
internal fun LegacyLoadOnline(failed: Boolean, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
    ) {
        if (failed) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Text(stringResource(Res.string.connection_failed))
                Button(stringResource(Res.string.retry_connection), onClick = onRetry)
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
