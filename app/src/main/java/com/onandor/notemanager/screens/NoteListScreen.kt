package com.onandor.notemanager.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.onandor.notemanager.NMNavigationActions
import com.onandor.notemanager.R
import com.onandor.notemanager.components.TopBar

@Composable
fun NoteListScreen(
    onAddTask: () -> Unit,
    openDrawer: () -> Unit
) {
    Scaffold (
        topBar = { TopBar(openDrawer) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddTask() }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.note_list_new_note))
            }
        }
    ) { innerPadding ->
        Text(text = "Note list", modifier = Modifier.padding(innerPadding))
    }
}