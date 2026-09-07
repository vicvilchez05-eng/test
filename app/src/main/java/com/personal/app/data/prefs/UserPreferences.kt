package com.personal.app.data.prefs

import com.personal.app.data.store.Store
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Everything the user can set in Settings / Profile. Persisted as `settings.json`. */
@Serializable
data class UserPreferences(
    val name: String = "",
    /** Display currency for totals and the default for new manual accounts. No FX conversion. */
    val currency: String = "EUR",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Hide every amount behind dots (Esforia's eye toggle). */
    val privacyMode: Boolean = false,
    /** Pause the bank-notification listener without revoking the system permission. */
    val captureEnabled: Boolean = true,
)

/** Currencies offered in Settings: code → shown with its symbol by java.util.Currency. */
val SupportedCurrencies = listOf("EUR", "USD", "GBP", "CHF", "MXN", "ARS", "COP", "CLP", "PEN", "BRL")

class PreferencesRepository(private val store: Store<UserPreferences>) {
    val prefs: StateFlow<UserPreferences> = store.data
    suspend fun update(transform: (UserPreferences) -> UserPreferences) = store.update(transform)
    suspend fun setName(name: String) = update { it.copy(name = name.trim()) }
    suspend fun setCurrency(code: String) = update { it.copy(currency = code) }
    suspend fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }
    suspend fun setPrivacyMode(on: Boolean) = update { it.copy(privacyMode = on) }
    suspend fun setCaptureEnabled(on: Boolean) = update { it.copy(captureEnabled = on) }
}
