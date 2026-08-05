package ua.vald_zx.game.rat.race.card.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AmountFormRenderTest {

    private fun ComposeUiTest.form(
        dark: Boolean = true,
        onConfirm: (Long) -> Unit = {},
    ) {
        setContent {
            AppTheme(forceDark = dark) {
                Box(
                    Modifier
                        .size(400.dp, 720.dp)
                        .background(Design.scaffold.surface1)
                        .testTag("form")
                        .padding(16.dp)
                ) {
                    DesignAmountForm(
                        title = "Купівля бізнесу",
                        subtitle = "Автомийка · перше коло",
                        confirmLabel = { "Купити за $it" },
                        onConfirm = onConfirm,
                        onCancel = {},
                        cancelLabel = "Скасувати",
                        maxAmount = 15700,
                        quickOptions = proportionalAmountOptions(15700, "Усе"),
                        hint = { "доступно 15 700 · лишиться ${15700 - it}" },
                    )
                }
            }
        }
        waitForIdle()
    }

    @Test
    fun systemInputBuildsTheAmountAndConfirmCarriesIt() = runComposeUiTest {
        var confirmed: Long? = null
        form(onConfirm = { confirmed = it })

        onNodeWithTag("system-amount-field").performTextReplacement("12500")
        onNodeWithText("Купити за 12500").performClick()

        assertEquals(12500L, confirmed)
    }

    @Test
    fun systemInputCanReplaceTheAmount() = runComposeUiTest {
        var confirmed: Long? = null
        form(onConfirm = { confirmed = it })

        onNodeWithTag("system-amount-field").performTextReplacement("999")
        onNodeWithTag("system-amount-field").performTextReplacement("99")
        onNodeWithText("Купити за 99").performClick()

        assertEquals(99L, confirmed)
    }

    @Test
    fun contextualQuickOptionSetsTheExactAmount() = runComposeUiTest {
        var confirmed: Long? = null
        form(onConfirm = { confirmed = it })

        onNodeWithText("50%").performClick()
        onNodeWithText("Купити за 7850").performClick()

        assertEquals(7850L, confirmed)
    }

    @Test
    fun confirmIsDisabledUntilSomethingIsTyped() = runComposeUiTest {
        var confirmed: Long? = null
        form(onConfirm = { confirmed = it })

        onNodeWithText("Купити за 0").performClick()
        assertEquals(null, confirmed)
    }

    @Test
    fun renderDark() = runComposeUiTest {
        form(dark = true)
        onNodeWithTag("system-amount-field").performTextReplacement("5000")
        val image = onNodeWithTag("form").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-amount-dark.png"))
    }

    @Test
    fun renderLight() = runComposeUiTest {
        form(dark = false)
        onNodeWithTag("system-amount-field").performTextReplacement("5000")
        val image = onNodeWithTag("form").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-amount-light.png"))
    }
}
