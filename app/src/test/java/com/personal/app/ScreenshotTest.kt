package com.personal.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.personal.app.ui.theme.PersonalAppTheme
import com.personal.app.ui.theme.Skin
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Renders the app on the JVM (Robolectric + Roborazzi) and writes PNGs to app/screenshots/.
 * Run with `./gradlew recordRoborazziDebug`. No emulator needed.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class ScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private fun setApp(dark: Boolean, skin: Skin) {
        compose.setContent {
            PersonalAppTheme(darkTheme = dark, skin = skin) {
                FinanceApp(animatedBackground = false, blurBackground = false)
            }
        }
    }

    private fun prefix(skin: Skin) = skin.name.lowercase()

    // ---- Esforia skin ----

    @Test
    fun esforia_home_dark() {
        setApp(dark = true, Skin.Esforia)
        compose.onRoot().captureRoboImage("screenshots/esforia_home_dark.png")
    }

    @Test
    fun esforia_home_light() {
        setApp(dark = false, Skin.Esforia)
        compose.onRoot().captureRoboImage("screenshots/esforia_home_light.png")
    }

    @Test
    fun esforia_other_tabs_light() {
        setApp(dark = false, Skin.Esforia)
        listOf("accounts", "balance", "settings", "profile").forEach { route ->
            compose.onNodeWithTag("nav_$route").performClick()
            compose.waitForIdle()
            compose.onRoot().captureRoboImage("screenshots/esforia_${route}_light.png")
        }
    }

    // ---- NavyGold skin ----

    @Test
    fun navygold_home_dark() {
        setApp(dark = true, Skin.NavyGold)
        compose.onRoot().captureRoboImage("screenshots/navygold_home_dark.png")
    }

    @Test
    fun navygold_home_light() {
        setApp(dark = false, Skin.NavyGold)
        compose.onRoot().captureRoboImage("screenshots/navygold_home_light.png")
    }

    @Test
    fun navygold_home_light_scrolled_navbar_collapsed() {
        setApp(dark = false, Skin.NavyGold)
        compose.onNodeWithTag("screen_list").performTouchInput { swipeUp() }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/navygold_home_light_scrolled.png")
    }

    @Test
    fun navygold_other_tabs_light() {
        setApp(dark = false, Skin.NavyGold)
        listOf("accounts", "balance", "settings", "profile").forEach { route ->
            compose.onNodeWithTag("nav_$route").performClick()
            compose.waitForIdle()
            compose.onRoot().captureRoboImage("screenshots/navygold_${route}_light.png")
        }
    }

    @Test
    fun navygold_other_tabs_dark() {
        setApp(dark = true, Skin.NavyGold)
        listOf("accounts", "balance").forEach { route ->
            compose.onNodeWithTag("nav_$route").performClick()
            compose.waitForIdle()
            compose.onRoot().captureRoboImage("screenshots/navygold_${route}_dark.png")
        }
    }
}
