package ua.vald_zx.game.rat.race.card.screen.second

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import io.ktor.util.*
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import rat_race_card.composeapp.generated.resources.*
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardStore
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back

class ImportScreen : Screen {
    @Composable
    override fun Content() {
        val raceRate2store = koinInject<RatRace2CardStore>()
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
                    label = { Text(stringResource(Res.string.card_code)) },
                    value = base64Card,
                    onValueChange = { base64Card = it }
                )
                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = {
                        runCatching {
                            val importedState: RatRace2CardState =
                                Json.decodeFromString(base64Card.decodeBase64String())
                            raceRate2store.dispatch(RatRace2CardAction.LoadState(importedState))
                            navigator?.replaceAll(RaceRate2Screen())
                        }.onFailure {
                            invalidData = true
                        }
                    },
                    enabled = base64Card.isNotEmpty(),
                    content = { Text(stringResource(Res.string.import_card_label)) }
                )
            }
        }

        if (invalidData) {
            AlertDialog(
                title = { Text(text = stringResource(Res.string.import_data)) },
                text = {
                    Text(text = stringResource(Res.string.wrong_import_data))
                },
                onDismissRequest = { invalidData = false },
                confirmButton = {
                    TextButton(onClick = { invalidData = false }) { Text(stringResource(Res.string.ok)) }
                }
            )
        }
    }
}