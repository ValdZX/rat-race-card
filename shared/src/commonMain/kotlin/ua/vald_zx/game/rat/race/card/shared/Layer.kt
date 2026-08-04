package ua.vald_zx.game.rat.race.card.shared

enum class BoardLayer(val cellCount: Int, val level: Int, val places: List<PlaceType>) {
    INNER(inPlaces.size, 0, inPlaces),
    OUTER(outPlaces.size, 1, outPlaces),
}

fun Int.toLayer(): BoardLayer {
    return BoardLayer.entries.find { it.level == this } ?: BoardLayer.INNER
}

fun List<PlaceType>.nextPositionOf(type: PlaceType, from: Int): Int {
    for (step in 1..size) {
        val index = (from + step) % size
        if (this[index] == type) return index
    }
    return from
}

fun BoardLayer.nextPositionOf(type: PlaceType, from: Int): Int = places.nextPositionOf(type, from)
