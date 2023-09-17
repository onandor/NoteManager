package com.onandor.notemanager.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onandor.notemanager.R

@Composable
fun TopBar(openDrawer: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { openDrawer() }) {
                Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.topbar_drawer))
            }
            Text(stringResource(R.string.topbar_search_notes))
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.topbar_change_view))
            }
        }
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TopBarPreview() {
    TopBar(openDrawer = { })
}