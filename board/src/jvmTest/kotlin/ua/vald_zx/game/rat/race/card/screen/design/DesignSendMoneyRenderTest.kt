package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.screen.board.SendMoneyScreen
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
class DesignSendMoneyRenderTest {

    @AfterTest
    fun reset() {
        designV2Enabled.value = false
    }

    @Test
    fun sendingMoreThanYouHaveIsRefused() = runComposeUiTest {
        designV2Enabled.value = true
        showSheet()

        onNodeWithTag("key_5").performClick()
        waitForIdle()
        onNodeWithText("More than available").assertExists()

        val image = onNodeWithTag("sheet").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-send-money.png"))
    }

    private fun ComposeUiTest.showSheet() {
        setContent {
            InitPreviewWithVm { vm ->
                BottomSheetNavigator {
                    Box(Modifier.width(420.dp).testTag("sheet")) {
                        SendMoneyScreen(vm = vm, playerId = "2", playerName = "Данило").Content()
                    }
                }
            }
        }
        waitForIdle()
    }
}
