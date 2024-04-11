package ua.vald_zx.game.rat.race.card

fun String.getDigits() = this.replace("\\D".toRegex(), "")

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