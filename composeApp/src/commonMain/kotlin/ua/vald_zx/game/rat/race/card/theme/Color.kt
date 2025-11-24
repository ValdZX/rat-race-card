package ua.vald_zx.game.rat.race.card.theme

import androidx.compose.runtime.*
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
val surfaceLight = Color(0xFFFFFFFF)
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
    chance: Color,
    business: Color,
    bigBusiness: Color,
    smallBusiness: Color,
    expenses: Color,
    store: Color,
    shopping: Color,
    deputy: Color,
    desire: Color,
    divorce: Color,
    bankruptcy: Color,
    salary: Color,
    inspection: Color,
    exaltation: Color,
    love: Color,
    rest: Color,
    start: Color,
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

    var chance by mutableStateOf(chance)
        private set
    var business by mutableStateOf(business)
        private set
    var bigBusiness by mutableStateOf(bigBusiness)
        private set
    var smallBusiness by mutableStateOf(smallBusiness)
        private set
    var expenses by mutableStateOf(expenses)
        private set
    var store by mutableStateOf(store)
        private set
    var shopping by mutableStateOf(shopping)
        private set
    var deputy by mutableStateOf(deputy)
        private set
    var desire by mutableStateOf(desire)
        private set
    var divorce by mutableStateOf(divorce)
        private set
    var bankruptcy by mutableStateOf(bankruptcy)
        private set
    var salary by mutableStateOf(salary)
        private set
    var inspection by mutableStateOf(inspection)
        private set
    var exaltation by mutableStateOf(exaltation)
        private set
    var love by mutableStateOf(love)
        private set
    var rest by mutableStateOf(rest)
        private set
    var start by mutableStateOf(start)
        private set

    fun update(other: AppColors) {
        cash = other.cash
        positive = other.positive
        negative = other.negative
        action = other.action
        buy = other.buy
        family = other.family
        funds = other.funds
        chance = other.chance
        business = other.business
        bigBusiness = other.bigBusiness
        smallBusiness = other.smallBusiness
        expenses = other.expenses
        store = other.store
        shopping = other.shopping
        deputy = other.deputy
        desire = other.desire
        divorce = other.divorce
        bankruptcy = other.bankruptcy
        salary = other.salary
        inspection = other.inspection
        exaltation = other.exaltation
        love = other.love
        rest = other.rest
        start = other.start
        isDark = other.isDark
    }

    fun copy() = AppColors(
        cash = cash,
        positive = positive,
        negative = negative,
        action = action,
        buy = buy,
        family = family,
        funds = funds,
        chance = chance,
        business = business,
        bigBusiness = bigBusiness,
        smallBusiness = smallBusiness,
        expenses = expenses,
        store = store,
        shopping = shopping,
        deputy = deputy,
        desire = desire,
        divorce = divorce,
        bankruptcy = bankruptcy,
        salary = salary,
        inspection = inspection,
        exaltation = exaltation,
        love = love,
        rest = rest,
        start = start,
        isDark = isDark,
    )
}

internal val LightIfobsColors = AppColors(
    cash = Color(0xFF64b06c),
    positive = Color(0xFFbaffc9),
    negative = Color(0xFFffb3ba),
    action = Color(0xFFffffba),
    buy = Color(0xFFffdfba),
    family = Color(0xFFbae1ff),
    funds = Color(0xFFb4a7d6),
    chance = Color(0xffFFAC1C),
    business = Color(0xff00cc00),
    bigBusiness = Color(0xff2db300),
    smallBusiness = Color(0xffb3ff99),
    expenses = Color(0xFFFF0000),
    store = Color(0xff0788e8),
    shopping = Color(0xFF00FFFF),
    deputy = Color(0xff8a8fdc),
    desire = Color(0xffde9bc2),
    divorce = Color(0xFF0000FF),
    bankruptcy = Color(0xff94a5dd),
    salary = Color(0xffa1e64c),
    inspection = Color(0xffc5dcc7),
    exaltation = Color(0xFF000000),
    love = Color(0xFFFF00FF),
    rest = Color(0xFFFFFFFF),
    start = Color(0xFFFFFF00),
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
    chance = Color(0xFFB37400),
    business = Color(0xFF248F24),
    bigBusiness = Color(0xFF1A661A),
    smallBusiness = Color(0xFF336633),
    expenses = Color(0xFFB22222),
    store = Color(0xFF1668B2),
    shopping = Color(0xFF1A7D7D),
    deputy = Color(0xFF4D53A3),
    desire = Color(0xFF995C84),
    divorce = Color(0xFF2020B2),
    bankruptcy = Color(0xFF5B6D99),
    salary = Color(0xFF7A9C30),
    inspection = Color(0xFF6E8B76),
    exaltation = Color(0xFFAAAAAA),
    love = Color(0xFF884488),
    rest = Color(0xFF888888),
    start = Color(0xFF998C1A),
    isDark = true,
)