package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.design.DesignShapes
import ua.vald_zx.game.rat.race.card.designV2Enabled

/**
 * Кутовий штамп карти: літера роду й номер. У новій мові — латунна печатка,
 * у старій — чорний квадрат.
 */
@Composable
fun CardStamp(
    glyph: String,
    unitTS: TextUnit,
    unitDp: Dp,
    id: Int? = null,
    glyphSize: Float = 20f,
) {
    if (designV2Enabled.value) {
        DesignCardStamp(glyph, unitTS, unitDp, id, glyphSize)
    } else {
        LegacyCardStamp(glyph, unitTS, unitDp, id, glyphSize)
    }
}

@Composable
internal fun Stamp(
    glyph: String,
    unitTS: TextUnit,
    unitDp: Dp,
    id: Int?,
    glyphSize: Float,
    background: Color,
    ink: Color,
    rounded: Boolean,
) {
    Column(
        modifier = Modifier
            .then(if (rounded) Modifier.clip(DesignShapes.sm) else Modifier)
            .background(background)
            .size(unitDp * 40),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (id != null) {
            Text(
                text = "#$id",
                color = ink,
                fontSize = unitTS * 10,
                lineHeight = unitTS * 7,
                modifier = Modifier.align(Alignment.End),
            )
        }
        Text(
            text = glyph,
            color = ink,
            fontSize = unitTS * glyphSize,
            lineHeight = unitTS * glyphSize * 0.85f,
            fontWeight = FontWeight.Bold,
        )
    }
}
