package com.onandor.notemanager.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.ui.components.EmptyContent
import com.onandor.notemanager.ui.components.MainTopAppBar
import com.onandor.notemanager.ui.components.MultiSelectTopAppBar
import com.onandor.notemanager.ui.components.NoteList
import com.onandor.notemanager.ui.components.SwipeableSnackbarHost
import com.onandor.notemanager.utils.AddEditResults
import com.onandor.notemanager.viewmodels.NotesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    onOpenDrawer: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
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
            AnimatedContent(
                targetState = uiState.selectedNotes.isEmpty(),
                label = "",
                transitionSpec = {
                    if (targetState) {
                        slideInVertically { fullHeight -> -fullHeight } + fadeIn() togetherWith
                                slideOutVertically { fullHeight -> fullHeight } + fadeOut()
                    } else {
                        slideInVertically { fullHeight -> fullHeight } + fadeIn() togetherWith
                                slideOutVertically { fullHeight -> -fullHeight } + fadeOut()
                    }
                }
            ) { noneSelected ->
                if (noneSelected) {
                    MainTopAppBar(
                        title = stringResource(id = R.string.drawer_notes),
                        scrollBehavior = scrollBehavior,
                        onOpenDrawer = onOpenDrawer,
                        noteListCollapsedView = uiState.noteListState.collapsed,
                        onToggleNoteListCollapsedView = viewModel::toggleNoteListCollapsedView,
                        onNoteSortingChanged = viewModel::changeSorting,
                        currentSorting = uiState.noteListState.sorting,
                        onSearchClicked = viewModel::showSearch
                    )
                } else {
                    MultiSelectTopAppBar(
                        onClearSelection = viewModel::clearSelection,
                        selectedCount = uiState.selectedNotes.size
                    ) {
                        IconButton(onClick = { viewModel.moveSelectedNotes(NoteLocation.ARCHIVE) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_note_archive_filled),
                                contentDescription = stringResource(id = R.string.notes_archive_selected)
                            )
                        }
                        IconButton(onClick = { viewModel.moveSelectedNotes(NoteLocation.TRASH) }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(id = R.string.notes_trash_selected)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = scrollBehavior.state.collapsedFraction == 0.0f,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(onClick = viewModel::addNote) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.notes_new_note))
                }
            }
        },
        snackbarHost = {
            SwipeableSnackbarHost(hostState = snackbarHostState) {
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    ) { innerPadding ->
        if (uiState.notes.isEmpty()) {
            EmptyContent(
                painter = painterResource(id = R.drawable.ic_drawer_notes_filled),
                text = stringResource(id = R.string.notes_empty)
            )
        }
        else {
            NoteList(
                notes = uiState.notes,
                selectedNotes = uiState.selectedNotes,
                onNoteClick = viewModel::noteClick,
                onNoteLongClick = viewModel::noteLongClick,
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