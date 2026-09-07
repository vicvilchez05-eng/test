package com.personal.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.app.R
import com.personal.app.data.model.AccountType
import com.personal.app.ui.components.EsforiaTextField
import com.personal.app.ui.components.FieldLabel
import com.personal.app.ui.components.FormHeader
import com.personal.app.ui.components.OptionPill
import com.personal.app.ui.components.PrimaryButton
import com.personal.app.ui.components.icon
import com.personal.app.ui.components.labelRes
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.MonoText
import com.personal.app.ui.viewmodel.AddAccountViewModel
import com.personal.app.ui.viewmodel.appViewModel

/** A manual account (cash, a bank you don't want to link...): name, type, opening balance. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddAccountScreen(onDone: () -> Unit) {
    val vm = appViewModel { AddAccountViewModel(it.repository) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current
    LaunchedEffect(s.saved) { if (s.saved) onDone() }

    ScreenScaffold {
        item { FormHeader(stringResource(R.string.add_account_title), onBack = onDone) }
        item {
            FieldLabel(stringResource(R.string.field_name))
            EsforiaTextField(s.name, vm::setName, placeholder = stringResource(R.string.field_name_hint))
            Spacer(Modifier.height(18.dp))
            FieldLabel(stringResource(R.string.field_type))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AccountType.entries.forEach { t ->
                    OptionPill(stringResource(t.labelRes), selected = s.type == t, onClick = { vm.setType(t) }, icon = t.icon)
                }
            }
            Spacer(Modifier.height(18.dp))
            FieldLabel(stringResource(R.string.field_opening_balance))
            EsforiaTextField(
                s.balanceText, vm::setBalance, placeholder = "0,00", keyboardType = KeyboardType.Decimal,
                textStyle = MonoText, leading = { Text("€", style = MonoText, color = p.inkSoft) },
            )
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.opening_balance_hint), style = MaterialTheme.typography.bodySmall, color = p.inkSoft)
            Spacer(Modifier.height(24.dp))
            PrimaryButton(stringResource(R.string.save), onClick = vm::save, enabled = s.canSave)
        }
    }
}
