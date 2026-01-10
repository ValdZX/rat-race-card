package ua.vald_zx.game.rat.race.card.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicMaterialThemeState


val Primary = Color(0xFFA2FF82)

internal val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }

@Composable
internal fun AppTheme(
    content: @Composable() () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val isDarkState = remember { mutableStateOf(systemInDarkTheme) }
    val isDark by isDarkState
    val typography = montserratTypography()
    val colors = if (isDark) DarkIfobsColors else LightIfobsColors
    val colorPalette = remember { colors.copy() }
    colorPalette.update(colors)
    val dynamicThemeState = rememberDynamicMaterialThemeState(
        isDark = isDark,
        style = PaletteStyle.TonalSpot,
        primary = Primary,
    )
    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState,
        LocalAppColors provides colorPalette
    ) {
        DynamicMaterialTheme(
            state = dynamicThemeState,
            typography = typography,
            animate = true,
            content = { Surface(content = content) },
        )
    }
}

object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}

@Composable
internal expect fun SystemAppearance(isDark: Boolean)
