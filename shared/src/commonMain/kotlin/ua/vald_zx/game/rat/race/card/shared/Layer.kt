package ua.vald_zx.game.rat.race.card.shared

enum class BoardLayer(val cellCount: Int, val level: Int, val places: List<PlaceType>) {
    INNER(inPlaces.size, 0, inPlaces),
    OUTER(outPlaces.size, 1, outPlaces),
}

fun Int.toLayer(): BoardLayer {
    return BoardLayer.entries.find { it.level == this } ?: BoardLayer.INNER
}