package com.personal.app.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Summarize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.personal.app.R
import com.personal.app.ui.components.EsforiaSwitch
import com.personal.app.ui.components.Group
import com.personal.app.ui.components.GroupLabel
import com.personal.app.ui.components.GroupRow
import com.personal.app.ui.components.PageHeader
import com.personal.app.ui.theme.LocalPalette

@Composable
fun SettingsScreen() {
    val p = LocalPalette.current
    val phase2 = stringResource(R.string.phase_2)
    val phase3 = stringResource(R.string.phase_3)
    val phase4 = stringResource(R.string.phase_4)
    ScreenScaffold {
        item {
            PageHeader(title = stringResource(R.string.settings_title), eyebrow = stringResource(R.string.settings_eyebrow))
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.group_general))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_currency), Icons.Outlined.Paid, "EUR"),
                    GroupRow(stringResource(R.string.row_notifications), Icons.Outlined.NotificationsNone, trailing = { EsforiaSwitch(checked = true, onChange = {}) }),
                    GroupRow(stringResource(R.string.row_dark_mode), Icons.Outlined.DarkMode, trailing = { EsforiaSwitch(checked = p.isDark, onChange = {}) }),
                ),
            )
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.group_data))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_bank_apis), Icons.Outlined.AccountBalance, phase2),
                    GroupRow(stringResource(R.string.row_manual_entries), Icons.Outlined.Edit, phase2),
                    GroupRow(stringResource(R.string.row_reports), Icons.Outlined.Summarize, phase4),
                    GroupRow(stringResource(R.string.row_backup), Icons.Outlined.Backup, phase4),
                ),
            )
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.group_help))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_support), Icons.Outlined.HelpOutline, phase3),
                    GroupRow(stringResource(R.string.row_about), Icons.Outlined.Info, phase3),
                ),
            )
        }
    }
}
