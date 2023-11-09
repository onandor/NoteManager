package com.onandor.notemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.ui.components.ColoredStatusBarTopAppBar
import com.onandor.notemanager.ui.components.LabelSelectionBottomDialog
import com.onandor.notemanager.ui.components.LifecycleObserver
import com.onandor.notemanager.ui.components.PinButton
import com.onandor.notemanager.ui.components.PinEntryDialog
import com.onandor.notemanager.ui.components.SimpleConfirmationDialog
import com.onandor.notemanager.utils.indexOfDifference
import com.onandor.notemanager.viewmodels.AddEditNoteViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: AddEditNoteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier
            .imePadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AddEditNoteTopAppBar(
                noteLocation = uiState.location,
                notePinned = uiState.pinned,
                onSaveNote = viewModel::finishEditing,
                onNavigateBack = { focusManager.clearFocus(); viewModel.navigateBack() },
                onArchiveNote = viewModel::archiveNote,
                onUnArchiveNote = viewModel::unArchiveNote,
                onTrashNote = viewModel::trashNote,
                onDeleteNote = viewModel::deleteNote,
                onAddLabels = viewModel::showEditLabelsDialog,
                onChangePinned = viewModel::changePinned,
                onSetPin = viewModel::openChangePinDialog,
                onRemovePin = viewModel::removePin,
                locked = uiState.pinHash.isNotEmpty(),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.clickedLink != null,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::openLinkConfirmDialog,
                    text = { Text(stringResource(id = R.string.addeditnote_open_link)) },
                    icon = { Icon(painterResource(id = R.drawable.ic_open_link), "") }
                )
            }
        }
    ) { innerPadding ->
        TitleAndContentEditor(
            modifier = Modifier.padding(innerPadding),
            title = uiState.title,
            content = uiState.content,
            modificationDate = uiState.modificationDate,
            onTitleChanged = viewModel::updateTitle,
            onContentChanged = viewModel::updateContent,
            onMoveCursor = viewModel::moveCursor,
            editDisabled = uiState.location == NoteLocation.TRASH,
            focusManager = focusManager,
            newNote = uiState.newNote,
            editLabelsDialogOpen = uiState.editLabelsDialogOpen,
            titleLinkRanges = uiState.titleLinkRanges,
            contentLinkRanges = uiState.contentLinkRanges
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

    if (uiState.changePinDialogOpen) {
        PinEntryDialog(
            onConfirmPin = viewModel::setPin,
            onDismissRequest = viewModel::closeChangePinDialog,
            description = stringResource(id = R.string.dialog_pin_entry_set_pin_desc)
        )
    }
    
    if (uiState.linkConfirmDialogOpen) {
        val confirmationText = stringResource(id = R.string.addeditnote_open_link_confirmation) +
                "\n\n" + uiState.clickedLink
        SimpleConfirmationDialog(
            onDismissRequest = viewModel::closeLinkConfirmDialog,
            onConfirmation = { viewModel.closeLinkConfirmDialog(); uriHandler.openUri(uiState.clickedLink!!) },
            text = confirmationText
        )
    }

    LifecycleObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            viewModel.onPause()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TitleAndContentEditor(
    modifier: Modifier,
    title: TextFieldValue,
    content: TextFieldValue,
    modificationDate: String,
    onTitleChanged: (TextFieldValue) -> Unit,
    onContentChanged: (TextFieldValue) -> Unit,
    onMoveCursor: (TextRange) -> Unit,
    editDisabled: Boolean,
    focusManager: FocusManager,
    newNote: Boolean,
    editLabelsDialogOpen: Boolean,
    titleLinkRanges: List<IntRange>,
    contentLinkRanges: List<IntRange>
) {
    val titleFocusRequester = remember { FocusRequester() }
    val contentFocusRequester = remember { FocusRequester() }
    var titleFocused by remember { mutableStateOf(false) }
    var contentFocused by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    var dateHeight by remember { mutableIntStateOf(0) }
    var titleHeight by remember { mutableIntStateOf(0) }
    var editorHeight by remember { mutableIntStateOf(0) }

    val scrollState = rememberScrollState()
    var scrollToEnd by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val keyboard = LocalSoftwareKeyboardController.current

    Box(modifier = modifier
        .fillMaxSize()
        .navigationBarsPadding()
        .onGloballyPositioned { coordinates ->
            editorHeight = coordinates.size.height
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

    Column(modifier = modifier.verticalScroll(scrollState)) {
        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    dateHeight = coordinates.size.height
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = modificationDate,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic
            )
        }
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
                    titleHeight =
                        coordinates.size.height + with(density) { 25.dp.toPx() }.roundToInt()
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
            ),
            linkRanges = titleLinkRanges
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
            onCursorYCoordChanged = { cursorYTop, cursorYBottom ->
                val cursorYTopWithOffset = cursorYTop + titleHeight + dateHeight
                val cursorYBottomWithOffset = cursorYBottom + titleHeight + dateHeight
                if (cursorYTopWithOffset < scrollState.value) {
                    // the top of the cursor is out of the view on the top of the screen
                    coroutineScope.launch {
                        scrollState.animateScrollTo(cursorYTopWithOffset)
                    }
                }
                else if (cursorYBottomWithOffset > scrollState.value + editorHeight) {
                    // the bottom of the cursor is out of the view on the bottom of the screen
                    val scrollPosition = cursorYBottomWithOffset - editorHeight
                    coroutineScope.launch {
                        scrollState.animateScrollTo(scrollPosition)
                    }
                }
            },
            linkRanges = contentLinkRanges
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

private fun buildAnnotatedString(text: String, linkRanges: List<IntRange>): AnnotatedString {
    if (linkRanges.isEmpty())
        return AnnotatedString.Builder(text).toAnnotatedString()

    val builder = AnnotatedString.Builder()
    var rangeIdx = 0
    var currentRange = linkRanges[rangeIdx]
    for (charIdx in text.indices) {
        if (charIdx >= currentRange.first) {
            builder.withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                append(text[charIdx])
            }
        } else {
            builder.append(text[charIdx])
        }
        if (charIdx >= currentRange.last) {
            currentRange = if (rangeIdx < linkRanges.indices.last) {
                linkRanges[++rangeIdx]
            } else {
                IntRange(Int.MAX_VALUE, Int.MAX_VALUE)
            }
        }
    }
    return builder.toAnnotatedString()
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
    onCursorYCoordChanged: (Int, Int) -> Unit = { _, _ -> },
    linkRanges: List<IntRange> = emptyList()
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
                    onCursorYCoordChanged(cursorYTop, cursorYBottom)
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
            visualTransformation = {
                TransformedText(
                    text = buildAnnotatedString(value.text, linkRanges),
                    offsetMapping = OffsetMapping.Identity
                )
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
                    contentPadding = PaddingValues(start = 19.dp, end = 19.dp)
                )
            }
        )
    }
}

