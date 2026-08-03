package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.design.DesignAmountForm
import ua.vald_zx.game.rat.race.card.design.DesignChip
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DesignSalaryRenderTest {

    @Test
    fun highRiskNeedsBothStakeAndGuess() = runComposeUiTest {
        var played: Pair<Long, Int>? = null
        setContent {
            AppTheme(forceDark = true) {
                var guess by remember { mutableStateOf<Int?>(null) }
                Column(
                    Modifier
                        .size(400.dp, 760.dp)
                        .background(Design.scaffold.surface1)
                        .testTag("form")
                        .padding(16.dp)
                ) {
                    DesignAmountForm(
                        title = "Високоризикові",
                        subtitle = "Вгадай число — ставка ×6",
                        confirmLabel = { "Грати $it" },
                        onConfirm = { amount -> played = amount to (guess ?: 0) },
                        validate = { it > 0 && guess != null },
                        extraContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                (1..6).forEach { number ->
                                    DesignChip(
                                        text = number.toString(),
                                        selected = guess == number,
                                        modifier = Modifier.weight(1f).testTag("guess_$number"),
                                    ) { guess = number }
                                }
                            }
                        },
                    )
                }
            }
        }
        waitForIdle()

        onNodeWithTag("system-amount-field").performTextReplacement("500")
        onNodeWithText("Грати 500").performClick()
        assertEquals(null, played, "без вибраного числа ставка не приймається")

        onNodeWithTag("guess_4").performClick()
        onNodeWithText("Грати 500").performClick()
        assertEquals(500L to 4, played)

        val image = onNodeWithTag("form").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-salary-high.png"))
    }
}
