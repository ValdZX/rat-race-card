package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import ua.vald_zx.game.rat.race.card.design.Design

@Composable
internal fun DesignCardStamp(glyph: String, unitTS: TextUnit, unitDp: Dp, id: Int?, glyphSize: Float) {
    Stamp(
        glyph = glyph,
        unitTS = unitTS,
        unitDp = unitDp,
        id = id,
        glyphSize = glyphSize,
        background = Design.scaffold.brass,
        ink = Design.scaffold.brassInk,
        rounded = true,
    )
}
