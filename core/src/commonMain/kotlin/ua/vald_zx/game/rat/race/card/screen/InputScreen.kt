package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.components.ClosableBottomSheetContainer
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.design.AmountQuickOption

@Composable
fun InputScreen(
    inputLabel: String,
    buttonText: String,
    validation: (String) -> Boolean,
    onClick: (String) -> Unit,
    value: String = "",
    quickOptions: List<AmountQuickOption> = emptyList(),
) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    ClosableBottomSheetContainer {
        val input = remember { mutableStateOf(TextFieldValue(value)) }
        NumberTextField(
            input = input,
            inputLabel = inputLabel,
            quickOptions = quickOptions,
        )
        ElevatedButton(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .widthIn(min = 200.dp),
            onClick = {
                bottomSheetNavigator.hide()
                onClick(input.value.text)
            },
            enabled = validation(input.value.text),
            content = {
                Text(buttonText)
            }
        )
    }
}
