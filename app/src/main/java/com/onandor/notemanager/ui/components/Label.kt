package com.onandor.notemanager.ui.components

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.ui.theme.LocalTheme
import com.onandor.notemanager.utils.LabelColorType
import com.onandor.notemanager.utils.LabelColors
import com.onandor.notemanager.utils.getAccentColor
import com.onandor.notemanager.utils.getColor
import java.util.UUID

@Composable
fun LabelComponent(
    modifier: Modifier = Modifier,
    label: Label,
    clickable: Boolean = false,
    onClick: (Label) -> Unit = {},
    maxLength: Int = 30,
    padding: Dp = 5.dp,
    fontSize: TextUnit = TextUnit.Unspecified,
    borderWidth: Dp = 1.dp,
    roundedCornerSize: Dp = 5.dp
) {
    val color: Color
    val accentColor: Color
    if (label.color.type == LabelColorType.None) {
        color = MaterialTheme.colorScheme.surface
        accentColor = MaterialTheme.colorScheme.onSurface
    }
    else {
        color = label.color.getColor(LocalTheme.current.isDark)
        accentColor = label.color.getAccentColor(LocalTheme.current.isDark)
    }

    var _modifier = modifier
        .border(
            width = borderWidth,
            color = accentColor,
            shape = RoundedCornerShape(roundedCornerSize)
        )
        .clip(RoundedCornerShape(roundedCornerSize))
    if (clickable)
        _modifier = _modifier.clickable { onClick(label) }

    var title = label.title.take(maxLength)
    if (title.length < label.title.length)
        title += "..."

    Surface(
        modifier = _modifier,
        color = color,
        contentColor = accentColor
    ) {
        Row(
            modifier = Modifier.padding(padding)
        ) {
            Text(
                text = title,
                fontSize = fontSize,
                lineHeight = fontSize
            )
        }
    }
}

@Preview
@Composable
fun LabelComponentPreview() {
    val label = Label(UUID.randomUUID(), "Very long test label wow", LabelColors.green)
    LabelComponent(
        label = label,
        clickable = false,
        onClick = { },
        maxLength = 12
    )
}