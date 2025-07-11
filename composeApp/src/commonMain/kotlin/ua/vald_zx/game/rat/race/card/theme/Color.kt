package ua.vald_zx.game.rat.race.card.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val primaryLight = Color(0xFF79590C)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFFFFDEA4)
val onPrimaryContainerLight = Color(0xFF5D4200)
val secondaryLight = Color(0xFF6C5C3F)
val onSecondaryLight = Color(0xFFFFFFFF)
val secondaryContainerLight = Color(0xFFF6E0BB)
val onSecondaryContainerLight = Color(0xFF53452A)
val tertiaryLight = Color(0xFF4C6545)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFFCDEBC2)
val onTertiaryContainerLight = Color(0xFF344D2F)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF93000A)
val backgroundLight = Color(0xFFFFF8F2)
val onBackgroundLight = Color(0xFF201B13)
val surfaceLight = Color(0xFFFFF8F2)
val onSurfaceLight = Color(0xFF201B13)
val surfaceVariantLight = Color(0xFFEEE1CF)
val onSurfaceVariantLight = Color(0xFF4E4639)
val outlineLight = Color(0xFF7F7667)
val outlineVariantLight = Color(0xFFD1C5B4)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF353027)
val inverseOnSurfaceLight = Color(0xFFFAEFE2)
val inversePrimaryLight = Color(0xFFEBC06C)
val surfaceDimLight = Color(0xFFE3D9CC)
val surfaceBrightLight = Color(0xFFFFF8F2)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFFDF2E5)
val surfaceContainerLight = Color(0xFFF7ECDF)
val surfaceContainerHighLight = Color(0xFFF1E7D9)
val surfaceContainerHighestLight = Color(0xFFEBE1D4)

val primaryDark = Color(0xFFEBC06C)
val onPrimaryDark = Color(0xFF412D00)
val primaryContainerDark = Color(0xFF5D4200)
val onPrimaryContainerDark = Color(0xFFFFDEA4)
val secondaryDark = Color(0xFFD9C4A0)
val onSecondaryDark = Color(0xFF3B2F15)
val secondaryContainerDark = Color(0xFF53452A)
val onSecondaryContainerDark = Color(0xFFF6E0BB)
val tertiaryDark = Color(0xFFB2CFA8)
val onTertiaryDark = Color(0xFF1E361A)
val tertiaryContainerDark = Color(0xFF344D2F)
val onTertiaryContainerDark = Color(0xFFCDEBC2)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF17130B)
val onBackgroundDark = Color(0xFFEBE1D4)
val surfaceDark = Color(0xFF17130B)
val onSurfaceDark = Color(0xFFEBE1D4)
val surfaceVariantDark = Color(0xFF4E4639)
val onSurfaceVariantDark = Color(0xFFD1C5B4)
val outlineDark = Color(0xFF9A8F80)
val outlineVariantDark = Color(0xFF4E4639)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFEBE1D4)
val inverseOnSurfaceDark = Color(0xFF353027)
val inversePrimaryDark = Color(0xFF79590C)
val surfaceDimDark = Color(0xFF17130B)
val surfaceBrightDark = Color(0xFF3E382F)
val surfaceContainerLowestDark = Color(0xFF120E07)
val surfaceContainerLowDark = Color(0xFF201B13)
val surfaceContainerDark = Color(0xFF241F17)
val surfaceContainerHighDark = Color(0xFF2E2921)
val surfaceContainerHighestDark = Color(0xFF3A342B)


internal val seed = Color(0xFF2C3639)

internal val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No ColorPalette provided")
}

@Stable
class AppColors(
    cash: Color,
    positive: Color,
    negative: Color,
    action: Color,
    buy: Color,
    family: Color,
    funds: Color,
    isDark: Boolean,
) {
    var cash by mutableStateOf(cash)
        private set
    var positive by mutableStateOf(positive)
        private set
    var negative by mutableStateOf(negative)
        private set
    var action by mutableStateOf(action)
        private set
    var buy by mutableStateOf(buy)
        private set
    var family by mutableStateOf(family)
        private set
    var funds by mutableStateOf(funds)
        private set
    var isDark by mutableStateOf(isDark)
        private set

    fun update(other: AppColors) {
        cash = other.cash
        positive = other.positive
        negative = other.negative
        action = other.action
        buy = other.buy
        family = other.family
        funds = other.funds
        isDark = other.isDark
    }
}

internal val LightIfobsColors = AppColors(
    cash = Color(0xFF64b06c),
    positive = Color(0xFFbaffc9),
    negative = Color(0xFFffb3ba),
    action = Color(0xFFffffba),
    buy = Color(0xFFffdfba),
    family = Color(0xFFbae1ff),
    funds = Color(0xFFb4a7d6),
    isDark = false
)

internal val DarkIfobsColors = AppColors(
    cash = Color(0xFF8eca92),
    positive = Color(0xFF38761d),
    negative = Color(0xFF990100),
    action = Color(0xFFc09001),
    buy = Color(0xFFb45f07),
    family = Color(0xFF0c5394),
    funds = Color(0xFF351c75),
    isDark = true,
)