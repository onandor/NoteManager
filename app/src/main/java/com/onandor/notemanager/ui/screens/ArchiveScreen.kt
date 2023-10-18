package com.onandor.notemanager.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.onandor.notemanager.ui.components.NoteList
import com.onandor.notemanager.ui.components.TopBar
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.utils.AddEditResults
import com.onandor.notemanager.viewmodels.ArchiveViewModel
import kotlinx.coroutines.launch

@Composable
fun ArchiveScreen(
    onOpenDrawer: () -> Unit,
    onToggleCollapsedView: () -> Unit,
    collapsedView: Boolean,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    Scaffold (
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopBar(
                onOpenDrawer = onOpenDrawer,
                noteListCollapsedView = collapsedView,
                onToggleNoteListCollapsedView = onToggleCollapsedView
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        NoteList(
            notes = uiState.notes,
            onNoteClick = viewModel::noteClick,
            modifier = Modifier.padding(innerPadding),
            collapsedView = collapsedView,
            emptyContent = { ArchiveEmptyContent() }
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
private fun ArchiveEmptyContent() {
    Text("Your archive is empty")
}