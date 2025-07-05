package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.ui.graphics.Color


val cnanceColor = Color(0xffFFAC1C)

val businessColor = Color(0xff00cc00)
val bigBusinessColor = Color(0xff2db300)
val expensesColor = Color.Red

val storeColor = Color(0xff0788e8)
val shoppingColor = Color.Cyan

val deputyColor = Color(0xff8a8fdc)

enum class BoardCardType {
    Chance,
    SmallBusiness,
    MediumBusiness,
    BigBusiness,
    Expenses,
    EventStore,
    Shopping,
    Deputy,
}

val BoardCardType.color: Color
    get() {
        return when (this) {
            BoardCardType.Chance -> cnanceColor
            BoardCardType.SmallBusiness -> Color(0xffb3ff99)
            BoardCardType.MediumBusiness -> businessColor
            BoardCardType.BigBusiness -> bigBusinessColor
            BoardCardType.Expenses -> expensesColor
            BoardCardType.EventStore -> storeColor
            BoardCardType.Shopping -> shoppingColor
            BoardCardType.Deputy -> deputyColor
        }
    }


sealed class PlaceType(val name: String, val color: Color, val isBig: Boolean = false) {
    data object Start : PlaceType("Start", Color.Yellow)
    data object Salary : PlaceType("Salary", Color(0xffa1e64c), isBig = true)

    data object Business : PlaceType("Business", businessColor)
    data object BigBusiness : PlaceType("BigBusiness", bigBusinessColor)
    data object Shopping : PlaceType("Shopping", shoppingColor)

    data object Chance : PlaceType("Chance", cnanceColor)
    data object Expenses : PlaceType("Expenses", expensesColor)

    data object Store : PlaceType("Store", storeColor)
    data object Bankruptcy : PlaceType("Bankruptcy", Color(0xff94a5dd), isBig = true)
    data object Child : PlaceType("Child", Color.Black, isBig = true)
    data object Love : PlaceType("Love", Color.Magenta)
    data object Rest : PlaceType("Rest", Color.White)
    data object Divorce : PlaceType("Divorce", Color.Red)
    data object Desire : PlaceType("Desire", Color(0xffde9bc2))

    data object Deputy : PlaceType("Deputy", deputyColor)
    data object TaxInspection : PlaceType("TaxInspection", Color(0xffc5dcc7), isBig = true)
    data object Exaltation : PlaceType("Exaltation", Color.Black)
}


val inPlaces = listOf(
    PlaceType.Salary,
    PlaceType.Start,
    PlaceType.Business,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Business,
    PlaceType.Bankruptcy,
    PlaceType.Store,
    PlaceType.Expenses,
    PlaceType.Business,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,

    PlaceType.Salary,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Love,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Rest,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,

    PlaceType.Salary,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Business,
    PlaceType.Child,
    PlaceType.Expenses,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Divorce,
    PlaceType.Shopping,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Expenses,

    PlaceType.Salary,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Business,
    PlaceType.Exaltation,
    PlaceType.Expenses,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Business,
    PlaceType.Love,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Expenses,
)

val outPlaces = listOf(
    PlaceType.Salary,
    PlaceType.Start,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.BigBusiness,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.BigBusiness,
    PlaceType.Store,
    PlaceType.Bankruptcy,
    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.BigBusiness,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Salary,

    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.BigBusiness,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.BigBusiness,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Salary,

    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.BigBusiness,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.BigBusiness,
    PlaceType.Chance,
    PlaceType.TaxInspection,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.BigBusiness,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Salary,

    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.BigBusiness,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
)