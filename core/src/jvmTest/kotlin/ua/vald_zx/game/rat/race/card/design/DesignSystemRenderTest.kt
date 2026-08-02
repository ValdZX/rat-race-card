package ua.vald_zx.game.rat.race.card.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
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
import ua.vald_zx.game.rat.race.card.theme.AppTheme
import ua.vald_zx.game.rat.race.card.theme.LocalThemeIsDark
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class DesignSystemRenderTest {

    private fun render(dark: Boolean, name: String) = runComposeUiTest {
        setContent {
            AppTheme(forceDark = dark) {
                Column(
                    modifier = Modifier
                        .size(380.dp, 620.dp)
                        .background(Design.scaffold.background)
                        .testTag("sheet")
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("КОМПОНЕНТИ", style = Design.type.micro, color = Design.scaffold.onSurfaceMuted)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ValueField(
                            "БАЛАНС", 15700,
                            tone = Design.semantic.cash, modifier = Modifier.weight(1f)
                        )
                        ValueField(
                            "CASHFLOW", 2140, signed = true,
                            tone = Design.semantic.positive, modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ValueField(
                            "ДОХІД", 4900, signed = true,
                            tone = Design.semantic.business, modifier = Modifier.weight(1f)
                        )
                        ValueField(
                            "КРЕДИТ", -2760,
                            tone = Design.semantic.negative, modifier = Modifier.weight(1f)
                        )
                    }

                    BrassToken("ЗАРПЛАТА", 3200)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DesignChip("Стан", selected = true) {}
                        DesignChip("Бізнес", selected = false) {}
                        DesignChip("Фонди", selected = false) {}
                    }

                    DesignButton("Кинути кубик", modifier = Modifier.fillMaxWidth()) {}
                    DesignButton(
                        "Отримати 3 200",
                        kind = DesignButtonKind.Brass,
                        modifier = Modifier.fillMaxWidth(),
                    ) {}
                    DesignButton(
                        "Скасувати",
                        kind = DesignButtonKind.Tonal,
                        modifier = Modifier.fillMaxWidth(),
                    ) {}
                    DesignButton(
                        "Не твій хід",
                        enabled = false,
                        disabledReason = "ходить Данило",
                        modifier = Modifier.fillMaxWidth(),
                    ) {}
                }
            }
        }
        waitForIdle()
        val image = onNodeWithTag("sheet").captureToImage().toAwtImage()
        File("build").mkdirs()
        ImageIO.write(image, "png", File("build/design-$name.png"))
    }

    @Test
    fun renderDark() = render(dark = true, name = "dark")

    @Test
    fun renderLight() = render(dark = false, name = "light")
}
