package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import rat_race_card.composeapp.generated.resources.Bubbleboddy
import rat_race_card.composeapp.generated.resources.Res
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.OutlinedText
import ua.vald_zx.game.rat.race.card.components.Rotation
import ua.vald_zx.game.rat.race.card.components.optionalModifier
import ua.vald_zx.game.rat.race.card.components.rotateLayout

@Composable
fun BoxWithConstraintsScope.BoardCardFront(
    card: BoardCardType,
    modifier: Modifier = Modifier,
    discardCard: () -> Unit,
) {
    val width = maxWidth
    val height = maxHeight
    val rounding = min(width, height) / 16
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(rounding))
            .border(2.dp, card.color, RoundedCornerShape(rounding))
            .background(Color.White)
    ) {
        Button("Pass") {
            discardCard()
        }
    }
}

@Composable
fun BoxWithConstraintsScope.BoardCardBack(card: BoardCardType, modifier: Modifier = Modifier) {
    val width = maxWidth
    val height = maxHeight
    val rounding = min(width, height) / 16
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(rounding))
            .background(card.color)
    ) {
        OutlinedText(
            text = when (card) {
                BoardCardType.BigBusiness -> "Big Business"
                BoardCardType.Chance -> "Chance"
                BoardCardType.Deputy -> "Deputy"
                BoardCardType.EventStore -> "Event Store"
                BoardCardType.Expenses -> "Expenses"
                BoardCardType.MediumBusiness -> "Medium Business"
                BoardCardType.Shopping -> "Shopping"
                BoardCardType.SmallBusiness -> "Small Business"
            },
            autoSize = TextAutoSize.StepBased(minFontSize = 1.sp),
            fontFamily = FontFamily(
                Font(
                    Res.font.Bubbleboddy,
                    weight = FontWeight.Medium
                )
            ),
            modifier = Modifier.align(Alignment.Center)
                .padding(min(width, height) / 14)
                .optionalModifier(width < height) {
                    rotateLayout(Rotation.ROT_90)
                },
            fillColor = Color.White,
            outlineColor = Color(0xFF8A8A8A),
            outlineDrawStyle = Stroke(2f),
            maxLines = 1
        )
    }
}