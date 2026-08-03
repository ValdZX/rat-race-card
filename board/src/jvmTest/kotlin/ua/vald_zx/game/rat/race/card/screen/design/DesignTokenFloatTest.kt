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
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.screen.board.calculateBoardLayout
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.shared.PlayerLocation
import ua.vald_zx.game.rat.race.card.shared.moveTo
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.math.hypot
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
class DesignTokenFloatTest {

    private val board = DpSize(960.dp, 700.dp)

    private companion object {
        const val NEIGHBOUR_COLOR = 0xFFAA3355
    }

    @AfterTest
    fun reset() {
        players.value = emptyList()
    }

    @Test
    fun tokenFloatsAsideAndStaysClickable() = runComposeUiTest {
        val layout = showBoard()
        val place = hoveredPlace(layout)
        waitForIdle()
        val before = tokenBounds()

        val hoverPoint = Offset(
            x = (board.width - layout.innerRoute.size.width).value / 2 +
                    place.place.offset.x.value + 4f,
            y = (board.height - layout.innerRoute.size.height).value / 2 +
                    place.place.offset.y.value + place.place.size.height.value / 2,
        )
        onNodeWithTag("board").performMouseInput { moveTo(hoverPoint) }
        waitForIdle()
        val after = tokenBounds()

        capture()
        assertTrue(after != before, "фішка не відпливла з розкритої клітинки")
        assertTrue(after.top >= 0.dp && after.bottom <= board.height, "фішка виїхала за дошку")

        val token = Offset(
            x = (after.left + after.right).value / 2,
            y = (after.top + after.bottom).value / 2,
        )
        onNodeWithTag("board").performMouseInput { moveTo(token) }
        waitForIdle()
        onNodeWithText("Bankruptcy", useUnmergedTree = true).assertExists()
        assertEquals(after, tokenBounds(), "фішка поїхала з-під вказівника")
    }

    private fun ComposeUiTest.showBoard() =
        calculateBoardLayout(board, isVertical = false)!!.also { layout ->
            val standsOn = layout.innerRoute.places.first { it.place.type.name == "Bankruptcy" }
            players.value = listOf(
                Player(
                    id = "2",
                    boardId = "b",
                    attrs = PlayerAttributes(0xFF3355AA, 0),
                    location = PlayerLocation(position = standsOn.index, level = layout.innerRoute.layer.level),
                )
            )
            setContent {
                InitPreviewWithVm { vm ->
                    BottomSheetNavigator {
                        val focus = rememberCellFocus()
                        Box(
                            Modifier.size(board)
                                .background(Design.scaffold.background)
                                .cellFocusTracking(listOf(layout.innerRoute), focus)
                                .testTag("board")
                        ) {
                            DesignTrackForTest(layout.innerRoute, CellSurface.Tile, focus) {
                                DesignPlayerTokens(vm = vm, layout = layout.innerRoute, focus = focus)
                            }
                        }
                    }
                }
            }
            waitForIdle()
        }

    private fun hoveredPlace(layout: ua.vald_zx.game.rat.race.card.screen.board.BoardLayout) =
        layout.innerRoute.places.first {
            it.index == moveTo(
                layout.innerRoute.places.first { p -> p.place.type.name == "Bankruptcy" }.index,
                layout.innerRoute.layer.cellCount,
                layout.innerRoute.route.offset,
            )
        }

