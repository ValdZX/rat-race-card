package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.screen.board.BoardLayout
import ua.vald_zx.game.rat.race.card.screen.board.calculateBoardLayout
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.shared.PlayerLocation
import ua.vald_zx.game.rat.race.card.shared.PlayerSpeech
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import kotlin.math.abs
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
class DesignSpeechBubbleTest {

    private val boardSize = DpSize(960.dp, 700.dp)
    private val layout: BoardLayout = calculateBoardLayout(boardSize, isVertical = false)!!
    private val route = layout.innerRoute
    private val standsOn = route.places.first { it.place.type.name == "Bankruptcy" }
    private val playerId = "2"

    private val board = Board(
        id = "b",
        name = "b",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        playerIds = setOf(playerId),
    )

    private val player = Player(
        id = playerId,
        boardId = "b",
        attrs = PlayerAttributes(0xFF3355AA, 0),
        location = PlayerLocation(position = standsOn.index, level = route.layer.level),
    )

    @AfterTest
    fun reset() {
        players.value = emptyList()
    }

    @Test
    fun theSpeechSitsAboveTheToken() = runComposeUiTest {
        showBoard()
        assertAboveToken(bubble(), token())
    }

    @Test
    fun theSpeechFollowsTheTokenOutOfItsCell() = runComposeUiTest {
        showBoard()
        val parked = token()

        hoverOwnCell()
        val stepped = token()
        assertTrue(
            stepped != parked,
            "фішка не відсунулась від розкритої клітинки — перевірка нічого не доводить",
        )
        assertAboveToken(bubble(), stepped)
    }

    private fun assertAboveToken(bubble: DpRect, token: DpRect) {
        assertTrue(
            bubble.bottom <= token.top + 1.dp,
            "хмарка $bubble не над фішкою $token",
        )
        val bubbleCenter = (bubble.left + bubble.right).value / 2
        val tokenCenter = (token.left + token.right).value / 2
        assertTrue(
            abs(bubbleCenter - tokenCenter) < 2f,
            "хмарка не над фішкою по горизонталі: $bubbleCenter проти $tokenCenter",
        )
    }

    private fun ComposeUiTest.token(): DpRect =
        onNodeWithTag("player-token-$playerId", useUnmergedTree = true).getUnclippedBoundsInRoot()

    private fun ComposeUiTest.bubble(): DpRect =
        onNodeWithTag(speechBubbleTag(playerId), useUnmergedTree = true).getUnclippedBoundsInRoot()

    private fun ComposeUiTest.hoverOwnCell() {
        val place = standsOn.place
        val point = Offset(
            x = (boardSize.width - route.size.width).value / 2 + place.offset.x.value + 4f,
            y = (boardSize.height - route.size.height).value / 2 +
                    place.offset.y.value + place.size.height.value / 2,
        )
        onNodeWithTag("board").performMouseInput { moveTo(point) }
        waitForIdle()
    }

    private fun ComposeUiTest.showBoard() {
        players.value = listOf(player)
        val vm = BoardViewModel(board, player, { error("офлайн-тест") })
        setContent {
            BottomSheetNavigator {
                AppTheme(forceDark = true) {
                    val focus = rememberCellFocus()
                    Box(
                        Modifier.size(boardSize)
                            .background(Design.scaffold.background)
                            .cellFocusTracking(listOf(route), focus)
                            .testTag("board")
                    ) {
                        DesignTrackForTest(route, CellSurface.Tile, focus) {
                            DesignPlayerTokens(vm, route, focus, rememberTokenBubbleState())
                        }
                        DesignPlayerMessages(vm = vm, layout = route, focus = focus)
                    }
                }
            }
        }
        waitForIdle()
        vm.showSpeech(playerId, PlayerSpeech("Привіт", Long.MAX_VALUE))
        waitForIdle()
    }
}
