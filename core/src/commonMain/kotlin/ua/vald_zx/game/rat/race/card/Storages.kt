package ua.vald_zx.game.rat.race.card

import io.github.xxfast.kstore.KStore
import kotlinx.serialization.Serializable

expect inline fun <reified T : @Serializable Any> getStore(name: String, default: T? = null): KStore<T>
