package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class StartCellInflationRenderTest {

    @Test
    fun theRateShowsInBothCollapsedAndExpandedStart() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Row(Modifier.background(Design.scaffold.background).padding(12.dp).testTag("cells")) {
                    Box(Modifier.size(56.dp).padding(2.dp)) {
                        DesignPlaceCell(type = PlaceType.Start, inflationPercent = 5)
                    }
                    Box(Modifier.size(140.dp, 56.dp).padding(2.dp)) {
                        DesignPlaceCell(
                            type = PlaceType.Start,
                            label = "Старт",
                            expanded = true,
                            inflationPercent = 5,
                        )
                    }
                }
            }
        }
        waitForIdle()

        onAllNodesWithText("5% inf").assertCountEquals(2)
        onNodeWithTag("cells").assertIsDisplayed()
        val image = onNodeWithTag("cells").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-start-inflation.png"))
    }

    @Test
    fun disabledInflationLeavesTheStartCellClean() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(56.dp).background(Design.scaffold.background)) {
                    DesignPlaceCell(type = PlaceType.Start, inflationPercent = null)
                }
            }
        }
        waitForIdle()

        onAllNodesWithText("5% inf").assertCountEquals(0)
    }
}
