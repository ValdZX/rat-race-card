package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SalaryScreenRenderTest {

    private val playButton = hasText("Play") or hasText("Грати")

    @Test
    fun investmentCardsRenderInsideTheScrollingBottomSheet() = runComposeUiTest {
        setContent {
            Box(Modifier.size(400.dp, 800.dp).testTag("sheet")) {
                BottomSheetContainer {
                    HighRiskCard { _, _ -> }
                    MediumRiskCard { _, _ -> }
                    LowRiskCard(rate = 20) { }
                }
            }
        }
        waitForIdle()
        onNodeWithTag("sheet").assertIsDisplayed()
    }

    @Test
    fun highRiskPlaysOnlyWithBothStakeAndGuess() = runComposeUiTest {
        val played = mutableListOf<Pair<Long, Int>>()
        setContent {
            Box(Modifier.size(400.dp, 800.dp)) {
                BottomSheetContainer {
                    HighRiskCard { stake, guess -> played += stake to guess }
                }
            }
        }
        waitForIdle()

        onNode(playButton).performClick()
        assertEquals(emptyList(), played, "без ставки й числа гра не запускається")

        onNodeWithText("Stake", substring = true).performTextInput("500")
        onNodeWithText("4").performClick()
        onNode(playButton).performClick()

        assertEquals(listOf(500L to 4), played)
    }
}
