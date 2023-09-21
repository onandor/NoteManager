package com.onandor.notemanager.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.components.NoteList
import com.onandor.notemanager.components.TopBar
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.viewmodels.NotesViewModel

@Composable
fun NotesScreen(
    onAddTask: () -> Unit,
    openDrawer: () -> Unit,
    onNoteClick: (Note) -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    Scaffold (
        topBar = { TopBar(openDrawer) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddTask() }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.notes_new_note))
            }
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        NoteList(
            notes = uiState.notes,
            onNoteClick = onNoteClick,
            modifier = Modifier.padding(innerPadding),
            emptyContent = { NotesEmptyContent() }
        )
    }
}

@Composable
fun NotesEmptyContent() {
    // TODO
    Text("You don't have any notes")
}