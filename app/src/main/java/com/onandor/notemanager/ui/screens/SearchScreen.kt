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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.ui.components.EmptyContent
import com.onandor.notemanager.ui.components.LabelSelectionBottomDialog
import com.onandor.notemanager.ui.components.MultiSelectTopAppBar
import com.onandor.notemanager.ui.components.NoteList
import com.onandor.notemanager.ui.components.PinButton
import com.onandor.notemanager.ui.components.PinEntryDialog
import com.onandor.notemanager.ui.components.SwipeableSnackbarHost
import com.onandor.notemanager.viewmodels.SearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .navigationBarsPadding()
            .imePadding(),
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
                    SearchBar(
                        text = uiState.searchForm.text,
                        onTextChanged = viewModel::updateSearchText,
                        onBackClicked = { focusManager.clearFocus(); viewModel.navigateBack() }
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
        floatingActionButton =  {
            AnimatedVisibility(
                visible = !scrollState.canScrollBackward,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.changeSearchByLabelsDialogOpen(true) },
                    icon = { Icon(painterResource(id = R.drawable.ic_label_filled), "")},
                    text = { Text(stringResource(id = R.string.search_search_by_labels)) }
                )
            }
        },
        snackbarHost = {
            SwipeableSnackbarHost(hostState = snackbarHostState) {
                SnackbarHost(hostState = snackbarHostState)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LoadingIndicator(loading = uiState.loading)
            AnimatedVisibility(
                visible = uiState.emptySearch,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyContent(
                    imageVector = Icons.Filled.Search,
                    text = stringResource(id = R.string.search_empty_search)
                )
            }
            AnimatedVisibility(
                visible = !uiState.emptySearch && !uiState.loading && uiState.emptyResult,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyContent(
                    imageVector = Icons.Filled.Search,
                    text = stringResource(id = R.string.search_no_results)
                )
            }
            AnimatedVisibility(
                visible = !uiState.emptySearch && !uiState.loading && !uiState.emptyResult,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NoteList(
                    mainNotes = uiState.mainNotes,
                    archiveNotes = uiState.archiveNotes,
                    selectedNotes = uiState.selectedNotes,
                    onNoteClick = { note -> focusManager.clearFocus(); viewModel.noteClick(note) },
                    onNoteLongClick = { note -> focusManager.clearFocus(); viewModel.noteLongClick(note) },
                    scrollState = scrollState,
                    topPadding = 8.dp
                )
            }
        }

        if (uiState.searchByLabelsDialogOpen) {
            val navBarInsets = WindowInsets.navigationBars
            LabelSelectionBottomDialog(
                onDismissRequest = viewModel::confirmSearchLabels,
                insets = navBarInsets,
                labels = uiState.labels,
                selectedLabels = uiState.searchLabels,
                selectedText = stringResource(id = R.string.dialog_search_by_labels_selected),
                unSelectedText = stringResource(id = R.string.dialog_search_by_labels_unselected),
                onChangeLabelSelection = viewModel::addRemoveSearchLabel
            )
        }

        if (uiState.pinEntryDialogOpen) {
            PinEntryDialog(
                onConfirmPin = viewModel::confirmPinEntry,
                onDismissRequest = viewModel::closePinEntryDialog,
                description = stringResource(id = R.string.dialog_pin_entry_locked_note_desc)
            )
        }
    }

    if (uiState.snackbarResource != 0) {
        val resultText = stringResource(id = uiState.snackbarResource)
        LaunchedEffect(uiState.snackbarResource) {
            coroutineScope.launch { snackbarHostState.showSnackbar(resultText) }
            viewModel.snackbarShown()
        }
    }

    LaunchedEffect(uiState.mainNotes) {
        scrollState.scrollToItem(0)
    }

    BackHandler {
        if (uiState.selectedNotes.isNotEmpty()) {
            viewModel.clearSelection()
        } else {
            viewModel.navigateBack()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSelectionTopAppBar(
    selectedNotes: List<Note>,
    onClearSelection: () -> Unit,
    onMoveNotes: (NoteLocation) -> Unit,
    onChangePinned: (Boolean) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val newLocation = if (selectedNotes.any { it.location == NoteLocation.NOTES })
        NoteLocation.ARCHIVE
    else
        NoteLocation.NOTES

    MultiSelectTopAppBar(
        onClearSelection = onClearSelection,
        selectedCount = selectedNotes.size,
        scrollBehavior = scrollBehavior
    ) {
        PinButton(
            pinned = !selectedNotes.any { note -> !note.pinned },
            onChangePinned = onChangePinned
        )
        IconButton(onClick = { onMoveNotes(newLocation) }) {
            if (newLocation == NoteLocation.ARCHIVE) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_note_archive_filled),
                    contentDescription = stringResource(id = R.string.search_archive)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_note_unarchive_filled),
                    contentDescription = stringResource(id = R.string.search_unarchive_selected)
                )
            }
        }
        IconButton(onClick = { onMoveNotes(NoteLocation.TRASH) }) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(id = R.string.search_trash_selected)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onBackClicked: () -> Unit
) {
    Surface(
        color = TopAppBarDefaults.topAppBarColors().scrolledContainerColor
    ) {
        val density = LocalDensity.current
        val statusBarHeight = WindowInsets.statusBars.getTop(density)
        Row(
            modifier = Modifier
                .height(with(density) { statusBarHeight.toDp() } + 64.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
            IconButton(
                modifier = Modifier.padding(start = 5.dp),
                onClick = onBackClicked
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.search_button_hint_go_back)
                )
            }
            TextField(
                modifier = Modifier.weight(1f),
                value = text,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_hint_search)
                    )
                },
                colors = textFieldColors,
                singleLine = true
            )
            if (text.isNotEmpty()) {
                IconButton(
                    modifier = Modifier.padding(end = 5.dp),
                    onClick = { onTextChanged("") }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(id = R.string.search_hint_clear_search)
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator(loading: Boolean) {
    AnimatedVisibility(
        visible = loading,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(40.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(100.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(7.dp)
                        .width(20.dp)
                        .aspectRatio(1f)
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewLoadingIndicator() {
    LoadingIndicator(loading = true)
}