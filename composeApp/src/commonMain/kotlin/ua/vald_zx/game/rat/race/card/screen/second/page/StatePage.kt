package ua.vald_zx.game.rat.race.card.screen.second.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.bottomSheet.LocalBottomSheetNavigator
import ua.vald_zx.game.rat.race.card.beans.BusinessType
import ua.vald_zx.game.rat.race.card.components.DetailsField
import ua.vald_zx.game.rat.race.card.components.GoldBackground
import ua.vald_zx.game.rat.race.card.components.SilverBackground
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardAction
import ua.vald_zx.game.rat.race.card.logic.RatRace2CardState
import ua.vald_zx.game.rat.race.card.raceRate2store
import ua.vald_zx.game.rat.race.card.resource.Images
import ua.vald_zx.game.rat.race.card.resource.images.Baby
import ua.vald_zx.game.rat.race.card.resource.images.Car
import ua.vald_zx.game.rat.race.card.resource.images.Estate
import ua.vald_zx.game.rat.race.card.resource.images.Flat
import ua.vald_zx.game.rat.race.card.resource.images.Fly
import ua.vald_zx.game.rat.race.card.resource.images.Mariage
import ua.vald_zx.game.rat.race.card.resource.images.Work
import ua.vald_zx.game.rat.race.card.resource.images.Yacht
import ua.vald_zx.game.rat.race.card.screen.second.BabyScreen
import ua.vald_zx.game.rat.race.card.screen.second.BuyApartmentScreen
import ua.vald_zx.game.rat.race.card.screen.second.BuyCarScreen
import ua.vald_zx.game.rat.race.card.screen.second.BuyCottageScreen
import ua.vald_zx.game.rat.race.card.screen.second.BuyFlightScreen
import ua.vald_zx.game.rat.race.card.screen.second.BuyYachtScreen
import ua.vald_zx.game.rat.race.card.screen.second.MarriageScreen
import ua.vald_zx.game.rat.race.card.splitDecimal
import ua.vald_zx.game.rat.race.card.theme.AppTheme

val greyScaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
fun StatePage(state: RatRace2CardState) {
    val bottomSheetNavigator = LocalBottomSheetNavigator.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(state = rememberScrollState())) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val hasWork = state.business.any { it.type == BusinessType.WORK }
            StateItem(
                name = "Робота",
                imageVector = Images.Work,
                isPositivePrice = true,
                price = state.playerCard.salary,
                enabled = hasWork
            ) {
                if (hasWork) {
                    raceRate2store.dispatch(RatRace2CardAction.Fired)
                }
            }
            StateItem(
                name = "Шлюб",
                imageVector = Images.Mariage,
                enabled = state.isMarried
            ) {
                bottomSheetNavigator.show(MarriageScreen())
            }
            StateItem(
                name = "Діти",
                imageVector = Images.Baby,
                enabled = state.babies > 0,
                count = state.babies,
                price = state.babies * state.config.babyCost
            ) {
                bottomSheetNavigator.show(BabyScreen())
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StateItem(
                name = "Авто",
                imageVector = Images.Car,
                enabled = state.cars > 0,
                count = state.cars,
                price = state.cars * state.config.carCost
            ) {
                bottomSheetNavigator.show(BuyCarScreen())
            }
            StateItem(
                name = "Квартира",
                imageVector = Images.Flat,
                enabled = state.apartment > 0,
                count = state.apartment,
                price = state.apartment * state.config.apartmentCost
            ) {
                bottomSheetNavigator.show(BuyApartmentScreen())
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StateItem(
                name = "Маєток",
                imageVector = Images.Estate,
                enabled = state.cottage > 0,
                count = state.cottage,
                price = state.cottage * state.config.cottageCost
            ) {
                bottomSheetNavigator.show(BuyCottageScreen())
            }
            StateItem(
                name = "Яхта",
                imageVector = Images.Yacht,
                enabled = state.yacht > 0,
                count = state.yacht,
                price = state.yacht * state.config.yachtCost
            ) {
                bottomSheetNavigator.show(BuyYachtScreen())
            }
            StateItem(
                name = "Літак",
                imageVector = Images.Fly,
                enabled = state.flight > 0,
                count = state.flight,
                price = state.flight * state.config.flightCost
            ) {
                bottomSheetNavigator.show(BuyFlightScreen())
            }
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
    }
}

@Composable
fun StateItem(
    name: String,
    imageVector: ImageVector,
    price: Long = 0,
    isPositivePrice: Boolean = false,
    count: Long = 0,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        if (enabled) {
            GoldBackground(modifier = Modifier.padding(4.dp).clip(CircleShape).matchParentSize())
        } else {
            SilverBackground(modifier = Modifier.padding(4.dp).clip(CircleShape).matchParentSize())
        }
        Image(
            imageVector = imageVector,
            contentDescription = null,
            colorFilter = if (enabled) null else greyScaleFilter,
            modifier = Modifier.padding(16.dp)
        )
        if (count > 1) {
            Text(
                count.toString(),
                fontSize = 11.sp,
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterEnd),
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp
            )
        }
        Text(
            name,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                .padding(horizontal = 4.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.surface,
            lineHeight = 13.sp
        )
        if (price > 0) {
            Text(
                "${price.splitDecimal()} $",
                fontSize = 11.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clip(CircleShape)
                    .background(
                        if (isPositivePrice) {
                            AppTheme.colors.cash
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                color = MaterialTheme.colorScheme.onError,
                fontWeight = FontWeight.Bold,
                lineHeight = 13.sp
            )
        }
    }
}