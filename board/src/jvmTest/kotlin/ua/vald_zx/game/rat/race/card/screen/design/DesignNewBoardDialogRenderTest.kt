package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.shared.OuterCircleConditions
import ua.vald_zx.game.rat.race.card.shared.VictoryConditions
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DesignNewBoardDialogRenderTest {

    private class Created(
        val name: String,
        val loanLimit: Long,
        val businessLimit: Long,
        val transportBonus: Boolean,
        val outerCircle: OuterCircleConditions,
        val victory: VictoryConditions,
    )

    @Test
    fun formDefaultsTravelToTheServerCall() = runComposeUiTest {
        var created: Created? = null
        setContent {
            AppTheme(forceDark = true) {
                Box(
                    Modifier
                        .width(420.dp)
                        .background(Design.scaffold.background)
                        .testTag("dialog")
                        .padding(12.dp)
                ) {
                    DesignNewBoardDialog(
                        onDismiss = {},
                        onCreate = { name, loan, business, bonus, outer, victory ->
                            created = Created(name, loan, business, bonus, outer, victory)
                        },
                    )
                }
            }
        }
        waitForIdle()

        // порожня назва — створення заблоковане
        onNodeWithText("Create Table").performClick()
        assertNull(created)

        onAllNodes(hasSetTextAction()).onFirst().performTextInput("Пʼятничні перегони")
        onNodeWithText("Plane required").performScrollTo().performClick()
        onNodeWithText("Create Table").performClick()

        val result = requireNotNull(created)
        assertEquals("Пʼятничні перегони", result.name)
        assertEquals(10_000L, result.loanLimit)
        assertEquals(10L, result.businessLimit)
        assertTrue(result.transportBonus)
        assertEquals(50_000L, result.outerCircle.minimumCashFlow)
        assertEquals(200_000L, result.outerCircle.minimumAccountBalance)
        assertTrue(result.outerCircle.apartmentRequired)
        assertEquals(10_000_000L, result.victory.minimumAccountBalance)
        assertTrue(result.victory.dreamRequired)
        assertEquals(false, result.victory.planeRequired)
    }

    @Test
    fun dialogRenders() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(
                    Modifier
                        .width(420.dp)
                        .fillMaxWidth()
                        .background(Design.scaffold.background)
                        .testTag("dialog")
                        .padding(12.dp)
                ) {
                    DesignNewBoardDialog(onDismiss = {}, onCreate = { _, _, _, _, _, _ -> })
                }
            }
        }
        waitForIdle()
        val image = onAllNodes(isRoot()).onLast().captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-new-board.png"))
    }
}
