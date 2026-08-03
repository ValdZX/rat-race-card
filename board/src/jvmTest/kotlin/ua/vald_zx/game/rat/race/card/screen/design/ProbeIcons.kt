package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.*
import org.jetbrains.compose.resources.painterResource
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ProbeIcons {
    @Test
    fun sheet() = runComposeUiTest {
        val assets = listOf(
            "Work" to Res.drawable.asset_work,
            "Marriage" to Res.drawable.cell_love,
            "Kids" to Res.drawable.cell_child,
            "Car" to Res.drawable.asset_car,
            "Apartment" to Res.drawable.asset_apartment,
            "Estate" to Res.drawable.asset_estate,
            "Yacht" to Res.drawable.asset_yacht,
            "Plane" to Res.drawable.asset_plane,
        )
        setContent {
            AppTheme(forceDark = false) {
                FlowRow(
                    modifier = Modifier.width(560.dp).background(Color.White).testTag("icons"),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    assets.forEach { (name, res) ->
                        Column(
                            Modifier.width(68.dp).padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                painterResource(res),
                                null,
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp),
                            )
                            Text(name, fontSize = 8.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
        waitForIdle()
        ImageIO.write(
            onNodeWithTag("icons").captureToImage().toAwtImage(),
            "png",
            File("build/probe-icons.png"),
        )
    }
}
