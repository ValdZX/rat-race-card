package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.shared.*
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark
import java.io.File
import javax.imageio.ImageIO
import kotlinx.datetime.LocalDateTime
import ua.vald_zx.game.rat.race.card.shared.Board
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ua.vald_zx.game.rat.race.card.formatAmount

@OptIn(ExperimentalTestApi::class)
class DesignStatePageRenderTest {

    private val player = Player(
        id = "p1",
        boardId = "b1",
        attrs = PlayerAttributes(color = 0xFF2196F3),
        card = PlayerCard(name = "Олена", profession = "Інженер", salary = 4900),
        cash = 15700,
        deposit = 8000,
        loan = 2760,
        funds = listOf(Fund(rate = 20, amount = 12500)),
        businesses = listOf(
            Business(type = BusinessType.WORK, name = "Інженер", price = 0, profit = 4900)
        ),
    )

    private fun render(dark: Boolean, name: String) = runComposeUiTest {
        setContent {
            AppTheme(forceDark = dark) {
                Column(
                    Modifier
                        .size(400.dp, 300.dp)
                        .background(Design.scaffold.surface1)
                        .testTag("page")
                        .padding(16.dp)
                ) {
                    DesignPlayerStatePageForTest(player, previewBoard)
                }
            }
        }
        waitForIdle()
        val image = onNodeWithTag("page").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-sheet-$name.png"))
        assertTrue(image.width > 0)
    }

    @Test
    fun renderDark() = render(dark = true, name = "dark")

    @Test
    fun renderLight() = render(dark = false, name = "light")

    @Test
    fun statePageShowsBoardLoanLimit() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Column(Modifier.size(400.dp, 600.dp)) {
                    DesignPlayerStatePageForTest(player, previewBoard)
                }
            }
        }
        waitForIdle()

        onNodeWithText("Loan limit").assertIsDisplayed()
        onNodeWithText(previewBoard.loanLimit.formatAmount()).assertIsDisplayed()
    }

    @Test
    fun depositActionIsOfferedWhileThereIsCash() = runComposeUiTest {
        var deposits = 0
        setContent {
            AppTheme(forceDark = true) {
                Column(Modifier.size(400.dp, 600.dp)) {
                    DesignPlayerStatePageForTest(player, previewBoard) { deposits++ }
                }
            }
        }
        waitForIdle()

        onNodeWithContentDescription("Deposit to Deposit").performClick()
        assertEquals(1, deposits)
    }

    @Test
    fun depositActionHidesWithoutCash() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Column(Modifier.size(400.dp, 600.dp)) {
                    DesignPlayerStatePageForTest(player.copy(cash = 0), previewBoard)
                }
            }
        }
        waitForIdle()

        onNodeWithContentDescription("Deposit to Deposit").assertDoesNotExist()
    }

    @Test
    fun statePageShowsPlayerAnimals() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Column(Modifier.size(400.dp, 600.dp)) {
                    DesignPlayerStatePageForTest(player.copy(animal = 2), previewBoard)
                }
            }
        }
        waitForIdle()

        onNodeWithText("Pets ×2").assertExists()
    }

    @Test
    fun statePageScrollsDownToTheLastBlock() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Column(
                    Modifier
                        .size(400.dp, 300.dp)
                        .background(Design.scaffold.surface1)
                        .testTag("page")
                ) {
                    DesignPlayerStatePageForTest(player, previewBoard)
                }
            }
        }
        waitForIdle()
        val hiddenAtStart = onNodeWithText("Outer circle conditions").getBoundsInRoot()
        assertTrue(
            hiddenAtStart.bottom - hiddenAtStart.top == 0.dp,
            "тест не має сенсу: останнє поле й так у видимій частині",
        )

        repeat(4) {
            onNodeWithTag("page").performTouchInput {
                swipeUp(startY = bottom - 4f, endY = top + 4f, durationMillis = 200)
            }
            waitForIdle()
        }
        val shown = onNodeWithText("Outer circle conditions").getBoundsInRoot()
        ImageIO.write(
            onNodeWithTag("page").captureToImage().toAwtImage(),
            "png",
            File("build/design-state-scrolled.png"),
        )

        assertTrue(shown.bottom - shown.top > 0.dp, "сторінка стану не скролиться")
        assertTrue(shown.bottom <= 300.dp, "останнє поле так і не доїхало у видиму частину")
    }
}

private val previewBoard = Board(
    id = "b",
    name = "b",
    loanLimit = 10_000,
    businessLimit = 0,
    createDateTime = LocalDateTime(2026, 1, 1, 0, 0),
    cards = emptyMap(),
)
