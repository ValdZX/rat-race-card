package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SecretUnlockTest {
    @Test
    fun fiveTapsThenALongPressUnlocks() {
        val secret = SecretUnlock()

        repeat(5) { secret.tap() }
        secret.longPress()

        assertTrue(secret.unlocked)
    }

    @Test
    fun aLongPressAloneDoesNothing() {
        val secret = SecretUnlock()

        secret.longPress()

        assertFalse(secret.unlocked)
    }

    @Test
    fun tooFewTapsDoNotUnlockAndTheCounterResets() {
        val secret = SecretUnlock()

        repeat(4) { secret.tap() }
        secret.longPress()
        assertFalse(secret.unlocked)

        secret.tap()
        secret.longPress()
        assertFalse(secret.unlocked, "лічильник має скинутись після невдалої спроби")
    }

    @Test
    fun tapsAloneNeverUnlock() {
        val secret = SecretUnlock()

        repeat(20) { secret.tap() }

        assertFalse(secret.unlocked)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun theGestureOnTheStartFlagUnlocksAndStillForwardsTaps() = runComposeUiTest {
        val secret = SecretUnlock()
        var taps = 0
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(72.dp).background(Design.scaffold.background).testTag("start")) {
                    DesignPlaceCell(
                        type = PlaceType.Start,
                        secret = secret,
                        onClick = { taps++ },
                    )
                }
            }
        }
        waitForIdle()

        repeat(5) { onNodeWithTag("start").performClick() }
        waitForIdle()
        assertFalse(secret.unlocked, "самих натискань замало")
        assertEquals(5, taps, "звичайна дія клітинки має продовжувати працювати")

        onNodeWithTag("start").performTouchInput { longClick() }
        waitForIdle()

        assertTrue(secret.unlocked)
    }
}
