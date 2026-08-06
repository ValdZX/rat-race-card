package ua.vald_zx.game.rat.race.card.screen.board.deck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.logic.BoardViewModel
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.ok
import ua.vald_zx.game.rat.race.card.shared.InteractionField
import ua.vald_zx.game.rat.race.card.shared.InteractionFieldType

@Composable
fun GenericInteractionDialog(vm: BoardViewModel) {
    val state by vm.uiState.collectAsState()
    val interaction = state.board.pendingInteractions.firstOrNull {
        it.playerId == state.player.id
    } ?: return
    val values = remember(interaction.id) {
        mutableStateMapOf<String, String>().apply {
            interaction.fields.forEach { field ->
                field.options.singleOrNull()?.let { option -> put(field.id, option.value) }
            }
        }
    }
    val valid = interaction.fields.all { field -> field.validates(values[field.id]) }

    AlertDialog(
        onDismissRequest = {},
        title = { Text(interaction.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (interaction.description.isNotBlank()) {
                    Text(interaction.description)
                }
                interaction.fields.forEach { field ->
                    InteractionField(
                        field = field,
                        value = values[field.id].orEmpty(),
                        onValueChange = { values[field.id] = it },
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = valid && !state.isProgress,
                onClick = {
                    val input = buildJsonObject {
                        interaction.fields.forEach { field ->
                            val value = values.getValue(field.id)
                            when (field.type) {
                                InteractionFieldType.AMOUNT -> put(field.id, JsonPrimitive(value.toLong()))
                                InteractionFieldType.CHOICE,
                                InteractionFieldType.CONFIRMATION -> put(field.id, value)
                            }
                        }
                    }
                    vm.chooseInteraction(interaction.id, input)
                },
            ) {
                Text(stringResource(Res.string.ok))
            }
        },
    )
}

@Composable
private fun InteractionField(
    field: InteractionField,
    value: String,
    onValueChange: (String) -> Unit,
) {
    when (field.type) {
        InteractionFieldType.AMOUNT -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = { entered -> onValueChange(entered.filter(Char::isDigit)) },
                label = { Text(field.label) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (field.quickValues.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    field.quickValues.forEach { amount ->
                        OutlinedButton(onClick = { onValueChange(amount.toString()) }) {
                            Text(amount.toString())
                        }
                    }
                }
            }
        }

        InteractionFieldType.CHOICE,
        InteractionFieldType.CONFIRMATION -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(field.label)
            field.options.forEach { option ->
                val selected = option.value == value
                if (selected) {
                    Button(
                        onClick = { onValueChange(option.value) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(option.label)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onValueChange(option.value) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(option.label)
                    }
                }
            }
        }
    }
}

private fun InteractionField.validates(value: String?): Boolean = when (type) {
    InteractionFieldType.AMOUNT -> value?.toLongOrNull()?.let { amount ->
        amount >= (minimum ?: Long.MIN_VALUE) && amount <= (maximum ?: Long.MAX_VALUE)
    } == true

    InteractionFieldType.CHOICE,
    InteractionFieldType.CONFIRMATION -> value != null && options.any { it.value == value }
}
