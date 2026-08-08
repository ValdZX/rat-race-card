package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import kotlin.test.Test
import ua.vald_zx.game.rat.race.card.screen.design.waitingAmountLabel

@OptIn(ExperimentalTestApi::class)
class DesignThemeGuardTest {

    @Test
    fun placeCellRendersUnderAppTheme() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(80.dp, 90.dp).testTag("cell")) {
                    DesignPlaceCell(type = PlaceType.Salary, label = "Salary")
                }
            }
        }
        waitForIdle()
        onNodeWithTag("cell").assertIsDisplayed()
        onNodeWithText("Salary").assertDoesNotExist()
        onNodeWithContentDescription("Salary", useUnmergedTree = true).assertExists()
    }

    @Test
    fun expandedPlaceCellShowsTheLabel() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(120.dp, 40.dp).testTag("cell")) {
                    DesignPlaceCell(type = PlaceType.Salary, label = "Salary", expanded = true)
                }
            }
        }
        waitForIdle()
        onNodeWithText("Salary").assertExists()
    }

    @Test
    fun waitingTokenRendersTheAmount() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(90.dp, 100.dp).testTag("cell")) {
                    DesignPlaceCell(
                        type = PlaceType.Salary,
                        label = "Salary",
                        expanded = true,
                        waitingAmount = 3200,
                    )
                }
            }
        }
        waitForIdle()
        onNodeWithText(waitingAmountLabel(3_200)).assertExists()
    }
}
