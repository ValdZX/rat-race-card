package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import io.ktor.util.decodeBase64String
import kotlinx.serialization.json.Json
import ua.vald_zx.game.rat.race.card.logic.AppAction
import ua.vald_zx.game.rat.race.card.logic.AppState
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.fromvectorimages.Back

class ImportScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        var invalidData by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
        ) {
            IconButton(
                modifier = Modifier.align(Alignment.TopStart),
                onClick = { navigator?.pop() },
                content = {
                    Icon(Images.Back, contentDescription = null)
                }
            )
            Column(
                modifier = Modifier
                    .padding(top = 48.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                var base64Card by remember { mutableStateOf("") }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Код картки") },
                    value = base64Card,
                    onValueChange = { base64Card = it }
                )
                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = {
                        runCatching {
                            val importedState: AppState =
                                Json.decodeFromString(base64Card.decodeBase64String())
                            store.dispatch(AppAction.LoadState(importedState))
                            navigator?.replaceAll(MainScreen())
                        }.onFailure {
                            invalidData = true
                        }
                    },
                    enabled = base64Card.isNotEmpty(),
                    content = { Text("Імпортувати") }
                )
            }
        }

        if (invalidData) {
            AlertDialog(
                title = { Text(text = "Імпорт") },
                text = {
                    Text(text = "Некоректні данні для імпорту")
                },
                onDismissRequest = { invalidData = false },
                confirmButton = {
                    TextButton(onClick = { invalidData = false }) { Text("Гаразд") }
                }
            )
        }
    }
}