package com.personal.app

import android.app.Application
import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import com.personal.app.data.bank.BankProvider
import com.personal.app.data.bank.MockBankProvider
import com.personal.app.data.repository.FinanceRepository
import com.personal.app.data.store.FinanceStore
import com.personal.app.data.store.JsonFileFinanceStore
import java.io.File

/**
 * Hand-rolled dependency graph: one store, one repository, the registered bank providers.
 * No DI framework (HANDOFF D-025); a personal app has a dozen objects, not a thousand.
 * Tests and screenshots build their own container with an in-memory store.
 */
class AppContainer(
    val store: FinanceStore,
    providers: List<BankProvider>,
    clock: () -> Long = { System.currentTimeMillis() },
) {
    val providers: Map<String, BankProvider> = providers.associateBy { it.id }
    val repository = FinanceRepository(store, this.providers, clock)

    companion object {
        fun production(context: Context): AppContainer = AppContainer(
            store = JsonFileFinanceStore(File(context.filesDir, "finance.json")),
            providers = listOf(MockBankProvider()),
        )
    }
}

val LocalAppContainer = staticCompositionLocalOf<AppContainer> { error("No AppContainer provided") }

class PersonalApplication : Application() {
    val container: AppContainer by lazy { AppContainer.production(this) }
}
