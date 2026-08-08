package ua.vald_zx.game.rat.race.card.screen.board.deck

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import kotlinx.datetime.LocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ua.vald_zx.game.rat.race.card.components.preview.InitPreview
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
class CardDialogCollapseRenderTest {

    @AfterTest
    fun reset() {
        designV2Enabled.value = false
    }

    private fun board() = Board(
        id = "board",
        name = "board",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = mapOf(BoardCardType.Chance to listOf(1)),
        takenCard = CardLink(BoardCardType.Chance, 1),
        playerIds = setOf("p"),
        activePlayerId = "p",
        canRoll = false,
    )

    private fun player() = Player(id = "p", boardId = "board", attrs = PlayerAttributes(0, 0))

    @Test
    fun cardCanBeCollapsedAndReopened() = runComposeUiTest {
        designV2Enabled.value = true
        setContent {
            InitPreview {
                val vm: BoardViewModel = koinViewModel(
                    parameters = { parametersOf(board(), player()) }
                )
                BottomSheetNavigator {
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        CardDialog(vm)
                    }
                }
            }
        }
        waitForIdle()

        onNodeWithTag("card-collapse").assertExists()
        onNodeWithTag("collapsed-card-chip").assertDoesNotExist()

        onNodeWithTag("card-collapse").performClick()
        waitForIdle()

        onNodeWithTag("collapsed-card-chip").assertExists()
        onNodeWithTag("card-collapse").assertDoesNotExist()

        onNodeWithTag("collapsed-card-chip").performClick()
        waitForIdle()

        onNodeWithTag("card-collapse").assertExists()
        onNodeWithTag("collapsed-card-chip").assertDoesNotExist()
    }
}
