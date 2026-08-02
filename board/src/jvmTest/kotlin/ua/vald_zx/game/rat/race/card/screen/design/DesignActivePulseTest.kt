package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DesignActivePulseTest {

    private val palette = listOf(Color.Red, Color.Yellow, Color.Green, Color.Blue)

    @Test
    fun rotatedPaletteKeepsAClosedMulticolorSweep() {
        val rotated = rotatePulsePalette(palette, 0.37f)

        assertEquals(palette.size + 1, rotated.size)
        assertEquals(rotated.first(), rotated.last())
        assertNotEquals(rotated[0], rotated[1])
    }

    @Test
    fun colorCycleReturnsToItsStartingPoint() {
        assertEquals(
            rotatePulsePalette(palette, 0f),
            rotatePulsePalette(palette, 1f),
        )
    }
}
