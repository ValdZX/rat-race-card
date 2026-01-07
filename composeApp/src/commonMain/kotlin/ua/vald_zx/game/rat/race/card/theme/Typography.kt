package ua.vald_zx.game.rat.race.card.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import rat_race_card.composeapp.generated.resources.*

@Composable
fun montserratFontFamily() = FontFamily(
    Font(
        resource = Res.font.Montserrat_Black,
        weight = FontWeight.Black
    ),
    Font(
        resource = Res.font.Montserrat_BlackItalic,
        weight = FontWeight.Black,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.Montserrat_Bold,
        weight = FontWeight.Bold
    ),
    Font(
        resource = Res.font.Montserrat_BoldItalic,
        weight = FontWeight.Bold,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.Montserrat_ExtraBold,
        weight = FontWeight.ExtraBold
    ),
    Font(
        resource = Res.font.Montserrat_ExtraBoldItalic,
        weight = FontWeight.ExtraBold,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.Montserrat_ExtraLight,
        weight = FontWeight.ExtraLight
    ),
    Font(
        resource = Res.font.Montserrat_ExtraLightItalic,
        weight = FontWeight.ExtraLight,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.Montserrat_Italic,
        weight = FontWeight.Normal,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.Montserrat_Light,
        weight = FontWeight.Light
    ),
    Font(
        resource = Res.font.Montserrat_LightItalic,
        weight = FontWeight.Light,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.Montserrat_Medium,
        weight = FontWeight.Medium
    ),
    Font(
        resource = Res.font.Montserrat_MediumItalic,
        weight = FontWeight.Medium,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.Montserrat_Regular,
        weight = FontWeight.Normal
    ),
    Font(
        resource = Res.font.Montserrat_SemiBold,
        weight = FontWeight.SemiBold
    ),
    Font(
        resource = Res.font.Montserrat_SemiBoldItalic,
        weight = FontWeight.SemiBold,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.Montserrat_Thin,
        weight = FontWeight.Thin
    ),
    Font(
        resource = Res.font.Montserrat_ThinItalic,
        weight = FontWeight.Thin,
        style = FontStyle.Italic
    )
)

@Composable
fun montserratTypography(): Typography {
    val montserratFontFamily = montserratFontFamily()
    return Typography(
        displayLarge = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.1.sp
        ),
        titleSmall = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = montserratFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}