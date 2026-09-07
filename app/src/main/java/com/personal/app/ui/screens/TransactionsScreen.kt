package com.personal.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.personal.app.R
import com.personal.app.data.model.Source
import com.personal.app.data.model.Transaction
import com.personal.app.ui.components.ConfirmDialog
import com.personal.app.ui.components.FormHeader
import com.personal.app.ui.components.GroupLabel
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.components.labelRes
import com.personal.app.ui.components.pressable
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.viewmodel.TransactionsViewModel
import com.personal.app.ui.viewmodel.appViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Every movement (or one account's), grouped by day. Tap a row to see details / delete it. */
@Composable
fun TransactionsScreen(accountId: String?, onDone: () -> Unit) {
    val vm = appViewModel { TransactionsViewModel(it.repository, accountId) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current
    var selected by remember { mutableStateOf<Transaction?>(null) }
    val dayFormat = remember { DateTimeFormatter.ofPattern("EEEE d MMMM") }

    ScreenScaffold {
        item { FormHeader(s.title ?: stringResource(R.string.all_transactions), onBack = onDone) }
        if (s.days.isEmpty()) {
            item { SurfaceCard(Modifier.fillMaxWidth()) { Text(stringResource(R.string.no_transactions_yet), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft) } }
        }
        s.days.forEach { (day, txs) ->
            item(key = "day-$day") {
                GroupLabel(dayLabel(day, dayFormat))
                SurfaceCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    txs.forEachIndexed { i, tx ->
                        Box(Modifier.pressable { selected = tx }) { TransactionRow(tx) }
                        if (i < txs.lastIndex) Box(Modifier.fillMaxWidth().padding(start = 40.dp).height(1.dp).background(p.line))
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
        }
    }

    selected?.let { tx ->
        val account = s.accountsById[tx.accountId]
        val details = buildString {
            append(stringResource(tx.category.labelRes))
            account?.let { append(" · ").append(it.name) }
            if (tx.source == Source.LINKED) append(" · ").append(stringResource(R.string.linked))
            tx.note?.let { append("\n").append(it) }
        }
        ConfirmDialog(
            title = tx.description,
            body = details,
            confirmText = stringResource(R.string.delete),
            dismissText = stringResource(R.string.close),
            danger = true,
            onConfirm = { vm.delete(tx.id); selected = null },
            onDismiss = { selected = null },
        )
    }
}

@Composable
private fun dayLabel(day: LocalDate, format: DateTimeFormatter): String {
    val today = LocalDate.now()
    return when (day) {
        today -> stringResource(R.string.today)
        today.minusDays(1) -> stringResource(R.string.yesterday)
        else -> day.format(format).replaceFirstChar { it.uppercase() }
    }
}
