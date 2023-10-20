package com.onandor.notemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.ui.components.LabelComponent
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.viewmodels.AddEditNoteUiState
import com.onandor.notemanager.viewmodels.AddEditNoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val labelDialogState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            AddEditNoteTopAppBar(
                viewModel = viewModel,
                uiState = uiState
            )
        }
    ) { innerPadding ->
        TitleAndContentEditor(
            modifier = Modifier.padding(innerPadding),
            title = uiState.title,
            content = uiState.content,
            onTitleChanged = viewModel::updateTitle,
            onContentChanged = viewModel::updateContent,
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
        ModalBottomSheet(
            onDismissRequest = viewModel::hideEditLabelsDialog,
            sheetState = labelDialogState,
            windowInsets = WindowInsets(0, 0, 0, 0),
            dragHandle = { }
        ) {
            EditLabelsDialogContent(
                labels = uiState.labels,
                remainingLabels = uiState.remainingLabels,
                onAddLabel = viewModel::addLabel,
                onRemoveLabel = viewModel::removeLabel,
                navBarInsets = navBarInsets
            )
        }
    }
}

@Composable
private fun TitleAndContentEditor(
    modifier: Modifier,
    title: String,
    content: String,
    onTitleChanged: (String) -> Unit,
    onContentChanged: (String) -> Unit,
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

        EditorTextField(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
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
        Spacer(modifier = Modifier.height(10.dp))
        EditorTextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            value = content,
            onValueChange = onContentChanged,
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
fun EditorTextField(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditLabelsDialogContent(
    labels: List<Label>,
    remainingLabels: List<Label>,
    onAddLabel: (Label) -> Unit,
    onRemoveLabel: (Label) -> Unit,
    navBarInsets: WindowInsets
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(navBarInsets)
            .padding(start = 15.dp, end = 15.dp, top = 35.dp, bottom = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(id = R.string.dialog_edit_note_labels_added),
            fontSize = 23.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (labels.isEmpty()) {
            Text(
                text = stringResource(id = R.string.dialog_edit_note_labels_empty),
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
            ) {
                labels.forEach { label ->
                    LabelComponent(
                        label = label,
                        clickable = true,
                        onClick = onRemoveLabel,
                        padding = 10.dp,
                        fontSize = 20.sp,
                        borderWidth = 2.dp,
                        roundedCornerSize = 10.dp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(id = R.string.dialog_edit_note_labels_available),
            fontSize = 23.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (remainingLabels.isEmpty()) {
            Text(
                text = stringResource(id = R.string.dialog_edit_note_labels_empty),
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)
            ) {
                remainingLabels.forEach { label ->
                    LabelComponent(
                        label = label,
                        clickable = true,
                        onClick = onAddLabel,
                        padding = 10.dp,
                        fontSize = 20.sp,
                        borderWidth = 2.dp,
                        roundedCornerSize = 10.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun AddEditNoteTopAppBar(
    viewModel: AddEditNoteViewModel,
    uiState: AddEditNoteUiState
) {
    val focusManager = LocalFocusManager.current

    when(uiState.location) {
        NoteLocation.NOTES -> {
            AddEditNoteTopAppBar_Notes(
                onSaveNote = viewModel::saveNote,
                navigateBack = { focusManager.clearFocus(); viewModel.navigateBack() },
                onArchiveNote = viewModel::archiveNote,
                onTrashNote = viewModel::trashNote,
                onAddLabels = viewModel::showEditLabelsDialog
            )
        }
        NoteLocation.ARCHIVE -> {
            AddEditNoteTopAppBar_Archive(
                onSaveNote = viewModel::saveNote,
                navigateBack = { focusManager.clearFocus(); viewModel.navigateBack() },
                onUnArchiveNote = viewModel::unArchiveNote,
                onTrashNote = viewModel::trashNote,
                onAddLabels = viewModel::showEditLabelsDialog
            )
        }
        NoteLocation.TRASH -> {
            AddEditNoteTopAppBar_Trash(
                navigateBack = { focusManager.clearFocus(); viewModel.navigateBack() },
                onDeleteNote = viewModel::deleteNote
            )
        }
        NoteLocation.ALL -> { }
    }
}

@Composable
private fun AddEditNoteTopAppBar_Notes(
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAddLabels() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_note_add_label_filled),
                        contentDescription = stringResource(id = R.string.addeditnote_add_labels)
                    )
                }
                IconButton(onClick = { onArchiveNote(); navigateBack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_note_archive_filled),
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
private fun AddEditNoteTopAppBar_Archive(
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onAddLabels() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_note_add_label_filled),
                        contentDescription = stringResource(id = R.string.addeditnote_add_labels)
                    )
                }
                IconButton(onClick = { onUnArchiveNote(); navigateBack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_note_unarchive_filled),
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
private fun AddEditNoteTopAppBar_Trash(
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
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

@Preview
@Composable
fun TitleAndContentEditorPreview() {
    TitleAndContentEditor(
        modifier = Modifier,
        title = "",
        content = "",
        onTitleChanged = { },
        onContentChanged = { }
    )
}