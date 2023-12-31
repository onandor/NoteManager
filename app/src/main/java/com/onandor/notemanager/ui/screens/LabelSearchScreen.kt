package com.onandor.notemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.ui.components.ColoredStatusBarTopAppBar
import com.onandor.notemanager.ui.components.EmptyContent
import com.onandor.notemanager.ui.components.NoteList
import com.onandor.notemanager.ui.components.NoteSortingMenu
import com.onandor.notemanager.ui.components.PinEntryDialog
import com.onandor.notemanager.ui.components.SimpleConfirmationDialog
import com.onandor.notemanager.ui.components.SwipeableSnackbarHost
import com.onandor.notemanager.utils.NoteSorting
import com.onandor.notemanager.viewmodels.LabelSearchViewmodel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelSearchScreen(
    onOpenDrawer: () -> Unit,
    onCloseDrawer: () -> Unit,
    drawerOpen: Boolean,
    viewModel: LabelSearchViewmodel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = if (uiState.selectedNotes.isEmpty())
        TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    else
        TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
    val editLabelDialogState = rememberModalBottomSheetState()

    Scaffold(
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
                    LabelSearchTopAppBar(
                        title = uiState.searchedLabel.title,
                        scrollBehavior = scrollBehavior,
                        onOpenDrawer = onOpenDrawer,
                        noteListCollapsedView = uiState.noteListState.collapsed,
                        onToggleNoteListCollapsedView = viewModel::toggleNoteListCollapsedView,
                        currentSorting = uiState.noteListState.sorting,
                        onNoteSortingChanged = viewModel::changeSorting,
                        onSearchClicked = viewModel::navigateToSearch,
                        onDeleteLabel = viewModel::openDeleteDialog,
                        onEditLabel = viewModel::openEditLabelDialog
                    )
                } else {
                    SearchSelectionTopAppBar(
                        selectedNotes = uiState.selectedNotes,
                        onClearSelection = viewModel::clearSelection,
                        onMoveNotes = viewModel::moveSelectedNotes,
                        onChangePinned = viewModel::changeSelectedNotesPinning,
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        },
        snackbarHost = {
            SwipeableSnackbarHost(hostState = snackbarHostState) {
                SnackbarHost(hostState = snackbarHostState)
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
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            AnimatedVisibility(
                visible = uiState.mainNotes.isEmpty() && uiState.archiveNotes.isEmpty() && !uiState.loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyContent(
                    painter = painterResource(id = R.drawable.ic_label_filled),
                    text = stringResource(id = R.string.label_search_screen_empty)
                )
            }
            AnimatedVisibility(
                visible = (uiState.mainNotes.isNotEmpty() || uiState.archiveNotes.isNotEmpty())
                        && !uiState.loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NoteList(
                    mainNotes = uiState.mainNotes,
                    archiveNotes = uiState.archiveNotes,
                    selectedNotes = uiState.selectedNotes,
                    onNoteClick = viewModel::noteClick,
                    onNoteLongClick = viewModel::noteLongClick,
                    collapsedView = uiState.noteListState.collapsed
                )
            }
        }
    }

    fun hideEditLabelDialog() {
        coroutineScope.launch {
            editLabelDialogState.hide()
            viewModel.closeEditLabelDialog()
        }
    }

    if (uiState.editLabelDialogOpen) {
        val navBarInsets = WindowInsets.navigationBars
        ModalBottomSheet(
            onDismissRequest = viewModel::closeEditLabelDialog,
            sheetState = editLabelDialogState,
            windowInsets = WindowInsets.ime,
            dragHandle = { }
        ) {
            AddEditLabelDialogContent(
                title = uiState.editLabelForm.title,
                color = uiState.editLabelForm.color,
                titleValid = uiState.editLabelForm.titleValid,
                onTitleChanged = viewModel::updateLabelTitle,
                onColorChanged = viewModel::updateLabelColor,
                onSubmitChange = { viewModel.saveLabel(); hideEditLabelDialog() },
                colorSelection = viewModel.colorSelection,
                navBarInsets = navBarInsets
            )
        }
    }

    if (uiState.deleteDialogOpen) {
        SimpleConfirmationDialog(
            onDismissRequest = viewModel::closeDeleteDialog,
            onConfirmation = viewModel::deleteLabel,
            text = stringResource(id = R.string.edit_labels_delete_confirmation)
        )
    }

    if (uiState.pinEntryDialogOpen) {
        PinEntryDialog(
            onConfirmPin = viewModel::confirmPinEntry,
            onDismissRequest = viewModel::closePinEntryDialog,
            description = stringResource(id = R.string.dialog_pin_entry_locked_note_desc)
        )
    }

    if (uiState.snackbarResource != 0) {
        val snackbarText = stringResource(id = uiState.snackbarResource)
        LaunchedEffect(uiState.addEditSnackbarResource) {
            coroutineScope.launch { snackbarHostState.showSnackbar(snackbarText) }
            viewModel.snackbarShown()
        }
    }

    if (uiState.addEditSnackbarResource != 0) {
        val resultText = stringResource(id = uiState.addEditSnackbarResource)
        LaunchedEffect(uiState.addEditSnackbarResource) {
            coroutineScope.launch { snackbarHostState.showSnackbar(resultText) }
            viewModel.addEditResultSnackbarShown()
        }
    }

    LaunchedEffect(uiState.selectedNotes.size) {
        if (uiState.selectedNotes.isNotEmpty()) {
            scrollBehavior.state.heightOffset = 0f
        }
    }

    BackHandler {
        if (drawerOpen) {
            onCloseDrawer()
        } else if (uiState.editLabelDialogOpen) {
            viewModel.closeEditLabelDialog()
        } else if (uiState.selectedNotes.isNotEmpty()) {
            viewModel.clearSelection()
        } else {
            viewModel.navigateBack()
        }
    }
}

@Composable
private fun MoreOptionsMenu(
    noteListCollapsedView: Boolean,
    onToggleNoteListCollapsedView: () -> Unit,
    onDeleteLabel: () -> Unit,
    onEditLabel: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(contentAlignment = Alignment.Center) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(id = R.string.more_options)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (noteListCollapsedView) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.label_search_screen_expand_list_view)) },
                    onClick = { onToggleNoteListCollapsedView(); expanded = false }
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.label_search_screen_collapse_list_view)) },
                    onClick = { onToggleNoteListCollapsedView(); expanded = false }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.label_search_screen_edit_label)) },
                onClick = { onEditLabel(); expanded = false }
            )
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.label_search_screen_delete_label)) },
                onClick = { onDeleteLabel(); expanded = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabelSearchTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onOpenDrawer: () -> Unit,
    noteListCollapsedView: Boolean,
    onToggleNoteListCollapsedView: () -> Unit,
    currentSorting: NoteSorting,
    onNoteSortingChanged: (NoteSorting) -> Unit,
    onSearchClicked: () -> Unit,
    onDeleteLabel: () -> Unit,
    onEditLabel: () -> Unit
) {
    ColoredStatusBarTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = title,
                modifier = Modifier.padding(start = 5.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.topbar_drawer))
            }
        },
        actions = {
            IconButton(onClick = onSearchClicked) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(id = R.string.topbar_search_notes)
                )
            }
            NoteSortingMenu(
                currentSorting = currentSorting,
                onSortingClicked = onNoteSortingChanged
            )
            MoreOptionsMenu(
                noteListCollapsedView = noteListCollapsedView,
                onToggleNoteListCollapsedView = onToggleNoteListCollapsedView,
                onDeleteLabel = onDeleteLabel,
                onEditLabel = onEditLabel
            )
        }
    )
}