package com.onandor.notemanager.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.ui.components.NoteList
import com.onandor.notemanager.ui.components.TopBar
import com.onandor.notemanager.utils.AddEditResults
import com.onandor.notemanager.viewmodels.NotesViewModel
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(
    onOpenDrawer: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold (
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopBar(
                onOpenDrawer = onOpenDrawer,
                noteListCollapsedView = uiState.noteListState.collapsed,
                onToggleNoteListCollapsedView = viewModel::toggleNoteListCollapsedView,
                onNoteSortingChanged = viewModel::changeSorting,
                currentSorting = uiState.noteListState.sorting
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::addNote) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.notes_new_note))
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        NoteList(
            notes = uiState.notes,
            onNoteClick = viewModel::noteClick,
            modifier = Modifier.padding(innerPadding),
            collapsedView = uiState.noteListState.collapsed,
            emptyContent = { NotesEmptyContent() }
        )

        if (uiState.addEditResult != AddEditResults.NONE) {
            val resultText = stringResource(id = uiState.addEditResult.resource)
            LaunchedEffect(uiState.addEditResult) {
                scope.launch {
                    snackbarHostState.showSnackbar(resultText)
                }
                viewModel.addEditResultSnackbarShown()
            }
        }
    }
}

@Composable
private fun NotesEmptyContent() {
    // TODO
    Text("You don't have any notes")
}