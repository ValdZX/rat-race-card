package ua.vald_zx.game.rat.race.card.shared

enum class StuckTurnRecovery {
    NONE,
    COMPLETE_ROLL,
    ADVANCE_TURN,
}

fun Player.awaitsSalaryDecision(): Boolean = salaryPosition != null || investmentPosition != null

fun Board.stuckTurnRecovery(activePlayer: Player? = null): StuckTurnRecovery = when {
    winnerId != null -> StuckTurnRecovery.NONE
    activePlayerId.isEmpty() -> StuckTurnRecovery.NONE
    diceRolling -> StuckTurnRecovery.COMPLETE_ROLL
    canRoll -> StuckTurnRecovery.NONE
    canTakeCard.isNotEmpty() -> StuckTurnRecovery.NONE
    takenCard != null -> StuckTurnRecovery.NONE
    pendingInteractions.isNotEmpty() -> StuckTurnRecovery.NONE
    auction != null -> StuckTurnRecovery.NONE
    activePlayer?.awaitsSalaryDecision() == true -> StuckTurnRecovery.NONE
    else -> StuckTurnRecovery.ADVANCE_TURN
}

fun StuckTurnRecovery.command(): GameCommand? = when (this) {
    StuckTurnRecovery.NONE -> null
    StuckTurnRecovery.COMPLETE_ROLL -> GameCommand.CompleteRoll
    StuckTurnRecovery.ADVANCE_TURN -> GameCommand.AdvanceTurn
}
