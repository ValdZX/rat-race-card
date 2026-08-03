package ua.vald_zx.game.rat.race.card.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DesignIconDriftTest {

    private val cycleMillis = 90_000L

    @Test
    fun patternLooksTheSameAtTheStartAndEndOfACycle() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent {
            AppTheme(forceDark = true) {
                Box(
                    Modifier
                        .size(420.dp, 320.dp)
                        .background(Design.scaffold.background)
                        .testTag("drift")
                ) {
                    DesignIconDrift(Modifier.matchParentSize())
                }
            }
        }
        waitForIdle()
        val start = capture("build/design-icon-drift.png")

        mainClock.advanceTimeBy(cycleMillis)
        waitForIdle()
        val looped = capture("build/design-icon-drift-looped.png")

        assertTrue(
            difference(start, looped) < 0.01f,
            "візерунок стрибає на межі циклу — перехід буде помітний",
        )
    }

    @Test
    fun patternActuallyMovesWithinACycle() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent {
            AppTheme(forceDark = true) {
                Box(
                    Modifier
                        .size(420.dp, 320.dp)
                        .background(Design.scaffold.background)
                        .testTag("drift")
                ) {
                    DesignIconDrift(Modifier.matchParentSize())
                }
            }
        }
        waitForIdle()
        val start = capture("build/design-icon-drift-t0.png")

        mainClock.advanceTimeBy(cycleMillis / 4)
        waitForIdle()
        val moved = capture("build/design-icon-drift-t1.png")

        assertTrue(difference(start, moved) > 0.02f, "фон не рухається")
    }

    private fun androidx.compose.ui.test.ComposeUiTest.capture(target: String): BufferedImage {
        val image = onNodeWithTag("drift").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File(target))
        return image
    }

    private fun difference(first: BufferedImage, second: BufferedImage): Float {
        var changed = 0
        for (y in 0 until first.height) {
            for (x in 0 until first.width) {
                if (first.getRGB(x, y) != second.getRGB(x, y)) changed++
            }
        }
        return changed.toFloat() / (first.width * first.height)
    }
}
