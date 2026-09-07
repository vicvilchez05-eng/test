package com.personal.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.app.R
import com.personal.app.data.model.BankConnection
import com.personal.app.ui.components.Chip
import com.personal.app.ui.components.ConfirmDialog
import com.personal.app.ui.components.EsforiaSwitch
import com.personal.app.ui.components.EsforiaTextField
import com.personal.app.ui.components.Group
import com.personal.app.ui.components.GroupLabel
import com.personal.app.ui.components.GroupRow
import com.personal.app.ui.components.OutlineButton
import com.personal.app.ui.components.PageHeader
import com.personal.app.ui.components.PrimaryButton
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.components.softShadow
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.viewmodel.ProfileViewModel
import com.personal.app.ui.viewmodel.appViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ProfileScreen(onLinkBank: () -> Unit) {
    val vm = appViewModel { ProfileViewModel(it.preferences, it.repository) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current
    var editing by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    var unlinkTarget by remember { mutableStateOf<BankConnection?>(null) }
    var confirmWipe by remember { mutableStateOf(false) }

    ScreenScaffold {
        item {
            PageHeader(title = stringResource(R.string.profile_title), eyebrow = stringResource(R.string.profile_eyebrow))
            Spacer(Modifier.height(20.dp))
        }
        item {
            SurfaceCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(72.dp)
                            .softShadow(CircleShape, 24.dp, p.heroShadow, offsetY = 10.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(p.financeGradient, start = Offset.Zero, end = Offset.Infinite)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(s.name.trim().take(1).uppercase().ifEmpty { "·" }, style = MaterialTheme.typography.headlineMedium, color = p.onAccent)
                    }
                    Spacer(Modifier.height(12.dp))
                    if (editing) {
                        EsforiaTextField(draft, { draft = it }, placeholder = stringResource(R.string.field_name_person_hint))
                        Spacer(Modifier.height(10.dp))
                        PrimaryButton(stringResource(R.string.save), onClick = { vm.setName(draft); editing = false }, enabled = draft.isNotBlank())
                    } else {
                        Text(s.name.ifBlank { stringResource(R.string.no_name_yet) }, style = MaterialTheme.typography.titleLarge, color = p.ink)
                        Spacer(Modifier.height(3.dp))
                        val since = s.since?.let { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())) }
                        Text(if (since != null) stringResource(R.string.member_since, since) else stringResource(R.string.profile_personal_use), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Chip(pluralStringResource(R.plurals.n_accounts, s.accounts, s.accounts))
                            Chip(pluralStringResource(R.plurals.n_transactions, s.transactions, s.transactions), background = p.blueSoft, color = p.blue)
                        }
                        Spacer(Modifier.height(14.dp))
                        OutlineButton(stringResource(R.string.edit_name), onClick = { draft = s.name; editing = true }, trailingIcon = null)
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.row_linked_banks))
            if (s.connections.isEmpty()) {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.no_linked_banks), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                    Spacer(Modifier.height(12.dp))
                    OutlineButton(stringResource(R.string.link_bank_title), onClick = onLinkBank)
                }
            } else {
                Group(
                    s.connections.map { c ->
                        val linkedAt = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(Instant.ofEpochMilli(c.linkedAt).atZone(ZoneId.systemDefault()))
                        GroupRow(c.institutionName, Icons.Outlined.AccountBalance, value = linkedAt, onClick = { unlinkTarget = c })
                    } + GroupRow(stringResource(R.string.link_another_bank), Icons.Outlined.Link, onClick = onLinkBank),
                )
            }
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.group_privacy))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_privacy_mode), Icons.Outlined.VisibilityOff, trailing = { EsforiaSwitch(checked = s.privacyMode, onChange = vm::setPrivacy) }),
                    GroupRow(stringResource(R.string.row_delete_all), Icons.Outlined.DeleteOutline, danger = true, chevron = false, onClick = { confirmWipe = true }),
                ),
            )
        }
    }

    unlinkTarget?.let { c ->
        ConfirmDialog(
            title = stringResource(R.string.unlink_title, c.institutionName),
            body = stringResource(R.string.unlink_body),
            confirmText = stringResource(R.string.unlink),
            dismissText = stringResource(R.string.cancel),
            danger = true,
            onConfirm = { vm.unlink(c.id); unlinkTarget = null },
            onDismiss = { unlinkTarget = null },
        )
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
