package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DesignActiveElementsRenderTest {

    @Test
    fun activeStatesShareTheColorPulse() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent {
            AppTheme(forceDark = false) {
                Row(
                    modifier = Modifier
                        .background(Design.scaffold.surface4)
                        .padding(32.dp)
                        .testTag("active-elements"),
                    horizontalArrangement = Arrangement.spacedBy(40.dp),
                ) {
                    val tokenSize = DpSize(64.dp, 64.dp)
                    Box(Modifier.size(tokenSize)) {
                        DesignPlayerToken(
                            player = Player(
                                id = "active",
                                boardId = "board",
                                attrs = PlayerAttributes(0xFF3355AA, 0),
                            ),
                            isCurrentPlayer = true,
                            isActivePlayer = true,
                            spotSize = tokenSize,
                        )
                    }
                    val deckSize = DpSize(74.dp, 96.dp)
                    Box(Modifier.size(deckSize)) {
                        DeckSlot(
                            type = BoardCardType.Chance,
                            kind = CardDeckSlotKind.DRAW,
                            size = deckSize,
                            count = 12,
                            actionable = true,
                            onClick = {},
                        )
                    }
                    DesignPlaceCell(
                        type = PlaceType.Salary,
                        waitingAmount = 1_200,
                        onClick = {},
                        modifier = Modifier.size(72.dp, 82.dp),
                    )
                }
            }
        }
        mainClock.advanceTimeBy(640)
        waitForIdle()
        val image = onNodeWithTag("active-elements").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-active-elements.png"))
    }
}
