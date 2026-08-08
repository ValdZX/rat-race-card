package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
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
import ua.vald_zx.game.rat.race.card.vividPaletteEnabled
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class VividPaletteRenderTest {

    private val allTypes = listOf(
        PlaceType.Salary, PlaceType.Start, PlaceType.Rest, PlaceType.TaxInspection,
        PlaceType.Chance, PlaceType.Store, PlaceType.Shopping, PlaceType.Deputy,
        PlaceType.Expenses, PlaceType.Bankruptcy, PlaceType.Divorce,
        PlaceType.Business, PlaceType.BigBusiness,
        PlaceType.Child, PlaceType.Love, PlaceType.Resignation, PlaceType.Desire("world_trip"),
    )

    private fun render(name: String, dark: Boolean, vivid: Boolean) = runComposeUiTest {
        vividPaletteEnabled.value = vivid
        setContent {
            AppTheme(forceDark = dark) {
                Box(
                    Modifier
                        .background(Design.scaffold.surface4)
                        .testTag("cells")
                        .padding(12.dp)
                ) { CellGrid() }
            }
        }
        waitForIdle()
        val image = onNodeWithTag("cells").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/vivid-$name.png"))
        vividPaletteEnabled.value = false
    }

    @Test
    fun vividDark() = render("dark-on", dark = true, vivid = true)

    @Test
    fun pastelDark() = render("dark-off", dark = true, vivid = false)

    @Test
    fun vividLight() = render("light-on", dark = false, vivid = true)

    @Test
    fun pastelLight() = render("light-off", dark = false, vivid = false)

    @Composable
    private fun CellGrid() {
        Column(verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
            allTypes.chunked(6).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                    row.forEach { type ->
                        DesignPlaceCell(
                            type = type,
                            surface = CellSurface.Tile,
                            label = type.name.take(6),
                            modifier = Modifier.size(64.dp, 74.dp),
                        )
                    }
                }
            }
        }
    }
}
