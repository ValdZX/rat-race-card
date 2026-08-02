package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
internal fun LegacyCardStamp(glyph: String, unitTS: TextUnit, unitDp: Dp, id: Int?, glyphSize: Float) {
    Stamp(
        glyph = glyph,
        unitTS = unitTS,
        unitDp = unitDp,
        id = id,
        glyphSize = glyphSize,
        background = Color.Black,
        ink = Color.White,
        rounded = false,
    )
}
