package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ua.vald_zx.game.rat.race.card.design.Design

/** Затемнення набігає, поки карта летить до центру, і сходить, коли вона йде у збірку. */
fun cardScrimAlpha(progress: Float): Float = when {
    progress < 1f -> progress.coerceAtLeast(0f)
    progress < 5f -> 1f
    else -> (6f - progress).coerceIn(0f, 1f)
}

/**
 * Затемнення на час, поки карта в польоті й розкрита: доки триває хід картою,
 * дошка позаду свідомо гасне. У старій мові його немає.
 */
@Composable
fun DesignCardScrim(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Design.scaffold.shadow.copy(alpha = cardScrimAlpha(progress) * 0.62f))
    )
}
