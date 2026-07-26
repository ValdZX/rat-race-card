package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val CANVAS_W = 240
private const val CANVAS_H = 160
private const val TOKEN = 40
private const val SHORT = "Го!"
private const val LONG = "Дуже довге повідомлення яке точно не поміститься у один рядок"
private val backgroundArgb = Color(0xFF2E7D32).toArgb()

@OptIn(ExperimentalTestApi::class)
class SpeechBubbleRenderTest {

    @Test
    fun tailPointsDownAtTheToken() = runComposeUiTest {
        val image = renderBubble(SHORT)
        val scale = image.width.toFloat() / CANVAS_W
        val centerX = image.width / 2
        val insideTailY = image.bubbleBottom() - (bubbleTailHeight.value * scale / 2).toInt()

        assertTrue(
            image.isBubble(centerX, insideTailY),
            "на висоті хвостика по центру має бути намальована фігура"
        )
        assertTrue(
            image.isBackground(centerX - (50 * scale).toInt(), insideTailY),
            "збоку на висоті хвостика має бути фон — інакше це не хвостик, а частина тіла"
        )
        assertTrue(
            image.isBackground(centerX, image.bubbleBottom() + 2),
            "нижче кінчика хвостика нічого не малюється"
        )
    }

    @Test
    fun tailStaysAttachedToTheTokenRegardlessOfTextLength() = runComposeUiTest {
        val short = renderBubble(SHORT)
        val long = renderBubble(LONG)
        assertEquals(
            short.bubbleBottom(),
            long.bubbleBottom(),
            "кінчик хвостика прив'язаний до фішки й не має їхати від довжини тексту"
        )
    }

    @Test
    fun shortTextIsCenteredInsideTheBubble() = runComposeUiTest {
        val image = renderBubble(SHORT)
        val bodyRows = (image.bubbleTop()..image.bubbleBottom()).associateWith { y ->
            (0 until image.width).filter { image.isBubble(it, y) }
        }
        val widest = bodyRows.values.maxOf { it.size }
        val fullWidthRows = bodyRows.filterValues { it.size == widest }
        val left = fullWidthRows.values.first().first() + 4
        val right = fullWidthRows.values.first().last() - 4
        val ink = fullWidthRows.keys.flatMap { y -> (left..right).filter { image.isInk(it, y) } }

        assertTrue(ink.isNotEmpty(), "у тілі бульбашки має бути текст")
        val leftMargin = ink.min() - left
        val rightMargin = right - ink.max()
        assertTrue(
            kotlin.math.abs(leftMargin - rightMargin) <= 3,
            "текст має бути по центру: відступи $leftMargin проти $rightMargin"
        )
    }

    @Test
    fun longMessageWrapsInsteadOfCollapsingToOneLine() = runComposeUiTest {
        val short = renderBubble(SHORT)
        val long = renderBubble(LONG)
        assertTrue(
            long.bubbleTop() < short.bubbleTop(),
            "довгий текст має рости вгору кількома рядками, а не обрізатись в один"
        )
    }

    private fun ComposeUiTest.renderBubble(text: String): BufferedImage {
        setContent {
            Box(
                modifier = Modifier
                    .size(CANVAS_W.dp, CANVAS_H.dp)
                    .background(Color(0xFF2E7D32))
                    .testTag("canvas")
            ) {
                Box(
                    modifier = Modifier
                        .size(TOKEN.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Box(Modifier.fillMaxSize().background(Color.White, CircleShape))
                    SpeechBubble(text = text, modifier = Modifier.align(Alignment.TopCenter))
                }
            }
        }
        waitForIdle()
        val image = onNodeWithTag("canvas").captureToImage().toAwtImage()
        val name = if (text == SHORT) "short" else "long"
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/speech-bubble-$name.png"))
        return image
    }
}

private fun BufferedImage.isBackground(x: Int, y: Int) = getRGB(x, y) == backgroundArgb

private fun BufferedImage.isBubble(x: Int, y: Int) = !isBackground(x, y)

private fun BufferedImage.isInk(x: Int, y: Int): Boolean {
    val rgb = getRGB(x, y)
    val r = (rgb shr 16) and 0xFF
    val g = (rgb shr 8) and 0xFF
    val b = rgb and 0xFF
    return !isBackground(x, y) && (r + g + b) < 3 * 160
}

private fun BufferedImage.bubbleRows() =
    (0 until height).filter { y -> (0 until width).any { x -> isBubble(x, y) } }

private fun BufferedImage.bubbleTop() = bubbleRows().first()

private fun BufferedImage.bubbleBottom() =
    bubbleRows().last { it < (height * (CANVAS_H - TOKEN) / CANVAS_H) }
