package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.screen.board.page.FundsPage
import ua.vald_zx.game.rat.race.card.shared.Fund
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import ua.vald_zx.game.rat.race.card.formatAmount

@OptIn(ExperimentalTestApi::class)
class FundsPageRenderTest {

    private fun player(vararg funds: Fund) = Player(
        id = "p1",
        boardId = "b1",
        attrs = PlayerAttributes(color = 0xFF2196F3),
        funds = funds.toList(),
    )

    @Test
    fun showsEveryFundWithItsRateAndTotal() = runComposeUiTest {
        setContent {
            MaterialTheme {
                Box(
                    Modifier.size(360.dp, 320.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .testTag("page")
                ) {
                    FundsPage(player(Fund(rate = 20, amount = 12500), Fund(rate = 5, amount = 3000)))
                }
            }
        }
        waitForIdle()

        onNodeWithText("20%").assertIsDisplayed()
        onNodeWithText("5%").assertIsDisplayed()
        onNodeWithText(12_500L.formatAmount()).assertIsDisplayed()
        onNodeWithText(15_500L.formatAmount()).assertIsDisplayed()

        val image = onNodeWithTag("page").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/funds-page.png"))
    }

    @Test
    fun rendersWithoutTotalWhenThereAreNoFunds() = runComposeUiTest {
        setContent {
            MaterialTheme {
                Box(Modifier.size(360.dp, 320.dp).testTag("page")) {
                    FundsPage(player())
                }
            }
        }
        waitForIdle()
        onNodeWithTag("page").assertIsDisplayed()
    }
}
