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
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.screen.board.CardDeckSlotKind
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DesignCardDecksRenderTest {

    private val slotSize = DpSize(74.dp, 96.dp)

    @Test
    fun everyDeckTypeRendersWithToneLabelAndCount() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent {
            AppTheme(forceDark = true) {
                Column(
                    Modifier
                        .background(Design.scaffold.surface4)
                        .testTag("decks")
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BoardCardType.entries.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { type ->
                                Box(Modifier.size(slotSize)) {
                                    DeckSlot(
                                        type = type,
                                        kind = CardDeckSlotKind.DRAW,
                                        size = slotSize,
                                        count = 12,
                                        canTake = type == BoardCardType.Chance,
                                        onClick = {},
                                    )
                                }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(slotSize)) {
                            DeckSlot(
                                type = BoardCardType.Chance,
                                kind = CardDeckSlotKind.DISCARD,
                                size = slotSize,
                                count = 0,
                                canTake = false,
                                onClick = {},
                            )
                        }
                    }
                }
            }
        }
        mainClock.advanceTimeBy(640)
        waitForIdle()
        val image = onNodeWithTag("decks").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-decks.png"))
    }
}
