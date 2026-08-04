package ua.vald_zx.game.rat.race.card.screen.board.deck.front

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.design.DesignShapes
import ua.vald_zx.game.rat.race.card.designV2Enabled

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
    Box(
        modifier = Modifier
            .then(if (rounded) Modifier.clip(DesignShapes.sm) else Modifier)
            .background(background)
            .size(unitDp * 40),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = unitDp * 3,
                    top = unitDp * if (id == null) 3 else 10,
                    end = unitDp * 3,
                    bottom = unitDp * 2,
                ),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = glyph,
                style = TextStyle(
                    color = ink,
                    fontSize = unitTS * glyphSize,
                    lineHeight = unitTS * glyphSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                ),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = unitTS * MIN_STAMP_GLYPH_SIZE,
                    maxFontSize = unitTS * glyphSize,
                ),
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (id != null) {
            BasicText(
                text = "#$id",
                style = TextStyle(
                    color = ink,
                    fontSize = unitTS * STAMP_ID_SIZE,
                    lineHeight = unitTS * STAMP_ID_SIZE,
                    textAlign = TextAlign.End,
                ),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = unitTS * MIN_STAMP_ID_SIZE,
                    maxFontSize = unitTS * STAMP_ID_SIZE,
                ),
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxWidth()
                    .padding(horizontal = unitDp * 2.5f, vertical = unitDp * 1.5f),
            )
        }
    }
}

private const val STAMP_ID_SIZE = 10f
private const val MIN_STAMP_ID_SIZE = 6f
private const val MIN_STAMP_GLYPH_SIZE = 5f
