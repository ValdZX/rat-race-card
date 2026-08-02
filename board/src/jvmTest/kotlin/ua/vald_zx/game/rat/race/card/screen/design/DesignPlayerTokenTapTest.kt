package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.shared.Player
import ua.vald_zx.game.rat.race.card.shared.PlayerAttributes
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DesignPlayerTokenTapTest {

    private val player = Player(id = "2", boardId = "b", attrs = PlayerAttributes(0xFF3355AA, 0))
    private val spot = DpSize(40.dp, 40.dp)

    @Test
    fun onlyOtherPlayersTokensAreTappable() = runComposeUiTest {
        var taps = 0
        setContent {
            AppTheme(forceDark = true) {
                Row {
                    Box(Modifier.size(spot).testTag("mine")) {
                        DesignPlayerToken(
                            player = player,
                            isCurrentPlayer = true,
                            isActivePlayer = false,
                            spotSize = spot,
                            onClick = null,
                        )
                    }
                    Box(Modifier.size(spot).testTag("theirs")) {
                        DesignPlayerToken(
                            player = player,
                            isCurrentPlayer = false,
                            isActivePlayer = false,
                            spotSize = spot,
                            onClick = { taps++ },
                        )
                    }
                }
            }
        }
        waitForIdle()

        onNodeWithTag("mine").performClick()
        assertEquals(0, taps)

        onNodeWithTag("theirs").performClick()
        assertEquals(1, taps)
    }
}
