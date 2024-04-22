package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.components.BottomSheetContainer
import ua.vald_zx.game.rat.race.card.getDigits

@Composable
fun InputScreen(
    inputLabel: String,
    buttonText: String,
    validation: (String) -> Boolean,
    onClick: (String) -> Unit,
    value: String = ""
) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    BottomSheetContainer {
        var input by remember { mutableStateOf(value) }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            label = { Text(inputLabel) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            value = input,
            onValueChange = { input = it.getDigits() }
        )
        ElevatedButton(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .widthIn(min = 200.dp),
            onClick = {
                bottomSheetNavigator.hide()
                onClick(input)
            },
            enabled = validation(input),
            content = {
                Text(buttonText)
            }
        )
    }
}