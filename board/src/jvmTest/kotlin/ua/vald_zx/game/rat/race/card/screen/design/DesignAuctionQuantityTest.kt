package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.shared.Auction
import ua.vald_zx.game.rat.race.card.shared.Shares
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DesignAuctionQuantityTest {

    private val auction = Auction.SharesAuction(
        shares = Shares(type = "IT", count = 250, buyPrice = 12),
        firstBid = 10,
    )

    @Test
    fun quantityStepsByTenAndHundredAndClampsToLotSize() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Column(
                    Modifier
                        .size(420.dp, 620.dp)
                        .background(Design.scaffold.surface1)
                        .testTag("bidForm")
                        .padding(16.dp)
                ) {
                    DesignAuctionBidFormForTest(auction, minBid = 10)
                }
            }
        }
        waitForIdle()

        onNodeWithText("+10").performClick()
        onNodeWithText("11").assertExists()

        onNodeWithText("+100").performClick()
        onNodeWithText("111").assertExists()

        repeat(2) { onNodeWithText("+100").performClick() }
        onNodeWithText("250").assertExists()

        val image = onNodeWithTag("bidForm").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-auction-quantity.png"))
    }
}
