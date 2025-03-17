package ua.vald_zx.game.rat.race.card.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import ua.vald_zx.game.rat.race.card.getDigits
import ua.vald_zx.game.rat.race.card.splitDecimal

object AmountTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val decimalPart = text.text.getDigits()
        val outDecimalPart = decimalPart.splitDecimal()
        val out = outDecimalPart

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val length = decimalPart.length
                val startBlockSize = length % 3
                if (offset <= startBlockSize || (startBlockSize == 0 && offset <= 3)) return offset
                val transformed = if (decimalPart.length < offset) {
                    val spaceCount = length / 3 - if (startBlockSize == 0) 1 else 0
                    offset + spaceCount
                } else {
                    val spaceCountInOffset = when (startBlockSize) {
                        1 -> offset / 3
                        2 -> offset / 3
                        else -> (offset / 3) - 1
                    }
                    offset + spaceCountInOffset
                }
                return transformed
            }

            override fun transformedToOriginal(offset: Int): Int {
                val length = decimalPart.length
                val startBlockSize = length % 3
                if (offset <= startBlockSize || (startBlockSize == 0 && offset <= 3)) return offset
                val original = if (outDecimalPart.length < offset) {
                    val spaceCount = length / 3 - if (startBlockSize == 0) 1 else 0
                    offset - spaceCount
                } else {
                    if (offset < startBlockSize) {
                        offset
                    } else {
                        val spaceCountInOffset = offset / 4
                        offset - spaceCountInOffset
                    }
                }
                return original
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}