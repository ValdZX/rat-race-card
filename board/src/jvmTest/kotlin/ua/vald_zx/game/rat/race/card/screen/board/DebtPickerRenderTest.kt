package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.shared.Debt
import ua.vald_zx.game.rat.race.card.shared.DebtKind
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DebtPickerRenderTest {

    private val debts = listOf(
        Debt("credit-line", DebtKind.CREDIT_LINE, 40_000, 10),
        Debt("payday", DebtKind.PAYDAY, 6_000, 40),
    )

    @Test
    fun theCostliestDebtIsCalledOut() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.width(420.dp).background(Design.scaffold.background).testTag("debts")) {
                    DebtPicker(debts) {}
                }
            }
        }
        waitForIdle()

        onNodeWithText("Payday loan").assertIsDisplayed()
        onNodeWithText("Credit line").assertIsDisplayed()
        onNodeWithText("Costliest first").assertIsDisplayed()
        val image = onNodeWithTag("debts").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-debts.png"))
    }

    @Test
    fun pickingADebtReportsIt() = runComposeUiTest {
        var picked: Debt? = null
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.width(420.dp).background(Design.scaffold.background)) {
                    DebtPicker(debts) { picked = it }
                }
            }
        }
        waitForIdle()

        onNodeWithText("Payday loan").performClick()

        assertEquals("payday", picked?.id)
    }
}
