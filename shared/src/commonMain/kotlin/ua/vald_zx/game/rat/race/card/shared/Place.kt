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
    data class Desire(val dreamId: String) : PlaceType("Desire")
    data object Deputy : PlaceType("Deputy")
    data object TaxInspection : PlaceType("TaxInspection", isBig = true)
    data object Resignation : PlaceType("Exaltation")
}

private const val DESIRE_CODE_PREFIX = "Desire:"

private val placeTypesByName = listOf(
    PlaceType.Start,
    PlaceType.Salary,
    PlaceType.Business,
    PlaceType.BigBusiness,
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Expenses,
    PlaceType.Store,
    PlaceType.Bankruptcy,
    PlaceType.Child,
    PlaceType.Love,
    PlaceType.Rest,
    PlaceType.Divorce,
    PlaceType.Deputy,
    PlaceType.TaxInspection,
    PlaceType.Resignation,
).associateBy { it.name }

fun PlaceType.code(): String =
    if (this is PlaceType.Desire) DESIRE_CODE_PREFIX + dreamId else name

fun placeTypeOfCode(code: String): PlaceType? =
    if (code.startsWith(DESIRE_CODE_PREFIX)) {
        PlaceType.Desire(code.removePrefix(DESIRE_CODE_PREFIX))
    } else {
        placeTypesByName[code]
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
    PlaceType.Resignation,
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
    PlaceType.Desire("world_trip"),
    PlaceType.Shopping,
    PlaceType.BigBusiness,
    PlaceType.Desire("education_center"),
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Desire("charity_foundation"),
    PlaceType.BigBusiness,
    PlaceType.Store,
    PlaceType.Bankruptcy,
    PlaceType.Chance,
    PlaceType.Desire("film_studio"),
    PlaceType.Shopping,
    PlaceType.Desire("eco_village"),
    PlaceType.BigBusiness,
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire("children_centers"),
    PlaceType.Shopping,
    PlaceType.Desire("winery"),
    PlaceType.Salary,

    PlaceType.Desire("football_club"),
    PlaceType.Shopping,
    PlaceType.BigBusiness,
    PlaceType.Deputy,
    PlaceType.Desire("island_resort"),
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire("polar_expedition"),
    PlaceType.Shopping,
    PlaceType.BigBusiness,
    PlaceType.Deputy,
    PlaceType.Desire("modern_art_museum"),
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Salary,

    PlaceType.Chance,
    PlaceType.Desire("private_university"),
    PlaceType.BigBusiness,
    PlaceType.Shopping,
    PlaceType.Desire("medical_foundation"),
    PlaceType.Store,
    PlaceType.Chance,
    PlaceType.Desire("nature_reserve"),
    PlaceType.BigBusiness,
    PlaceType.Chance,
    PlaceType.TaxInspection,
    PlaceType.Desire("private_island"),
    PlaceType.Shopping,
    PlaceType.Desire("world_regatta"),
    PlaceType.Store,
    PlaceType.BigBusiness,
    PlaceType.Desire("space_trip"),
    PlaceType.Chance,
    PlaceType.Shopping,
    PlaceType.Desire("research_institute"),
    PlaceType.Store,
    PlaceType.Salary,

    PlaceType.Desire("future_city"),
    PlaceType.Shopping,
    PlaceType.BigBusiness,
    PlaceType.Deputy,
    PlaceType.Desire("global_media"),
    PlaceType.Chance,
    PlaceType.Store,
    PlaceType.Desire("ocean_station"),
    PlaceType.Shopping,
    PlaceType.Chance,
    PlaceType.Deputy,
    PlaceType.Desire("mars_mission"),
    PlaceType.Chance,
    PlaceType.Store,
)
