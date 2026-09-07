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
import com.personal.app.ui.theme.LocalGlass

val NavBarExpandedHeight = 64.dp
val NavBarCollapsedHeight = 50.dp
val NavBarBottomMargin = 12.dp

/**
 * Floating bottom bar from the guide: a compact white frosted pill, thin outline icons with tiny
 * labels, and a raised white circle that slides under the selected icon.
 *
 * [collapsed] drives a single spring-animated progress: height (64→50dp), side margins, icon
 * size, circle size and label opacity all move together and can be interrupted mid-way.
 */
@Composable
fun BubbleNavBar(
    destinations: List<Destination>,
    selected: Destination,
    onSelect: (Destination) -> Unit,
    collapsed: Boolean,
    modifier: Modifier = Modifier,
) {
    val glass = LocalGlass.current
    val progress by animateFloatAsState(
        targetValue = if (collapsed) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "navBarCollapse",
    )
    val height = lerp(NavBarExpandedHeight, NavBarCollapsedHeight, progress)
    val sideMargin = lerp(24.dp, 56.dp, progress)
    val iconSize = lerp(22.dp, 20.dp, progress)
    val circleSize = lerp(40.dp, 36.dp, progress)
    val labelHeight = lerp(14.dp, 0.dp, progress)
    val labelAlpha = (1f - progress * 1.8f).coerceIn(0f, 1f)
    val verticalPadding = lerp(6.dp, 7.dp, progress)

    GlassSurface(
        modifier = modifier
            .padding(horizontal = sideMargin)
            .fillMaxWidth()
            .height(height)
            .testTag("bubble_nav_bar"),
        shape = CircleShape,
        shadowElevation = 16.dp,
    ) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = verticalPadding),
        ) {
            val itemWidth = maxWidth / destinations.size
            val selectedIndex = destinations.indexOf(selected).coerceAtLeast(0)
            val circleX by animateDpAsState(
                targetValue = itemWidth * selectedIndex + (itemWidth - circleSize) / 2,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "navCircle",
            )
            // Raised white circle under the selected icon.
            Box(
                Modifier
                    .offset { IntOffset(circleX.roundToPx(), 0) }
                    .size(circleSize)
                    .glassShadow(CircleShape, 8.dp, glass.shadow)
                    .clip(CircleShape)
                    .background(glass.navSelectedBackground),
            )
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.Top) {
                destinations.forEach { destination ->
                    NavItem(
                        destination = destination,
                        selected = destination == selected,
                        iconSize = iconSize,
                        circleSize = circleSize,
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
private fun RowScope.NavItem(
    destination: Destination,
    selected: Boolean,
    iconSize: Dp,
    circleSize: Dp,
    labelHeight: Dp,
    labelAlpha: Float,
    onClick: () -> Unit,
) {
    val glass = LocalGlass.current
    val tint = if (selected) glass.navSelectedForeground else glass.navUnselected
    val labelColor = if (selected) MaterialTheme.colorScheme.onBackground else glass.navUnselected
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
        Box(Modifier.size(circleSize), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = destination.icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
        }
        Box(
            Modifier
                .height(labelHeight)
                .graphicsLayer { alpha = labelAlpha },
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                softWrap = false,
            )
        }
    }
}
