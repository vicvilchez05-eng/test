package com.personal.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.app.R
import com.personal.app.data.capture.BankNotificationListener
import com.personal.app.data.capture.BankNotificationParser
import com.personal.app.ui.components.Chip
import com.personal.app.ui.components.EsforiaSwitch
import com.personal.app.ui.components.EsforiaTextField
import com.personal.app.ui.components.FieldLabel
import com.personal.app.ui.components.FormHeader
import com.personal.app.ui.components.Group
import com.personal.app.ui.components.GroupLabel
import com.personal.app.ui.components.GroupRow
import com.personal.app.ui.components.OutlineButton
import com.personal.app.ui.components.PrimaryButton
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.components.labelRes
import com.personal.app.ui.components.money
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.viewmodel.CaptureSettingsViewModel
import com.personal.app.ui.viewmodel.appViewModel

/**
 * "Read bank notifications": explains what it does, shows whether notification access is
 * granted (re-checked every time the screen comes back to the foreground), lets the user pause
 * it, lists the watched apps, and offers a text box to try the parser on a pasted notification.
 */
@Composable
fun CaptureSettingsScreen(onDone: () -> Unit, onInbox: () -> Unit) {
    val vm = appViewModel { CaptureSettingsViewModel(it.preferences, it.repository) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current
    val context = LocalContext.current
    var hasAccess by remember { mutableStateOf(BankNotificationListener.hasAccess(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) hasAccess = BankNotificationListener.hasAccess(context) }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ScreenScaffold {
        item { FormHeader(stringResource(R.string.capture_title), onBack = onDone) }
        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.capture_intro), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip(
                        stringResource(if (hasAccess) R.string.capture_access_granted else R.string.capture_access_missing),
                        background = if (hasAccess) p.emberSoft else p.statusMidSoft,
                        color = if (hasAccess) p.emberText else p.statusMid,
                    )
                    if (s.pending > 0) Chip(stringResource(R.string.inbox_pending_n, s.pending))
                }
                Spacer(Modifier.height(14.dp))
                if (!hasAccess) {
                    PrimaryButton(stringResource(R.string.capture_grant), onClick = { BankNotificationListener.openAccessSettings(context) })
                } else {
                    OutlineButton(stringResource(R.string.capture_manage_access), onClick = { BankNotificationListener.openAccessSettings(context) }, trailingIcon = Icons.Outlined.Security)
                }
            }
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.group_general))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.capture_enabled), Icons.Outlined.NotificationsActive, trailing = { EsforiaSwitch(checked = s.enabled, onChange = vm::setEnabled) }),
                    GroupRow(stringResource(R.string.inbox_title), Icons.Outlined.Inbox, value = s.pending.toString(), onClick = onInbox),
                ),
            )
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.capture_watched_apps))
            Group(BankNotificationParser.bankApps.map { (pkg, name) -> GroupRow(name, value = pkg, chevron = false) })
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.capture_try_title))
            SurfaceCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.capture_try_body), style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
                Spacer(Modifier.height(10.dp))
                EsforiaTextField(s.sampleText, vm::setSample, placeholder = stringResource(R.string.capture_try_hint), singleLine = false)
                val parsed = s.sampleParsed
                if (s.sampleText.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    if (parsed == null) {
                        Text(stringResource(R.string.capture_try_no_amount), style = MaterialTheme.typography.bodyMedium, color = p.statusMid)
                    } else {
                        FieldLabel(stringResource(R.string.capture_try_result))
                        Text(
                            "${money(parsed.amountMinor, "EUR", signed = true)} · ${parsed.merchant ?: "—"} · ${stringResource(parsed.category.labelRes)}",
                            style = MaterialTheme.typography.bodyLarge, color = p.ink,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlineButton(
                        stringResource(if (s.sampleAdded) R.string.capture_try_added else R.string.capture_try_add),
                        onClick = vm::addSampleToInbox, trailingIcon = Icons.Outlined.Inbox,
                    )
                }
            }
        }
    }
}