    @Test
    fun neighbourTokenDoesNotCoverTheLabel() = runComposeUiTest {
        val layout = calculateBoardLayout(board, isVertical = false)!!
        val target = layout.innerRoute.places.first { it.place.type.name == "Bankruptcy" }
        val neighbour = layout.innerRoute.places.first { it.index == target.index + 1 }
        players.value = listOf(neighbour.index).map { position ->
            Player(
                id = "3",
                boardId = "b",
                attrs = PlayerAttributes(NEIGHBOUR_COLOR, 0),
                location = PlayerLocation(position = position, level = layout.innerRoute.layer.level),
            )
        }
        setContent {
            InitPreviewWithVm { vm ->
                BottomSheetNavigator {
                    val focus = rememberCellFocus()
                    Box(
                        Modifier.size(board)
                            .background(Design.scaffold.background)
                            .cellFocusTracking(listOf(layout.innerRoute), focus)
                            .testTag("board")
                    ) {
                        DesignTrackForTest(layout.innerRoute, CellSurface.Tile, focus) {
                            DesignPlayerTokens(vm = vm, layout = layout.innerRoute, focus = focus)
                        }
                    }
                }
            }
        }
        waitForIdle()
        val tokenHome = tokenBounds("3")

        val shown = layout.innerRoute.places.first {
            it.index == moveTo(target.index, layout.innerRoute.layer.cellCount, layout.innerRoute.route.offset)
        }
        onNodeWithTag("board").performMouseInput {
            moveTo(
                Offset(
                    x = (board.width - layout.innerRoute.size.width).value / 2 + shown.place.offset.x.value + 4f,
                    y = (board.height - layout.innerRoute.size.height).value / 2 +
                            shown.place.offset.y.value + shown.place.size.height.value / 2,
                )
            )
        }
        waitForIdle()

        assertTrue(
            tokenBounds("3") != tokenHome,
            "сусідня фішка лишилась на місці й накриває підпис",
        )
    }

    @Test
    fun tokenOnAVerticalStripClearsTheOpenedCell() = runComposeUiTest {
        val layout = calculateBoardLayout(board, isVertical = false)!!
        val route = layout.innerRoute
        val standsOn = route.places.first {
            !it.place.location.side.isHorizontal && it.place.type.name == "Shopping"
        }
        players.value = listOf(
            Player(
                id = "5",
                boardId = "b",
                attrs = PlayerAttributes(NEIGHBOUR_COLOR, 0),
                location = PlayerLocation(position = standsOn.index, level = route.layer.level),
            )
        )
        setContent {
            InitPreviewWithVm { vm ->
                BottomSheetNavigator {
                    val focus = rememberCellFocus()
                    Box(
                        Modifier.size(board)
                            .background(Design.scaffold.background)
                            .cellFocusTracking(listOf(route), focus)
                            .testTag("board")
                    ) {
                        DesignTrackForTest(route, CellSurface.Tile, focus) {
                            DesignPlayerTokens(vm = vm, layout = route, focus = focus)
                        }
                    }
                }
            }
        }
        waitForIdle()

        val shown = route.places.first {
            it.index == moveTo(standsOn.index, route.layer.cellCount, route.route.offset)
        }
        onNodeWithTag("board").performMouseInput {
            moveTo(
                Offset(
                    x = (board.width - route.size.width).value / 2 +
                            shown.place.offset.x.value + shown.place.size.width.value / 2,
                    y = (board.height - route.size.height).value / 2 +
                            shown.place.offset.y.value + shown.place.size.height.value / 2,
                )
            )
        }
        mainClock.advanceTimeBy(600)
        waitForIdle()

        val label = onNodeWithText("Shopping", useUnmergedTree = true).getBoundsInRoot()
        val token = tokenBounds("5")
        assertTrue(
            token.left >= label.right || token.right <= label.left ||
                    token.top >= label.bottom || token.bottom <= label.top,
            "фішка $token накрила підпис розкритої клітинки $label",
        )
    }

    @Test
    fun everyTokenStepsAwayFromTheBoardCentre() {
        val layout = calculateBoardLayout(board, isVertical = false)!!
        listOf(layout.outerRoute, layout.innerRoute).forEach { route ->
            val centreX = route.size.width / 2
            val centreY = route.size.height / 2
            route.places.forEach { (index, place) ->
                val float = tokenFloat(route, place, expandedCellBox(route))
                val fromX = place.offset.x + place.size.width / 2 - centreX
                val fromY = place.offset.y + place.size.height / 2 - centreY
                val moved = hypot((fromX + float.x).value, (fromY + float.y).value)
                assertTrue(
                    moved > hypot(fromX.value, fromY.value),
                    "клітинка $index на ${route.layer}: фішка пішла до центру дошки, а не назовні",
                )
            }
        }
    }

