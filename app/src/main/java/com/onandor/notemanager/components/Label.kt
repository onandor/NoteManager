package com.onandor.notemanager.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onandor.notemanager.data.Label
import java.util.UUID
import kotlin.math.sqrt

private fun getBorderColor(color: Color, isLightColor: Boolean): Color {
    val d = if (isLightColor) -0.2f else 0.2f
    val red = if (color.component1() + d in 0f .. 1f) color.component1() + d else color.component1()
    val green = if (color.component2() + d in 0f .. 1f) color.component2() + d else color.component2()
    val blue = if (color.component3() + d in 0f .. 1f) color.component3() + d else color.component3()
    return Color(red, green, blue)
}

@Composable
fun LabelComponent(
    label: Label,
    clickable: Boolean = false,
    onClick: (Label) -> Unit = {},
    maxLength: Int = 30
) {
    val color = if (label.color.isEmpty())
        MaterialTheme.colorScheme.surfaceVariant
    else
        Color(android.graphics.Color.parseColor(label.color))

    val isLightColor = color.luminance() > sqrt(1.05 * 0.05) - 0.05
    val contentColor = if (isLightColor) Color.Black else Color.White

    var modifier = Modifier
        .border(
            width = 1.dp,
            color = getBorderColor(color, isLightColor),
            shape = RoundedCornerShape(10.dp)
        )
        .clip(RoundedCornerShape(10.dp))
    if (clickable)
        modifier = modifier.clickable { onClick(label) }

    var title = label.title.take(maxLength)
    if (title.length < label.title.length)
        title += "..."

    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(5.dp)
        ) {
            Text(title)
        }
    }
}

@Preview
@Composable
fun LabelComponentPreview() {
    val label = Label(UUID.randomUUID(), "Very long test label wow", "#005500")
    LabelComponent(
        label = label,
        clickable = false,
        onClick = { },
        maxLength = 12
    )
}