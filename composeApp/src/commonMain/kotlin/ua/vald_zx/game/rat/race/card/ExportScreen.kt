package ua.vald_zx.game.rat.race.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import io.ktor.util.encodeBase64
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.vectorResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.back
import rat_race_card.composeapp.generated.resources.copy
import rat_race_card.composeapp.generated.resources.share
import ua.vald_zx.game.rat.race.card.beans.Config
import ua.vald_zx.game.rat.race.card.logic.AppAction

class ExportScreen : Screen {
    @Composable
    override fun Content() {
        val state by store.observeState().collectAsState()
        val navigator = LocalNavigator.current
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
                    Icon(vectorResource(Res.drawable.back), contentDescription = null)
                }
            )
            Column(
                modifier = Modifier
                    .padding(top = 48.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val base64Card by remember {
                    mutableStateOf(Json.encodeToString(state).encodeBase64())
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Код картки") },
                    value = base64Card,
                    onValueChange = {}
                )
                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = { share(base64Card) },
                    enabled = base64Card.isNotEmpty(),
                    content = {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(vectorResource(Res.drawable.share), contentDescription = null)
                            Text("Віправити")
                        }
                    },
                )
                val clipboardManager = LocalClipboardManager.current
                ElevatedButton(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        .widthIn(min = 200.dp),
                    onClick = {
                        clipboardManager.setText(AnnotatedString(base64Card))
                    },
                    enabled = base64Card.isNotEmpty(),
                    content = {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(vectorResource(Res.drawable.copy), contentDescription = null)
                            Text("Копіювати")
                        }
                    },
                )
            }
        }
    }
}