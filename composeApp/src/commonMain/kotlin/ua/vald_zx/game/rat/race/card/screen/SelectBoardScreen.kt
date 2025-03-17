package ua.vald_zx.game.rat.race.card.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.jetbrains.compose.resources.painterResource
import rat_race_card.composeapp.generated.resources.Res
import rat_race_card.composeapp.generated.resources.kris2
import rat_race_card.composeapp.generated.resources.kris4
import ua.vald_zx.game.rat.race.card.raceRate2store
import ua.vald_zx.game.rat.race.card.raceRate4store
import ua.vald_zx.game.rat.race.card.screen.fourth.BornRaceRate4Screen
import ua.vald_zx.game.rat.race.card.screen.fourth.PersonCard4Screen
import ua.vald_zx.game.rat.race.card.screen.second.RaceRate2Screen
import ua.vald_zx.game.rat.race.card.screen.second.PersonCard2Screen

class SelectBoardScreen : Screen {
    @Composable
    override fun Content() {
        val raceRate2State by raceRate2store.observeState().collectAsState()
        val raceRate4State by raceRate4store.observeState().collectAsState()
        val navigator = LocalNavigator.current
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.systemBarsPadding().padding(24.dp).fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val startScreen =
                            if (raceRate2State.professionCard.profession.isNotEmpty()) {
                                RaceRate2Screen()
                            } else {
                                PersonCard2Screen()
                            }
                        navigator?.push(startScreen)
                    }) {
                Image(
                    painter = painterResource(Res.drawable.kris2),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Inside
                )
                Text("Крисині біга 2")
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        val startScreen = if (raceRate4State.list.isNotEmpty()) {
                            BornRaceRate4Screen()
                        } else {
                            PersonCard4Screen()
                        }
                        navigator?.push(startScreen)
                    }) {
                Image(
                    painter = painterResource(Res.drawable.kris4),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Inside
                )
                Text("Крисині біга 4")
            }
        }
    }
}