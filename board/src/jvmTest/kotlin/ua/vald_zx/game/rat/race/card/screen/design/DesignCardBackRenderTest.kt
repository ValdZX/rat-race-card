package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.screen.board.deck.BoardCardBack
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DesignCardBackRenderTest {

    private val slot = DpSize(74.dp, 96.dp)
    private val flying = DpSize(150.dp, 100.dp)

    @AfterTest
    fun reset() {
        designV2Enabled.value = false
    }

    @Test
    fun cardBacksCarryTheDeckTone() = runComposeUiTest {
        designV2Enabled.value = true
        renderBacks("build/design-card-backs.png")
    }

    @Test
    fun oldCardBacksStayUntouched() = runComposeUiTest {
        designV2Enabled.value = false
        renderBacks("build/old-card-backs.png")
    }

    private fun ComposeUiTest.renderBacks(target: String) {
        setContent {
            AppTheme(forceDark = true) {
                Column(
                    Modifier
                        .background(Design.scaffold.background)
                        .testTag("backs")
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BoardCardType.entries.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { type ->
                                Box(Modifier.size(slot)) {
                                    BoardCardBack(type, slot, isVertical = true)
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(BoardCardType.Chance, BoardCardType.EventStore).forEach { type ->
                            Box(Modifier.size(flying)) {
                                BoardCardBack(type, flying, isVertical = false)
                            }
                        }
                    }
                }
            }
        }
        waitForIdle()
        val image = onNodeWithTag("backs").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File(target))
    }
}
