package ua.vald_zx.game.rat.race.card.screen.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ua.vald_zx.game.rat.race.card.components.Button
import ua.vald_zx.game.rat.race.card.components.DetailsField
import ua.vald_zx.game.rat.race.card.shared.Board
import ua.vald_zx.game.rat.race.card.shared.PlayerCard
import ua.vald_zx.game.rat.race.card.shared.ProfessionCard
import ua.vald_zx.game.rat.race.card.shared.RaceRatService

class ProfessionScreen(
    private val board: Board,
    private val card: ProfessionCard,
    private val playerName: String,
    private val color: Long,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val service = koinInject<RaceRatService>()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Робота",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            DetailsField(
                name = card.name,
                value = card.salary.toString(),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Витрати",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
            DetailsField(
                "Оренда житла", card.rent.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
            DetailsField(
                "Витрати на їжу", card.food.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
            DetailsField(
                "Витрати на одяг", card.cloth.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
            DetailsField(
                "Витрати на проїзд", card.transport.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
            DetailsField(
                "Витрати на телефонні переговори", card.phone.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
            Button("Далі") {
                coroutineScope.launch {
                    val player = service.makePlayer(
                        color = color,
                        card = PlayerCard(
                            name = playerName,
                            gender = card.gender,
                            profession = card.name,
                            salary = card.salary,
                            rent = card.rent,
                            food = card.food,
                            cloth = card.cloth,
                            transport = card.transport,
                            phone = card.phone,
                        ),
                    )
                    navigator.push(BoardScreen(board, player))
                }
            }
        }
    }
}