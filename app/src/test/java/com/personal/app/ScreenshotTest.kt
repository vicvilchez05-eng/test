package com.personal.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.personal.app.data.bank.MockBankProvider
import com.personal.app.data.model.AccountType
import com.personal.app.data.model.Category
import com.personal.app.data.store.InMemoryFinanceStore
import com.personal.app.ui.theme.PersonalAppTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the app on the JVM (Robolectric + Roborazzi) and writes PNGs to app/screenshots/.
 * Run with `./gradlew recordRoborazziDebug`. No emulator needed.
 * Data: an in-memory ledger with the sandbox bank linked at a fixed clock, so every run is identical.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class ScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val now = 1_757_260_800_000L // 2025-09-07T16:00:00Z

    private fun exporter() = com.personal.app.data.export.ExportManager(androidx.test.core.app.ApplicationProvider.getApplicationContext(), clock = { now })

    private fun seededContainer(): AppContainer {
        val container = AppContainer(InMemoryFinanceStore(), listOf(MockBankProvider(clock = { now }, latencyMillis = 0)), clock = { now }, exporter = exporter())
        runBlocking {
            container.preferences.setName("Vic")
            container.repository.linkBank("mock", "demo")
            val cash = container.repository.addManualAccount("Efectivo", AccountType.CASH, 120_00)
            container.repository.addManualTransaction(cash.id, -12_50, Category.FOOD, "Bocadillo", timestamp = now - 3600_000L)
        }
        return container
    }

    private fun emptyContainer() = AppContainer(InMemoryFinanceStore(), listOf(MockBankProvider(clock = { now }, latencyMillis = 0)), clock = { now }, exporter = exporter())

    private fun setApp(dark: Boolean, container: AppContainer = seededContainer()) {
        compose.setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                PersonalAppTheme(darkTheme = dark) {
                    FinanceApp(animatedBackground = false, blurBackground = false)
                }
            }
        }
    }

    @Test
    fun home_dark() {
        setApp(dark = true)
        compose.onRoot().captureRoboImage("screenshots/home_dark.png")
    }

    @Test
    fun home_light() {
        setApp(dark = false)
        compose.onRoot().captureRoboImage("screenshots/home_light.png")
    }

    @Test
    fun home_empty_light() {
        setApp(dark = false, container = emptyContainer())
        compose.onRoot().captureRoboImage("screenshots/home_empty_light.png")
    }

    @Test
    fun home_light_scrolled_navbar_collapsed() {
        setApp(dark = false)
        compose.onRoot().performTouchInput { swipeUp() }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/home_light_scrolled.png")
    }

    @Test
    fun other_tabs_light() {
        setApp(dark = false)
        listOf("accounts", "settings", "profile").forEach { route ->
            compose.onNodeWithTag("nav_$route").performClick()
            compose.waitForIdle()
            compose.onRoot().captureRoboImage("screenshots/${route}_light.png")
        }
    }

    @Test
    fun balance_light_and_dark() {
        setApp(dark = false)
        compose.onNodeWithTag("nav_balance").performClick(); compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/balance_light.png")
        compose.onRoot().performTouchInput { swipeUp() }; compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/balance_light_scrolled.png")
    }

    @Test
    fun balance_dark() {
        setApp(dark = true)
        compose.onNodeWithTag("nav_balance").performClick(); compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/balance_dark.png")
    }

    @Test
    fun settings_profile_dark() {
        setApp(dark = true)
        compose.onNodeWithTag("nav_settings").performClick(); compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/settings_dark.png")
        compose.onNodeWithTag("nav_profile").performClick(); compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/profile_dark.png")
    }

    @Test
    fun account_detail_and_transactions_light() {
        setApp(dark = false)
        compose.onNodeWithTag("tile_mock:demo-cc").performClick(); compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/account_detail_light.png")
    }

    @Test
    fun all_transactions_light() {
        setApp(dark = false)
        compose.onNodeWithTag("home_all_transactions").performClick(); compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/transactions_light.png")
    }

    @Test
    fun privacy_mode_light() {
        setApp(dark = false)
        compose.onNodeWithTag("home_privacy").performClick(); compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/home_privacy_light.png")
    }

    @Test
    fun swipe_between_tabs_light() {
        setApp(dark = false)
        compose.onRoot().performTouchInput { swipeLeft() }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/swipe_to_accounts_light.png")
    }

    @Test
    fun forms_light() {
        setApp(dark = false)
        compose.onNodeWithTag("home_add").performClick()
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/add_transaction_light.png")
    }

    @Test
    fun link_bank_light() {
        setApp(dark = false, container = emptyContainer())
        compose.onNodeWithTag("home_link_bank").performClick()
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/link_bank_light.png")
    }

    @Test
    fun add_account_light() {
        setApp(dark = false, container = emptyContainer())
        compose.onNodeWithTag("home_add_account").performClick()
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/add_account_light.png")
    }
}
