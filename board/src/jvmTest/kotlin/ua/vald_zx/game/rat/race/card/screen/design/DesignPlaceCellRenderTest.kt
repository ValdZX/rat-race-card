package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ua.vald_zx.game.rat.race.card.design.Design
import ua.vald_zx.game.rat.race.card.shared.PlaceType
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class DesignPlaceCellRenderTest {

    private val allTypes = listOf(
        PlaceType.Salary, PlaceType.Start, PlaceType.Rest, PlaceType.TaxInspection,
        PlaceType.Chance, PlaceType.Store, PlaceType.Shopping, PlaceType.Deputy,
        PlaceType.Expenses, PlaceType.Bankruptcy, PlaceType.Divorce,
        PlaceType.Business, PlaceType.BigBusiness,
        PlaceType.Child, PlaceType.Love, PlaceType.Resignation, PlaceType.Desire("world_trip"),
    )

    @Test
    fun everyPlaceTypeHasAFamily() {
        val byFamily = allTypes.groupBy { it.family }
        assertEquals(5, byFamily.size, "усі п'ять родин мають бути задіяні")
        byFamily.forEach { (family, types) ->
            assertEquals(true, types.isNotEmpty(), "родина $family порожня")
        }
    }

    private fun render(name: String, dark: Boolean, content: @Composable () -> Unit) = runComposeUiTest {
        setContent {
            AppTheme(forceDark = dark) {
                Box(
                    Modifier
                        .background(Design.scaffold.surface4)
                        .testTag("cells")
                        .padding(12.dp)
                ) { content() }
            }
        }
        waitForIdle()
        val image = onNodeWithTag("cells").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-cells-$name.png"))
    }

    @Test
    fun tilesRenderEveryFamily() = render("tiles", dark = true) {
        CellGrid(CellSurface.Tile)
    }

    @Test
    fun engravedRendersEveryFamily() = render("engraved", dark = true) {
        CellGrid(CellSurface.Engraved)
    }

    @Test
    fun tilesRenderInLightTheme() = render("tiles-light", dark = false) {
        CellGrid(CellSurface.Tile)
    }

    @Composable
    private fun CellGrid(surface: CellSurface) {
        Column(verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
            allTypes.chunked(6).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                    row.forEach { type ->
                        DesignPlaceCell(
                            type = type,
                            surface = surface,
                            label = type.name.take(6),
                            modifier = Modifier.size(64.dp, 74.dp),
                        )
                    }
                }
            }
        }
    }
}
