package com.personal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.personal.app.data.prefs.ThemeMode
import com.personal.app.ui.theme.PersonalAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Transparent system bars: the blob background runs under the status and gesture bars.
        enableEdgeToEdge()
        val container = (application as PersonalApplication).container
        setContent {
            val prefs by container.preferences.prefs.collectAsState()
            val dark = when (prefs.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            CompositionLocalProvider(LocalAppContainer provides container) {
                PersonalAppTheme(darkTheme = dark) {
                    FinanceApp()
                }
            }
        }
    }
}
