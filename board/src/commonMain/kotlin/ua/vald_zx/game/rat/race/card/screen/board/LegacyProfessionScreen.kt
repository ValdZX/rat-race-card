package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import ua.vald_zx.game.rat.race.card.appKStore
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.DetailsField
import ua.vald_zx.game.rat.race.card.launchWithHandler
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Back
import ua.vald_zx.game.rat.race.card.screen.LoadOnlineScreen
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.PlayerCard
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.shared.RaceRatService
import kotlin.uuid.Uuid

@Composable
internal fun LegacyProfessionContent(
    card: ProfessionCard,
    isLoading: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                modifier = Modifier.align(Alignment.Start),
                onClick = onBack,
            ) {
                Icon(Images.Back, contentDescription = null)
            }
            Text(
                text = stringResource(Res.string.work),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            DetailsField(
                name = card.name,
                value = card.salary.toString(),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(Res.string.expenses),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            DetailsField(
                stringResource(Res.string.rent), card.rent.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
            DetailsField(
                stringResource(Res.string.food), card.food.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
            DetailsField(
                stringResource(Res.string.cloth), card.cloth.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
            DetailsField(
                stringResource(Res.string.transport), card.transport.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
            DetailsField(
                stringResource(Res.string.phone), card.phone.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
            Button(
                text = stringResource(if (isLoading) Res.string.connecting_to_server else Res.string.next),
                enabled = !isLoading,
                onClick = onNext,
            )
        }
}
