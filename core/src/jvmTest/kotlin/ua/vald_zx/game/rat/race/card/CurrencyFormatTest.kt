package ua.vald_zx.game.rat.race.card

import androidx.compose.ui.text.AnnotatedString
import ua.vald_zx.game.rat.race.card.components.AmountTransformation
import ua.vald_zx.game.rat.race.card.design.formatSigned
import ua.vald_zx.game.rat.race.card.shared.DEFAULT_CURRENCY
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyFormatTest {

    @AfterTest
    fun resetCurrency() {
        currentCurrency.value = DEFAULT_CURRENCY
    }

    @Test
    fun theDefaultGameUsesTheDollar() {
        assertEquals("$", DEFAULT_CURRENCY)
        assertEquals("1 500 $", 1_500L.formatAmount())
    }

    @Test
    fun everyAmountFollowsTheBoardCurrency() {
        currentCurrency.value = "₴"

        assertEquals("1 500 ₴", 1_500L.formatAmount())
        assertEquals("1 500 ₴", "1500".formatAmount())
        assertEquals("+1 500 ₴", 1_500L.formatSigned())
        assertEquals("−1 500 ₴", (-1_500L).formatSigned())
    }

    @Test
    fun aCompactDeltaCanOptOutOfTheCurrency() {
        currentCurrency.value = "₴"

        assertEquals("+1 500", 1_500L.formatSigned(withCurrency = false))
        assertEquals("−1 500", (-1_500L).formatSigned(withCurrency = false))
    }

    @Test
    fun anAlreadyFormattedStringIsNotSuffixedTwice() {
        currentCurrency.value = "₴"

        assertEquals("1 500 ₴", 1_500L.formatAmount().formatAmount())
    }

    @Test
    fun theAmountFieldShowsTheCurrencyWhileTyping() {
        currentCurrency.value = "₴"

        val transformed = AmountTransformation.filter(AnnotatedString("1500"))

        assertEquals("1 500 ₴", transformed.text.text)
    }

    @Test
    fun anEmptyAmountFieldStaysEmpty() {
        currentCurrency.value = "₴"

        val transformed = AmountTransformation.filter(AnnotatedString(""))

        assertEquals("", transformed.text.text)
    }

    @Test
    fun theCursorNeverLandsInsideTheCurrencySuffix() {
        currentCurrency.value = "₴"
        val digits = "1500"

        val transformed = AmountTransformation.filter(AnnotatedString(digits))
        val mapping = transformed.offsetMapping

        assertEquals(5, mapping.originalToTransformed(digits.length), "курсор має стояти після \"1 500\"")
        assertEquals(digits.length, mapping.transformedToOriginal(transformed.text.text.length))
    }
}
