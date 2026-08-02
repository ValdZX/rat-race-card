package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.screen.board.RouteLayout
import ua.vald_zx.game.rat.race.card.screen.board.calculateBoardLayout
import ua.vald_zx.game.rat.race.card.shared.BoardLayer
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DesignBoardTracksRenderTest {

    @Test
    fun layoutProducesEveryCellForBothTracks() {
        val layout = calculateBoardLayout(DpSize(900.dp, 620.dp), isVertical = false)
        requireNotNull(layout)

        assertEquals(
            BoardLayer.OUTER.places.size,
            layout.outerRoute.places.size,
            "зовнішній трек має рівно стільки клітинок, скільки типів у розкладці",
        )
        assertEquals(
            BoardLayer.INNER.places.size,
            layout.innerRoute.places.size,
            "внутрішній трек має рівно стільки клітинок, скільки типів у розкладці",
        )
    }

    @Test
    fun everyCellStaysInsideItsTrack() {
        val layout = calculateBoardLayout(DpSize(900.dp, 620.dp), isVertical = false)
        requireNotNull(layout)
        listOf(layout.outerRoute, layout.innerRoute).forEach { route ->
            route.places.forEach { (index, place) ->
                assertTrue(
                    place.offset.x >= 0.dp && place.offset.y >= 0.dp,
                    "клітинка $index виїхала за верхній/лівий край ${route.layer}",
                )
                assertTrue(
                    place.offset.x + place.size.width <= route.size.width + 0.5.dp &&
                            place.offset.y + place.size.height <= route.size.height + 0.5.dp,
                    "клітинка $index виїхала за правий/нижній край ${route.layer}",
                )
            }
        }
    }

    @Test
    fun portraitRebuildsInsteadOfSquashing() {
        val landscape = calculateBoardLayout(DpSize(900.dp, 620.dp), isVertical = false)
        val portrait = calculateBoardLayout(DpSize(620.dp, 900.dp), isVertical = true)
        requireNotNull(landscape)
        requireNotNull(portrait)

        assertEquals(
            landscape.outerRoute.places.size,
            portrait.outerRoute.places.size,
            "кількість клітинок не змінюється від орієнтації",
        )
        assertTrue(
            portrait.outerRoute.size.height > portrait.outerRoute.size.width,
            "у портреті дошка має бути вищою за ширину",
        )
    }

    private fun render(name: String, dark: Boolean, playerLevel: Int) = runComposeUiTest {
        val layout = calculateBoardLayout(DpSize(760.dp, 520.dp), isVertical = false)!!
        setContent {
            AppTheme(forceDark = dark) {
                Box(
                    Modifier
                        .size(760.dp, 520.dp)
                        .background(Design.scaffold.background)
                        .testTag("board")
                ) {
                    StaticTracks(layout.outerRoute, layout.innerRoute, playerLevel)
                }
            }
        }
        waitForIdle()
        val image = onNodeWithTag("board").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-board-$name.png"))
        val crop = image.getSubimage(60, 55, 340, 80)
        val zoom = java.awt.image.BufferedImage(crop.width * 3, crop.height * 3, crop.type)
        val g = zoom.createGraphics()
        g.drawImage(crop, 0, 0, zoom.width, zoom.height, null)
        g.dispose()
        ImageIO.write(zoom, "png", File("build/design-board-$name-zoom.png"))
    }

    @Composable
    private fun BoxScope.StaticTracks(outer: RouteLayout, inner: RouteLayout, playerLevel: Int) {
        DesignTrackForTest(outer, if (playerLevel == outer.layer.level) CellSurface.Tile else CellSurface.Engraved)
        DesignTrackForTest(inner, if (playerLevel == inner.layer.level) CellSurface.Tile else CellSurface.Engraved)
    }

    /**
     * На телефоні клітинка треку ~15dp. Слово там не влазить за жодного кегля,
     * тому дрібна клітинка живого треку несе піктограму, а неактивний трек
     * мовчить — на ньому лишаються тільки кутові події.
     */
    @Test
    fun phoneSizedCellsKeepAReadableLabel() = runComposeUiTest {
        val size = DpSize(360.dp, 420.dp)
        val layout = calculateBoardLayout(size, isVertical = true)!!
        assertTrue(
            layout.outerRoute.cellSize.width < 30.dp,
            "тест має сенс лише поки клітинка на телефоні справді дрібна",
        )
        setContent {
            AppTheme(forceDark = false) {
                Box(Modifier.size(size).testTag("phone")) {
                    StaticTracks(layout.outerRoute, layout.innerRoute, BoardLayer.INNER.level)
                }
            }
        }
        waitForIdle()
        onAllNodesWithContentDescription("Chance!", useUnmergedTree = true).fetchSemanticsNodes().size.let { assertTrue(it > 0, "піктограми клітинок мають бути в дереві, знайдено $it") }
        onAllNodesWithText("Chance!", useUnmergedTree = true).assertCountEquals(0)

        val image = onNodeWithTag("phone").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-board-phone.png"))
    }

    @Test
    fun playerOnInnerTrack() = render("inner-live", dark = true, playerLevel = BoardLayer.INNER.level)

    @Test
    fun playerOnOuterTrack() = render("outer-live", dark = true, playerLevel = BoardLayer.OUTER.level)
}
