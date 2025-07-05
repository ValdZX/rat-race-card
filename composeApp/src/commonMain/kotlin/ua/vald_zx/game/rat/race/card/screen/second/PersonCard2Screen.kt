package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import ua.vald_zx.game.rat.race.card.components.NumberTextField
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.raceRate2store
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.shared.PlayerCard

class PersonCard2Screen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProfessionCardForm {
                raceRate2store.dispatch(RatRace2CardAction.FillProfessionCardRat(it))
                navigator?.replace(RaceRate2Screen())
            }
        }
    }
}

class EditPersonCardScreen : Screen {
    @Composable
    override fun Content() {
        val state by raceRate2store.observeState().collectAsState()
        val navigator = LocalNavigator.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(
                modifier = Modifier.align(Alignment.Start),
                onClick = { navigator?.pop() },
                content = {
                    Icon(Images.Back, contentDescription = null)
                }
            )
            ProfessionCardForm(state.playerCard) {
                raceRate2store.dispatch(RatRace2CardAction.EditFillProfessionCardRat(it))
                navigator?.popUntilRoot()
            }
        }
    }
}

@Composable
fun ProfessionCardForm(card: PlayerCard? = null, filled: (PlayerCard) -> Unit) {
    var profession by remember { mutableStateOf(card?.profession.orEmpty()) }
    val salary = remember { mutableStateOf(TextFieldValue(card?.salary?.toString().orEmpty())) }
    val rent = remember { mutableStateOf(TextFieldValue(card?.rent?.toString().orEmpty())) }
    val food = remember { mutableStateOf(TextFieldValue(card?.food?.toString().orEmpty())) }
    val cloth = remember { mutableStateOf(TextFieldValue(card?.cloth?.toString().orEmpty())) }
    val transport = remember { mutableStateOf(TextFieldValue(card?.transport?.toString().orEmpty())) }
    val phone = remember { mutableStateOf(TextFieldValue(card?.phone?.toString().orEmpty())) }
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Професія") },
        value = profession,
        onValueChange = { profession = it },
        keyboardOptions = KeyboardOptions( imeAction = ImeAction.Next ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    )
    NumberTextField(
        input = salary,
        inputLabel = "Зарплата",
    )
    NumberTextField(
        input = rent,
        inputLabel = "Оренда житла",
    )
    NumberTextField(
        input = food,
        inputLabel = "Витрати на харчування",
    )
    NumberTextField(
        input = cloth,
        inputLabel = "Витрати на одяг",
    )
    NumberTextField(
        input = transport,
        inputLabel = "Витрати на проїзд",
    )
    NumberTextField(
        input = phone,
        inputLabel = "Витрати на телефонні переговори",
    )
    ElevatedButton(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            .widthIn(min = 200.dp),
        onClick = {
            filled(
                PlayerCard(
                    profession = profession.trim(),
                    salary = salary.value.text.toLong(),
                    rent = rent.value.text.toLong(),
                    food = food.value.text.toLong(),
                    cloth = cloth.value.text.toLong(),
                    transport = transport.value.text.toLong(),
                    phone = phone.value.text.toLong(),
                )
            )
        },
        enabled = profession.isNotEmpty()
                && salary.value.text.isNotEmpty()
                && rent.value.text.isNotEmpty()
                && food.value.text.isNotEmpty()
                && cloth.value.text.isNotEmpty()
                && transport.value.text.isNotEmpty()
                && phone.value.text.isNotEmpty(),
        content = {
            Text("Готово")
        }
    )
}