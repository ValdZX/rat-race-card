package ua.vald_zx.game.rat.race.card

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.max
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import ua.vald_zx.game.rat.race.card.shared.DEFAULT_CURRENCY

fun String.getDigits() = this.replace("\\D".toRegex(), "").toLongOrNull()?.toString().orEmpty()

fun Long.splitDecimal(step: Int = 3, divider: String = " "): String {
    return toString().splitDecimal(step, divider)
}

val currentCurrency = mutableStateOf(DEFAULT_CURRENCY)

fun Long.formatAmount(): String {
    return "${this.splitDecimal()} ${currentCurrency.value}"
}

fun String.formatAmount(): String {
    if (toLongOrNull() == null) return this
    return "${splitDecimal()} ${currentCurrency.value}"
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

val dateFullDotsFormat = LocalDateTime.Format {
    day(); char('.'); monthNumber(); char('.'); year(); char(' ')
    hour(); char(':'); minute(); char(':'); second()
}

val DpSize.isVertical: Boolean
    get() = height > width

val DpSize.max: Dp
    get() = max(width, height)

fun launchWithHandler(onFailed: () -> Unit, todo: suspend () -> Unit) {
    val handler = CoroutineExceptionHandler { _, t ->
        Napier.e("Invalid server", t)
        onFailed()
    }
    CoroutineScope(SupervisorJob() + handler).launch {
        todo()
    }
}
