package com.personal.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.personal.app.R
import com.personal.app.ui.components.Chip
import com.personal.app.ui.components.FormHeader
import com.personal.app.ui.components.Group
import com.personal.app.ui.components.GroupLabel
import com.personal.app.ui.components.GroupRow
import com.personal.app.ui.components.OutlineButton
import com.personal.app.ui.components.PrimaryButton
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.viewmodel.LinkBankViewModel
import com.personal.app.ui.viewmodel.LinkStep
import com.personal.app.ui.viewmodel.appViewModel

/**
 * The "primary sync" entry point: pick an institution from the provider's catalogue, run the
 * consent flow, import accounts and history. With the sandbox provider this is all local.
 */
@Composable
fun LinkBankScreen(onDone: () -> Unit) {
    val vm = appViewModel { LinkBankViewModel(it.repository) }
    val s by vm.state.collectAsStateWithLifecycle()
    val p = LocalPalette.current

    ScreenScaffold {
        item { FormHeader(stringResource(R.string.link_bank_title), onBack = onDone) }
        when (val step = s.step) {
            LinkStep.Choose -> {
                item {
                    SurfaceCard(Modifier.fillMaxWidth()) {
                        Chip(s.providerName)
                        Spacer(Modifier.height(10.dp))
                        Text(stringResource(R.string.link_bank_sandbox_note), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                    }
                    Spacer(Modifier.height(22.dp))
                    GroupLabel(stringResource(R.string.link_bank_choose))
                    if (s.loading) {
                        SurfaceCard(Modifier.fillMaxWidth()) { CircularProgressIndicator(color = p.moss, modifier = Modifier.size(22.dp)) }
                    } else {
                        Group(s.institutions.map { inst ->
                            GroupRow(inst.name, icon = Icons.Outlined.AccountBalance, value = inst.country, onClick = { vm.link(inst) })
                        })
                    }
                }
            }
            is LinkStep.Working -> item {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = p.moss, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(14.dp))
                        Text(step.institution.name, style = MaterialTheme.typography.titleMedium, color = p.ink)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(step.message), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                    }
                }
            }
            is LinkStep.Done -> item {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = p.ember, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.link_bank_done_title, step.institution.name), style = MaterialTheme.typography.titleMedium, color = p.ink)
                        Spacer(Modifier.height(4.dp))
                        Text(pluralStringResource(R.plurals.accounts_imported, step.accounts, step.accounts), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                        Spacer(Modifier.height(18.dp))
                        PrimaryButton(stringResource(R.string.done), onClick = onDone)
                    }
                }
            }
            is LinkStep.Failed -> item {
                SurfaceCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = p.danger, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(step.message, style = MaterialTheme.typography.bodyMedium, color = p.ink)
                        Spacer(Modifier.height(18.dp))
                        OutlineButton(stringResource(R.string.retry), onClick = vm::reset, trailingIcon = null)
                    }
                }
            }
        }
    }
}
