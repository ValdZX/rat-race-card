package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.BottomSheetNavigator
import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.designV2Enabled
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.logic.players
import ua.vald_zx.game.rat.race.card.screen.design.deckSlotTag
import ua.vald_zx.game.rat.race.card.screen.design.tokenBubbleTag
import ua.vald_zx.game.rat.race.card.shared.BoardCardType
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.shared.PlayerCard
import ua.vald_zx.game.rat.race.card.shared.PlayerLocation
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, androidx.compose.material.ExperimentalMaterialApi::class)
class BoardBubbleLayerTest {

    private val screen = DpSize(420.dp, 760.dp)

    private val player = Player(
        id = "rival",
        boardId = "b",
        attrs = PlayerAttributes(0xFF3355AA, 0),
        card = PlayerCard(name = "Olena", profession = "Engineer", salary = 4900),
        location = PlayerLocation(position = 20, level = BoardLayer.INNER.level),
    )

    private val board = Board(
        id = "b",
        name = "b",
        loanLimit = 10_000,
        businessLimit = 10,
        createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
        cards = emptyMap(),
        playerIds = setOf(player.id),
        activePlayerId = player.id,
        canRoll = true,
    )

    @AfterTest
    fun reset() {
        players.value = emptyList()
    }

    @Test
    fun theBubbleCoversTheDeck() = runComposeUiTest {
        showBoard()
        val before = capture("build/board-bubble-under-deck.png")

        openBubble()
        val bubble = bubbleRect()
        val after = capture("build/board-bubble-over-deck.png")

        val point = deckTags.mapNotNull { tag ->
            intersect(bubble, onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot())
        }.firstOrNull()
        assertTrue(point != null, "бабл $bubble не перекриває жодної колоди — перевірка нічого не доводить")
        assertTrue(
            before.getRGB(point!!.first, point.second) != after.getRGB(point.first, point.second),
            "колода намальована поверх бабла у точці $point",
        )
    }

    @Test
    fun theBubbleTakesTheTapOverTheDice() = runComposeUiTest {
        showBoard()
        openBubble()

        val overlap = intersect(bubbleRect(), diceRect())
        assertTrue(overlap != null, "бабл не перекриває кубик — перевірка нічого не доводить")

        onNodeWithContentDescription(diceDescription, useUnmergedTree = true).performClick()
        waitForIdle()

        onNodeWithTag(tokenBubbleTag, useUnmergedTree = true).assertDoesNotExist()
    }

    private fun ComposeUiTest.showBoard() {
        designV2Enabled.value = true
        players.value = listOf(player)
        setContent {
            BottomSheetNavigator {
                AppTheme(forceDark = true) {
                    Box(
                        Modifier.size(screen)
                            .background(Design.scaffold.background)
                            .testTag("screen")
                    ) {
                        BoardFragment(BoardViewModel(board, player, { error("офлайн-тест") }))
                    }
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.openBubble() {
        onNodeWithTag("player-token-" + player.id).performClick()
        waitForIdle()
    }

    private fun ComposeUiTest.bubbleRect(): DpRect =
        onNodeWithTag(tokenBubbleTag, useUnmergedTree = true).getUnclippedBoundsInRoot()

    private fun ComposeUiTest.diceRect(): DpRect =
        onNodeWithContentDescription(diceDescription, useUnmergedTree = true).getUnclippedBoundsInRoot()

    private fun ComposeUiTest.capture(target: String): BufferedImage {
        val image = onNodeWithTag("screen").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File(target))
        return image
    }

    private val diceDescription = "Lottie animation"

    private val deckTags = listOf(CardDeckSlotKind.DRAW, CardDeckSlotKind.DISCARD).flatMap { kind ->
        BoardCardType.entries.map { type -> deckSlotTag(type, kind) }
    }

    private fun intersect(first: DpRect, second: DpRect): Pair<Int, Int>? {
        val left = maxOf(first.left, second.left)
        val right = minOf(first.right, second.right)
        val top = maxOf(first.top, second.top)
        val bottom = minOf(first.bottom, second.bottom)
        if (left >= right || top >= bottom) return null
        return ((left + right).value / 2).toInt() to ((top + bottom).value / 2).toInt()
    }
}
