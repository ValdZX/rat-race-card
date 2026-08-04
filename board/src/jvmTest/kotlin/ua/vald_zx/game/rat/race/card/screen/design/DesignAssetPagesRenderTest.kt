package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DesignAssetPagesRenderTest {

    private val board = Board(
        id = "b1",
        name = "Board",
        loanLimit = 0,
        businessLimit = 0,
        createDateTime = kotlinx.datetime.LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
    )

    private val player = Player(
        id = "p1",
        boardId = "b1",
        attrs = PlayerAttributes(color = 0xFF2196F3),
        businesses = listOf(
            Business(type = BusinessType.WORK, name = "Інженер", price = 0, profit = 4900),
            Business(type = BusinessType.SMALL, name = "Кав'ярня у центрі", price = 9500, profit = 380),
            Business(
                type = BusinessType.LARGE, name = "Автомийка «Крапля»", price = 24000, profit = 1200,
                extentions = listOf(200L, 300L),
            ),
            Business(
                type = BusinessType.MEDIUM, name = "Пекарня", price = 14000, profit = -500,
                alarmed = true,
            ),
        ),
        sharesList = listOf(Shares(type = SharesType.IT, count = 120, buyPrice = 45)),
        landList = listOf(Land(name = "Ділянка за містом", area = 12, price = 36000)),
        estateList = listOf(Estate(name = "Квартира на Позняках", price = 300000)),
    )

    private fun render(name: String, dark: Boolean = true, content: @androidx.compose.runtime.Composable () -> Unit) =
        runComposeUiTest {
            setContent {
                AppTheme(forceDark = dark) {
                    Column(
                        Modifier
                            .size(420.dp, 340.dp)
                            .background(Design.scaffold.surface1)
                            .testTag("page")
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) { content() }
                }
            }
            waitForIdle()
            val image = onNodeWithTag("page").captureToImage().toAwtImage()
            File("build").mkdirs()
            ImageIO.write(image, "png", File("build/design-assets-$name.png"))
        }

    @Test
    fun businessPageShowsTypeExtensionsAndProfit() = render("business") {
        DesignBusinessPage(player)
    }

    @Test
    fun sharesLandEstateShare() = render("mixed") {
        DesignSharesPage(player, board)
        DesignLandPage(player)
        DesignEstatePage(player)
    }

    @Test
    fun emptyPageShowsPlaceholderInsteadOfBlank() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Column(Modifier.size(420.dp, 200.dp).testTag("empty")) {
                    DesignLandPage(player.copy(landList = emptyList()))
                }
            }
        }
        waitForIdle()
        onNodeWithText("Nothing here yet").assertExists()
    }
}
