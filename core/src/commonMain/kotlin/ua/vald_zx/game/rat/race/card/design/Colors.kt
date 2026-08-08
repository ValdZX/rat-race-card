package ua.vald_zx.game.rat.race.card.design

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val DesignSeed = Color(0xFF0F7A3D)

val VividDesignSeed = Color(0xFF00C853)

@Immutable
data class SemanticTone(
    val fill: Color,
    val edge: Color,
)

@Immutable
data class SemanticColors(
    val cash: SemanticTone,
    val positive: SemanticTone,
    val salary: SemanticTone,
    val business: SemanticTone,
    val bigBusiness: SemanticTone,
    val smallBusiness: SemanticTone,
    val negative: SemanticTone,
    val expenses: SemanticTone,
    val bankruptcy: SemanticTone,
    val action: SemanticTone,
    val buy: SemanticTone,
    val chance: SemanticTone,
    val start: SemanticTone,
    val family: SemanticTone,
    val store: SemanticTone,
    val deputy: SemanticTone,
    val divorce: SemanticTone,
    val shopping: SemanticTone,
    val inspection: SemanticTone,
    val funds: SemanticTone,
    val desire: SemanticTone,
    val love: SemanticTone,
    val dream: SemanticTone,
    val exaltation: SemanticTone,
    val rest: SemanticTone,
)

@Immutable
data class ScaffoldColors(
    val background: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val surface4: Color,
    val outline: Color,
    val outlineStrong: Color,
    val onSurface: Color,
    val onSurfaceMuted: Color,
    val accent: Color,
    val accentDim: Color,
    val onAccent: Color,
    val brass: Color,
    val brassInk: Color,
    val onFill: Color,
    val shadow: Color,
)

@Immutable
data class DesignColors(
    val scaffold: ScaffoldColors,
    val semantic: SemanticColors,
    val isDark: Boolean,
)

private val DarkScaffold = ScaffoldColors(
    background = Color(0xFF090F0B),
    surface1 = Color(0xFF111814),
    surface2 = Color(0xFF19221D),
    surface3 = Color(0xFF212C26),
    surface4 = Color(0xFF2F3B34),
    outline = Color(0xFF2F3B34),
    outlineStrong = Color(0xFF4F5C54),
    onSurface = Color(0xFFF2F8F5),
    onSurfaceMuted = Color(0xFF9AA9A0),
    accent = Color(0xFF6EE895),
    accentDim = Color(0xFF2CBB67),
    onAccent = Color(0xFF09371B),
    brass = Color(0xFFE7B643),
    brassInk = Color(0xFF33240A),
    onFill = Color(0xFFF4F7F5),
    shadow = Color(0x8C000000),
)

private val LightScaffold = ScaffoldColors(
    background = Color(0xFFD8E1DB),
    surface1 = Color(0xFFF2F8F5),
    surface2 = Color(0xFFFFFFFF),
    surface3 = Color(0xFFF2F8F5),
    surface4 = Color(0xFFE6EDE9),
    outline = Color(0xFFC6D1CB),
    outlineStrong = Color(0xFFA1AFA6),
    onSurface = Color(0xFF0D1512),
    onSurfaceMuted = Color(0xFF4F5C54),
    accent = Color(0xFF006D2C),
    accentDim = Color(0xFF00924A),
    onAccent = Color(0xFFFFFFFF),
    brass = Color(0xFFB58300),
    brassInk = Color(0xFFFFFFFF),
    onFill = Color(0xFF141A18),
    shadow = Color(0x38142819),
)

private fun tone(fill: Long, edge: Long) = SemanticTone(Color(fill), Color(edge))

private val DarkSemantic = SemanticColors(
    cash = tone(0xFF1B683E, 0xFF5CA477),
    positive = tone(0xFF296818, 0xFF64A556),
    salary = tone(0xFF5E5D00, 0xFF979828),
    business = tone(0xFF036946, 0xFF53A57F),
    bigBusiness = tone(0xFF006E4A, 0xFF15AB83),
    smallBusiness = tone(0xFF3F6246, 0xFF789D7F),
    negative = tone(0xFF9E2225, 0xFFE3645E),
    expenses = tone(0xFF92361F, 0xFFD5725A),
    bankruptcy = tone(0xFF814151, 0xFFC17B8B),
    action = tone(0xFF7D4C00, 0xFFBB881A),
    buy = tone(0xFF933800, 0xFFD67523),
    chance = tone(0xFF7D4A07, 0xFFBC854D),
    start = tone(0xFF735400, 0xFFAF8F00),
    family = tone(0xFF1C5C8C, 0xFF5A98CB),
    store = tone(0xFF006095, 0xFF259ED6),
    deputy = tone(0xFF425490, 0xFF7A8FD0),
    divorce = tone(0xFF1E55A4, 0xFF5992E7),
    shopping = tone(0xFF006768, 0xFF42A3A3),
    inspection = tone(0xFF29616B, 0xFF659DA7),
    funds = tone(0xFF5E4793, 0xFF9882D4),
    desire = tone(0xFF793F75, 0xFFB879B2),
    love = tone(0xFF873761, 0xFFC8729C),
    dream = tone(0xFF753A8D, 0xFFB276CD),
    exaltation = tone(0xFF555473, 0xFF8E8EB0),
    rest = tone(0xFF54595E, 0xFF8E9398),
)

