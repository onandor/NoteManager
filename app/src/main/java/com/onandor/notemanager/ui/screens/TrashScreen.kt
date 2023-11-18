package com.onandor.notemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.ui.components.ColoredStatusBarTopAppBar
import com.onandor.notemanager.ui.components.EmptyContent
import com.onandor.notemanager.ui.components.MultiSelectTopAppBar
import com.onandor.notemanager.ui.components.NoteList
import com.onandor.notemanager.ui.components.SwipeableSnackbarHost
import com.onandor.notemanager.viewmodels.TrashViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: TrashViewModel = hiltViewModel()
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
                    TrashTopAppBar(
                        onNavigateBack = viewModel::navigateBack,
                        onEmptyTrash = viewModel::openConfirmationDialog,
                        emptyTrashEnabled = uiState.notes.isNotEmpty(),
                        scrollBehavior = scrollBehavior
                    )
                } else {
                    MultiSelectTopAppBar(
                        onClearSelection = viewModel::clearSelection,
                        selectedCount = uiState.selectedNotes.size,
                        scrollBehavior = scrollBehavior
                    ) {
                        IconButton(onClick = viewModel::restoreSelectedNotes) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_restore_from_trash_filled),
                                contentDescription = stringResource(id = R.string.trash_restore_selected)
                            )
                        }
                        IconButton(onClick = viewModel::openConfirmationDialog) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete_forever_filled),
                                contentDescription = stringResource(id = R.string.trash_delete_selected)
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
                painter = painterResource(id = R.drawable.ic_trash_empty),
                text = stringResource(id = R.string.trash_empty)
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
                collapsedView = false
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

        if (uiState.snackbarResource != 0) {
            val snackbarText = stringResource(id = uiState.snackbarResource)
            val undoText = stringResource(id = R.string.undo)
            LaunchedEffect(uiState.addEditSnackbarResource) {
                if (!uiState.showUndoableSnackbar) {
                    coroutineScope.launch { snackbarHostState.showSnackbar(snackbarText) }
                } else {
                    coroutineScope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = snackbarText,
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
                viewModel.snackbarShown()
            }
        }

        if (uiState.confirmationDialogOpen) {
            ConfirmationDialog(
                onConfirmation = viewModel::dialogConfirmed,
                onDismissRequest = viewModel::closeConfirmationDialog,
                deleteSelection = uiState.selectedNotes.isNotEmpty()
            )
        }

        BackHandler {
            viewModel.navigateBack()
        }

        LaunchedEffect(uiState.selectedNotes.size) {
            if (uiState.selectedNotes.isNotEmpty()) {
                scrollBehavior.state.heightOffset = 0f
            }
        }
    }
}

@Composable
private fun ConfirmationDialog(
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
    deleteSelection: Boolean
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Icon(
                    modifier = Modifier.size(35.dp),
                    imageVector = Icons.Filled.Warning,
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (deleteSelection)
                    Text(stringResource(id = R.string.dialog_trash_confirmation_delete_selected))
                else
                    Text(stringResource(id = R.string.dialog_trash_confirmation_empty_trash))
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(id = R.string.dialog_empty_trash_button_cancel))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onConfirmation,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(id = R.string.dialog_empty_trash_button_confirm))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrashTopAppBar(
    onNavigateBack: () -> Unit,
    onEmptyTrash: () -> Unit,
    emptyTrashEnabled: Boolean,
    scrollBehavior: TopAppBarScrollBehavior
) {
    ColoredStatusBarTopAppBar(
        title = { Text(stringResource(R.string.trash)) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.topbar_drawer)
                )
            }
        },
        actions = {
            IconButton(
                onClick = onEmptyTrash,
                enabled = emptyTrashEnabled
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_trash_empty_filled),
                    contentDescription = stringResource(R.string.trash_empty_trash)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}