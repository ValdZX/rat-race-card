package ua.vald_zx.game.rat.race.card.screen

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import ua.vald_zx.game.rat.race.card.beans.ProfessionCard
import ua.vald_zx.game.rat.race.card.getDigits
import ua.vald_zx.game.rat.race.card.logic.AppAction
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.store

class PersonCardScreen : Screen {
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
                store.dispatch(AppAction.FillProfessionCard(it))
                navigator?.replace(MainScreen())
            }
        }
    }
}

class EditPersonCardScreen() : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
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
            ProfessionCardForm(state.professionCard) {
                store.dispatch(AppAction.EditFillProfessionCard(it))
                navigator?.popUntilRoot()
            }
        }
    }
}

@Composable
fun ProfessionCardForm(card: ProfessionCard? = null, filled: (ProfessionCard) -> Unit) {
    var profession by remember { mutableStateOf(card?.profession.orEmpty()) }
    var salary by remember { mutableStateOf(card?.salary?.toString().orEmpty()) }
    var rent by remember { mutableStateOf(card?.rent?.toString().orEmpty()) }
    var food by remember { mutableStateOf(card?.food?.toString().orEmpty()) }
    var cloth by remember { mutableStateOf(card?.cloth?.toString().orEmpty()) }
    var transport by remember { mutableStateOf(card?.transport?.toString().orEmpty()) }
    var phone by remember { mutableStateOf(card?.phone?.toString().orEmpty()) }
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Професія") },
        value = profession,
        onValueChange = { profession = it }
    )
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Зарплата") },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        value = salary,
        onValueChange = { salary = it.getDigits() }
    )
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Оренда житла") },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        value = rent,
        onValueChange = { rent = it.getDigits() }
    )
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Витрати на харчування") },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        value = food,
        onValueChange = { food = it.getDigits() }
    )
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Витрати на одяг") },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        value = cloth,
        onValueChange = { cloth = it.getDigits() }
    )
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Витрати на проїзд") },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        value = transport,
        onValueChange = { transport = it.getDigits() }
    )
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Витрати на телефонні переговори") },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        value = phone,
        onValueChange = { phone = it.getDigits() }
    )
    ElevatedButton(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            .widthIn(min = 200.dp),
        onClick = {
            filled(
                ProfessionCard(
                    profession = profession.trim(),
                    salary = salary.toLong(),
                    rent = rent.toLong(),
                    food = food.toLong(),
                    cloth = cloth.toLong(),
                    transport = transport.toLong(),
                    phone = phone.toLong(),
                )
            )
        },
        enabled = profession.isNotEmpty()
                && salary.isNotEmpty()
                && rent.isNotEmpty()
                && food.isNotEmpty()
                && cloth.isNotEmpty()
                && transport.isNotEmpty()
                && phone.isNotEmpty(),
        content = {
            Text("Готово")
        }
    )
}