private val LightSemantic = SemanticColors(
    cash = tone(0xFFB4EEC7, 0xFF438C60),
    positive = tone(0xFFC2ECB9, 0xFF4D8C3E),
    salary = tone(0xFFE1E3A6, 0xFF808000),
    business = tone(0xFFAEEFCE, 0xFF388D68),
    bigBusiness = tone(0xFFA9F0D4, 0xFF00926B),
    smallBusiness = tone(0xFFC3EAC9, 0xFF618568),
    negative = tone(0xFFFFCAC3, 0xFFC74B47),
    expenses = tone(0xFFFFCCBB, 0xFFBA5A42),
    bankruptcy = tone(0xFFFFC8D7, 0xFFA76373),
    action = tone(0xFFFAD9A2, 0xFFA27000),
    buy = tone(0xFFFFD1AC, 0xFFBB5D00),
    chance = tone(0xFFFFD4A6, 0xFFA36D34),
    start = tone(0xFFEEDEA1, 0xFF967700),
    family = tone(0xFFB2E4FF, 0xFF4280B2),
    store = tone(0xFFAAE7FF, 0xFF0085BC),
    deputy = tone(0xFFC9DCFF, 0xFF6377B6),
    divorce = tone(0xFFBEE0FF, 0xFF417ACC),
    shopping = tone(0xFF9DEFEE, 0xFF238B8B),
    inspection = tone(0xFFB0EAF4, 0xFF4D848E),
    funds = tone(0xFFE1D4FF, 0xFF806AB9),
    desire = tone(0xFFFDCBF8, 0xFF9E6199),
    love = tone(0xFFFFC9E5, 0xFFAE5A84),
    dream = tone(0xFFF2CFFF, 0xFF995EB3),
    exaltation = tone(0xFFDADAFF, 0xFF777797),
    rest = tone(0xFFD9DFE5, 0xFF767B80),
)

private val VividDarkSemantic = SemanticColors(
    cash = tone(0xFF007A38, 0xFF2BE47E),
    positive = tone(0xFF2A7A00, 0xFF74F03A),
    salary = tone(0xFF6E6100, 0xFFE3D01F),
    business = tone(0xFF00754F, 0xFF19E0A0),
    bigBusiness = tone(0xFF007555, 0xFF00F5AE),
    smallBusiness = tone(0xFF3F7A1F, 0xFF9BE86B),
    negative = tone(0xFFB3001B, 0xFFFF5C6E),
    expenses = tone(0xFFB33A00, 0xFFFF7F42),
    bankruptcy = tone(0xFF9E0044, 0xFFFF5C96),
    action = tone(0xFF8A5C00, 0xFFFFC61A),
    buy = tone(0xFF9E4A00, 0xFFFF9126),
    chance = tone(0xFF8F5000, 0xFFFFAE33),
    start = tone(0xFF7A6A00, 0xFFFFE01F),
    family = tone(0xFF0F4FA8, 0xFF5AA8FF),
    store = tone(0xFF00648F, 0xFF00C2FF),
    deputy = tone(0xFF2A2FB3, 0xFF7C86FF),
    divorce = tone(0xFF0B47B3, 0xFF4D92FF),
    shopping = tone(0xFF00706F, 0xFF00E5E5),
    inspection = tone(0xFF00646E, 0xFF3FD5EC),
    funds = tone(0xFF5A18B3, 0xFFB07AFF),
    desire = tone(0xFF9E008F, 0xFFFF6BEC),
    love = tone(0xFFA8004F, 0xFFFF64A8),
    dream = tone(0xFF6E0FB3, 0xFFC97AFF),
    exaltation = tone(0xFF4442B3, 0xFF9E9CFF),
    rest = tone(0xFF44505C, 0xFF9FB0BE),
)

private val VividLightSemantic = SemanticColors(
    cash = tone(0xFF17D07A, 0xFF00794A),
    positive = tone(0xFF5DE02E, 0xFF2E7D00),
    salary = tone(0xFFE8E11A, 0xFF8A8300),
    business = tone(0xFF12C97E, 0xFF00734A),
    bigBusiness = tone(0xFF00E5A0, 0xFF00875E),
    smallBusiness = tone(0xFF8CE86B, 0xFF4F8F2E),
    negative = tone(0xFFFF4A4A, 0xFFB3000A),
    expenses = tone(0xFFFF6A2B, 0xFFB33000),
    bankruptcy = tone(0xFFFF3D7F, 0xFFB0003F),
    action = tone(0xFFFFC400, 0xFFB37A00),
    buy = tone(0xFFFF8A00, 0xFFB35400),
    chance = tone(0xFFFFA31A, 0xFFA85F00),
    start = tone(0xFFFFE000, 0xFFA38A00),
    family = tone(0xFF35B8FF, 0xFF00629E),
    store = tone(0xFF00B4FF, 0xFF0072B3),
    deputy = tone(0xFF6E7CFF, 0xFF2A2FB3),
    divorce = tone(0xFF2E8BFF, 0xFF0B4FB3),
    shopping = tone(0xFF00D8D8, 0xFF007A7A),
    inspection = tone(0xFF3FC7E8, 0xFF00708C),
    funds = tone(0xFFA45BFF, 0xFF5B18B3),
    desire = tone(0xFFFF5BE0, 0xFFA8009B),
    love = tone(0xFFFF4D94, 0xFFB3005A),
    dream = tone(0xFFC85BFF, 0xFF7A0FB3),
    exaltation = tone(0xFF8E8CFF, 0xFF4442B3),
    rest = tone(0xFFB8C4CC, 0xFF5A6672),
)

val DarkDesignColors = DesignColors(DarkScaffold, DarkSemantic, isDark = true)
val LightDesignColors = DesignColors(LightScaffold, LightSemantic, isDark = false)
val VividDarkDesignColors = DesignColors(DarkScaffold, VividDarkSemantic, isDark = true)
val VividLightDesignColors = DesignColors(LightScaffold, VividLightSemantic, isDark = false)

val LocalDesignColors = staticCompositionLocalOf { DarkDesignColors }
