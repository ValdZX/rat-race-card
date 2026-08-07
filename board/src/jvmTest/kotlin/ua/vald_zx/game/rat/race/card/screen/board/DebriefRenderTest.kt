package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.shared.GameDebrief
import ua.vald_zx.game.rat.race.card.shared.LedgerEntry
import ua.vald_zx.game.rat.race.card.shared.LedgerReason
import ua.vald_zx.game.rat.race.card.shared.NEUTRAL_INDEX_PERCENT
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DebriefRenderTest {

    @Test
    fun debriefShowsWhereMoneyWent() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.width(420.dp).background(Design.scaffold.background).testTag("debrief")) {
                    DebriefContent(debrief())
                }
            }
        }
        waitForIdle()

        onNodeWithText("Where your money went").assertIsDisplayed()
        onNodeWithText("Biggest drains").assertIsDisplayed()
        val image = onNodeWithTag("debrief").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-debrief.png"))
    }

    @Test
    fun emptyLedgerRendersInsteadOfCrashing() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.width(420.dp).background(Design.scaffold.background).testTag("debrief")) {
                    DebriefContent(GameDebrief("first", emptyList()))
                }
            }
        }
        waitForIdle()

        onNodeWithText("This game has no recorded financial history.").assertIsDisplayed()
    }

    @Test
    fun loadingStateRendersWhileTheLedgerArrives() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(Modifier.width(420.dp).background(Design.scaffold.background).testTag("debrief")) {
                    DebriefContent(null)
                }
            }
        }
        waitForIdle()

        onNodeWithTag("debrief").assertIsDisplayed()
    }

    private fun debrief() = GameDebrief(
        playerId = "first",
        entries = listOf(
            entry(0, LedgerReason.SALARY, total = 120_000, cashFlow = 4_000, creditExpenses = 500),
            entry(1, LedgerReason.CONSUMER_PURCHASE, total = 80_000, cashFlow = 2_400, loan = 40_000),
            entry(2, LedgerReason.SCAM, total = 55_000, cashFlow = 2_400, loan = 40_000),
            entry(3, LedgerReason.MARKET_CRASH, total = 40_000, cashFlow = -900, loan = 60_000),
            entry(
                4,
                LedgerReason.SALARY,
                total = 62_000,
                cashFlow = -900,
                creditExpenses = 6_000,
                loan = 60_000,
                priceIndexPercent = 133,
                businessValue = 40_000,
            ),
        ),
    )

    private fun entry(
        sequence: Long,
        reason: LedgerReason,
        total: Long,
        cashFlow: Long = 0,
        creditExpenses: Long = 0,
        loan: Long = 0,
        priceIndexPercent: Long = NEUTRAL_INDEX_PERCENT,
        businessValue: Long = 0,
    ) = LedgerEntry(
        playerId = "first",
        sequence = sequence,
        atEpochMs = 1_000 + sequence,
        reason = reason,
        economyPeriod = sequence,
        priceIndexPercent = priceIndexPercent,
        cash = 10_000,
        deposit = 0,
        loan = loan,
        funds = 0,
        businessValue = businessValue,
        shareValue = 0,
        total = total,
        cashFlow = cashFlow,
        creditExpenses = creditExpenses,
        livingExpenses = 3_000,
    )
}
