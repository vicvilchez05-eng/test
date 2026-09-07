package com.personal.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.app.R
import com.personal.app.data.prefs.SupportedCurrencies
import com.personal.app.ui.components.FormHeader
import com.personal.app.ui.components.Group
import com.personal.app.ui.components.GroupRow
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.viewmodel.SettingsViewModel
import com.personal.app.ui.viewmodel.appViewModel
import java.util.Currency
import java.util.Locale

/** Display currency picker. No conversion: it labels totals and seeds new manual accounts. */
@Composable
fun CurrencyScreen(onDone: () -> Unit) {
    val vm = appViewModel { SettingsViewModel(it.preferences, it.repository) }
    val prefs by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current
    ScreenScaffold {
        item { FormHeader(stringResource(R.string.row_currency), onBack = onDone) }
        item {
            Text(stringResource(R.string.currency_note), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
            Spacer(Modifier.height(14.dp))
            Group(
                SupportedCurrencies.map { code ->
                    val cur = runCatching { Currency.getInstance(code) }.getOrNull()
                    GroupRow(
                        label = cur?.getDisplayName(Locale.getDefault())?.replaceFirstChar { it.uppercase() } ?: code,
                        value = "$code · ${cur?.getSymbol(Locale.getDefault()) ?: ""}",
                        chevron = false,
                        trailing = {
                            Box(Modifier.size(20.dp).clip(CircleShape).border(if (prefs.currency == code) 6.dp else 1.5.dp, if (prefs.currency == code) p.moss else p.mutedLight, CircleShape))
                        },
                        onClick = { vm.setCurrency(code); onDone() },
                    )
                },
            )
        }
    }
}
