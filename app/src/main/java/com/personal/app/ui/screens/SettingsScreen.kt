package com.personal.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.app.BuildConfig
import com.personal.app.R
import com.personal.app.data.prefs.ThemeMode
import com.personal.app.ui.components.ConfirmDialog
import com.personal.app.ui.components.EsforiaSwitch
import com.personal.app.ui.components.Group
import com.personal.app.ui.components.GroupLabel
import com.personal.app.ui.components.GroupRow
import com.personal.app.ui.components.OptionPill
import com.personal.app.ui.components.PageHeader
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.viewmodel.SettingsViewModel
import com.personal.app.ui.viewmodel.appViewModel

@Composable
fun SettingsScreen(onCurrency: () -> Unit, onAccounts: () -> Unit, onExport: () -> Unit) {
    val vm = appViewModel { SettingsViewModel(it.preferences, it.repository) }
    val prefs by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current
    var confirmWipe by remember { mutableStateOf(false) }

    ScreenScaffold {
        item {
            PageHeader(title = stringResource(R.string.settings_title), eyebrow = stringResource(R.string.settings_eyebrow))
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.group_general))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_currency), Icons.Outlined.Paid, prefs.currency, onClick = onCurrency),
                    GroupRow(stringResource(R.string.row_notifications), Icons.Outlined.NotificationsNone, trailing = { EsforiaSwitch(checked = prefs.notifications, onChange = vm::setNotifications) }),
                    GroupRow(stringResource(R.string.row_privacy_mode), Icons.Outlined.VisibilityOff, trailing = { EsforiaSwitch(checked = prefs.privacyMode, onChange = vm::setPrivacy) }),
                ),
            )
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.group_appearance))
            SurfaceCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.row_theme), style = MaterialTheme.typography.bodyLarge, color = p.ink)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OptionPill(stringResource(R.string.theme_system), prefs.themeMode == ThemeMode.SYSTEM, { vm.setThemeMode(ThemeMode.SYSTEM) })
                    OptionPill(stringResource(R.string.theme_light), prefs.themeMode == ThemeMode.LIGHT, { vm.setThemeMode(ThemeMode.LIGHT) })
                    OptionPill(stringResource(R.string.theme_dark), prefs.themeMode == ThemeMode.DARK, { vm.setThemeMode(ThemeMode.DARK) })
                }
            }
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.group_data))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_bank_apis), Icons.Outlined.AccountBalance, onClick = onAccounts),
                    GroupRow(stringResource(R.string.row_reports), Icons.Outlined.Description, onClick = onExport),
                    GroupRow(stringResource(R.string.row_delete_all), Icons.Outlined.DeleteOutline, danger = true, chevron = false, onClick = { confirmWipe = true }),
                ),
            )
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.group_help))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_about), Icons.Outlined.Info, "v${BuildConfig.VERSION_NAME}", chevron = false),
                ),
            )
        }
    }

    if (confirmWipe) {
        ConfirmDialog(
            title = stringResource(R.string.delete_all_title),
            body = stringResource(R.string.delete_all_body),
            confirmText = stringResource(R.string.delete),
            dismissText = stringResource(R.string.cancel),
            danger = true,
            onConfirm = { vm.wipeAll(); confirmWipe = false },
            onDismiss = { confirmWipe = false },
        )
    }
}
