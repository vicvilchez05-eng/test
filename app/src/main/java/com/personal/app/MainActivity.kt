package com.personal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.personal.app.ui.theme.PersonalAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Transparent system bars: the blob background runs under the status and gesture bars.
        enableEdgeToEdge()
        val container = (application as PersonalApplication).container
        setContent {
            CompositionLocalProvider(LocalAppContainer provides container) {
                PersonalAppTheme {
                    FinanceApp()
                }
            }
        }
    }
}
