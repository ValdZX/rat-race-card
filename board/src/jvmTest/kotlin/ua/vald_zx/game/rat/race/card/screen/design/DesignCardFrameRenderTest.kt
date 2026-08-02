package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
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
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DesignCardFrameRenderTest {

    @Test
    fun frameCarriesTheDeckTypeForEveryCard() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Column(
                    Modifier
                        .background(Design.scaffold.background)
                        .testTag("cards")
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BoardCardType.entries.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { type ->
                                DesignCardFrame(type = type, modifier = Modifier.size(180.dp, 120.dp)) {
                                    Text(
                                        text = "Опис події на карті, кілька слів",
                                        style = Design.type.body,
                                        color = Design.scaffold.onSurface,
                                        modifier = Modifier.align(Alignment.Center).padding(10.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        waitForIdle()

        onNodeWithText("Chance!").assertExists()

        val image = onNodeWithTag("cards").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-card-frames.png"))
    }
}
