package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.Serializable

@Serializable
enum class PlayerResource {
    CASH,
    DEPOSIT,
    DEPUTIES,
    BUSINESS_COUNT,
    SHARES_COUNT,
    LAND_AREA,
    ESTATE_COUNT,
    BABIES,
    CARS,
    APARTMENT,
    COTTAGE,
    YACHT,
    FLIGHT,
    ANIMAL,
    IN_REST,
}

fun Player.resource(resource: PlayerResource): Long = when (resource) {
    PlayerResource.CASH -> cash
    PlayerResource.DEPOSIT -> deposit
    PlayerResource.DEPUTIES -> deputies.toLong()
    PlayerResource.BUSINESS_COUNT -> businesses.count { it.type != BusinessType.WORK }.toLong()
    PlayerResource.SHARES_COUNT -> sharesList.sumOf { it.count }
    PlayerResource.LAND_AREA -> landList.sumOf { it.area }
    PlayerResource.ESTATE_COUNT -> estateList.size.toLong()
    PlayerResource.BABIES -> babies
    PlayerResource.CARS -> cars
    PlayerResource.APARTMENT -> apartment
    PlayerResource.COTTAGE -> cottage
    PlayerResource.YACHT -> yacht
    PlayerResource.FLIGHT -> flight
    PlayerResource.ANIMAL -> animal
    PlayerResource.IN_REST -> inRest
}

fun Player.withResource(resource: PlayerResource, value: Long): Player {
    val safe = value.coerceAtLeast(0)
    return when (resource) {
        PlayerResource.CASH -> copy(cash = value)
        PlayerResource.DEPOSIT -> copy(deposit = safe)
        PlayerResource.DEPUTIES -> copy(deputies = safe.toInt())
        PlayerResource.BABIES -> copy(babies = safe)
        PlayerResource.CARS -> copy(cars = safe)
        PlayerResource.APARTMENT -> copy(apartment = safe)
        PlayerResource.COTTAGE -> copy(cottage = safe)
        PlayerResource.YACHT -> copy(yacht = safe)
        PlayerResource.FLIGHT -> copy(flight = safe)
        PlayerResource.ANIMAL -> copy(animal = safe)
        PlayerResource.IN_REST -> copy(inRest = safe)
        PlayerResource.BUSINESS_COUNT,
        PlayerResource.SHARES_COUNT,
        PlayerResource.LAND_AREA,
        PlayerResource.ESTATE_COUNT -> this
    }
}

val PlayerResource.isDirectlySettable: Boolean
    get() = when (this) {
        PlayerResource.BUSINESS_COUNT,
        PlayerResource.SHARES_COUNT,
        PlayerResource.LAND_AREA,
        PlayerResource.ESTATE_COUNT -> false

        else -> true
    }

@Serializable
enum class PlayerPredicate {
    IS_MARRIED,
    IS_SINGLE,
    IS_MALE,
    IS_FEMALE,
    HAS_CHILDREN,
    HAS_JOB,
    HAS_BUSINESS,
    HAS_SHARES,
    HAS_LAND,
    HAS_ESTATE,
    HAS_CORRUPT_ASSET,
    ON_INNER_TRACK,
    ON_OUTER_TRACK,
}

fun Player.matches(predicate: PlayerPredicate, board: Board): Boolean = when (predicate) {
    PlayerPredicate.IS_MARRIED -> isMarried
    PlayerPredicate.IS_SINGLE -> !isMarried
    PlayerPredicate.IS_MALE -> card.gender == Gender.MALE
    PlayerPredicate.IS_FEMALE -> card.gender == Gender.FEMALE
    PlayerPredicate.HAS_CHILDREN -> babies > 0
    PlayerPredicate.HAS_JOB -> businesses.any { it.type == BusinessType.WORK }
    PlayerPredicate.HAS_BUSINESS -> businesses.any { it.type != BusinessType.WORK }
    PlayerPredicate.HAS_SHARES -> sharesList.isNotEmpty()
    PlayerPredicate.HAS_LAND -> landList.isNotEmpty()
    PlayerPredicate.HAS_ESTATE -> estateList.isNotEmpty()
    PlayerPredicate.HAS_CORRUPT_ASSET ->
        businesses.any { it.type == BusinessType.CORRUPTION } || landList.any(board::isCorruptLand)

    PlayerPredicate.ON_INNER_TRACK -> location.trackId == CoreTrackIds.Inner
    PlayerPredicate.ON_OUTER_TRACK -> location.trackId == CoreTrackIds.Outer
}

@Serializable
enum class AssetKind {
    BUSINESS,
    SHARES,
    LAND,
    ESTATE,
}

@Serializable
enum class AssetSelector {
    RANDOM,
    FIRST,
    ALL,
}
