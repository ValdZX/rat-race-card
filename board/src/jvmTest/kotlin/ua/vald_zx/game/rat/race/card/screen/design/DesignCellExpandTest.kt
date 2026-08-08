package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.Dream
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ua.vald_zx.game.rat.race.card.screen.board.calculateBoardLayout
import ua.vald_zx.game.rat.race.card.screen.board.Place
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

        onNodeWithTag("board").performClick()
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
        mainClock.advanceTimeBy(300)
        waitForIdle()
        onNodeWithText("Chance!").assertDoesNotExist()
    }

    @Test
    fun verticalAndHorizontalCellsExpandToTheSameBox() = runComposeUiTest {
        val layout = showBoard(liveOuter = true, board = DpSize(960.dp, 700.dp))
        val chances = layout.outerRoute.places.filter { it.place.type.name == "Chance" }
        val vertical = chances.first { it.place.isVertical }.place
        val horizontal = chances.first { !it.place.isVertical }.place

        tapCell(cellCenter(layout.outerRoute.size, vertical, DpSize(960.dp, 700.dp)))
        waitForIdle()
        val verticalBox = onNodeWithText("Chance!").getBoundsInRoot()
        val verticalSize = (verticalBox.right - verticalBox.left) to (verticalBox.bottom - verticalBox.top)

        tapCell(cellCenter(layout.outerRoute.size, horizontal, DpSize(960.dp, 700.dp)))
        waitForIdle()
        val horizontalBox = onNodeWithText("Chance!").getBoundsInRoot()
        val horizontalSize = (horizontalBox.right - horizontalBox.left) to (horizontalBox.bottom - horizontalBox.top)

        assertEquals(verticalSize, horizontalSize, "розкриті клітинки різного розміру")
    }

    @Test
    fun expandedLabelIsNotTruncatedOnALargeBoard() = runComposeUiTest {
        val board = DpSize(960.dp, 700.dp)
        val layout = calculateBoardLayout(board, isVertical = false)!!
        val place = layout.outerRoute.places.first { it.place.type.name == "Bankruptcy" }.place
        setContent {
            AppTheme(forceDark = false) {
                val focus = rememberCellFocus()
                Box(
                    Modifier.size(board)
                        .background(Design.scaffold.background)
                        .cellFocusTracking(listOf(layout.outerRoute), focus)
                        .testTag("board")
                ) {
                    DesignTrackForTest(layout.outerRoute, CellSurface.Tile, focus)
                    BasicText(
                        text = "\u200BBankruptcy",
                        style = expandedLabelStyle(),
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.testTag("reference"),
                    )
                }
            }
        }
        waitForIdle()
        val referenceBounds = onNodeWithTag("reference").getBoundsInRoot()
        val referenceWidth = referenceBounds.right - referenceBounds.left
        val referenceHeight = referenceBounds.bottom - referenceBounds.top

        tapCell(cellCenter(layout.outerRoute.size, place, board))
        waitForIdle()

        val labelBounds = onNodeWithText("Bankruptcy", useUnmergedTree = true).getBoundsInRoot()
        val shownWidth = labelBounds.right - labelBounds.left
        val shownHeight = labelBounds.bottom - labelBounds.top
        assertTrue(
            shownWidth >= referenceWidth,
            "підпис обрізано: показано $shownWidth, а повний напис $referenceWidth",
        )
        assertTrue(
            shownHeight > referenceHeight,
            "текст великої дошки не масштабується: $referenceHeight -> $shownHeight",
        )
    }

    @Test
    fun expandedIconIsNotSmallerThanCollapsed() = runComposeUiTest {
        val board = DpSize(960.dp, 700.dp)
        val layout = calculateBoardLayout(board, isVertical = false)!!
        val place = layout.outerRoute.places.first { it.place.type.name == "Bankruptcy" }.place
        setContent {
            AppTheme(forceDark = false) {
                val focus = rememberCellFocus()
                Box(
                    Modifier.size(board)
                        .background(Design.scaffold.background)
                        .cellFocusTracking(listOf(layout.outerRoute), focus)
                        .testTag("board")
                ) {
                    DesignTrackForTest(layout.outerRoute, CellSurface.Tile, focus)
                }
            }
        }
        waitForIdle()
        val collapsed = iconWidth("Bankruptcy")

        tapCell(cellCenter(layout.outerRoute.size, place, board))
        waitForIdle()
        val expanded = iconWidth("Bankruptcy")

        assertTrue(
            expanded >= collapsed,
            "знак зменшився при розкритті: було $collapsed, стало $expanded",
        )
    }

    private fun ComposeUiTest.iconWidth(label: String) =
        onAllNodesWithContentDescription(label, useUnmergedTree = true)
            .onFirst()
            .getBoundsInRoot()
            .let { it.right - it.left }

    @Test
    fun engravedRingDoesNotExpand() = runComposeUiTest {
        val layout = showBoard()
        val place = layout.outerRoute.places.first { it.place.type.name == "Chance" }.place
        val center = cellCenter(layout.outerRoute.size, place)

        onNodeWithTag("board").performMouseInput { moveTo(center) }
        waitForIdle()
        onNodeWithText("Chance!").assertDoesNotExist()

        tapCell(center)
        waitForIdle()
        onNodeWithText("Chance!").assertDoesNotExist()
    }

    @Test
    fun expandedCellStaysInsideTheTrack() = runComposeUiTest {
        val layout = showBoard()
        val place = layout.innerRoute.places
            .maxBy { it.place.offset.x.value }.place
        val center = cellCenter(layout.innerRoute.size, place)

        tapCell(center)
        waitForIdle()

        val trackLeft = (size.width - layout.innerRoute.size.width) / 2
        val bounds = onNodeWithTag("board").getBoundsInRoot()
        val label = onNodeWithText(place.type.name, useUnmergedTree = true, substring = true)
        val labelRight = label.getBoundsInRoot().right
        assertTrue(
            labelRight <= trackLeft + layout.innerRoute.size.width,
            "розкрита клітинка вилізла за трек: $labelRight > ${trackLeft + layout.innerRoute.size.width}",
        )
        assertTrue(labelRight <= bounds.right, "підпис виїхав за екран")
    }

    private fun cellCenter(trackSize: DpSize, place: Place, board: DpSize = size) = Offset(
        x = (board.width - trackSize.width).value / 2 + place.offset.x.value + place.size.width.value / 2,
        y = (board.height - trackSize.height).value / 2 + place.offset.y.value + place.size.height.value / 2,
    )

    private fun ComposeUiTest.tapCell(point: Offset) {
        onNodeWithTag("board").performMouseInput {
            moveTo(point)
            press()
            release()
        }
    }

    @Test
    fun waitingAmountShowsUpOnlyWhenTheCellOpens() = runComposeUiTest {
        var expanded by mutableStateOf(false)
        setContent {
            AppTheme(forceDark = false) {
                Box(Modifier.size(200.dp).background(Design.scaffold.background).testTag("board")) {
                    DesignPlaceCell(
                        type = PlaceType.Salary,
                        label = "Salary",
                        expanded = expanded,
                        waitingAmount = 3200,
                        modifier = Modifier.size(if (expanded) 140.dp else 40.dp, 60.dp),
                    )
                }
            }
        }
        waitForIdle()
        onNodeWithText(waitingAmountLabel(3_200), useUnmergedTree = true).assertDoesNotExist()

        expanded = true
        waitForIdle()
        onNodeWithText(waitingAmountLabel(3_200), useUnmergedTree = true).assertExists()
    }

    @Test
    fun expandedWaitingAmountKeepsTheFloatingOverlay() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = false) {
                Box(Modifier.size(240.dp, 120.dp)) {
                    DesignPlaceCell(
                        type = PlaceType.Salary,
                        label = "Salary",
                        expanded = true,
                        waitingAmount = 123_456_789,
                        modifier = Modifier.offset(20.dp, 20.dp).size(160.dp, 64.dp).testTag("cell"),
                    )
                }
            }
        }
        waitForIdle()

        val cell = onNodeWithTag("cell").getBoundsInRoot()
        val amount = onNodeWithText(waitingAmountLabel(123_456_789), useUnmergedTree = true).getBoundsInRoot()
        assertTrue(amount.top < cell.top)
        assertTrue(amount.right > cell.right)
    }

    @Test
    fun expandedDreamShowsTheWholeDescription() = runComposeUiTest {
        val board = DpSize(960.dp, 700.dp)
        val layout = calculateBoardLayout(board, isVertical = false)!!
        val place = layout.outerRoute.places.first { it.place.type is PlaceType.Desire }.place
        val dreamId = (place.type as PlaceType.Desire).dreamId
        val description = "Створити міжнародний освітній простір, де діти й дорослі вчаться, досліджують світ та запускають корисні проєкти разом."
        val dream = Dream(dreamId, "Відкрита академія майбутнього", description, 12_000_000)
        setContent {
            AppTheme(forceDark = false) {
                val focus = rememberCellFocus()
                Box(
                    Modifier.size(board)
                        .background(Design.scaffold.background)
                        .cellFocusTracking(listOf(layout.outerRoute), focus)
                        .testTag("board")
                ) {
                    DesignTrackForTest(
                        layout = layout.outerRoute,
                        surface = CellSurface.Tile,
                        focus = focus,
                        dreams = DreamCellContext(
                            dream = { dream },
                            selectors = { emptyList() },
                            canSelect = { true },
                            currentPlayerId = "player",
                            purchasedDreamIds = emptySet(),
                            onSelect = {},
                        ),
                    )
                }
            }
        }
        waitForIdle()

        tapCell(cellCenter(layout.outerRoute.size, place, board))
        waitForIdle()

        val descriptionBounds = onNodeWithText(description, useUnmergedTree = true).getBoundsInRoot()
        assertTrue(
            descriptionBounds.bottom - descriptionBounds.top > 24.dp,
            "опис мрії залишився обрізаним до двох рядків",
        )
        capture("build/design-dream-expanded.png")
    }

    private fun ComposeUiTest.showBoard(liveOuter: Boolean = false, board: DpSize = size) =
        calculateBoardLayout(board, isVertical = board.height > board.width)!!.also { layout ->
            setContent {
                AppTheme(forceDark = false) {
                    val focus = rememberCellFocus()
                    Box(
                        Modifier.size(board)
                            .background(Design.scaffold.background)
                            .cellFocusTracking(listOf(layout.innerRoute, layout.outerRoute), focus)
                            .testTag("board")
                    ) {
                        DesignTrackForTest(
                            layout.outerRoute,
                            if (liveOuter) CellSurface.Tile else CellSurface.Engraved,
                            focus,
                        )
                        DesignTrackForTest(
                            layout.innerRoute,
                            if (liveOuter) CellSurface.Engraved else CellSurface.Tile,
                            focus,
                        )
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
