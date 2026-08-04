package ua.vald_zx.game.rat.race.card.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ua.vald_zx.game.rat.race.card.resources.*
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DrawableResourcesTest {

    private val drawables = File("src/commonMain/composeResources/drawable")

    private val boardIcons: Map<String, DrawableResource> = mapOf(
        "cell_salary" to Res.drawable.cell_salary,
        "cell_start" to Res.drawable.cell_start,
        "cell_chance" to Res.drawable.cell_chance,
        "cell_store" to Res.drawable.cell_store,
        "cell_shopping" to Res.drawable.cell_shopping,
        "cell_expenses" to Res.drawable.cell_expenses,
        "cell_business" to Res.drawable.cell_business,
        "cell_small_business" to Res.drawable.cell_small_business,
        "cell_big_business" to Res.drawable.cell_big_business,
        "cell_deputy" to Res.drawable.cell_deputy,
        "cell_rest" to Res.drawable.cell_rest,
        "cell_exaltation" to Res.drawable.cell_exaltation,
        "cell_divorce" to Res.drawable.cell_divorce,
        "cell_bankruptcy" to Res.drawable.cell_bankruptcy,
        "cell_child" to Res.drawable.cell_child,
        "cell_love" to Res.drawable.cell_love,
        "cell_tax" to Res.drawable.cell_tax,
        "cell_dream" to Res.drawable.cell_dream,
        "asset_work" to Res.drawable.asset_work,
        "asset_car" to Res.drawable.asset_car,
        "asset_apartment" to Res.drawable.asset_apartment,
        "asset_estate" to Res.drawable.asset_estate,
        "asset_yacht" to Res.drawable.asset_yacht,
        "asset_plane" to Res.drawable.asset_plane,
    )

    @Test
    fun noSvgReachesTheResources() {
        val svg = drawables.listFiles().orEmpty().filter { it.extension.equals("svg", ignoreCase = true) }
        assertTrue(
            svg.isEmpty(),
            "Android не читає SVG у compose-ресурсах — переклади у vector drawable: ${svg.map { it.name }}",
        )
    }

    @Test
    fun everyBoardIconDraws() {
        boardIcons.forEach { (name, resource) ->
            var painted = 0
            runComposeUiTest {
                setContent {
                    AppThemeless {
                        Icon(
                            painter = painterResource(resource),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp).testTag(name),
                        )
                    }
                }
                waitForIdle()
                val image = onNodeWithTag(name).captureToImage().toAwtImage()
                for (x in 0 until image.width) {
                    for (y in 0 until image.height) {
                        if (image.getRGB(x, y) and 0xFF > 128) painted++
                    }
                }
            }
            assertTrue(painted > 20, "іконка $name не намалювалась: світлих пікселів $painted")
        }
    }
}

@androidx.compose.runtime.Composable
private fun AppThemeless(content: @androidx.compose.runtime.Composable () -> Unit) {
    Box(Modifier.size(96.dp).background(Color.Black)) { content() }
}
