package com.personal.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.personal.app.ui.theme.LocalGlass

/** One line of a grouped list card, as in the guide's settings groups. */
data class GlassRowItem(
    val title: String,
    val icon: ImageVector? = null,
    val value: String? = null,
    val chevron: Boolean = true,
    val onClick: (() -> Unit)? = null,
)

/** A glass card holding a list of rows separated by hairlines. */
@Composable
fun GlassRowGroup(rows: List<GlassRowItem>, modifier: Modifier = Modifier) {
    GlassSurface(modifier = modifier.fillMaxWidth()) {
        Column {
            rows.forEachIndexed { index, row ->
                GlassRow(row)
                if (index < rows.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = if (row.icon != null) 46.dp else 16.dp),
                        thickness = 1.dp,
                        color = LocalGlass.current.divider,
                    )
                }
            }
        }
    }
}

@Composable
fun GlassRow(row: GlassRowItem) {
    val onBackground = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        row.icon?.let {
            Icon(it, contentDescription = null, tint = onBackground, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
        }
        Text(row.title, style = MaterialTheme.typography.titleSmall, color = onBackground, modifier = Modifier.weight(1f))
        row.value?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = muted)
            Spacer(Modifier.width(6.dp))
        }
        if (row.chevron) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = muted,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
