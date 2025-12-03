package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

@Serializable
sealed class PlaceType(val name: String, val isBig: Boolean = false) {
    data object Start : PlaceType("Start")
    data object Salary : PlaceType("Salary", isBig = true)

    data object Business : PlaceType("Business")
    data object BigBusiness : PlaceType("BigBusiness")
    data object Shopping : PlaceType("Shopping")

    data object Chance : PlaceType("Chance")
    data object Expenses : PlaceType("Expenses")

    data object Store : PlaceType("Store")
    data object Bankruptcy : PlaceType("Bankruptcy", isBig = true)
    data object Child : PlaceType("Child", isBig = true)
    data object Love : PlaceType("Love")
    data object Rest : PlaceType("Rest")
    data object Divorce : PlaceType("Divorce")
    data object Desire : PlaceType("Desire")
    data object Deputy : PlaceType("Deputy")
    data object TaxInspection : PlaceType("TaxInspection", isBig = true)
    data object Exaltation : PlaceType("Exaltation")
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

