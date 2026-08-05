package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertTrue

class BoardPanelLayoutTest {
    @Test
    fun boardReservesAStableMarginForFloatingOverlays() {
        val frame = DpSize(960.dp, 700.dp)
        val content = frame.boardContentSize()

        assertTrue((frame.width - content.width) / 2 >= 12.dp)
        assertTrue((frame.height - content.height) / 2 >= 12.dp)
        assertTrue(kotlin.math.abs(frame.width / frame.height - content.width / content.height) < 0.001f)
    }
}
