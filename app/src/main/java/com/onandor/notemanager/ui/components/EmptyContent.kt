package com.onandor.notemanager.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
private fun InnerEmptyContent(
    text: String,
    icon: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Text(
                modifier = Modifier.padding(start = 40.dp, end = 40.dp),
                text = text,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EmptyContent(
    imageVector: ImageVector,
    text: String
) {
    InnerEmptyContent(
        text = text,
        icon = {
            Icon(
                modifier = Modifier
                    .width(120.dp)
                    .height(120.dp),
                imageVector = imageVector,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    )
}

@Composable
fun EmptyContent(
    painter: Painter,
    text: String
) {
    InnerEmptyContent(
        text = text,
        icon = {
            Icon(
                modifier = Modifier
                    .width(120.dp)
                    .height(120.dp),
                painter = painter,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    )
}
