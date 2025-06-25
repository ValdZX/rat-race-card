package ua.vald_zx.game.rat.race.card

import ua.vald_zx.game.rat.race.card.beans.SharesType

fun String.getDigits() = this.replace("\\D".toRegex(), "").toLongOrNull()?.toString().orEmpty()

fun Long.splitDecimal(step: Int = 3, divider: String = " "): String {
    return toString().splitDecimal(step, divider)
}

fun String.splitDecimal(step: Int = 3, divider: String = " "): String {
    if (this.toLongOrNull() == null) return this
    var current = 0
    val reverseAmount = StringBuilder(this).reverse()
    val reverseResult = StringBuilder()
    val length = reverseAmount.length
    while (current < length) {
        reverseResult.append(
            reverseAmount.substring(
                current,
                (current + step).coerceAtMost(length)
            )
        )
        reverseResult.append(divider)
        current += step
    }
    return reverseResult.reverse().toString().trim { it <= ' ' }
}

fun <T> List<T>.remove(item: T): List<T> {
    val index = indexOf(item)
    return if (index >= 0) {
        val newList = toMutableList()
        newList.remove(item)
        newList
    } else {
        this
    }
}

fun <T> List<T>.replace(item: T, newItem: T): List<T> {
    val index = indexOf(item)
    return if (index >= 0) {
        val newList = toMutableList()
        newList.remove(item)
        newList.add(index, newItem)
        newList
    } else {
        this
    }
}

fun Long.emptyIfZero(): String {
    return if (this == 0L) "" else this.toString()
}

fun SharesType.label(): String {
    return name.replace("SCT", "CST").replace("GS", "GC")
}