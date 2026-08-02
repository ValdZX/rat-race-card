package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import ua.vald_zx.game.rat.race.card.components.Rotation
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.components.rotateLayout
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.shared.BoardCardType

@Composable
fun DesignCardBack(card: BoardCardType, size: DpSize, isVertical: Boolean, modifier: Modifier) {
    val trueHeight = min(size.width, size.height)
    val shape = RoundedCornerShape(trueHeight / 8)
    val tone = card.tone()
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
            .background(tone.fill)
            .border(trueHeight / 40f, tone.edge, shape),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = card.shortLabel(),
            style = Design.type.cellLg.copy(
                color = Design.scaffold.onFill,
                textAlign = TextAlign.Center,
            ),
            autoSize = TextAutoSize.StepBased(minFontSize = 6.sp, maxFontSize = 22.sp),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .padding(trueHeight / 10)
                .optionalModifier(isVertical) { rotateLayout(Rotation.ROT_90) },
        )
    }
}
