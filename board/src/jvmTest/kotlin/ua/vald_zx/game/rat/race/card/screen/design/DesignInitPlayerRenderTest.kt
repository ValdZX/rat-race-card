package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
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
import ua.vald_zx.game.rat.race.card.shared.pointerColors
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DesignInitPlayerRenderTest {

    @Test
    fun renderDark() = render(dark = true, playerName = "Владислав", name = "design-init-player-dark")

    @Test
    fun renderLight() = render(dark = false, playerName = "Владислав", name = "design-init-player-light")

    @Test
    fun renderEmptyName() = render(dark = true, playerName = "", name = "design-init-player-empty")

    @Test
    fun tokenColorAndGenderAreSelectable() = runComposeUiTest {
        val colorState = mutableStateOf(pointerColors.first())
        var gender = Gender.MALE
        setContent {
            AppTheme(forceDark = true) {
                DesignInitPlayerContent(
                    colorState = colorState,
                    playerName = "Владислав",
                    onNameChange = {},
                    gender = gender,
                    onGenderChange = { gender = it },
                    onBack = {},
                    onNext = {},
                )
            }
        }
        waitForIdle()

        onNodeWithTag("swatch_${pointerColors[2]}").performClick()
        assertEquals(pointerColors[2], colorState.value)

        onNodeWithTag("gender_${Gender.FEMALE.name}").performClick()
        assertEquals(Gender.FEMALE, gender)
    }

    @Test
    fun emptyNameKeepsNextDisabled() = runComposeUiTest {
        var advanced = false
        setContent {
            AppTheme(forceDark = true) {
                DesignInitPlayerContent(
                    colorState = mutableStateOf(pointerColors.first()),
                    playerName = "",
                    onNameChange = {},
                    gender = Gender.MALE,
                    onGenderChange = {},
                    onBack = {},
                    onNext = { advanced = true },
                )
            }
        }
        waitForIdle()

        onNodeWithText("Next").performClick()
        assertEquals(false, advanced)
    }

    private fun render(dark: Boolean, playerName: String, name: String) = runComposeUiTest {
        setContent {
            AppTheme(forceDark = dark) {
                val colorState = mutableStateOf(pointerColors[1])
                Box(Modifier.size(380.dp, 760.dp).testTag("screen")) {
                    DesignInitPlayerContent(
                        colorState = colorState,
                        playerName = playerName,
                        onNameChange = {},
                        gender = Gender.MALE,
                        onGenderChange = {},
                        onBack = {},
                        onNext = {},
                    )
                }
            }
        }
        waitForIdle()
        val image = onNodeWithTag("screen").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/$name.png"))
    }
}
