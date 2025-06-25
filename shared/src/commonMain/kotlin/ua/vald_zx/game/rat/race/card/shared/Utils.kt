package ua.vald_zx.game.rat.race.card.shared

fun <T> List<T>.replaceItem(item: T, newItem: T): List<T> {
    val list = toMutableList()
    val index = list.indexOf(item)
    if (index >= 0) {
        list.remove(item)
        list.add(index, newItem)
    }
    return list
}