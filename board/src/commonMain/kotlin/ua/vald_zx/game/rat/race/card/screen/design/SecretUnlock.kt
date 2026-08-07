package ua.vald_zx.game.rat.race.card.screen.design

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class SecretUnlock(private val requiredTaps: Int = REQUIRED_TAPS) {
    var unlocked by mutableStateOf(false)
        private set

    private var taps = 0

    fun tap() {
        if (!unlocked) taps++
    }

    fun longPress() {
        if (!unlocked && taps >= requiredTaps) unlocked = true
        taps = 0
    }

    fun lock() {
        unlocked = false
        taps = 0
    }

    private companion object {
        const val REQUIRED_TAPS = 5
    }
}

val debugToolsUnlock = SecretUnlock()
