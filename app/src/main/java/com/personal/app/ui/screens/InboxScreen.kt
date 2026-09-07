package com.personal.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.app.R
import com.personal.app.data.model.Account
import com.personal.app.data.model.CapturedTransaction
import com.personal.app.data.model.Category
import com.personal.app.data.model.Money
import com.personal.app.ui.components.Chip
import com.personal.app.ui.components.EsforiaTextField
import com.personal.app.ui.components.FieldLabel
import com.personal.app.ui.components.FormHeader
import com.personal.app.ui.components.OptionPill
import com.personal.app.ui.components.OutlineButton
import com.personal.app.ui.components.PrimaryButton
import com.personal.app.ui.components.SegmentedToggle
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.components.icon
import com.personal.app.ui.components.labelRes
import com.personal.app.ui.components.money
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.MonoText
import com.personal.app.ui.viewmodel.InboxViewModel
import com.personal.app.ui.viewmodel.appViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Bank notifications waiting for a decision. Each card shows what the parser read (or the raw
 * text if it could not) and expands into a small form: account, sign, amount, category, save or
 * discard. Nothing reaches the ledger until the user taps save.
 */
@Composable
fun InboxScreen(onDone: () -> Unit, onAddAccount: () -> Unit) {
    val vm = appViewModel { InboxViewModel(it.repository) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }

    ScreenScaffold {
        item { FormHeader(stringResource(R.string.inbox_title), onBack = onDone) }
        if (s.items.isEmpty()) {
            item {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.inbox_empty), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                }
            }
        }
        items(s.items.size, key = { s.items[it].id }) { i ->
            val item = s.items[i]
            CaptureCard(
                modifier = Modifier.testTag("inbox_item_$i"),
                item = item,
                accounts = s.accounts,
                expanded = expanded == item.id,
                onToggle = { expanded = if (expanded == item.id) null else item.id },
                onAccept = { accountId, amount, category, description -> vm.accept(item.id, accountId, amount, category, description); expanded = null },
                onDismiss = { vm.dismiss(item.id) },
                onAddAccount = onAddAccount,
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CaptureCard(
    modifier: Modifier = Modifier,
    item: CapturedTransaction,
    accounts: List<Account>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAccept: (accountId: String, amountMinor: Long, category: Category, description: String) -> Unit,
    onDismiss: () -> Unit,
    onAddAccount: () -> Unit,
) {
    val p = LocalPalette.current
    val when_ = remember(item.postedAt) {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).format(Instant.ofEpochMilli(item.postedAt).atZone(ZoneId.systemDefault()))
    }
    var isExpense by rememberSaveable(item.id) { mutableStateOf((item.amountMinor ?: -1L) < 0) }
    var amountText by rememberSaveable(item.id) { mutableStateOf(item.amountMinor?.let { Money.format(kotlin.math.abs(it), "EUR").filter { c -> c.isDigit() || c == ',' || c == '.' } } ?: "") }
    var category by rememberSaveable(item.id) { mutableStateOf(item.suggestedCategory ?: Category.OTHER) }
    var description by rememberSaveable(item.id) { mutableStateOf(item.merchant ?: item.title) }
    var accountId by rememberSaveable(item.id) { mutableStateOf(accounts.firstOrNull()?.id) }
    if (accountId == null && accounts.isNotEmpty()) accountId = accounts.first().id

    SurfaceCard(modifier.fillMaxWidth(), onClick = onToggle) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.merchant ?: item.title, style = MaterialTheme.typography.titleMedium, color = p.ink)
                Spacer(Modifier.height(2.dp))
                Text(when_, style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
            }
            Spacer(Modifier.width(8.dp))
            if (item.amountMinor != null) {
                Text(money(item.amountMinor, "EUR", signed = true), style = MonoText, color = if (item.amountMinor > 0) p.emberText else p.ink)
            } else {
                Chip(stringResource(R.string.inbox_unparsed), background = p.statusMidSoft, color = p.statusMid)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(item.text, style = MaterialTheme.typography.bodySmall, color = p.inkSoft, maxLines = if (expanded) 10 else 2)
        item.suggestedCategory?.let {
            Spacer(Modifier.height(8.dp))
            Chip(stringResource(it.labelRes))
        }

        if (expanded) {
            Spacer(Modifier.height(16.dp))
            SegmentedToggle(stringResource(R.string.expense), stringResource(R.string.income), leftSelected = isExpense, onChange = { isExpense = it })
            Spacer(Modifier.height(12.dp))
            FieldLabel(stringResource(R.string.report_amount))
            EsforiaTextField(amountText, { amountText = it }, placeholder = "0,00", keyboardType = KeyboardType.Decimal, textStyle = MonoText, leading = { Text("€", style = MonoText, color = p.inkSoft) })
            Spacer(Modifier.height(12.dp))
            FieldLabel(stringResource(R.string.field_description))
            EsforiaTextField(description, { description = it })
            Spacer(Modifier.height(12.dp))
            FieldLabel(stringResource(R.string.field_category))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.filter { it.isIncome == !isExpense }.forEach { c ->
                    OptionPill(stringResource(c.labelRes), selected = category == c, onClick = { category = c }, icon = c.icon)
                }
            }
            Spacer(Modifier.height(12.dp))
            FieldLabel(stringResource(R.string.field_account))
            if (accounts.isEmpty()) {
                Text(stringResource(R.string.no_accounts_yet), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                Spacer(Modifier.height(8.dp))
                OutlineButton(stringResource(R.string.add_account_manually), onClick = onAddAccount)
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    accounts.forEach { a -> OptionPill(a.name, selected = accountId == a.id, onClick = { accountId = a.id }, icon = a.type.icon) }
                }
            }
            Spacer(Modifier.height(16.dp))
            val amount = Money.parseToMinor(amountText)?.let { kotlin.math.abs(it) } ?: 0L
            val canSave = amount > 0 && accountId != null && description.isNotBlank()
            PrimaryButton(stringResource(R.string.save), onClick = { onAccept(accountId!!, if (isExpense) -amount else amount, category, description) }, enabled = canSave)
            Spacer(Modifier.height(8.dp))
            OutlineButton(stringResource(R.string.discard), onClick = onDismiss, trailingIcon = null)
        }
    }
}