@Composable
fun MoreOptionsMenu(
    onSetPin: () -> Unit,
    onRemovePin: () -> Unit,
    onTrashNote: () -> Unit,
    locked: Boolean
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
            if (locked) {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.addeditnote_remove_pin)) },
                    onClick = { onRemovePin(); expanded = false }
                )
            } else {
                DropdownMenuItem(
                    text = { Text(stringResource(id = R.string.addeditnote_set_pin)) },
                    onClick = { onSetPin(); expanded = false }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(id = R.string.addeditnote_trash_note)) },
                onClick = { onTrashNote(); expanded = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditNoteTopAppBar(
    noteLocation: NoteLocation,
    notePinned: Boolean,
    onSaveNote: () -> Unit,
    onNavigateBack: () -> Unit,
    onArchiveNote: () -> Unit,
    onUnArchiveNote: () -> Unit,
    onTrashNote: () -> Unit,
    onDeleteNote: () -> Unit,
    onAddLabels: () -> Unit,
    onChangePinned: (Boolean) -> Unit,
    onSetPin: () -> Unit,
    onRemovePin: () -> Unit,
    locked: Boolean,
    scrollBehavior: TopAppBarScrollBehavior
) {

    val actions: @Composable RowScope.() -> Unit = when(noteLocation) {
        NoteLocation.NOTES -> {
            {
                if (locked) {
                    Icon(
                        modifier = Modifier.padding(end = 10.dp),
                        imageVector = Icons.Filled.Lock,
                        contentDescription = stringResource(id = R.string.addeditnote_note_locked),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                PinButton(pinned = notePinned, onChangePinned = onChangePinned)
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
                MoreOptionsMenu(
                    onSetPin = onSetPin,
                    onRemovePin = onRemovePin,
                    onTrashNote = { onTrashNote(); onNavigateBack() },
                    locked = locked
                )
            }
        }
        NoteLocation.ARCHIVE -> {
            {
                if (locked) {
                    Icon(
                        modifier = Modifier.padding(end = 10.dp),
                        imageVector = Icons.Filled.Lock,
                        contentDescription = stringResource(id = R.string.addeditnote_note_locked),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                PinButton(pinned = notePinned, onChangePinned = onChangePinned)
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
                        contentDescription = stringResource(id = R.string.addeditnote_trash_note)
                    )
                }
                MoreOptionsMenu(
                    onSetPin = onSetPin,
                    onRemovePin = onRemovePin,
                    onTrashNote = { onTrashNote(); onNavigateBack() },
                    locked = locked
                )
            }
        }
        NoteLocation.TRASH -> {
            {
                IconButton(onClick = { onDeleteNote(); onNavigateBack() }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.addeditnote_trash_note)
                    )
                }
            }
        }
        NoteLocation.ALL -> { { } }
    }

    ColoredStatusBarTopAppBar(
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