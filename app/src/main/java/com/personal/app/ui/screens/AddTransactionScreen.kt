package com.personal.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.app.R
import com.personal.app.ui.components.AmountField
import com.personal.app.ui.components.EsforiaTextField
import com.personal.app.ui.components.FieldLabel
import com.personal.app.ui.components.FormHeader
import com.personal.app.ui.components.Group
import com.personal.app.ui.components.GroupRow
import com.personal.app.ui.components.OptionPill
import com.personal.app.ui.components.PrimaryButton
import com.personal.app.ui.components.SegmentedToggle
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.components.icon
import com.personal.app.ui.components.labelRes
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.viewmodel.AddTransactionViewModel
import com.personal.app.ui.viewmodel.appViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Manual entry (the "secondary sync" of Phase 2): expense/income toggle, big amount, category
 * pills, account picker, date, description and note. Saves and pops back.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(onDone: () -> Unit, onAddAccount: () -> Unit) {
    val vm = appViewModel { AddTransactionViewModel(it.repository) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current
    var showDate by remember { mutableStateOf(false) }

    LaunchedEffect(s.saved) { if (s.saved) onDone() }

    ScreenScaffold {
        item { FormHeader(stringResource(R.string.add_transaction_title), onBack = onDone) }
        item {
            SegmentedToggle(stringResource(R.string.expense), stringResource(R.string.income), leftSelected = s.isExpense, onChange = vm::setExpense)
            Spacer(Modifier.height(22.dp))
            AmountField(s.amountText, vm::setAmount, "€", tint = if (s.isExpense) p.ink else p.emberText)
            Spacer(Modifier.height(22.dp))
        }
        item {
            FieldLabel(stringResource(R.string.field_category))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                s.categories.forEach { c ->
                    OptionPill(stringResource(c.labelRes), selected = s.category == c, onClick = { vm.setCategory(c) }, icon = c.icon)
                }
            }
            Spacer(Modifier.height(18.dp))
        }
        item {
            FieldLabel(stringResource(R.string.field_account))
            if (s.accounts.isEmpty()) {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.no_accounts_yet), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                    Spacer(Modifier.height(10.dp))
                    com.personal.app.ui.components.OutlineButton(stringResource(R.string.add_account_manually), onClick = onAddAccount)
                }
            } else {
                Group(
                    s.accounts.map { a ->
                        GroupRow(
                            label = a.name, icon = a.type.icon, value = a.institution ?: stringResource(a.type.labelRes), chevron = false,
                            trailing = { RadioDot(selected = s.accountId == a.id) },
                            onClick = { vm.setAccount(a.id) },
                        )
                    },
                )
            }
            Spacer(Modifier.height(18.dp))
        }
        item {
            FieldLabel(stringResource(R.string.field_description))
            EsforiaTextField(s.description, vm::setDescription, placeholder = stringResource(R.string.field_description_hint))
            Spacer(Modifier.height(14.dp))
            FieldLabel(stringResource(R.string.field_date))
            val zone = ZoneId.systemDefault()
            val dateText = remember(s.timestamp) { DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(Instant.ofEpochMilli(s.timestamp).atZone(zone)) }
            Group(listOf(GroupRow(dateText, icon = Icons.Outlined.CalendarMonth, chevron = true, onClick = { showDate = true })))
            Spacer(Modifier.height(14.dp))
            FieldLabel(stringResource(R.string.field_note))
            EsforiaTextField(s.note, vm::setNote, placeholder = stringResource(R.string.field_note_hint), singleLine = false)
            Spacer(Modifier.height(24.dp))
        }
        item {
            s.error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = p.danger); Spacer(Modifier.height(8.dp)) }
            PrimaryButton(stringResource(R.string.save), onClick = vm::save, enabled = s.canSave)
        }
    }

    if (showDate) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = s.timestamp)
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { vm.setTimestamp(it + 12 * 3600_000L) }
                    showDate = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showDate = false }) { Text(stringResource(R.string.cancel)) } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun RadioDot(selected: Boolean) {
    val p = LocalPalette.current
    Box(
        Modifier
            .size(20.dp)
            .clip(CircleShape)
            .border(if (selected) 6.dp else 1.5.dp, if (selected) p.moss else p.mutedLight, CircleShape),
    )
}
