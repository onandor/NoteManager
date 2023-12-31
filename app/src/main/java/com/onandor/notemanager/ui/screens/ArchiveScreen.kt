package com.onandor.notemanager.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
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
import com.onandor.notemanager.ui.components.PinButton
import com.onandor.notemanager.ui.components.PinEntryDialog
import com.onandor.notemanager.ui.components.SwipeableSnackbarHost
import com.onandor.notemanager.viewmodels.ArchiveViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onOpenDrawer: () -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = if (uiState.selectedNotes.isEmpty())
        TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    else
        TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    Scaffold (
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
                        title = stringResource(id = R.string.drawer_archive),
                        scrollBehavior = scrollBehavior,
                        onOpenDrawer = onOpenDrawer,
                        noteListCollapsedView = uiState.noteListState.collapsed,
                        onToggleNoteListCollapsedView = viewModel::toggleNoteListCollapsedView,
                        onNoteSortingChanged = viewModel::changeSorting,
                        currentSorting = uiState.noteListState.sorting,
                        onSearchClicked = viewModel::navigateToSearch
                    )
                } else {
                    MultiSelectTopAppBar(
                        onClearSelection = viewModel::clearSelection,
                        selectedCount = uiState.selectedNotes.size,
                        scrollBehavior = scrollBehavior
                    ) {
                        PinButton(
                            pinned = !uiState.selectedNotes.any { note -> !note.pinned },
                            onChangePinned = viewModel::changeSelectedNotesPinning
                        )
                        IconButton(onClick = { viewModel.moveSelectedNotes(NoteLocation.NOTES) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_note_unarchive_filled),
                                contentDescription = stringResource(id = R.string.archive_unarchive_selected)
                            )
                        }
                        IconButton(onClick = { viewModel.moveSelectedNotes(NoteLocation.TRASH) } ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(id = R.string.archive_trash_selected)
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = {
            SwipeableSnackbarHost(hostState = snackbarHostState) {
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    ) { innerPadding ->
        AnimatedVisibility(
            visible = uiState.notes.isEmpty() && !uiState.loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyContent(
                modifier = Modifier.padding(innerPadding),
                painter = painterResource(id = R.drawable.ic_archive_outlined),
                text = stringResource(id = R.string.archive_empty),
                refreshEnabled = true,
                refreshing = uiState.synchronizing,
                onStartRefresh = viewModel::synchronize
            )
        }
        AnimatedVisibility(
            visible = uiState.notes.isNotEmpty() && !uiState.loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            NoteList(
                mainNotes = uiState.notes,
                selectedNotes = uiState.selectedNotes,
                onNoteClick = viewModel::noteClick,
                onNoteLongClick = viewModel::noteLongClick,
                modifier = Modifier.padding(innerPadding),
                collapsedView = uiState.noteListState.collapsed,
                refreshable = true,
                refreshing = uiState.synchronizing,
                onStartRefresh = viewModel::synchronize
            )
        }

        if (uiState.addEditSnackbarResource != 0) {
            val resultText = stringResource(id = uiState.addEditSnackbarResource)
            val undoText = stringResource(id = R.string.undo)
            LaunchedEffect(uiState.addEditSnackbarResource) {
                if (!uiState.showUndoableAddEditSnackbar) {
                    coroutineScope.launch { snackbarHostState.showSnackbar(resultText) }
                } else {
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = resultText,
                            actionLabel = undoText,
                            duration = SnackbarDuration.Short
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                viewModel.undoLastAction()
                            }
                            SnackbarResult.Dismissed -> {
                                viewModel.clearLastUndoableAction()
                            }
                        }
                    }
                }
                viewModel.addEditResultSnackbarShown()
            }
        }

        if (uiState.selectionSnackbarResource != 0) {
            val snackbarText = stringResource(id = uiState.selectionSnackbarResource)
            val undoText = stringResource(id = R.string.undo)
            LaunchedEffect(uiState.selectionSnackbarResource) {
                if (!uiState.showUndoableSelectionSnackbar) {
                    coroutineScope.launch { snackbarHostState.showSnackbar(snackbarText) }
                } else {
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = snackbarText,
                            actionLabel = undoText,
                            duration = SnackbarDuration.Short
                        )
                        when(result) {
                            SnackbarResult.ActionPerformed -> {
                                viewModel.undoLastAction()
                            }
                            SnackbarResult.Dismissed -> {
                                viewModel.clearLastUndoableAction()
                            }
                        }
                    }
                }
                viewModel.selectionSnackbarShown()
            }
        }

        if (uiState.syncToastResource != 0) {
            val context = LocalContext.current
            val text = stringResource(id = uiState.syncToastResource)
            LaunchedEffect(uiState.syncToastResource) {
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                viewModel.syncToastShown()
            }
        }

        LaunchedEffect(uiState.selectedNotes.size) {
            if (uiState.selectedNotes.isNotEmpty()) {
                scrollBehavior.state.heightOffset = 0f
            }
        }

        if (uiState.pinEntryDialogOpen) {
            PinEntryDialog(
                onConfirmPin = viewModel::confirmPinEntry,
                onDismissRequest = viewModel::closePinEntryDialog,
                description = stringResource(id = R.string.dialog_pin_entry_locked_note_desc)
            )
        }

        BackHandler(enabled = uiState.selectedNotes.isNotEmpty()) {
            viewModel.clearSelection()
        }
    }
}