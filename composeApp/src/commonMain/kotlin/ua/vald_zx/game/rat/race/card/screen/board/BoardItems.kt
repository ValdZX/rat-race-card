package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.ui.graphics.Color


sealed class PlaceType(val name: String, val color: Color, val isBig: Boolean = false) {
    data object Start : PlaceType("Start", Color.Yellow)
    data object Salary : PlaceType("Salary", Color(0xffa1e64c), isBig = true)
    data object Business : PlaceType("Business", Color(0xff00ba87))
    data object Shopping : PlaceType("Shopping", Color.Cyan)
    data object Chance : PlaceType("Chance", Color(0xfff36d4e))
    data object Expenses : PlaceType("Expenses", Color.Red)
    data object Store : PlaceType("Store", Color(0xff0788e8))
    data object Bankruptcy : PlaceType("Bankruptcy", Color(0xff94a5dd), isBig = true)
    data object Child : PlaceType("Child", Color.Black, isBig = true)
    data object Love : PlaceType("Love", Color.Magenta)
    data object Rest : PlaceType("Rest", Color.White)
    data object Divorce : PlaceType("Divorce", Color.Red)
    data object Desire : PlaceType("Desire", Color(0xffde9bc2))
    data object Deputy : PlaceType("Deputy", Color(0xff8a8fdc))
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
    PlaceType.Business,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.Business,
    PlaceType.Store,
    PlaceType.Bankruptcy,
    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Business,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Salary,

    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Business,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Business,
    PlaceType.Deputy,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Salary,

    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.Business,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Desire,
    PlaceType.Business,
    PlaceType.Chance,
    PlaceType.TaxInspection,
    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Business,
    PlaceType.Desire,
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Desire,
    PlaceType.Store,
    PlaceType.Salary,

    PlaceType.Desire,
    PlaceType.Shopping,
    PlaceType.Business,
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