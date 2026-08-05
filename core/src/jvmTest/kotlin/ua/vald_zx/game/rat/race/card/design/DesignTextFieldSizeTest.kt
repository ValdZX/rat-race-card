package ua.vald_zx.game.rat.race.card.design

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DesignTextFieldSizeTest {

    private val screenHeight = 800.dp

    @Test
    fun multilineFieldWrapsItsLinesInsteadOfFillingTheScreen() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Column(Modifier.size(400.dp, screenHeight)) {
                    DesignTextField(
                        value = "Привіт",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().testTag("field"),
                        singleLine = false,
                        maxLines = 3,
                    )
                }
            }
        }
        waitForIdle()

        val bounds = onNodeWithTag("field").getBoundsInRoot()
        val height = bounds.bottom - bounds.top
        assertTrue(
            height < screenHeight / 3,
            "поле розтягнулось на $height замість висоти під три рядки",
        )
    }

    @Test
    fun explicitFieldHeightIsUsedForCompactCardInputs() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Column(Modifier.size(400.dp, screenHeight)) {
                    DesignTextField(
                        value = "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().testTag("compact-field"),
                        fieldHeight = 34.dp,
                    )
                }
            }
        }
        waitForIdle()

        val bounds = onNodeWithTag("compact-field").getBoundsInRoot()
        assertEquals(34.dp, bounds.bottom - bounds.top)
    }
}
