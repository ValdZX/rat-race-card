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
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onLast
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
            x = (board.width - layout.outerRoute.size.width).value / 2 +
                    place.place.offset.x.value + 4f,
            y = (board.height - layout.outerRoute.size.height).value / 2 +
                    place.place.offset.y.value + place.place.size.height.value / 2,
        )
        onNodeWithTag("board").performMouseInput { moveTo(hoverPoint) }
        waitForIdle()
        val after = tokenBounds()

        capture()
        assertTrue(after.top != before.top, "фішка не відпливла з розкритої клітинки")
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
            val standsOn = layout.outerRoute.places.first { it.place.type.name == "Bankruptcy" }
            players.value = listOf(
                Player(
                    id = "2",
                    boardId = "b",
                    attrs = PlayerAttributes(0xFF3355AA, 0),
                    location = PlayerLocation(position = standsOn.index, level = layout.outerRoute.layer.level),
                )
            )
            setContent {
                InitPreviewWithVm { vm ->
                    BottomSheetNavigator {
                        Box(Modifier.size(board).background(Design.scaffold.background).testTag("board")) {
                            val focus = rememberCellFocus()
                            DesignTrackForTest(layout.outerRoute, CellSurface.Tile, focus)
                            DesignPlayerTokens(vm = vm, layout = layout.outerRoute, focus = focus)
                        }
                    }
                }
            }
            waitForIdle()
        }

    private fun hoveredPlace(layout: ua.vald_zx.game.rat.race.card.screen.board.BoardLayout) =
        layout.outerRoute.places.first {
            it.index == moveTo(
                layout.outerRoute.places.first { p -> p.place.type.name == "Bankruptcy" }.index,
                layout.outerRoute.layer.cellCount,
                layout.outerRoute.route.offset,
            )
        }

    @Test
    fun neighbourTokenDoesNotCoverTheLabel() = runComposeUiTest {
        val layout = calculateBoardLayout(board, isVertical = false)!!
        val target = layout.outerRoute.places.first { it.place.type.name == "Bankruptcy" }
        val neighbour = layout.outerRoute.places.first { it.index == target.index + 1 }
        players.value = listOf(neighbour.index).map { position ->
            Player(
                id = "3",
                boardId = "b",
                attrs = PlayerAttributes(NEIGHBOUR_COLOR, 0),
                location = PlayerLocation(position = position, level = layout.outerRoute.layer.level),
            )
        }
        setContent {
            InitPreviewWithVm { vm ->
                BottomSheetNavigator {
                    Box(Modifier.size(board).background(Design.scaffold.background).testTag("board")) {
                        val focus = rememberCellFocus()
                        DesignTrackForTest(layout.outerRoute, CellSurface.Tile, focus)
                        DesignPlayerTokens(vm = vm, layout = layout.outerRoute, focus = focus)
                    }
                }
            }
        }
        waitForIdle()
        val tokenHome = tokenBounds()

        val shown = layout.outerRoute.places.first {
            it.index == moveTo(target.index, layout.outerRoute.layer.cellCount, layout.outerRoute.route.offset)
        }
        onNodeWithTag("board").performMouseInput {
            moveTo(
                Offset(
                    x = (board.width - layout.outerRoute.size.width).value / 2 + shown.place.offset.x.value + 4f,
                    y = (board.height - layout.outerRoute.size.height).value / 2 +
                            shown.place.offset.y.value + shown.place.size.height.value / 2,
                )
            )
        }
        waitForIdle()

        assertTrue(
            tokenBounds().top != tokenHome.top,
            "сусідня фішка лишилась на місці й накриває підпис",
        )
    }

    @Test
    fun hoveringTheTokenItselfKeepsItUnderTheCursor() = runComposeUiTest {
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

        onNodeWithText("Bankruptcy", useUnmergedTree = true).assertExists()
        assertEquals(home, tokenBounds(), "фішка втекла з-під курсора — саме з цього починалось блимання")
    }

    private fun ComposeUiTest.tokenBounds() =
        onAllNodes(hasClickAction()).onLast().getBoundsInRoot()

    private fun ComposeUiTest.capture() {
        val image = onNodeWithTag("board").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-token-float.png"))
    }
}
