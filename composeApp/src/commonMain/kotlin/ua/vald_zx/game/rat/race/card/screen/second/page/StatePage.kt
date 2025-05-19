package ua.vald_zx.game.rat.race.card.screen.second.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.DetailsField
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Baby
import ua.vald_zx.game.rat.race.card.resource.images.Car
import ua.vald_zx.game.rat.race.card.resource.images.Estate
import ua.vald_zx.game.rat.race.card.resource.images.Flat
import ua.vald_zx.game.rat.race.card.resource.images.Fly
import ua.vald_zx.game.rat.race.card.resource.images.Mariage
import ua.vald_zx.game.rat.race.card.resource.images.Work
import ua.vald_zx.game.rat.race.card.resource.images.Yacht

val greyScaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
fun StatePage(state: RatRace2CardState) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(state = rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Image(imageVector = Images.Work, contentDescription = null, colorFilter = greyScaleFilter)
            Image(imageVector = Images.Mariage, contentDescription = null, colorFilter = greyScaleFilter)
            Image(imageVector = Images.Baby, contentDescription = null, colorFilter = greyScaleFilter)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Image(imageVector = Images.Car, contentDescription = null, colorFilter = greyScaleFilter)
            Image(imageVector = Images.Flat, contentDescription = null, colorFilter = greyScaleFilter)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Image(imageVector = Images.Estate, contentDescription = null, colorFilter = greyScaleFilter)
            Image(imageVector = Images.Fly, contentDescription = null, colorFilter = greyScaleFilter)
            Image(imageVector = Images.Yacht, contentDescription = null, colorFilter = greyScaleFilter)
        }
        DetailsField(
            "Активний прибуток",
            state.activeProfit().toString(),
            MaterialTheme.colorScheme.primary
        )
        DetailsField(
            "Пасивний прибуток",
            state.passiveProfit().toString(),
            MaterialTheme.colorScheme.primary
        )
        DetailsField(
            "Загальний прибуток",
            state.totalProfit().toString(),
            MaterialTheme.colorScheme.primary
        )
        DetailsField(
            "Витрати по кредиту",
            state.creditExpenses().toString(),
            MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Загальні витрати",
            state.totalExpenses().toString(),
            MaterialTheme.colorScheme.tertiary
        )
        Text("Багатство", style = MaterialTheme.typography.titleSmall)
        DetailsField("Авто", "${state.cars}")
        DetailsField("Квартири", "${state.apartment}")
        DetailsField("Будинки", "${state.apartment}")
        DetailsField("Яхти", "${state.yacht}")
        DetailsField("Літаки", "${state.flight}")
        Text("Сімейний стан", style = MaterialTheme.typography.titleSmall)
        DetailsField("Шлюб", if (state.isMarried) "Так" else "Hi")
        DetailsField("Діти", "${state.babies}")
        if (state.business.any { it.type == BusinessType.WORK }) {
            Text(
                text = "Робота",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            DetailsField(
                name = state.professionCard.profession,
                value = state.professionCard.salary.toString(),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = "Витрати",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Оренда житла", state.professionCard.rent.toString(),
            color = MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Витрати на їжу", state.professionCard.food.toString(),
            color = MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Витрати на одяг", state.professionCard.cloth.toString(),
            color = MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Витрати на проїзд", state.professionCard.transport.toString(),
            color = MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Витрати на телефонні переговори", state.professionCard.phone.toString(),
            color = MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Витрати на дітей", "${state.babies * 300} (${state.babies} * 300)",
            color = MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Витрати на квартиру",
            "${state.apartment * 200} (${state.apartment} * 200)",
            color = MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Витрати на машину", "${state.cars * 600} (${state.cars} * 600)",
            color = MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Утримання котеджу", "${state.cottage * 1000} (${state.cottage} * 1000)",
            color = MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Утримання яхти", "${state.yacht * 1500} (${state.yacht} * 1500)",
            color = MaterialTheme.colorScheme.tertiary
        )
        DetailsField(
            "Утримання літака", "${state.flight * 5000} (${state.flight} * 5000)",
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}