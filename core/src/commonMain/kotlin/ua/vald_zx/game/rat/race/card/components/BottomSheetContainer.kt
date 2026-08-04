package ua.vald_zx.game.rat.race.card.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.noIme
import ua.vald_zx.game.rat.race.card.resources.Res
import ua.vald_zx.game.rat.race.card.resources.close

@Composable
fun BottomSheetContainer(
    verticalScrollState: ScrollState? = rememberScrollState(),
    onClose: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val screenHeight = with(density) { windowInfo.containerSize.height.toDp() }
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Column(
        modifier = Modifier
            .heightIn(max = screenHeight - statusBarHeight)
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
            .optionalModifier(!noIme) { imePadding() }
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        onClose?.let { close ->
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = close,
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.close),
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .optionalModifier(verticalScrollState != null) {
                    verticalScroll(verticalScrollState!!)
                }
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content()
            if (noIme) {
                Spacer(Modifier.fillMaxWidth().height(screenHeight / 2))
            }
        }
    }
}

@Composable
fun ClosableBottomSheetContainer(
    verticalScrollState: ScrollState? = rememberScrollState(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    BottomSheetContainer(
        verticalScrollState = verticalScrollState,
        onClose = { bottomSheetNavigator.hide() },
        content = content,
    )
}
