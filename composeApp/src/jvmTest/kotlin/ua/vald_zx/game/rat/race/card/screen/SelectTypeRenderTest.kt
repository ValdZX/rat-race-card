package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.layout.Box
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
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import io.github.sudarshanmhasrup.localina.api.LocalinaApp
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import ua.vald_zx.game.rat.race.card.AppLanguage
import ua.vald_zx.game.rat.race.card.design.DesignLanguagePicker
import ua.vald_zx.game.rat.race.card.di.boardModule
import ua.vald_zx.game.rat.race.card.di.cardModule
import ua.vald_zx.game.rat.race.card.di.coreModule
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SelectTypeRenderTest {

    @Test
    fun renderDark() = renderScreen(dark = true, name = "select-type-dark")

    @Test
    fun renderLight() = renderScreen(dark = false, name = "select-type-light")

    @Test
    fun languagePickerReportsTappedLanguage() = runComposeUiTest {
        var picked: AppLanguage? = null
        setContent {
            AppTheme(forceDark = true) {
                DesignLanguagePicker(selected = AppLanguage.English, onSelect = { picked = it })
            }
        }
        waitForIdle()

        onNodeWithText(AppLanguage.Ukrainian.label).performClick()
        assertEquals(AppLanguage.Ukrainian, picked)
    }

    private fun renderScreen(dark: Boolean, name: String) = runComposeUiTest {
        setContent {
            KoinApplication(
                configuration = koinConfiguration(declaration = { modules(coreModule, cardModule, boardModule) }),
            ) {
                Navigator(SelectTypeScreen()) {
                    LocalinaApp {
                        AppTheme(forceDark = dark) {
                            Box(Modifier.size(420.dp, 760.dp).testTag("screen")) {
                                CurrentScreen()
                            }
                        }
                    }
                }
            }
        }
        waitForIdle()
        val image = onNodeWithTag("screen").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/$name.png"))
    }
}
