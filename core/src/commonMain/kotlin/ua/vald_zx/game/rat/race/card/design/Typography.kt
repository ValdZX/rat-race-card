package ua.vald_zx.game.rat.race.card.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import ua.vald_zx.game.rat.race.card.resources.Montserrat_Bold
import ua.vald_zx.game.rat.race.card.resources.Montserrat_ExtraBold
import ua.vald_zx.game.rat.race.card.resources.Montserrat_Medium
import ua.vald_zx.game.rat.race.card.resources.Montserrat_SemiBold
import ua.vald_zx.game.rat.race.card.resources.Res

@Composable
private fun interfaceFamily() = FontFamily(
    Font(Res.font.Montserrat_Medium, FontWeight.Medium),
    Font(Res.font.Montserrat_SemiBold, FontWeight.SemiBold),
    Font(Res.font.Montserrat_Bold, FontWeight.Bold),
    Font(Res.font.Montserrat_ExtraBold, FontWeight.ExtraBold),
)

@Composable
private fun cellFamily() = interfaceFamily()

@Composable
private fun amountFamily() = interfaceFamily()

@Immutable
data class DesignTypography(
    val display: TextStyle,
    val amountXl: TextStyle,
    val amountLg: TextStyle,
    val amountMd: TextStyle,
    val title: TextStyle,
    val subtitle: TextStyle,
    val body: TextStyle,
    val label: TextStyle,
    val micro: TextStyle,
    val cellLg: TextStyle,
    val cellSm: TextStyle,
    val monoMeta: TextStyle,
)

@Composable
fun designTypography(): DesignTypography {
    val ui = interfaceFamily()
    val cell = cellFamily()
    val amount = amountFamily()
    return DesignTypography(
        display = TextStyle(ui, FontWeight.ExtraBold, 34.sp, lineHeight = 38.sp, letterSpacing = (-0.02).em),
        amountXl = TextStyle(amount, FontWeight.Bold, 38.sp, lineHeight = 40.sp),
        amountLg = TextStyle(amount, FontWeight.Bold, 26.sp, lineHeight = 30.sp),
        amountMd = TextStyle(amount, FontWeight.Bold, 19.sp, lineHeight = 24.sp),
        title = TextStyle(ui, FontWeight.ExtraBold, 20.sp, lineHeight = 26.sp, letterSpacing = (-0.01).em),
        subtitle = TextStyle(ui, FontWeight.Bold, 16.sp, lineHeight = 22.sp),
        body = TextStyle(ui, FontWeight.Medium, 14.sp, lineHeight = 20.sp),
        label = TextStyle(ui, FontWeight.Bold, 12.sp, lineHeight = 16.sp, letterSpacing = 0.04.em),
        micro = TextStyle(ui, FontWeight.ExtraBold, 11.sp, lineHeight = 14.sp, letterSpacing = 0.06.em),
        cellLg = TextStyle(cell, FontWeight.Bold, 13.sp, lineHeight = 14.sp, letterSpacing = 0.03.em),
        cellSm = TextStyle(cell, FontWeight.SemiBold, 11.sp, lineHeight = 12.sp, letterSpacing = 0.02.em),
        monoMeta = TextStyle(amount, FontWeight.Medium, 11.sp, lineHeight = 14.sp, letterSpacing = 0.02.em),
    )
}

private fun TextStyle(
    family: FontFamily,
    weight: FontWeight,
    size: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    letterSpacing = letterSpacing,
)

val LocalDesignTypography = staticCompositionLocalOf<DesignTypography> {
    error("DesignTypography not provided — обгорни вміст у AppTheme")
}
