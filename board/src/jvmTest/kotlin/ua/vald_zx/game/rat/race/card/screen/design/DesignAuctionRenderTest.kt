package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DesignAuctionRenderTest {

    private fun player(id: String, name: String, color: Long) = Player(
        id = id,
        boardId = "b1",
        attrs = PlayerAttributes(color = color),
        card = PlayerCard(name = name),
    )

    @Test
    fun bidListShowsBidderProfitAndSellAction() = runComposeUiTest {
        players.value = listOf(
            player("p1", "Мар'яна", 0xFFE91E63),
            player("p2", "Данило", 0xFF2196F3),
        )
        val auction = Auction.BusinessAuction(
            business = Business(type = BusinessType.SMALL, name = "Кав'ярня", price = 9500, profit = 380),
            firstBid = 5000,
        )
        val bids = listOf(
            Bid(playerId = "p1", bid = 6000, count = 0),
            Bid(playerId = "p2", bid = 8500, count = 0),
        )

        setContent {
            AppTheme(forceDark = true) {
                Column(
                    Modifier
                        .size(420.dp, 320.dp)
                        .background(Design.scaffold.surface1)
                        .testTag("auction")
                        .padding(16.dp)
                ) {
                    DesignAuctionBidListForTest(bids, auction, sellEnabled = true)
                }
            }
        }
        waitForIdle()

        onNodeWithText("Мар'яна").assertExists()
        onNodeWithText("Данило").assertExists()

        val image = onNodeWithTag("auction").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-auction.png"))
    }
}
