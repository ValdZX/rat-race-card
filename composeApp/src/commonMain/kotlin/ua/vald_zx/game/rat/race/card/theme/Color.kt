package ua.vald_zx.game.rat.race.card.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
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