package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DesignLoadOnlineRenderTest {

    @Test
    fun failedConnectionOffersRetry() = runComposeUiTest {
        var retries = 0
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(420.dp, 360.dp).testTag("failed")) {
                    DesignLoadOnline(failed = true, onRetry = { retries++ })
                }
            }
        }
        waitForIdle()

        onNodeWithText("Retry Connection").performClick()
        assertEquals(1, retries)

        val image = onNodeWithTag("failed").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-load-online-failed.png"))
    }

    @Test
    fun connectingStateNamesWhatItWaitsFor() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(420.dp, 360.dp).testTag("loading")) {
                    DesignLoadOnline(failed = false, onRetry = {})
                }
            }
        }
        waitForIdle()
        onNodeWithText("Connecting to the server").assertExists()

        val image = onNodeWithTag("loading").captureToImage().toAwtImage()
        ImageIO.write(image, "png", File("build/design-load-online.png"))
    }
}
