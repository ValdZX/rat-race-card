package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ResponsiveBoardTypographyTest {
    @Test
    fun phoneTypographyNeverShrinksBelowTheDesignBaseline() {
        assertEquals(1f, responsiveBoardTextScale(18.dp, 30.dp))
        assertEquals(1f, responsiveBoardTextScale(30.dp, 30.dp))
    }

    @Test
    fun typographyGrowsWithTheBoardAndStopsAtAReadableMaximum() {
        val desktop = responsiveBoardTextScale(45.dp, 30.dp)
        val fullscreen = responsiveBoardTextScale(60.dp, 30.dp)
        val oversized = responsiveBoardTextScale(120.dp, 30.dp)

        assertTrue(desktop > 1f)
        assertTrue(fullscreen > desktop)
        assertEquals(2f, fullscreen)
        assertEquals(2f, oversized)
    }
}
