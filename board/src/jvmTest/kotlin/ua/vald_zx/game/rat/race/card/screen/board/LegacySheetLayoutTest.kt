package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import com.composables.core.BottomSheet
import com.composables.core.rememberBottomSheetState
import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.Business
import ua.vald_zx.game.rat.race.card.shared.BusinessType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
class LegacySheetLayoutTest {

    private val board = Board(
        id = "b",
        name = "b",
        loanLimit = 10_000,
        businessLimit = 10,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
    )

    private val player = Player(
        id = "1",
        boardId = "b",
        attrs = PlayerAttributes(0xFF3355AA, 0),
        businesses = (1..6).map { index ->
            Business(BusinessType.SMALL, "Бізнес №$index", 9500L * index, 380L * index)
        },
    )

    private val phone = Pair(400.dp, 800.dp)
    private val settingsLabel = "Online game settings"
    private val density = androidx.compose.ui.unit.Density(1f)

    private fun statusBar(height: androidx.compose.ui.unit.Dp) {
        statusBarHeightState.value = height
        navigationBarHeightState.value = 24.dp
        sheetContentSize.value = 0.dp
    }

    private fun ComposeUiTest.showSheet() {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(phone.first, phone.second).testTag("screen")) {
                    BottomSheetNavigator {
                        val sheetState = rememberBottomSheetState(
                            initialDetent = ContentExpanded,
                            detents = listOf(HalfExpanded, ContentExpanded),
                        )
                        BottomSheet(state = sheetState, modifier = Modifier.fillMaxSize()) {
                            PlayerSheetContainer(sheetState) {
                                LegacyPlayerSheet(
                                    vm = BoardViewModel(board, player, { error("офлайн-тест") }),
                                    scaffoldState = sheetState,
                                )
                            }
                        }
                    }
                }
            }
        }
        waitForIdle()
    }

    @Test
    fun theSettingsButtonStaysOnScreenOnAPhone() = runComposeUiTest {
        statusBar(44.dp)
        showSheet()

        val settings = onNodeWithContentDescription(settingsLabel)
        val visible = settings.getBoundsInRoot()
        val whole = settings.getUnclippedBoundsInRoot()

        val visibleHeight = visible.bottom - visible.top
        val wholeHeight = whole.bottom - whole.top
        assertTrue(
            visibleHeight >= wholeHeight,
            "кнопку налаштувань обрізано: видно $visibleHeight з $wholeHeight",
        )
        assertTrue(
            visible.bottom <= onNodeWithTag("screen").getBoundsInRoot().bottom,
            "кнопка налаштувань нижче екрана: ${visible.bottom}",
        )
    }

    @Test
    fun theTabsFollowTheAmountsWithoutADeadStripe() = runComposeUiTest {
        statusBar(44.dp)
        showSheet()

        val amounts = onAllNodesWithText("0 $", useUnmergedTree = true)
            .fetchSemanticsNodes()
            .maxOf { with(density) { it.boundsInRoot.bottom.toDp() } }
        val tabs = onNodeWithText("Status", useUnmergedTree = true).getBoundsInRoot()
        val gap = tabs.top - amounts

        assertTrue(gap < 30.dp, "між сумами і табами порожня смуга $gap")
        assertTrue(gap >= 0.dp, "таби наїхали на суми: $gap")
    }
}
