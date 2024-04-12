package ua.vald_zx.game.rat.race.card

fun String.getDigits() = this.replace("\\D".toRegex(), "")

fun Int.splitDecimal(step: Int = 3, divider: String = " "): String {
    return toString().splitDecimal(step, divider)
}

fun String.splitDecimal(step: Int = 3, divider: String = " "): String {
    if (this.toIntOrNull() == null) return this
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