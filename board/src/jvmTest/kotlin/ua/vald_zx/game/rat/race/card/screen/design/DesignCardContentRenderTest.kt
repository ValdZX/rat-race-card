package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.components.preview.InitPreviewWithVm
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.screen.board.deck.front.BoardCardFront
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.CardLink
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
class DesignCardContentRenderTest {

    private val cards = listOf(
        CardLink(BoardCardType.Expenses, 1),
        CardLink(BoardCardType.Shopping, 1),
        CardLink(BoardCardType.Chance, 96),
        CardLink(BoardCardType.Chance, 61),
        CardLink(BoardCardType.EventStore, 1),
        CardLink(BoardCardType.SmallBusiness, 1),
    )

    @AfterTest
    fun reset() {
        designV2Enabled.value = false
    }

    @Test
    fun cardFacesFollowTheNewLanguage() = runComposeUiTest {
        designV2Enabled.value = true
        renderCards("build/design-card-content.png")
    }

    @Test
    fun oldCardFacesStayUntouched() = runComposeUiTest {
        designV2Enabled.value = false
        renderCards("build/old-card-content.png")
    }

    private fun androidx.compose.ui.test.ComposeUiTest.renderCards(target: String) {
        setContent {
            InitPreviewWithVm { vm ->
                BottomSheetNavigator {
                Column(
                    Modifier
                        .background(Design.scaffold.background)
                        .testTag("cards")
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    cards.chunked(2).forEach { row ->
                        androidx.compose.foundation.layout.Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            row.forEach { card ->
                                BoxWithConstraints(Modifier.width(300.dp)) {
                                    BoardCardFront(card = card, size = DpSize(300.dp, 260.dp), vm = vm)
                                }
                            }
                        }
                    }
                }
                }
            }
        }
        waitForIdle()
        val image = onNodeWithTag("cards").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File(target))
    }
}
