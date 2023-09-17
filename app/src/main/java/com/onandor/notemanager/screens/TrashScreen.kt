package com.onandor.notemanager.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onandor.notemanager.R

@Composable
fun TrashScreen(openDrawer: () -> Unit) {
    Scaffold(
        topBar = { TrashTopBar(openDrawer = openDrawer) }
    ) { innerPadding ->
        Text(text = "Trash", modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun TrashTopBar(openDrawer: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().height(65.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { openDrawer() }) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.topbar_drawer)
                    )
                }
                Text(stringResource(R.string.trash), fontSize = 20.sp)
            }
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.trash_empty_trash))
            }
        }
    }
}