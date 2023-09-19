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
import com.onandor.notemanager.NMNavigationActions
import com.onandor.notemanager.R
import com.onandor.notemanager.components.TopBar
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.viewmodels.NoteListViewModel

@Composable
fun NoteListScreen(
    onAddTask: () -> Unit,
    openDrawer: () -> Unit,
    onNoteClick: (Note) -> Unit,
    viewModel: NoteListViewModel = hiltViewModel()
) {
    Scaffold (
        topBar = { TopBar(openDrawer) },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddTask() }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.note_list_new_note))
            }
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        NoteListContent(
            notes = uiState.notes,
            onNoteClick = onNoteClick,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun NoteListContent(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (notes.isEmpty()) {
            NoteListEmptyContent(modifier)
        }
        else {
            LazyColumn {
                items(notes) { note ->
                    NoteItem(note, onNoteClick)
                }
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onNoteClick: (Note) -> Unit
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNoteClick(note) }
    ) {
        Text(note.title)
        Text(note.content)
    }
}

@Composable
fun NoteListEmptyContent(modifier: Modifier) {
    Text("You don't have any notes")
}