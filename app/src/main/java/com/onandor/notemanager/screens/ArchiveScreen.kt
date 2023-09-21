package com.onandor.notemanager.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.components.NoteList
import com.onandor.notemanager.components.TopBar
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.viewmodels.ArchiveViewModel

@Composable
fun ArchiveScreen(
    openDrawer: () -> Unit,
    onNoteClick: (Note) -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    Scaffold (
        topBar = { TopBar(openDrawer) },
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        NoteList(
            notes = uiState.notes,
            onNoteClick = onNoteClick,
            modifier = Modifier.padding(innerPadding),
            emptyContent = { ArchiveEmptyContent() }
        )
    }
}

@Composable
fun ArchiveEmptyContent() {
    Text("You don't have any notes")
}