package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.screen.board.calculateBoardLayout
import ua.vald_zx.game.rat.race.card.shared.Business
import ua.vald_zx.game.rat.race.card.shared.BusinessType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.shared.PlayerCard
import ua.vald_zx.game.rat.race.card.shared.PlayerLocation
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
class DesignTokenBubbleTest {

    private val board = DpSize(960.dp, 700.dp)

    private companion object {
        const val SINGLE_CLICK_WINDOW_MS = 350L
    }

    @AfterTest
    fun reset() {
        players.value = emptyList()
    }

    @Test
    fun tappingAnotherPlayerShowsProfileAndBothActions() = runComposeUiTest {
        showBoard(ownPlayer = false)
        tapToken("rival")

        onNodeWithText("Olena", useUnmergedTree = true).assertExists()
        assertEquals(2, onAllNodesWithText("Engineer", useUnmergedTree = true).fetchSemanticsNodes().size)
        onNodeWithText("Send", useUnmergedTree = true).assertExists()
        onNodeWithText("Send message", useUnmergedTree = true).assertExists()

        capture("build/design-token-bubble-rival.png")
    }

    @Test
    fun tappingOwnTokenSaysItIsYouAndOffersOnlyMessaging() = runComposeUiTest {
        showBoard(ownPlayer = true)
        tapToken("")

        onNodeWithText("YOU", useUnmergedTree = true).assertExists()
        onNodeWithText("Send message", useUnmergedTree = true).assertExists()
        onNodeWithText("Send", useUnmergedTree = true).assertDoesNotExist()

        capture("build/design-token-bubble-own.png")
    }

    @Test
    fun firedPlayerShowsStatusInsteadOfProfession() = runComposeUiTest {
        showBoard(ownPlayer = false, businesses = emptyList())
        tapToken("rival")

        onNodeWithText("Olena", useUnmergedTree = true).assertExists()
        assertEquals(2, onAllNodesWithText("Unemployed", useUnmergedTree = true).fetchSemanticsNodes().size)
        onNodeWithText("Engineer", useUnmergedTree = true).assertDoesNotExist()

        capture("build/design-token-bubble-fired.png")
    }

    @Test
    fun tappingTheTokenAgainClosesTheBubble() = runComposeUiTest {
        showBoard(ownPlayer = false)

        tapToken("rival")
        onNodeWithText("Olena", useUnmergedTree = true).assertExists()

        Thread.sleep(SINGLE_CLICK_WINDOW_MS)
        tapToken("rival")
        onNodeWithText("Olena", useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun tappingTheBoardClosesTheBubble() = runComposeUiTest {
        showBoard(ownPlayer = false)

        tapToken("rival")
        onNodeWithText("Olena", useUnmergedTree = true).assertExists()

        onNodeWithTag("board").performMouseInput {
            moveTo(Offset(board.width.value / 2, board.height.value / 2))
            press()
            release()
        }
        mainClock.advanceTimeBy(400)
        waitForIdle()
        onNodeWithText("Olena", useUnmergedTree = true).assertDoesNotExist()
    }

    private fun ComposeUiTest.tapToken(playerId: String) {
        onNodeWithTag("player-token-$playerId", useUnmergedTree = true).performClick()
        mainClock.advanceTimeBy(400)
        waitForIdle()
    }

    private fun ComposeUiTest.showBoard(ownPlayer: Boolean, businesses: List<Business> = defaultJob()) {
        val layout = calculateBoardLayout(board, isVertical = false)!!
        val route = layout.innerRoute
        val standsOn = route.places.first { it.place.type.name == "Bankruptcy" }
        players.value = listOf(
            Player(
                id = if (ownPlayer) "" else "rival",
                boardId = "b",
                attrs = PlayerAttributes(0xFF3355AA, 0),
                card = PlayerCard(name = "Olena", profession = "Engineer", salary = 4900),
                businesses = businesses,
                location = PlayerLocation(position = standsOn.index, trackId = route.trackId),
            )
        )
        setContent {
            InitPreviewWithVm { vm ->
                BottomSheetNavigator {
                    AppTheme(forceDark = true) {
                        val focus = rememberCellFocus()
                        val bubble = rememberTokenBubbleState()
                        Box(
                            Modifier.size(board)
                                .background(Design.scaffold.background)
                                .cellFocusTracking(listOf(route), focus)
                                .testTag("board")
                        ) {
                            DesignTrackForTest(route, CellSurface.Tile, focus) {
                                DesignPlayerTokens(vm = vm, layout = route, focus = focus, bubble = bubble)
                            }
                            TokenBubbleScrim(bubble)
                            DesignTokenBubbles(vm = vm, layout = route, focus = focus, bubble = bubble)
                        }
                    }
                }
            }
        }
        waitForIdle()
    }

    private fun defaultJob(): List<Business> =
        listOf(Business(BusinessType.WORK, "Engineer", price = 0, profit = 4900))

    private fun ComposeUiTest.capture(target: String) {
        val image = onNodeWithTag("board").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File(target))
    }
}
