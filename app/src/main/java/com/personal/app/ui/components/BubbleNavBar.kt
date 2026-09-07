package com.personal.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.personal.app.ui.navigation.Destination
import com.personal.app.ui.theme.LocalPalette

val NavBarExpandedHeight = 62.dp
val NavBarCollapsedHeight = 48.dp
val NavBarBottomMargin = 12.dp

/**
 * Esforia's tab bar (surface, 1px line, 19dp icons, 9.5sp labels, moss active / muted inactive,
 * thicker stroke on the active icon) — kept floating and scroll-collapsing as the brief asks.
 * A `mossSoft` pill slides under the active icon in place of Esforia's stroke-width change.
 */
@Composable
fun BubbleNavBar(
    destinations: List<Destination>,
    selected: Destination,
    onSelect: (Destination) -> Unit,
    collapsed: Boolean,
    modifier: Modifier = Modifier,
) {
    val p = LocalPalette.current
    val progress by animateFloatAsState(
        targetValue = if (collapsed) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "navBarCollapse",
    )
    val height = lerp(NavBarExpandedHeight, NavBarCollapsedHeight, progress)
    val sideMargin = lerp(18.dp, 52.dp, progress)
    val iconSize = lerp(19.dp, 18.dp, progress)
    val pillSize = lerp(34.dp, 32.dp, progress)
    val labelHeight = lerp(13.dp, 0.dp, progress)
    val labelAlpha = (1f - progress * 1.8f).coerceIn(0f, 1f)

    GlassSurface(
        modifier = modifier
            .padding(horizontal = sideMargin)
            .fillMaxWidth()
            .height(height)
            .testTag("bubble_nav_bar"),
        shape = CircleShape,
        fill = if (p.isDark) p.surface.copy(alpha = 0.94f) else p.surface.copy(alpha = 0.94f),
        border = p.line,
        shadow = p.glassShadow,
        shadowElevation = 22.dp,
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 6.dp)) {
            val itemWidth = maxWidth / destinations.size
            val selectedIndex = destinations.indexOf(selected).coerceAtLeast(0)
            val pillX by animateDpAsState(
                targetValue = itemWidth * selectedIndex + (itemWidth - pillSize) / 2,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "navPill",
            )
            Box(
                Modifier
                    .offset { IntOffset(pillX.roundToPx(), 0) }
                    .size(pillSize)
                    .clip(CircleShape)
                    .background(p.mossSoft),
            )
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) {
                destinations.forEach { destination ->
                    NavItem(destination, destination == selected, iconSize, pillSize, labelHeight, labelAlpha) { onSelect(destination) }
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(
    destination: Destination,
    selected: Boolean,
    iconSize: Dp,
    pillSize: Dp,
    labelHeight: Dp,
    labelAlpha: Float,
    onClick: () -> Unit,
) {
    val p = LocalPalette.current
    val tint = if (selected) p.moss else p.muted
    val interaction = remember { MutableInteractionSource() }
    val label = stringResource(destination.labelRes)

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null, role = Role.Tab, onClick = onClick)
            .testTag("nav_${destination.route}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(Modifier.size(pillSize), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (selected) destination.activeIcon else destination.icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
        }
        Box(Modifier.height(labelHeight).graphicsLayer { alpha = labelAlpha }, contentAlignment = Alignment.TopCenter) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = tint, maxLines = 1, overflow = TextOverflow.Clip, softWrap = false)
        }
    }
}
