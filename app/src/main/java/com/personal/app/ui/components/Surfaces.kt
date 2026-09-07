package com.personal.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.personal.app.ui.theme.LocalPalette

/*
 * Esforia's surfaces, one composable each (docs/design/identidad-esforia.md §3).
 * Cards are OPAQUE on purpose: the ambient glow lives in the margins and never tints a card.
 */

/** `Card`: surface, 1px line, radius 18, padding 16. */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .clip(shape)
            .background(p.surface)
            .border(1.dp, p.line, shape)
            .then(if (onClick != null) Modifier.pressable(onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

/** The hero: `financeGradient` at 135°, radius 22, padding 20×18, shadow `0 14 30 moss 24 %`. */
@Composable
fun HeroCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier
            .softShadow(shape, blur = 30.dp, color = p.heroShadow, offsetY = 14.dp)
            .clip(shape)
            .background(Brush.linearGradient(p.financeGradient, start = androidx.compose.ui.geometry.Offset.Zero, end = androidx.compose.ui.geometry.Offset.Infinite))
            .padding(horizontal = 18.dp, vertical = 20.dp),
        content = content,
    )
}

/** A stat pill inside the hero: white 16 % (highlighted: 30 % + border 35 %), radius 14. */
@Composable
fun RowScope.HeroStat(label: String, value: String, icon: ImageVector? = null, highlighted: Boolean = false) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier
            .weight(1f)
            .clip(shape)
            .background(Color.White.copy(alpha = if (highlighted) 0.30f else 0.16f))
            .then(if (highlighted) Modifier.border(1.dp, Color.White.copy(alpha = 0.35f), shape) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(Modifier.heightIn(min = 26.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            icon?.let { Icon(it, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(12.dp)) }
            Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f), maxLines = 2)
        }
        Text(value, style = com.personal.app.ui.theme.MonoText.copy(fontSize = 13.5.sp), color = p.onAccent, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** `SectionLabel`: Sora 19 / 500 with 22 above and 10 below, optional trailing slot. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: (@Composable () -> Unit)? = null) {
    val p = LocalPalette.current
    Row(
        modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 22.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text, style = MaterialTheme.typography.headlineSmall, color = p.ink)
        trailing?.invoke()
    }
}

/** `GroupLabel`: uppercase 11.5 inkSoft, 4 left / 8 below. */
@Composable
fun GroupLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.4.sp),
        color = LocalPalette.current.inkSoft,
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

/** One line of a `Group`. */
data class GroupRow(
    val label: String,
    val icon: ImageVector? = null,
    val value: String? = null,
    val chevron: Boolean = true,
    val danger: Boolean = false,
    val trailing: (@Composable () -> Unit)? = null,
    val onClick: (() -> Unit)? = null,
    val testTag: String? = null,
)

/** `Group` + `Row` + `Divider`: surface, line, radius 16; rows ≥ 50 tall, icon tile 28 / radius 9 / mossSoft. */
@Composable
fun Group(rows: List<GroupRow>, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(p.surface)
            .border(1.dp, p.line, shape),
    ) {
        rows.forEachIndexed { i, row ->
            GroupRowItem(row)
            if (i < rows.lastIndex) {
                Box(Modifier.fillMaxWidth().padding(start = if (row.icon != null) 54.dp else 14.dp).height(1.dp).background(p.line))
            }
        }
    }
}

@Composable
private fun GroupRowItem(row: GroupRow) {
    val p = LocalPalette.current
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (row.testTag != null) Modifier.testTag(row.testTag) else Modifier)
            .then(if (row.onClick != null) Modifier.pressable(row.onClick) else Modifier)
            .heightIn(min = 50.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        row.icon?.let {
            Box(
                Modifier.size(28.dp).clip(RoundedCornerShape(9.dp))
                    .background(if (row.danger) p.danger.copy(alpha = 0.10f) else p.mossSoft),
                contentAlignment = Alignment.Center,
            ) {
                Icon(it, contentDescription = null, tint = if (row.danger) p.danger else p.mossText, modifier = Modifier.size(15.dp))
            }
        }
        Text(row.label, style = MaterialTheme.typography.bodyLarge, color = if (row.danger) p.danger else p.ink, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        row.value?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = p.inkSoft, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        row.trailing?.invoke()
        if (row.chevron && row.trailing == null) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = p.mutedLight, modifier = Modifier.size(18.dp))
        }
    }
}

/** Chip: mossSoft / mossText, 11sp, radius 20, padding 3×9. */
@Composable
fun Chip(text: String, modifier: Modifier = Modifier, background: Color = LocalPalette.current.mossSoft, color: Color = LocalPalette.current.mossText) {
    Box(modifier.clip(CircleShape).background(background).padding(horizontal = 9.dp, vertical = 3.dp)) {
        Text(text, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = color)
    }
}

/** Outline button: radius 11, border moss 33 %, mossText 13sp, 12 vertical padding, full width. */
@Composable
fun OutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, trailingIcon: ImageVector? = Icons.AutoMirrored.Outlined.KeyboardArrowRight) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(11.dp)
    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, p.moss.copy(alpha = 0.33f), shape)
            .pressable(onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = p.mossText)
        trailingIcon?.let { Spacer(Modifier.width(6.dp)); Icon(it, contentDescription = null, tint = p.mossText, modifier = Modifier.size(14.dp)) }
    }
}

/** Circular icon button, 34dp, surface + line (the floating profile/settings buttons). */
@Composable
fun CircleIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 34.dp) {
    val p = LocalPalette.current
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(p.surface)
            .border(1.dp, p.line, CircleShape)
            .pressable(onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = p.inkSoft, modifier = Modifier.size(16.dp))
    }
}

/** `.tap`: no rectangular ripple; the press feedback is the element itself (see PressScale). */
@Composable
fun Modifier.pressable(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    return this.clickable(interactionSource = interaction, indication = null, onClick = onClick)
}

private val Int.sp get() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)
private val Double.sp get() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)

/** Esforia's `Switch`: 48×28, gradient + moss shadow when on, track + line when off, white knob. */
@Composable
fun EsforiaSwitch(checked: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val w = 48.dp; val h = 28.dp; val pad = 3.dp; val knob = h - pad * 2
    val knobX by androidx.compose.animation.core.animateDpAsState(if (checked) w - knob - pad * 2 - 2.dp else 0.dp, label = "switchKnob")
    Box(
        modifier
            .size(w, h)
            .then(if (checked) Modifier.softShadow(CircleShape, 16.dp, p.moss.copy(alpha = 0.28f), offsetY = 6.dp) else Modifier)
            .clip(CircleShape)
            .then(
                if (checked) Modifier.background(Brush.linearGradient(p.financeGradient, start = androidx.compose.ui.geometry.Offset.Zero, end = androidx.compose.ui.geometry.Offset.Infinite))
                else Modifier.background(p.track).border(1.dp, p.line, CircleShape),
            )
            .pressable { onChange(!checked) },
    ) {
        Box(
            Modifier
                .padding(start = pad - 1.dp, top = pad - 1.dp)
                .offset { androidx.compose.ui.unit.IntOffset(knobX.roundToPx(), 0) }
                .size(knob)
                .softShadow(CircleShape, 6.dp, Color.Black.copy(alpha = 0.22f), offsetY = 2.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}
