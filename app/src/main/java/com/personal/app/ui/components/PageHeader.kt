package com.personal.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.personal.app.ui.theme.LocalPalette

/** An icon-button slot at the top right. Phase 1: visual only. */
data class HeaderAction(val icon: ImageVector, val contentDescription: String, val onClick: () -> Unit = {})

/**
 * Esforia's page top: a 13sp inkSoft line (greeting or date), then the Sora title, left-aligned,
 * with the floating circular buttons (34dp, surface + line) on the right.
 */
@Composable
fun PageHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    actions: List<HeaderAction> = emptyList(),
) {
    val p = LocalPalette.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            eyebrow?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = p.inkSoft)
                Spacer(Modifier.height(4.dp))
            }
            Text(title, style = MaterialTheme.typography.headlineMedium, color = p.ink)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = if (eyebrow != null) 6.dp else 2.dp)) {
            actions.forEach { CircleIconButton(it.icon, it.contentDescription, it.onClick) }
        }
    }
}
