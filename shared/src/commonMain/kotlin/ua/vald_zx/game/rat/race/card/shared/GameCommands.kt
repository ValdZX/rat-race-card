package ua.vald_zx.game.rat.race.card.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameSnapshot(
    val board: Board,
    val players: List<Player>,
)

@Serializable
data class GameCommandEnvelope(
    val commandId: String,
    val boardId: String,
    val playerId: String,
    val expectedRevision: Long,
    val command: GameCommand,
)

@Serializable
sealed interface GameCommand {
    @Serializable
    @SerialName("rollDice")
    data class RollDice(val nonce: String) : GameCommand

    @Serializable
    @SerialName("completeRoll")
    data object CompleteRoll : GameCommand

    @Serializable
    @SerialName("endTurn")
    data class EndTurn(val reason: String) : GameCommand

    @Serializable
    @SerialName("advanceTurn")
    data object AdvanceTurn : GameCommand

    @Serializable
    @SerialName("moveTo")
    data class MoveTo(val position: Int) : GameCommand
}

sealed interface DomainEvent {
    data class DiceRolled(val playerId: String, val dice: Int) : DomainEvent
    data class PlayerMoved(val playerId: String, val from: Int, val to: Int) : DomainEvent
    data class PlayerChanged(val player: Player) : DomainEvent
    data class CardOptionsOpened(val cardTypes: List<BoardCardType>) : DomainEvent
    data class TurnAdvanced(val fromPlayerId: String, val toPlayerId: String) : DomainEvent
    data class PaymentApplied(
        val playerId: String,
        val amount: Long,
        val events: List<PaymentEvent>,
    ) : DomainEvent
}

sealed interface PresentationNotice {
    data class BankruptBusiness(val business: Business) : PresentationNotice
    data class PlayerHadBaby(val playerId: String, val babies: Long) : PresentationNotice
    data class PlayerDivorced(val playerId: String) : PresentationNotice
    data class PlayerMarried(val playerId: String) : PresentationNotice
    data class Resignation(val business: Business) : PresentationNotice
    data object DreamOffered : PresentationNotice
    data class TaxInspectionPaid(val amount: Long) : PresentationNotice
    data class CashSubtracted(val amount: Long) : PresentationNotice
    data class DepositWithdrawn(val amount: Long) : PresentationNotice
    data class LoanAdded(val amount: Long) : PresentationNotice
    data object LoanLimitExceeded : PresentationNotice
}

data class RuleResult(
    val snapshot: GameSnapshot,
    val events: List<DomainEvent> = emptyList(),
    val notices: List<PresentationNotice> = emptyList(),
    val turnDirective: TurnDirective = TurnDirective.KEEP_TURN,
)

enum class TurnDirective {
    KEEP_TURN,
    END_TURN,
}

sealed interface GameExecution {
    val snapshot: GameSnapshot

    data class Applied(val result: RuleResult) : GameExecution {
        override val snapshot: GameSnapshot = result.snapshot
    }

    data class Duplicate(override val snapshot: GameSnapshot) : GameExecution

    data class Rejected(
        override val snapshot: GameSnapshot,
        val reason: GameCommandRejection,
    ) : GameExecution
}

@Serializable
enum class GameCommandRejection {
    EMPTY_COMMAND_ID,
    BOARD_MISMATCH,
    PLAYER_NOT_FOUND,
    PLAYER_NOT_ACTIVE,
    REVISION_CONFLICT,
    ROLL_NOT_ALLOWED,
    ROLL_NOT_IN_PROGRESS,
    NO_ACTIVE_PLAYERS,
    COMMAND_NOT_AVAILABLE,
    INVALID_BOARD_DEFINITION,
}

@Serializable
data class GameCommandResponse(
    val status: GameCommandStatus,
    val snapshot: GameSnapshot,
    val rejection: GameCommandRejection? = null,
)

@Serializable
enum class GameCommandStatus {
    APPLIED,
    DUPLICATE,
    REJECTED,
}
