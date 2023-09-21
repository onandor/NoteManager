package com.onandor.notemanager.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.viewmodels.AddEditNoteUiState
import com.onandor.notemanager.viewmodels.AddEditNoteViewModel

@Composable
fun AddEditNoteScreen(
    goBack: () -> Unit,
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AddEditNoteTopAppBar(
                viewModel = viewModel,
                uiState = uiState,
                goBack = goBack
            )
        }
    ) { innerPadding ->
            AddEditNoteTitleAndContent(
            title = uiState.title,
            content = uiState.content,
            onTitleChanged = viewModel::updateTitle,
            onContentChanged = viewModel::updateContent,
            Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AddEditNoteTitleAndContent(
    title: String,
    content: String,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    modifier: Modifier
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

@Composable
fun AddEditNoteTopAppBar(
    viewModel: AddEditNoteViewModel,
    uiState: AddEditNoteUiState,
    goBack: () -> Unit,
) {
    when(uiState.noteLocation) {
        NoteLocation.NOTES -> {
            println("TopAppBar: NOTES")
            AddEditNoteTopAppBar_Notes(
                onSaveNote = viewModel::saveNote,
                goBack = goBack,
                onArchiveNote = viewModel::archiveNote,
                onTrashNote = viewModel::trashNote,
                onAddLabels = { }
            )
        }
        NoteLocation.ARCHIVE -> {
            println("TopAppBar: ARCHIVE")
            AddEditNoteTopAppBar_Archive(
                onSaveNote = viewModel::saveNote,
                goBack = goBack,
                onUnArchiveNote = viewModel::unArchiveNote,
                onTrashNote = viewModel::trashNote,
                onAddLabels = { }
            )
        }
        NoteLocation.TRASH -> {
            println("TopAppBar: TRASH")
            AddEditNoteTopAppBar_Trash(
                goBack = goBack,
                onDeleteNote = viewModel::deleteNote
            )
        }
    }
}

@Composable
fun AddEditNoteTopAppBar_Notes(
    onSaveNote: () -> Unit,
    goBack: () -> Unit,
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
            IconButton(onClick = { onSaveNote(); goBack() }) {
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
                IconButton(onClick = { onArchiveNote(); goBack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu_archive_list),
                        contentDescription = stringResource(id = R.string.addeditnote_archive_note)
                    )
                }
                IconButton(onClick = { onTrashNote(); goBack() }) {
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
    goBack: () -> Unit,
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
            IconButton(onClick = { onSaveNote(); goBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAddLabels(); goBack() }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.addeditnote_add_labels)
                    )
                }
                IconButton(onClick = { onUnArchiveNote(); goBack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu_archive_list),
                        contentDescription = stringResource(id = R.string.addeditnote_archive_note)
                    )
                }
                IconButton(onClick = { onTrashNote(); goBack() }) {
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
    goBack: () -> Unit,
    onDeleteNote: () -> Unit
) {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(65.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { goBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
            }
            IconButton(onClick = { onDeleteNote(); goBack() }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(id = R.string.addeditnote_delete_note)
                )
            }
        }
    }
}