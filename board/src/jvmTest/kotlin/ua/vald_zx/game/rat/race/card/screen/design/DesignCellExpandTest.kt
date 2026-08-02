package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.screen.board.calculateBoardLayout
import ua.vald_zx.game.rat.race.card.screen.board.Place
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Клітинки на дошці мовчать, а підпис показує розкриття — наведенням курсора
 * або тапом. Розкрита клітинка мусить вмістити слово в один рядок.
 */
@OptIn(ExperimentalTestApi::class)
class DesignCellExpandTest {

    private val size = DpSize(360.dp, 420.dp)

    @Test
    fun cellsStaySilentUntilTouched() = runComposeUiTest {
        showBoard()
        onNodeWithText("Chance!").assertDoesNotExist()
    }

    @Test
    fun tapExpandsTheCellAndFitsTheLabelInOneLine() = runComposeUiTest {
        val layout = showBoard()
        val place = layout.innerRoute.places.first { it.place.type.name == "Chance" }.place
        val center = cellCenter(layout.innerRoute.size, place)

        onNodeWithTag("board").performClick() // тап повз клітинку нічого не робить
        onNodeWithText("Chance!").assertDoesNotExist()

        tapCell(center)
        waitForIdle()

        val bounds = onNodeWithText("Chance!", useUnmergedTree = true).getBoundsInRoot()
        val labelWidth = bounds.right - bounds.left
        val labelHeight = bounds.bottom - bounds.top
        assertTrue(
            labelWidth > place.size.width,
            "підпис $labelWidth мав не вміщатись у вихідну клітинку ${place.size.width} — " +
                    "саме тому вона й росте",
        )
        assertTrue(labelHeight < 24.dp, "підпис мав лягти в один рядок, а зайняв $labelHeight")

        capture("build/design-cell-expanded.png")
    }

    @Test
    fun hoverExpandsAndLeavingCollapses() = runComposeUiTest {
        val layout = showBoard()
        val place = layout.innerRoute.places.first { it.place.type.name == "Chance" }.place
        val center = cellCenter(layout.innerRoute.size, place)

        onNodeWithTag("board").performMouseInput { moveTo(center) }
        waitForIdle()
        onNodeWithText("Chance!").assertExists()

        onNodeWithTag("board").performMouseInput { moveTo(Offset(size.width.value / 2, size.height.value / 2)) }
        waitForIdle()
        onNodeWithText("Chance!").assertDoesNotExist()
    }

    /** Трек відцентрований у дошці, тому зсув клітинки треба перевести в корінь. */
    private fun cellCenter(trackSize: DpSize, place: Place) = Offset(
        x = (size.width - trackSize.width).value / 2 + place.offset.x.value + place.size.width.value / 2,
        y = (size.height - trackSize.height).value / 2 + place.offset.y.value + place.size.height.value / 2,
    )

    private fun ComposeUiTest.tapCell(point: Offset) {
        onNodeWithTag("board").performMouseInput {
            moveTo(point)
            press()
            release()
        }
    }

    private fun ComposeUiTest.showBoard() =
        calculateBoardLayout(size, isVertical = true)!!.also { layout ->
            setContent {
                AppTheme(forceDark = false) {
                    Box(Modifier.size(size).background(Design.scaffold.background).testTag("board")) {
                        DesignTrackForTest(layout.outerRoute, CellSurface.Engraved)
                        DesignTrackForTest(layout.innerRoute, CellSurface.Tile)
                    }
                }
            }
            waitForIdle()
        }

    private fun ComposeUiTest.capture(target: String) {
        val image = onNodeWithTag("board").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File(target))
    }
}
