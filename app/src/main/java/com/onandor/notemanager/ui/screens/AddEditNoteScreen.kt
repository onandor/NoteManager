package com.onandor.notemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onInterceptKeyBeforeSoftKeyboard
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.ui.components.LabelSelectionBottomDialog
import com.onandor.notemanager.utils.indexOfDifference
import com.onandor.notemanager.viewmodels.AddEditNoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .imePadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AddEditNoteTopAppBar(
                noteLocation = uiState.location,
                onSaveNote = viewModel::finishEditing,
                onNavigateBack = { focusManager.clearFocus(); viewModel.navigateBack() },
                onArchiveNote = viewModel::archiveNote,
                onUnArchiveNote = viewModel::unArchiveNote,
                onTrashNote = viewModel::trashNote,
                onDeleteNote = viewModel::deleteNote,
                onAddLabels = viewModel::showEditLabelsDialog,
                scrollBehavior = scrollBehavior,
                scrolled = scrollBehavior.state.overlappedFraction > 0.01f
            )
        }
    ) { innerPadding ->
        TitleAndContentEditor(
            modifier = Modifier.padding(innerPadding),
            title = uiState.title,
            content = uiState.content,
            onTitleChanged = viewModel::updateTitle,
            onContentChanged = viewModel::updateContent,
            onMoveCursor = viewModel::moveCursor,
            editDisabled = uiState.location == NoteLocation.TRASH,
            focusManager = focusManager,
            newNote = uiState.newNote,
            editLabelsDialogOpen = uiState.editLabelsDialogOpen
        )
    }

    BackHandler {
        if (uiState.editLabelsDialogOpen) {
            viewModel.hideEditLabelsDialog()
        }
        else {
            viewModel.finishEditing()
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TitleAndContentEditor(
    modifier: Modifier,
    title: TextFieldValue,
    content: TextFieldValue,
    onTitleChanged: (TextFieldValue) -> Unit,
    onContentChanged: (TextFieldValue) -> Unit,
    onMoveCursor: (TextRange) -> Unit,
    editDisabled: Boolean,
    focusManager: FocusManager,
    newNote: Boolean,
    editLabelsDialogOpen: Boolean
) {
    val titleFocusRequester = remember { FocusRequester() }
    val contentFocusRequester = remember { FocusRequester() }
    var titleFocused by remember { mutableStateOf(false) }
    var contentFocused by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    var scrollToEnd by remember { mutableStateOf(false) }
    var spaceAboveContent by remember { mutableIntStateOf(0) }
    var editorViewportHeight by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val keyboard = LocalSoftwareKeyboardController.current

    Box(modifier = Modifier
        .fillMaxSize()
        .navigationBarsPadding()
        .onGloballyPositioned { coordinates ->
            editorViewportHeight = coordinates.size.height
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            if (editDisabled)
                return@clickable

            onMoveCursor(TextRange(content.text.length))
            scrollToEnd = true
            contentFocusRequester.requestFocus()
            keyboard?.show()
        }
    ) {}

    Column (modifier = modifier.verticalScroll(scrollState)) {
        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )

        EditorTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 15.dp)
                .animateContentSize()
                .focusRequester(titleFocusRequester)
                .onFocusChanged { titleFocused = it.isFocused }
                .onInterceptKeyBeforeSoftKeyboard { event ->
                    event.key == Key.Enter || event.key == Key.NumPadEnter
                }
                .onGloballyPositioned { coordinates ->
                    spaceAboveContent = coordinates.size.height
                },
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
            },
            readOnly = editDisabled,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = {
                    onMoveCursor(TextRange(content.text.length))
                    scrollToEnd = true
                    contentFocusRequester.requestFocus()
                }
            )
        )
        EditorTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(contentFocusRequester)
                .onFocusChanged { contentFocused = it.isFocused },
            value = content,
            onValueChange = onContentChanged,
            colors = textFieldColors,
            textStyle = TextStyle.Default.copy(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            placeholder = {
                Text(stringResource(id = R.string.addeditnote_hint_content))
            },
            readOnly = editDisabled,
            onCursorYCoordChanged = { cursorYTop, cursorYBottom, lineHeight ->
                val lowerLimit = scrollState.value - (1.5 * lineHeight).toInt()
                val upperLimit = scrollState.value + (editorViewportHeight - (4.7 * lineHeight).toInt())
                val cursorYTopWithTitleOffset = cursorYTop + spaceAboveContent
                val cursorYBottomWithTitleOffset = cursorYBottom + spaceAboveContent
                /*
                 * 1.5 and 4.7 are kinds of magic values, for some reason the calculation is always
                 * off by those amount of line heights, but since it works on 3 different DPIs I can't
                 * be bothered to check why. (Might have to do something with editorViewportHeight or
                 * spaceAboveContent not being calculated right.)
                */
                if (cursorYTopWithTitleOffset < lowerLimit) {
                    // the top of the cursor is out of the view on the top of the screen
                    val scrollPosition = cursorYTopWithTitleOffset + (1.5 * lineHeight).toInt()
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollPosition)
                    }
                }
                else if (cursorYBottomWithTitleOffset > upperLimit) {
                    // the bottom of the cursor is out of the view on the bottom of the screen
                    val scrollPosition = cursorYBottomWithTitleOffset - (editorViewportHeight - (4.7 * lineHeight).toInt())
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollPosition)
                    }
                }
            }
        )
    }

    LaunchedEffect(key1 = editLabelsDialogOpen, key2 = newNote) {
        if (editLabelsDialogOpen) {
            val _titleFocused = titleFocused
            val _contentFocused = contentFocused
            focusManager.clearFocus()
            titleFocused = _titleFocused
            contentFocused = _contentFocused
        }
        else {
            if (titleFocused)
                titleFocusRequester.requestFocus()
            else if (contentFocused || newNote)
                contentFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(scrollToEnd) {
        if (scrollToEnd) {
            scrollState.animateScrollTo(scrollState.maxValue)
            scrollToEnd = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorTextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = TextStyle.Default,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    readOnly: Boolean,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
    keyboardActions: KeyboardActions = KeyboardActions(),
    onCursorYCoordChanged: (Int, Int, Int) -> Unit = { _, _, _ -> }
) {
    val defaultTextSelectionColors = LocalTextSelectionColors.current
    val disabledHandleTextSelectionColors = TextSelectionColors(
        handleColor = Color.Transparent,
        backgroundColor = MaterialTheme.colorScheme.primaryContainer
    )
    val textSelectionColors = remember { mutableStateOf(defaultTextSelectionColors) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors.value) {
        BasicTextField(
            value = value,
            onValueChange = { newValue ->
                if (value.text.length != newValue.text.length) {
                    textSelectionColors.value = disabledHandleTextSelectionColors

                    val lineHeight = (textLayoutResult?.getLineBottom(0) ?: 0f).toInt()
                    val cursorLine = textLayoutResult?.getLineForOffset(newValue.selection.start) ?: 0
                    var cursorYTop = (textLayoutResult?.getLineTop(cursorLine) ?: 0f).toInt()
                    var cursorYBottom = (textLayoutResult?.getLineBottom(cursorLine) ?: 0f).toInt()

                    /*
                     * The whole next block stems from the fact that onValueChange gets called before
                     * onTextLayout, so textLayoutResult will always be outdated by one character.
                     * This causes issues mostly when a new line is added, since textLayoutResult
                     * doesn't know about that.
                    */
                    // If a character was added and not removed
                    if (newValue.text.length > value.text.length) {
                        // If we are at the end of the text and a new line was added
                        if (newValue.text.length == newValue.selection.start && newValue.text.last() == '\n') {
                            /*
                             * Manually add the height of a line, since at this stage we are still
                             * using the previous textLayoutResult and it doesn't know about the
                             * new line.
                            */
                            cursorYTop += lineHeight
                            cursorYBottom += lineHeight
                        }
                        else {
                            /*
                             * Else if a new character is added somewhere in the string, in the old
                             * textLayoutResult the character on which the cursor is will be the first
                             * character of the next line, and it will wrongly return that line, so
                             * the height manually needs to be taken away.
                             *
                             * The catch is that we don't want to take away from the height if the added
                             * character is a new line (because then textLayoutResult accidentally returns
                             * the right value), so we need to check for that.
                            */
                            val diffIdx = value.text.indexOfDifference(newValue.text)
                            if (textLayoutResult?.getLineForOffset(value.selection.start) != cursorLine
                                && diffIdx != -1 && newValue.text[diffIdx] != '\n') {
                                cursorYTop -= lineHeight
                                cursorYBottom -= lineHeight
                            }
                        }
                    }
                    onCursorYCoordChanged(cursorYTop, cursorYBottom, lineHeight)
                }
                onValueChange(newValue)
            },
            modifier = modifier
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)
                        val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                        if (upEvent != null) {
                            textSelectionColors.value = defaultTextSelectionColors
                        }
                    }
                },
            textStyle = textStyle,
            cursorBrush = SolidColor(colors.cursorColor),
            keyboardOptions = keyboardOptions.copy(capitalization = KeyboardCapitalization.Sentences),
            readOnly = readOnly,
            singleLine = singleLine,
            keyboardActions = keyboardActions,
            onTextLayout = {
                textLayoutResult = it
            },
            decorationBox = @Composable { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = value.text,
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
    scrollBehavior: TopAppBarScrollBehavior,
    scrolled: Boolean
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

    val color = if (scrolled)
        TopAppBarDefaults.topAppBarColors().scrolledContainerColor
    else
        MaterialTheme.colorScheme.surface

    val statusBarColor = animateColorAsState(
        targetValue = color,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = ""
    )

    Surface(color = statusBarColor.value) {
        Row(modifier = Modifier.statusBarsPadding()) {
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
                scrollBehavior = scrollBehavior
            )
        }
    }
}