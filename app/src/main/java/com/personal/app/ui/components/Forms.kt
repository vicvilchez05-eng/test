package com.personal.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.personal.app.ui.theme.LocalPalette
import com.personal.app.ui.theme.MonoText

/*
 * Esforia's form vocabulary: inputs are `surface` boxes with a `line` border and radius 12;
 * the big amount field is Plex Mono; option pickers are pills (mossSoft when selected);
 * the primary action is the `financeGradient` button with a moss shadow.
 */

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.4.sp),
        color = LocalPalette.current.inkSoft,
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

@Composable
fun EsforiaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    leading: (@Composable () -> Unit)? = null,
) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(p.surface)
            .border(1.dp, p.line, shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.let { it(); Spacer(Modifier.width(10.dp)) }
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text(placeholder, style = textStyle, color = p.mutedLight)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = textStyle.copy(color = p.ink),
                cursorBrush = SolidColor(p.moss),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** The hero of the manual form: a very large mono amount with the currency symbol beside it. */
@Composable
fun AmountField(value: String, onValueChange: (String) -> Unit, currencySymbol: String, modifier: Modifier = Modifier, tint: Color = LocalPalette.current.ink) {
    val p = LocalPalette.current
    val style = MonoText.copy(fontSize = MaterialTheme.typography.displaySmall.fontSize, lineHeight = MaterialTheme.typography.displaySmall.lineHeight, color = tint)
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(220.dp), contentAlignment = Alignment.Center) {
            if (value.isEmpty()) Text("0", style = style.copy(textAlign = TextAlign.Center), color = p.mutedLight, modifier = Modifier.fillMaxWidth())
            BasicTextField(
                value = value,
                onValueChange = { if (it.length <= 12) onValueChange(it) },
                singleLine = true,
                textStyle = style.copy(textAlign = TextAlign.Center),
                cursorBrush = SolidColor(p.moss),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.width(220.dp),
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(currencySymbol, style = style, color = p.inkSoft)
    }
}

/** A selectable pill with optional icon. */
@Composable
fun OptionPill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier
            .clip(shape)
            .background(if (selected) p.mossSoft else p.surface)
            .border(1.dp, if (selected) p.moss.copy(alpha = 0.45f) else p.line, shape)
            .pressable(onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon?.let { Icon(it, contentDescription = null, tint = if (selected) p.mossText else p.inkSoft, modifier = Modifier.size(15.dp)) }
        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (selected) p.mossText else p.ink, maxLines = 1)
    }
}

/** Two-way switch (Expense | Income): a track with a sliding surface pill. */
@Composable
fun SegmentedToggle(left: String, right: String, leftSelected: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Row(
        modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(p.track)
            .padding(3.dp),
    ) {
        listOf(left to true, right to false).forEach { (label, isLeft) ->
            val selected = leftSelected == isLeft
            Box(
                Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (selected) p.surface else Color.Transparent)
                    .then(if (selected) Modifier.border(1.dp, p.line, CircleShape) else Modifier)
                    .pressable { onChange(isLeft) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = if (selected) p.ink else p.inkSoft)
            }
        }
    }
}

/** Primary action: financeGradient, radius 12, white 15sp medium, moss shadow. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(if (enabled) Modifier.softShadow(shape, 16.dp, p.moss.copy(alpha = 0.28f), offsetY = 6.dp) else Modifier)
            .clip(shape)
            .background(Brush.linearGradient(p.financeGradient, start = Offset.Zero, end = Offset.Infinite))
            .then(if (enabled) Modifier.pressable(onClick) else Modifier.background(p.surface.copy(alpha = 0.55f))),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = if (enabled) p.onAccent else p.inkSoft)
    }
}

/** Header row for a form screen: back chevron + title. */
@Composable
fun FormHeader(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CircleIconButton(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", onClick = onBack)
        Text(title, style = MaterialTheme.typography.headlineSmall, color = p.ink)
    }
    Spacer(Modifier.height(20.dp))
}

private val Double.sp get() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)
