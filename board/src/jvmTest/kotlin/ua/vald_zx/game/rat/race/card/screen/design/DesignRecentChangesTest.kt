package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.shared.Business
import ua.vald_zx.game.rat.race.card.shared.BusinessType
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.shared.PlayerCard
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DesignRecentChangesTest {

    private val headerHeight = 74.dp

    private val player = Player(
        id = "1",
        boardId = "b",
        attrs = PlayerAttributes(0xFF3355AA, 0),
        card = PlayerCard(name = "Олена", profession = "Інженер", salary = 4900),
        cash = 15_700,
        businesses = listOf(
            Business(type = BusinessType.SMALL, name = "Кафе", price = 95_000, profit = 3_800)
        ),
        lastTotals = listOf(-1_400, 9_500, 3_200),
        lastCashFlows = listOf(120, -60, 380),
    )

    @Test
    fun headerShowsThreeLastChangesForBothAmounts() = runComposeUiTest {
        setContent {
            AppTheme(forceDark = true) {
                Box(
                    Modifier
                        .size(360.dp, 140.dp)
                        .background(Design.scaffold.surface1)
                        .testTag("header"),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(headerHeight).testTag("row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(Modifier.weight(1f).height(40.dp).testTag("name"))
                        BalanceRow(player)
                    }
                }
            }
        }
        waitForIdle()

        listOf("−1 400", "+9 500", "+3 200", "+120", "−60", "+380").forEach { change ->
            onNodeWithText(change, useUnmergedTree = true).assertExists()
        }

        val name = onNodeWithTag("name").getBoundsInRoot()
        assertTrue(
            name.right - name.left >= 100.dp,
            "на імʼя гравця лишилось ${name.right - name.left} — шапка переповнена",
        )

        val row = onNodeWithTag("row").getBoundsInRoot()
        listOf("−1 400", "+9 500", "+3 200", "+120", "−60", "+380").forEach { change ->
            val bounds = onNodeWithText(change, useUnmergedTree = true).getBoundsInRoot()
            assertTrue(
                bounds.bottom <= row.bottom,
                "зміну $change обрізало по висоті шапки: ${bounds.bottom} > ${row.bottom}",
            )
            assertTrue(bounds.right <= row.right, "зміну $change обрізало вбік")
        }

        val image = onNodeWithTag("header").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-recent-changes.png"))
    }
}
