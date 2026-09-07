package com.personal.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.personal.app.ui.theme.LocalGlass

val NavBarExpandedHeight = 72.dp
val NavBarCollapsedHeight = 54.dp
val NavBarBottomMargin = 14.dp

/**
 * Floating "bubble" bottom navigation bar.
 *
 * Two states driven by [collapsed]: expanded (icon + label, wide) and collapsed (icon only,
 * narrower, shorter). The transition is a single spring-animated progress value, so height,
 * side margins, icon size and label opacity all move together and can be interrupted mid-way.
 * A glass "bubble" indicator slides under the selected item.
 */
@Composable
fun BubbleNavBar(
    destinations: List<Destination>,
    selected: Destination,
    onSelect: (Destination) -> Unit,
    collapsed: Boolean,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = if (collapsed) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "navBarCollapse",
    )
    val height = lerp(NavBarExpandedHeight, NavBarCollapsedHeight, progress)
    val sideMargin = lerp(20.dp, 44.dp, progress)
    val iconSize = lerp(24.dp, 21.dp, progress)
    val labelHeight = lerp(16.dp, 0.dp, progress)
    val labelAlpha = (1f - progress * 1.8f).coerceIn(0f, 1f)

    GlassSurface(
        modifier = modifier
            .padding(horizontal = sideMargin)
            .fillMaxWidth()
            .height(height)
            .testTag("bubble_nav_bar"),
        shape = CircleShape,
        shadowElevation = 20.dp,
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            val itemWidth = maxWidth / destinations.size
            val selectedIndex = destinations.indexOf(selected).coerceAtLeast(0)
            val indicatorX by animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "navIndicator",
            )
            SelectionBubble(
                Modifier
                    .offset { IntOffset(indicatorX.roundToPx(), 0) }
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(horizontal = lerp(4.dp, 2.dp, progress)),
            )
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                destinations.forEach { destination ->
                    NavItem(
                        destination = destination,
                        selected = destination == selected,
                        iconSize = iconSize,
                        labelHeight = labelHeight,
                        labelAlpha = labelAlpha,
                        onClick = { onSelect(destination) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionBubble(modifier: Modifier) {
    val glass = LocalGlass.current
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(primary.copy(alpha = if (glass.isDark) 0.55f else 0.35f), primary.copy(alpha = 0.18f)),
                ),
            )
            .border(
                1.dp,
                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.05f))),
                CircleShape,
            ),
    )
}

@Composable
private fun RowScope.NavItem(
    destination: Destination,
    selected: Boolean,
    iconSize: Dp,
    labelHeight: Dp,
    labelAlpha: Float,
    onClick: () -> Unit,
) {
    val glass = LocalGlass.current
    val onBackground = MaterialTheme.colorScheme.onBackground
    val tint = if (selected) {
        if (glass.isDark) Color.White else MaterialTheme.colorScheme.primary
    } else {
        onBackground.copy(alpha = 0.62f)
    }
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
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = destination.icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(iconSize),
        )
        Box(
            Modifier
                .height(labelHeight)
                .graphicsLayer { alpha = labelAlpha },
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                softWrap = false,
            )
        }
    }
}
