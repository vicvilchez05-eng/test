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

    private fun setApp(dark: Boolean) {
        compose.setContent {
            PersonalAppTheme(darkTheme = dark) {
                FinanceApp(animatedBackground = false, blurBackground = false)
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
    fun home_dark_scrolled_navbar_collapsed() {
        setApp(dark = true)
        compose.onNodeWithTag("screen_list").performTouchInput { swipeUp() }
        compose.waitForIdle()
        compose.onRoot().captureRoboImage("screenshots/home_dark_scrolled.png")
    }

    @Test
    fun other_tabs_dark() {
        setApp(dark = true)
        listOf("accounts", "balance", "settings", "profile").forEach { route ->
            compose.onNodeWithTag("nav_$route").performClick()
            compose.waitForIdle()
            compose.onRoot().captureRoboImage("screenshots/${route}_dark.png")
        }
    }
}
