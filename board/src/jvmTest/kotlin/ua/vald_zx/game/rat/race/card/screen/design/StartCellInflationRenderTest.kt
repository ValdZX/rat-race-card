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
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class StartCellInflationRenderTest {

    @Test
    fun onlyTheExpandedStartCarriesTheLevel() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Row(Modifier.background(Design.scaffold.background).padding(20.dp).testTag("cells")) {
                    Box(Modifier.size(56.dp).padding(2.dp)) {
                        DesignPlaceCell(type = PlaceType.Start, inflationPercent = 21)
                    }
                    Box(Modifier.size(140.dp, 56.dp).padding(2.dp)) {
                        DesignPlaceCell(
                            type = PlaceType.Start,
                            label = "Старт",
                            expanded = true,
                            inflationPercent = 21,
                        )
                    }
                }
            }
        }
        waitForIdle()

        onAllNodesWithText("21% inf").assertCountEquals(1)
        onNodeWithTag("cells").assertIsDisplayed()
        val image = onNodeWithTag("cells").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-start-inflation.png"))
    }

    @Test
    fun theBadgeHangsOffTheTopLeftCornerWithoutCoveringTheIcon() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(200.dp).background(Design.scaffold.background).padding(40.dp)) {
                    Box(Modifier.size(120.dp, 56.dp).testTag("body")) {
                        DesignPlaceCell(
                            type = PlaceType.Start,
                            label = "Старт",
                            expanded = true,
                            inflationPercent = 21,
                        )
                    }
                }
            }
        }
        waitForIdle()

        val badge = onNodeWithText("21% inf").fetchSemanticsNode().boundsInRoot
        val body = onNodeWithTag("body").fetchSemanticsNode().boundsInRoot

        assertTrue(
            badge.top < body.top && badge.left < body.left,
            "бейдж має висіти в лівому верхньому розі клітинки: badge=$badge body=$body",
        )
        assertTrue(
            badge.bottom < body.center.y,
            "бейдж не має перекривати вміст клітинки: badge=$badge body=$body",
        )
    }

    @Test
    fun theWholeLabelFitsInsteadOfBeingClipped() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(200.dp).background(Design.scaffold.background).padding(40.dp)) {
                    Box(Modifier.size(60.dp, 40.dp)) {
                        DesignPlaceCell(
                            type = PlaceType.Start,
                            label = "Старт",
                            expanded = true,
                            inflationPercent = 133,
                        )
                    }
                }
            }
        }
        waitForIdle()

        val badge = onNodeWithText("133% inf").fetchSemanticsNode().boundsInRoot

        assertTrue(
            badge.width > 0f && badge.height > 0f,
            "бейдж не має схлопуватись на вузькій клітинці: badge=$badge",
        )
    }

    @Test
    fun disabledInflationLeavesTheStartCellClean() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(140.dp, 56.dp).background(Design.scaffold.background)) {
                    DesignPlaceCell(
                        type = PlaceType.Start,
                        label = "Старт",
                        expanded = true,
                        inflationPercent = null,
                    )
                }
            }
        }
        waitForIdle()

        onAllNodesWithText("0% inf").assertCountEquals(0)
    }
}
