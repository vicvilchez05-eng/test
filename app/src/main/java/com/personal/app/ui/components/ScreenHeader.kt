package com.personal.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** An icon-button slot in the header. Phase 1: visual only, actions arrive with their phase. */
data class HeaderAction(val icon: ImageVector, val contentDescription: String, val onClick: () -> Unit = {})

/**
 * Screen header from the guide: a small tracked-caps title centred in a 48dp bar, with optional
 * icon actions on the left (back) and right (sync, add...). The header scrolls with the content.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    leading: HeaderAction? = null,
    trailing: List<HeaderAction> = emptyList(),
) {
    Box(modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Row(Modifier.align(Alignment.CenterStart)) {
            leading?.let { HeaderIcon(it) }
        }
        Row(Modifier.align(Alignment.CenterEnd), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            trailing.forEach { HeaderIcon(it) }
        }
    }
}

@Composable
private fun HeaderIcon(action: HeaderAction) {
    IconButton(onClick = action.onClick, modifier = Modifier.size(40.dp)) {
        Icon(
            imageVector = action.icon,
            contentDescription = action.contentDescription,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(22.dp),
        )
    }
}
