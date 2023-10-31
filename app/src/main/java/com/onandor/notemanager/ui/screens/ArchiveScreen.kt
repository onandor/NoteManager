package com.onandor.notemanager.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.ui.components.EmptyContent
import com.onandor.notemanager.ui.components.MainTopAppBar
import com.onandor.notemanager.ui.components.NoteList
import com.onandor.notemanager.utils.AddEditResults
import com.onandor.notemanager.viewmodels.ArchiveViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onOpenDrawer: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold (
        modifier = Modifier
            .statusBarsPadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MainTopAppBar(
                title = stringResource(id = R.string.drawer_archive),
                scrollBehavior = scrollBehavior,
                onOpenDrawer = onOpenDrawer,
                noteListCollapsedView = uiState.noteListState.collapsed,
                onToggleNoteListCollapsedView = viewModel::toggleNoteListCollapsedView,
                onNoteSortingChanged = viewModel::changeSorting,
                currentSorting = uiState.noteListState.sorting,
                onSearchClicked = viewModel::showSearch
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (uiState.notes.isEmpty()) {
            EmptyContent(
                painter = painterResource(id = R.drawable.ic_archive_outlined),
                text = stringResource(id = R.string.archive_empty)
            )
        }
        else {
            NoteList(
                notes = uiState.notes,
                onNoteClick = viewModel::noteClick,
                modifier = Modifier.padding(innerPadding),
                collapsedView = uiState.noteListState.collapsed
            )
        }

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