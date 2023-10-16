package com.onandor.notemanager.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.components.LabelComponent
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.viewmodels.AddEditNoteUiState
import com.onandor.notemanager.viewmodels.AddEditNoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddEditNoteScreen(
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    ModalBottomSheetLayout(
        sheetContent = {
            EditLabelsDialogContent(
                labels = uiState.labels,
                remainingLabels = uiState.remainingLabels,
                onAddLabel = viewModel::addLabel,
                onRemoveLabel = viewModel::removeLabel
            )
        },
        sheetState = sheetState
    ) {
        Scaffold(
            topBar = {
                AddEditNoteTopAppBar(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        ) { innerPadding ->
            val coroutineScope = rememberCoroutineScope()

            AddEditNoteTitleAndContent(
                modifier = Modifier.padding(innerPadding),
                title = uiState.title,
                content = uiState.content,
                onTitleChanged = viewModel::updateTitle,
                onContentChanged = viewModel::updateContent,
            )

            LaunchedEffect(uiState.editLabelsDialogOpen) {
                if (uiState.editLabelsDialogOpen) {
                    coroutineScope.launch {
                        sheetState.show()
                    }
                }
            }
            LaunchedEffect(sheetState.isVisible) {
                if (!sheetState.isVisible) {
                    viewModel.hideEditLabelsDialog()
                }
            }
        }
    }

    BackHandler {
        if (uiState.editLabelsDialogOpen) {
            viewModel.hideEditLabelsDialog()
        }
        else {
            viewModel.saveNote()
            viewModel.navigateBack()
        }
    }
}

@Composable
fun AddEditNoteTitleAndContent(
    modifier: Modifier,
    title: String,
    content: String,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
) {
    Column (
        modifier = modifier
            .fillMaxWidth()
            //.padding(all = 16.dp)
            .verticalScroll(rememberScrollState())
    ){
        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )

        TextField(
            value = title,
            onValueChange = onTitleChanged,
            colors = textFieldColors,
            placeholder = {
                Text("Title") // TODO: resource
            }
        )
        TextField(
            value = content,
            onValueChange = onContentChanged,
            colors = textFieldColors,
            placeholder = {
                Text("Your note goes here...")
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditLabelsDialogContent(
    labels: List<Label>,
    remainingLabels: List<Label>,
    onAddLabel: (Label) -> Unit,
    onRemoveLabel: (Label) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 15.dp)
            .verticalScroll(rememberScrollState())
            .systemBarsPadding()
    ) {
        Text("Added")
        FlowRow {
            labels.forEach { label ->
                LabelComponent(
                    label = label,
                    clickable = true,
                    onClick = onRemoveLabel
                )
            }
        }
        FlowRow {
            remainingLabels.forEach { label ->
                LabelComponent(
                    label = label,
                    clickable = true,
                    onClick = onAddLabel
                )
            }
        }
    }
}

@Composable
fun AddEditNoteTopAppBar(
    viewModel: AddEditNoteViewModel,
    uiState: AddEditNoteUiState
) {
    when(uiState.location) {
        NoteLocation.NOTES -> {
            AddEditNoteTopAppBar_Notes(
                onSaveNote = viewModel::saveNote,
                navigateBack = viewModel::navigateBack,
                onArchiveNote = viewModel::archiveNote,
                onTrashNote = viewModel::trashNote,
                onAddLabels = viewModel::showEditLabelsDialog
            )
        }
        NoteLocation.ARCHIVE -> {
            AddEditNoteTopAppBar_Archive(
                onSaveNote = viewModel::saveNote,
                navigateBack = viewModel::navigateBack,
                onUnArchiveNote = viewModel::unArchiveNote,
                onTrashNote = viewModel::trashNote,
                onAddLabels = viewModel::showEditLabelsDialog
            )
        }
        NoteLocation.TRASH -> {
            AddEditNoteTopAppBar_Trash(
                navigateBack = viewModel::navigateBack,
                onDeleteNote = viewModel::deleteNote
            )
        }
        NoteLocation.ALL -> { }
    }
}

@Composable
fun AddEditNoteTopAppBar_Notes(
    onSaveNote: () -> Unit,
    navigateBack: () -> Unit,
    onTrashNote: () -> Unit,
    onArchiveNote: () -> Unit,
    onAddLabels: () -> Unit
) {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(65.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { onSaveNote(); navigateBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAddLabels() }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.addeditnote_add_labels)
                    )
                }
                IconButton(onClick = { onArchiveNote(); navigateBack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu_archive_list),
                        contentDescription = stringResource(id = R.string.addeditnote_archive_note)
                    )
                }
                IconButton(onClick = { onTrashNote(); navigateBack() }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.addeditnote_delete_note)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditNoteTopAppBar_Archive(
    onSaveNote: () -> Unit,
    navigateBack: () -> Unit,
    onTrashNote: () -> Unit,
    onUnArchiveNote: () -> Unit,
    onAddLabels: () -> Unit
) {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(65.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { onSaveNote(); navigateBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAddLabels(); navigateBack() }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.addeditnote_add_labels)
                    )
                }
                IconButton(onClick = { onUnArchiveNote(); navigateBack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu_archive_list),
                        contentDescription = stringResource(id = R.string.addeditnote_archive_note)
                    )
                }
                IconButton(onClick = { onTrashNote(); navigateBack() }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.addeditnote_delete_note)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditNoteTopAppBar_Trash(
    navigateBack: () -> Unit,
    onDeleteNote: () -> Unit
) {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(65.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navigateBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
            }
            IconButton(onClick = { onDeleteNote(); navigateBack() }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.addeditnote_delete_note)
                )
            }
        }
    }
}