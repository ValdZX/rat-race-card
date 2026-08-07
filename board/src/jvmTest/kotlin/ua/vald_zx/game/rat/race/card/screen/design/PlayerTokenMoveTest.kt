package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
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
import ua.vald_zx.game.rat.race.card.shared.PlayerCard
import ua.vald_zx.game.rat.race.card.shared.PlayerLocation
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
class PlayerTokenMoveTest {

    private val boardSize = DpSize(960.dp, 700.dp)

    @AfterTest
    fun reset() {
        players.value = emptyList()
    }

    @Test
    fun theTokenFollowsTheNewPositionAfterARoll() = runComposeUiTest {
        val layout = calculateBoardLayout(boardSize, isVertical = false)!!
        val route = layout.innerRoute
        players.value = listOf(player(position = 1, trackId = route.trackId))

        setContent {
            InitPreviewWithVm { vm ->
                BottomSheetNavigator {
                    AppTheme(forceDark = true) {
                        val focus = rememberCellFocus()
                        val bubble = rememberTokenBubbleState()
                        Box(
                            Modifier.size(boardSize)
                                .background(Design.scaffold.background)
                                .testTag("board")
                        ) {
                            DesignPlayerTokens(vm = vm, layout = route, focus = focus, bubble = bubble)
                        }
                    }
                }
            }
        }
        waitForIdle()
        mainClock.advanceTimeBy(1_000)
        waitForIdle()
        val before = onNodeWithTag("player-token-rival", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot

        players.value = listOf(player(position = 5, trackId = route.trackId))
        waitForIdle()
        mainClock.advanceTimeBy(2_000)
        waitForIdle()
        val after = onNodeWithTag("player-token-rival", useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot

        assertTrue(
            before.left != after.left || before.top != after.top,
            "фішка не переїхала після зміни позиції: before=$before after=$after",
        )
    }

    private fun player(position: Int, trackId: ua.vald_zx.game.rat.race.card.shared.TrackId) = Player(
        id = "rival",
        boardId = "b",
        attrs = PlayerAttributes(0xFF3355AA, 0),
        card = PlayerCard(name = "Olena", profession = "Engineer", salary = 4900),
        location = PlayerLocation(position = position, trackId = trackId),
    )
}
