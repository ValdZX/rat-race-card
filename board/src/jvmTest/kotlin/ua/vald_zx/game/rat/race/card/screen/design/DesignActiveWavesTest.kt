package ua.vald_zx.game.rat.race.card.screen.design

import kotlin.test.Test
import kotlin.test.assertTrue

class DesignActiveWavesTest {

    @Test
    fun aWaveStartsAtTheCentreAndNotAtTheEdge() {
        assertTrue(waveRadiusFraction(0f) < 0.01f, "хвиля стартує від краю, а не з середини")
        assertTrue(waveAlpha(0f) > 0.99f, "хвиля народжується вже напівпрозорою")
    }

    @Test
    fun aWaveOnlyGrowsAndOnlyFades() {
        var previousRadius = -1f
        var previousAlpha = 2f
        var progress = 0f
        while (progress < 0.7f) {
            val radius = waveRadiusFraction(progress)
            val alpha = waveAlpha(progress)
            assertTrue(radius >= previousRadius, "хвиля смикнулась назад на $progress")
            assertTrue(alpha <= previousAlpha, "хвиля яскравішає на $progress")
            previousRadius = radius
            previousAlpha = alpha
            progress += 0.01f
        }
    }

    @Test
    fun thereIsAlwaysAWaveOnScreen() {
        var progress = 0f
        while (progress < 1f) {
            val visible = (0 until WAVE_COUNT).count { index ->
                waveAlpha(wavePhase(progress, index)) > 0f
            }
            assertTrue(visible > 0, "на $progress кубик лишився без жодної хвилі")
            progress += 0.01f
        }
    }

    @Test
    fun wavesAreSpreadAcrossTheCycleInsteadOfOverlapping() {
        val radii = (0 until WAVE_COUNT).map { waveRadiusFraction(wavePhase(0.05f, it)) }
        radii.forEachIndexed { index, radius ->
            radii.drop(index + 1).forEach { other ->
                assertTrue(
                    kotlin.math.abs(radius - other) > 0.05f,
                    "хвилі злилися в одне кільце: $radii",
                )
            }
        }
    }
}
