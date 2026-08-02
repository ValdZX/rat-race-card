package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.shared.Gender
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DesignProfessionRenderTest {

    private val card = ProfessionCard(
        id = 1,
        name = "Інженер",
        gender = Gender.FEMALE,
        salary = 4900,
        rent = 1200,
        food = 600,
        cloth = 300,
        transport = 400,
        phone = 160,
    )

    @Test
    fun professionCardShowsSalaryExpensesAndCashFlow() = runComposeUiTest {
        var next = 0
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.size(420.dp, 700.dp).testTag("profession")) {
                    DesignProfessionContent(card = card) { next++ }
                }
            }
        }
        waitForIdle()

        onNodeWithText("+2 240").assertExists()
        onNodeWithText("−1 200").assertExists()

        val image = onNodeWithTag("profession").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-profession.png"))

        onNodeWithText("Next").performClick()
        assertEquals(1, next)
    }
}
