package com.personal.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.personal.app.R
import com.personal.app.ui.components.Chip
import com.personal.app.ui.components.Group
import com.personal.app.ui.components.GroupLabel
import com.personal.app.ui.components.GroupRow
import com.personal.app.ui.components.PageHeader
import com.personal.app.ui.components.SurfaceCard
import com.personal.app.ui.components.softShadow
import com.personal.app.ui.theme.LocalPalette

@Composable
fun ProfileScreen() {
    val p = LocalPalette.current
    val phase2 = stringResource(R.string.phase_2)
    val phase3 = stringResource(R.string.phase_3)
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
                        Text("V", style = MaterialTheme.typography.headlineMedium, color = p.onAccent)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Vic", style = MaterialTheme.typography.titleLarge, color = p.ink)
                    Spacer(Modifier.height(3.dp))
                    Text(stringResource(R.string.profile_personal_use), style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                    Spacer(Modifier.height(10.dp))
                    Chip(stringResource(R.string.placeholder_phase, phase3))
                }
            }
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.group_account))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_profile_info), Icons.Outlined.Person, phase3),
                    GroupRow(stringResource(R.string.row_linked_banks), Icons.Outlined.AccountBalance, phase2),
                    GroupRow(stringResource(R.string.row_security), Icons.Outlined.Lock, phase3),
                ),
            )
            Spacer(Modifier.height(22.dp))
        }
        item {
            GroupLabel(stringResource(R.string.group_privacy))
            Group(
                listOf(
                    GroupRow(stringResource(R.string.row_privacy), Icons.Outlined.Shield, phase3),
                    GroupRow(stringResource(R.string.row_about), Icons.Outlined.Info, phase3),
                ),
            )
        }
    }
}
