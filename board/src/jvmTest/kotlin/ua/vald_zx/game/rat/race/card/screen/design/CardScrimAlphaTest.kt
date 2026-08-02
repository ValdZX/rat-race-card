package ua.vald_zx.game.rat.race.card.screen.design

import kotlin.test.Test
import kotlin.test.assertEquals

class CardScrimAlphaTest {

    @Test
    fun scrimFollowsTheCardFlight() {
        assertEquals(0f, cardScrimAlpha(0f))
        assertEquals(0.5f, cardScrimAlpha(0.5f))
        assertEquals(1f, cardScrimAlpha(3f))
        assertEquals(1f, cardScrimAlpha(4.9f))
        assertEquals(0.5f, cardScrimAlpha(5.5f))
        assertEquals(0f, cardScrimAlpha(6f))
    }
}
