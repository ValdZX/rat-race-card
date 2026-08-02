package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import com.composables.core.BottomSheet
import com.composables.core.rememberBottomSheetState
import ua.vald_zx.game.rat.race.card.screen.board.ContentExpanded
import ua.vald_zx.game.rat.race.card.screen.board.HalfExpanded
import ua.vald_zx.game.rat.race.card.shared.Business
import ua.vald_zx.game.rat.race.card.shared.BusinessType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DesignPlayerSheetScrollTest {

    private val player = Player(
        id = "1",
        boardId = "b",
        attrs = PlayerAttributes(0xFF3355AA, 0),
        businesses = (1..12).map { index ->
            Business(
                type = BusinessType.SMALL,
                name = "Бізнес №$index",
                price = 9500L * index,
                profit = 380L * index,
            )
        },
    )

    @Test
    fun longAssetListScrollsToTheEndInsideTheSheet() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                val sheetState = rememberBottomSheetState(
                    initialDetent = ContentExpanded,
                    detents = listOf(HalfExpanded, ContentExpanded),
                )
                Box(Modifier.size(420.dp, 720.dp).testTag("screen")) {
                    BottomSheet(state = sheetState, modifier = Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize()) {
                            Spacer(Modifier.height(160.dp))
                            val pagerState = rememberPagerState(pageCount = { 5 })
                            LaunchedEffect(Unit) { pagerState.scrollToPage(1) }
                            DesignSheetPages(player, pagerState, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        waitForIdle()
        capture("build/design-sheet-list-top.png")

        repeat(2) {
            assetList().performTouchInput {
                swipeUp(startY = bottom - 4f, endY = top + 4f, durationMillis = 300)
            }
            waitForIdle()
        }
        capture("build/design-sheet-list-scrolled.png")

        onNodeWithText("Бізнес №12").assertExists()
    }

    @Test
    fun shortAssetListStaysAtTheTop() = runComposeUiTest {
        val twoBusinesses = player.copy(businesses = player.businesses.take(2))
        setContent {
            AppTheme(forceDark = true) {
                Column(Modifier.size(420.dp, 620.dp)) {
                    Spacer(Modifier.height(60.dp))
                    val pagerState = rememberPagerState(pageCount = { 5 })
                    LaunchedEffect(Unit) { pagerState.scrollToPage(1) }
                    DesignSheetPages(
                        twoBusinesses,
                        pagerState,
                        Modifier.weight(1f).testTag("pages"),
                    )
                }
            }
        }
        waitForIdle()

        val pagesTop = onNodeWithTag("pages").getBoundsInRoot().top
        val firstRowTop = onNodeWithText("Бізнес №1").getBoundsInRoot().top
        assertTrue(
            firstRowTop - pagesTop < 24.dp,
            "перший рядок мав бути вгорі вкладки, а він на ${firstRowTop - pagesTop} нижче",
        )
    }

    private fun ComposeUiTest.assetList() =
        onAllNodes(hasScrollAction() and hasAnyDescendant(hasText("Бізнес", substring = true))).onLast()

    private fun ComposeUiTest.capture(target: String) {
        val image = onNodeWithTag("screen").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File(target))
    }
}
