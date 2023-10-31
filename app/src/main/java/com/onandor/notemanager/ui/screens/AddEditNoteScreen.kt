package com.onandor.notemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.ui.components.LabelSelectionBottomDialog
import com.onandor.notemanager.viewmodels.AddEditNoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .imePadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AddEditNoteTopAppBar(
                noteLocation = uiState.location,
                onSaveNote = viewModel::saveNote,
                onNavigateBack = { focusManager.clearFocus(); viewModel.navigateBack() },
                onArchiveNote = viewModel::archiveNote,
                onUnArchiveNote = viewModel::unArchiveNote,
                onTrashNote = viewModel::trashNote,
                onDeleteNote = viewModel::deleteNote,
                onAddLabels = viewModel::showEditLabelsDialog,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        TitleAndContentEditor(
            modifier = Modifier.padding(innerPadding),
            title = uiState.title,
            content = uiState.content,
            onTitleChanged = viewModel::updateTitle,
            onContentChanged = viewModel::updateContent,
            newNote = uiState.newNote
        )
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

    if (uiState.editLabelsDialogOpen) {
        val navBarInsets = WindowInsets.navigationBars
        LabelSelectionBottomDialog(
            onDismissRequest = viewModel::hideEditLabelsDialog,
            insets = navBarInsets,
            selectedLabels = uiState.addedLabels,
            labels = uiState.labels,
            selectedText = stringResource(id = R.string.dialog_edit_note_labels_added),
            unSelectedText = stringResource(id = R.string.dialog_edit_note_labels_available),
            onChangeLabelSelection = viewModel::addRemoveLabel
        )
    }
}

@Composable
private fun TitleAndContentEditor(
    modifier: Modifier,
    title: String,
    content: String,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
    newNote: Boolean
) {
    Column (
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .height(IntrinsicSize.Max)
    ){
        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
        val focusRequester = FocusRequester()

        LaunchedEffect(newNote) {
            if (newNote) {
                focusRequester.requestFocus()
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        EditorTextField(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .focusRequester(focusRequester),
            value = title,
            onValueChange = onTitleChanged,
            colors = textFieldColors,
            textStyle = TextStyle.Default.copy(
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            placeholder = {
                Text(
                    text = stringResource(id = R.string.addeditnote_hint_title),
                    fontSize = 22.sp
                )
            }
        )
        Spacer(modifier = Modifier.height(15.dp))
        EditorTextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            value = content,
            onValueChange = { onContentChanged(it) } ,
            colors = textFieldColors,
            textStyle = TextStyle.Default.copy(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            placeholder = {
                Text(stringResource(id = R.string.addeditnote_hint_content))
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = TextStyle.Default,
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.cursorColor),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        decorationBox = @Composable { innerTextField ->
            TextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = false,
                visualTransformation = VisualTransformation.None,
                interactionSource = remember { MutableInteractionSource() },
                colors = colors,
                placeholder = placeholder,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditNoteTopAppBar(
    noteLocation: NoteLocation,
    onSaveNote: () -> Unit,
    onNavigateBack: () -> Unit,
    onArchiveNote: () -> Unit,
    onUnArchiveNote: () -> Unit,
    onTrashNote: () -> Unit,
    onDeleteNote: () -> Unit,
    onAddLabels: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val actions: @Composable RowScope.() -> Unit = when(noteLocation) {
        NoteLocation.NOTES -> {
            {
                IconButton(onClick = { onAddLabels() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_note_add_label_filled),
                        contentDescription = stringResource(id = R.string.addeditnote_add_labels)
                    )
                }
                IconButton(onClick = { onArchiveNote(); onNavigateBack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_note_archive_filled),
                        contentDescription = stringResource(id = R.string.addeditnote_archive_note)
                    )
                }
                IconButton(onClick = { onTrashNote(); onNavigateBack() }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.addeditnote_delete_note)
                    )
                }
            }
        }
        NoteLocation.ARCHIVE -> {
            {
                IconButton(onClick = { onAddLabels() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_note_add_label_filled),
                        contentDescription = stringResource(id = R.string.addeditnote_add_labels)
                    )
                }
                IconButton(onClick = { onUnArchiveNote(); onNavigateBack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_note_unarchive_filled),
                        contentDescription = stringResource(id = R.string.addeditnote_archive_note)
                    )
                }
                IconButton(onClick = { onTrashNote(); onNavigateBack() }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.addeditnote_delete_note)
                    )
                }
            }
        }
        NoteLocation.TRASH -> {
            {
                IconButton(onClick = { onNavigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.addeditnote_hint_go_back))
                }
                IconButton(onClick = { onDeleteNote(); onNavigateBack() }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.addeditnote_delete_note)
                    )
                }
            }
        }
        NoteLocation.ALL -> { { } }
    }

    TopAppBar(
        title = { },
        navigationIcon = {
            IconButton(onClick = { onSaveNote(); onNavigateBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.addeditnote_hint_go_back)
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        scrollBehavior = scrollBehavior
    )
}

@Preview
@Composable
private fun TitleAndContentEditorPreview() {
    TitleAndContentEditor(
        modifier = Modifier,
        title = "",
        content = "",
        onTitleChanged = { },
        onContentChanged = { },
        newNote = false
    )
}