package com.personal.app.ui.components.navygold

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.personal.app.ui.navigation.Destination
import com.personal.app.ui.theme.LocalPalette

val DockedNavExpandedHeight = 60.dp
val DockedNavCollapsedHeight = 46.dp

/**
 * The NavyGold tab bar: docked, full width, hairline on top, a short gold underline above the
 * active icon (light: navy icon; dark: gold icon). Still collapses on scroll: labels fade and the
 * bar shrinks, so the brief's scroll-responsive behaviour survives the docked layout.
 */
@Composable
fun DockedNavBar(
    destinations: List<Destination>,
    selected: Destination,
    onSelect: (Destination) -> Unit,
    collapsed: Boolean,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    val progress by animateFloatAsState(
        targetValue = if (collapsed) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "dockedNavCollapse",
    )
    val height = lerp(DockedNavExpandedHeight, DockedNavCollapsedHeight, progress)
    val labelHeight = lerp(14.dp, 0.dp, progress)
    val labelAlpha = (1f - progress * 1.8f).coerceIn(0f, 1f)

    Column(
        modifier
            .fillMaxWidth()
            .background(p.navBackground)
            .testTag("bubble_nav_bar"),
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(p.line))
        Row(Modifier.fillMaxWidth().height(height).navigationBarsPadding(), verticalAlignment = Alignment.Top) {
            destinations.forEach { destination ->
                NavItem(destination, destination == selected, labelHeight, labelAlpha) { onSelect(destination) }
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(destination: Destination, selected: Boolean, labelHeight: Dp, labelAlpha: Float, onClick: () -> Unit) {
    val p = LocalPalette.current
    val active = if (p.isDark) p.gold else p.ink
    val tint = if (selected) active else p.muted
    val interaction = remember { MutableInteractionSource() }
    val label = stringResource(destination.labelRes)
    Column(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(interactionSource = interaction, indication = null, role = Role.Tab, onClick = onClick)
            .testTag("nav_${destination.route}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(Modifier.width(30.dp).height(2.dp).clip(RoundedCornerShape(1.dp)).background(if (selected) p.gold else androidx.compose.ui.graphics.Color.Transparent))
        Box(Modifier.height(lerp(38.dp, 40.dp, (1f - labelAlpha))), contentAlignment = Alignment.Center) {
            Icon(if (selected) destination.activeIcon else destination.icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        }
        Box(Modifier.height(labelHeight).graphicsLayer { alpha = labelAlpha }, contentAlignment = Alignment.TopCenter) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = tint, maxLines = 1, overflow = TextOverflow.Clip, softWrap = false)
        }
    }
}
