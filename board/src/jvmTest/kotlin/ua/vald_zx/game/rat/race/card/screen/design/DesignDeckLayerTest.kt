package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.screen.board.CardDeckSlotKind
import ua.vald_zx.game.rat.race.card.screen.board.calculateBoardLayout
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DesignDeckLayerTest {

    private val size = DpSize(360.dp, 420.dp)

    @Test
    fun decksStayVisibleWhenACellIsFocused() = runComposeUiTest {
        val layout = calculateBoardLayout(size, isVertical = true)!!
        val place = layout.innerRoute.places.first { it.place.type.name == "Chance" }.place
        val slotSize = DpSize(60.dp, 60.dp)
        setContent {
            AppTheme(forceDark = true) {
                val focus = rememberCellFocus()
                Box(
                    Modifier.size(size)
                        .background(Design.scaffold.background)
                        .cellFocusTracking(listOf(layout.innerRoute, layout.outerRoute), focus)
                        .testTag("board")
                ) {
                    DesignTrackForTest(layout.outerRoute, CellSurface.Engraved, focus)
                    DesignTrackForTest(layout.innerRoute, CellSurface.Tile, focus)
                    Box(
                        Modifier
                            .offset(150.dp, 180.dp)
                            .size(slotSize)
                            .zIndex(DECKS_Z)
                            .testTag("deck")
                    ) {
                        DeckSlot(BoardCardType.Chance, CardDeckSlotKind.DRAW, slotSize, 7, false) {}
                    }
                }
            }
        }
        waitForIdle()
        val before = deckPixel()

        val center = Offset(
            x = (size.width - layout.innerRoute.size.width).value / 2 +
                    place.offset.x.value + place.size.width.value / 2,
            y = (size.height - layout.innerRoute.size.height).value / 2 +
                    place.offset.y.value + place.size.height.value / 2,
        )
        onNodeWithTag("board").performMouseInput { moveTo(center) }
        waitForIdle()

        assertEquals(before, deckPixel(), "колода зникла під ложем треку з розкритою кліткою")
        capture()
    }

    private fun androidx.compose.ui.test.ComposeUiTest.deckPixel(): Int =
        onNodeWithTag("board").captureToImage().toAwtImage().getRGB(180, 210)

    private fun androidx.compose.ui.test.ComposeUiTest.capture() {
        val image = onNodeWithTag("board").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-deck-layer.png"))
    }
}
