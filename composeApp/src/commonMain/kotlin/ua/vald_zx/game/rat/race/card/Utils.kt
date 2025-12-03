package ua.vald_zx.game.rat.race.card

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.max
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
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

fun SharesType.label(): String {
    return name.replace("SCT", "CST").replace("GS", "GC")
}

val dateFullDotsFormat = LocalDateTime.Format { //dd.MM.yyyy HH:mm:ss
    day(); char('.'); monthNumber(); char('.'); year(); char(' ')
    hour(); char(':'); minute(); char(':'); second()
}

val DpSize.isVertical: Boolean
    get() = height > width

val DpSize.max: Dp
    get() = max(width, height)