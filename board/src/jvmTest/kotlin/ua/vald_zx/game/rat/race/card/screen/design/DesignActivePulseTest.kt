package ua.vald_zx.game.rat.race.card.screen.design

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DesignActivePulseTest {

    @Test
    fun ringStartsFlushWithTheElementAndFullyOpaque() {
        assertEquals(0f, pulseSpreadFraction(0f))
        assertEquals(1f, pulseAlpha(0f))
    }

    @Test
    fun ringFadesOutExactlyWhenItReachesFullSpread() {
        val last = 0.69f
        assertTrue(pulseSpreadFraction(last) > 0.09f, "кільце не встигає вирости до кінця видимої фази")
        assertTrue(pulseAlpha(last) < 0.05f, "кільце ще помітне там, де дизайн уже прозорий")
    }

    @Test
    fun ringRestsForTheTailOfTheCycle() {
        listOf(0.7f, 0.85f, 1f).forEach { progress ->
            assertEquals(0f, pulseSpreadFraction(progress), "хвіст циклу має бути паузою")
            assertEquals(0f, pulseAlpha(progress), "хвіст циклу має бути паузою")
        }
    }

    @Test
    fun ringOnlyEverGrows() {
        var previous = -1f
        var progress = 0f
        while (progress < 0.7f) {
            val spread = pulseSpreadFraction(progress)
            assertTrue(spread >= previous, "кільце смикнулось назад на $progress")
            previous = spread
            progress += 0.01f
        }
    }
}
