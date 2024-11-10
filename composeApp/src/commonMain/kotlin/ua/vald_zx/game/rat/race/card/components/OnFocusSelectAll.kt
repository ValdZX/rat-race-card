package ua.vald_zx.game.rat.race.card.components

import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusEventModifierNode
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.launch


private class FocusSelectAllElementNode(
    private var textFieldValueState: MutableState<TextFieldValue>
) : Modifier.Node(), FocusEventModifierNode {

    override fun onFocusEvent(focusState: FocusState) {
        if (focusState.isFocused) {
            coroutineScope.launch {
                val textFieldValue = textFieldValueState.value
                textFieldValueState.value = textFieldValue.copy(
                    selection =TextRange(0, textFieldValue.text.length)
                )
            }
        }
    }

    fun updateTextFieldValueState(textFieldValueState: MutableState<TextFieldValue>) {
        this.textFieldValueState = textFieldValueState
    }
}

private data class FocusSelectAllElement(
    private val textFieldValueState: MutableState<TextFieldValue>
) : ModifierNodeElement<FocusSelectAllElementNode>() {

    override fun create(): FocusSelectAllElementNode {
        return FocusSelectAllElementNode(textFieldValueState)
    }

    override fun update(node: FocusSelectAllElementNode) {
        node.updateTextFieldValueState(textFieldValueState)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "onFocusSelectAll"
    }
}

fun Modifier.onFocusSelectAll(
    textFieldValueState: MutableState<TextFieldValue>
): Modifier = this then FocusSelectAllElement(textFieldValueState)