    @Test
    fun tokensOnOneCellFormOneRow() {
        val layout = calculateBoardLayout(board, isVertical = false)!!
        listOf(layout.outerRoute, layout.innerRoute).forEach { route ->
            route.places.forEach { (placeIndex, place) ->
                val offsets = List(5) { index ->
                    expandedTokenOffset(route, place, index, 5, expandedCellBox(route))
                }
                if (place.location.side.isHorizontal) {
                    assertEquals(1, offsets.map { it.second }.distinct().size)
                    assertTrue(offsets.zipWithNext().all { (first, second) -> first.first < second.first })
                } else {
                    assertEquals(1, offsets.map { it.first }.distinct().size)
                    assertTrue(offsets.zipWithNext().all { (first, second) -> first.second < second.second })
                }
                assertEquals(5, offsets.distinct().size, "клітинка $placeIndex на ${route.layer}")
            }
        }
    }

    @Test
    fun tokensStayPutOnTheInactiveRing() = runComposeUiTest {
        val layout = calculateBoardLayout(board, isVertical = false)!!
        val standsOn = layout.outerRoute.places.first { it.place.type.name == "Bankruptcy" }
        players.value = listOf(
            Player(
                id = "4",
                boardId = "b",
                attrs = PlayerAttributes(NEIGHBOUR_COLOR, 0),
                location = PlayerLocation(position = standsOn.index, level = layout.outerRoute.layer.level),
            )
        )
        setContent {
            InitPreviewWithVm { vm ->
                BottomSheetNavigator {
                    val focus = rememberCellFocus()
                    Box(
                        Modifier.size(board)
                            .background(Design.scaffold.background)
                            .cellFocusTracking(listOf(layout.outerRoute), focus)
                            .testTag("board")
                    ) {
                        DesignTrackForTest(layout.outerRoute, CellSurface.Engraved, focus) {
                            DesignPlayerTokens(vm = vm, layout = layout.outerRoute, focus = focus)
                        }
                    }
                }
            }
        }
        waitForIdle()
        val home = tokenBounds("4")

        val shown = layout.outerRoute.places.first {
            it.index == moveTo(standsOn.index, layout.outerRoute.layer.cellCount, layout.outerRoute.route.offset)
        }
        onNodeWithTag("board").performMouseInput {
            moveTo(
                Offset(
                    x = (board.width - layout.outerRoute.size.width).value / 2 +
                            shown.place.offset.x.value + shown.place.size.width.value / 2,
                    y = (board.height - layout.outerRoute.size.height).value / 2 +
                            shown.place.offset.y.value + shown.place.size.height.value / 2,
                )
            )
        }
        mainClock.advanceTimeBy(600)
        waitForIdle()

        assertEquals(home, tokenBounds("4"), "фішка рушила через наведення на неактивне коло")
    }

    @Test
    fun hoveringTheTokenItselfSettlesInsteadOfBlinking() = runComposeUiTest {
        val layout = showBoard()
        waitForIdle()
        val home = tokenBounds()

        val center = Offset(
            x = (home.left + home.right).value / 2,
            y = (home.top + home.bottom).value / 2,
        )
        onNodeWithTag("board").performMouseInput { moveTo(center) }
        mainClock.advanceTimeBy(600)
        waitForIdle()
        val settled = tokenBounds()

        mainClock.advanceTimeBy(600)
        waitForIdle()

        onNodeWithText("Bankruptcy", useUnmergedTree = true).assertExists()
        assertEquals(settled, tokenBounds(), "фішка й далі стрибає — саме з цього починалось блимання")
    }

    private fun ComposeUiTest.tokenBounds(playerId: String = "2") =
        onNodeWithTag("player-token-$playerId").getBoundsInRoot()

    private fun ComposeUiTest.capture() {
        val image = onNodeWithTag("board").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-token-float.png"))
    }
}
