package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ua.vald_zx.game.rat.race.card.components.AmountTransformation
import ua.vald_zx.game.rat.race.card.design.*
import ua.vald_zx.game.rat.race.card.getDigits
import ua.vald_zx.game.rat.race.card.resources.*
import ua.vald_zx.game.rat.race.card.shared.BoardGeneration
import ua.vald_zx.game.rat.race.card.shared.InflationSettings
import ua.vald_zx.game.rat.race.card.shared.OuterCircleConditions
import ua.vald_zx.game.rat.race.card.shared.VictoryConditions

@Composable
fun DesignNewBoardDialog(
    onDismiss: () -> Unit,
    onCreate: (
        name: String,
        loanLimit: Long,
        businessLimit: Long,
        transportMovementBonusEnabled: Boolean,
        outerCircleConditions: OuterCircleConditions,
        victoryConditions: VictoryConditions,
        generation: BoardGeneration,
        inflation: InflationSettings,
    ) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var loanLimit by remember { mutableStateOf("10000") }
    var businessLimit by remember { mutableStateOf("10") }
    var transportBonus by remember { mutableStateOf(true) }
    var minimumCashFlow by remember { mutableStateOf("50000") }
    var minimumAccountBalance by remember { mutableStateOf("200000") }
    var apartmentRequired by remember { mutableStateOf(true) }
    var carRequired by remember { mutableStateOf(true) }
    var victoryAccountBalance by remember { mutableStateOf("10000000") }
    var dreamRequired by remember { mutableStateOf(true) }
    var planeRequired by remember { mutableStateOf(true) }
    var estateRequired by remember { mutableStateOf(true) }
    var inflationEnabled by remember { mutableStateOf(false) }
    var inflationRate by remember { mutableStateOf("5") }
    var salaryIndexation by remember { mutableStateOf("50") }
    var generateCards by remember { mutableStateOf(false) }
    var theme by remember { mutableStateOf("") }
    var locality by remember { mutableStateOf("") }
    var epoch by remember { mutableStateOf("") }

    val amounts = if (generateCards) {
        emptyList()
    } else {
        listOf(loanLimit, businessLimit, minimumCashFlow, minimumAccountBalance, victoryAccountBalance)
    }
    val complete = name.isNotBlank() && amounts.none { it.isEmpty() }

    DesignDialog(
        title = stringResource(Res.string.new_table),
        onDismissRequest = onDismiss,
        confirmLabel = stringResource(Res.string.create_table),
        confirmEnabled = complete,
        confirmDisabledReason = stringResource(Res.string.fill_all_fields),
        onConfirm = {
            onCreate(
                name,
                loanLimit.toLong(),
                businessLimit.toLong(),
                transportBonus,
                OuterCircleConditions(
                    minimumCashFlow = minimumCashFlow.toLong(),
                    apartmentRequired = apartmentRequired,
                    carRequired = carRequired,
                    minimumAccountBalance = minimumAccountBalance.toLong(),
                ),
                VictoryConditions(
                    dreamRequired = dreamRequired,
                    planeRequired = planeRequired,
                    estateRequired = estateRequired,
                    minimumAccountBalance = victoryAccountBalance.toLong(),
                ),
                BoardGeneration(
                    enabled = generateCards,
                    theme = theme.trim(),
                    locality = locality.trim(),
                    epoch = epoch.trim(),
                ),
                if (inflationEnabled) {
                    InflationSettings(
                        enabled = true,
                        periodRatePercent = inflationRate.toLongOrNull() ?: 0,
                        salaryIndexationPercent = salaryIndexation.toLongOrNull() ?: 0,
                    )
                } else {
                    InflationSettings()
                },
            )
        },
        dismissLabel = stringResource(Res.string.cancel),
        onDismissAction = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            DesignTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(Res.string.table_name),
            )
            DesignSectionTitle(stringResource(Res.string.inflation))
            DesignToggleRow(
                label = stringResource(Res.string.inflation_enabled),
                checked = inflationEnabled,
                onCheckedChange = { inflationEnabled = it },
            )
            Text(
                text = stringResource(Res.string.inflation_hint),
                style = Design.type.label,
                color = Design.scaffold.onSurfaceMuted,
            )
            if (inflationEnabled) {
                PercentField(stringResource(Res.string.inflation_rate), inflationRate) { inflationRate = it }
                PercentField(stringResource(Res.string.salary_indexation_rate), salaryIndexation) {
                    salaryIndexation = it
                }
            }
            DesignSectionTitle(stringResource(Res.string.generated_deck))
            DesignToggleRow(
                label = stringResource(Res.string.generate_cards),
                checked = generateCards,
                onCheckedChange = { generateCards = it },
            )
            Text(
                text = stringResource(Res.string.generate_cards_hint),
                style = Design.type.label,
                color = Design.scaffold.onSurfaceMuted,
            )
            if (generateCards) {
                DesignTextField(
                    value = theme,
                    onValueChange = { theme = it },
                    label = stringResource(Res.string.world_theme),
                )
                DesignTextField(
                    value = locality,
                    onValueChange = { locality = it },
                    label = stringResource(Res.string.world_locality),
                )
                DesignTextField(
                    value = epoch,
                    onValueChange = { epoch = it },
                    label = stringResource(Res.string.world_epoch),
                )
            }
            if (!generateCards) {
                AmountField(stringResource(Res.string.loanLimit), loanLimit) { loanLimit = it }
                AmountField(stringResource(Res.string.businessLimit), businessLimit) { businessLimit = it }
                DesignToggleRow(
                    label = stringResource(Res.string.transport_movement_bonus),
                    checked = transportBonus,
                    onCheckedChange = { transportBonus = it },
                )

                DesignSectionTitle(stringResource(Res.string.outer_circle_conditions))
                AmountField(stringResource(Res.string.minimum_cash_flow), minimumCashFlow) {
                    minimumCashFlow = it
                }
                AmountField(stringResource(Res.string.minimum_account_balance), minimumAccountBalance) {
                    minimumAccountBalance = it
                }
                DesignToggleRow(
                    label = stringResource(Res.string.apartment_required),
                    checked = apartmentRequired,
                    onCheckedChange = { apartmentRequired = it },
                )
                DesignToggleRow(
                    label = stringResource(Res.string.car_required),
                    checked = carRequired,
                    onCheckedChange = { carRequired = it },
                )

                DesignSectionTitle(stringResource(Res.string.victory_conditions))
                AmountField(stringResource(Res.string.victory_account_balance), victoryAccountBalance) {
                    victoryAccountBalance = it
                }
                DesignToggleRow(
                    label = stringResource(Res.string.dream_required),
                    checked = dreamRequired,
                    onCheckedChange = { dreamRequired = it },
                )
                DesignToggleRow(
                    label = stringResource(Res.string.plane_required),
                    checked = planeRequired,
                    onCheckedChange = { planeRequired = it },
                )
                DesignToggleRow(
                    label = stringResource(Res.string.estate_required),
                    checked = estateRequired,
                    onCheckedChange = { estateRequired = it },
                )
            }
        }
    }
}

@Composable
private fun AmountField(label: String, value: String, onValueChange: (String) -> Unit) {
    DesignTextField(
        value = value,
        onValueChange = { onValueChange(it.getDigits()) },
        label = label,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = AmountTransformation,
    )
}

@Composable
private fun PercentField(label: String, value: String, onValueChange: (String) -> Unit) {
    DesignTextField(
        value = value,
        onValueChange = { onValueChange(it.getDigits().take(3)) },
        label = label,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}
