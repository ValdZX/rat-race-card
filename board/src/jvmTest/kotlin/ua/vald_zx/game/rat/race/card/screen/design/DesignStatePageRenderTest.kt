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
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DesignStatePageRenderTest {

    private val player = Player(
        id = "p1",
        boardId = "b1",
        attrs = PlayerAttributes(color = 0xFF2196F3),
        card = PlayerCard(name = "Олена", profession = "Інженер", salary = 4900),
        cash = 15700,
        deposit = 8000,
        loan = 2760,
        funds = listOf(Fund(rate = 20, amount = 12500)),
        businesses = listOf(
            Business(type = BusinessType.WORK, name = "Інженер", price = 0, profit = 4900)
        ),
    )

    private fun render(dark: Boolean, name: String) = runComposeUiTest {
        setContent {
            AppTheme(forceDark = dark) {
                Column(
                    Modifier
                        .size(400.dp, 300.dp)
                        .background(Design.scaffold.surface1)
                        .testTag("page")
                        .padding(16.dp)
                ) {
                    DesignPlayerStatePageForTest(player)
                }
            }
        }
        waitForIdle()
        val image = onNodeWithTag("page").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-sheet-$name.png"))
        assertTrue(image.width > 0)
    }

    @Test
    fun renderDark() = render(dark = true, name = "dark")

    @Test
    fun renderLight() = render(dark = false, name = "light")
}
