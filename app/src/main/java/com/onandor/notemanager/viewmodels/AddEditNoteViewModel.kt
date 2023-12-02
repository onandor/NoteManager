package com.onandor.notemanager.viewmodels

import android.os.CountDownTimer
import android.util.Patterns
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onandor.notemanager.R
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavDestinationArgs
import com.onandor.notemanager.utils.AddEditResultState
import com.onandor.notemanager.utils.AddEditResults
import com.onandor.notemanager.utils.AsyncResult
import com.onandor.notemanager.utils.undo.EditHistory
import com.onandor.notemanager.utils.undo.EditHistoryEntry
import com.onandor.notemanager.utils.undo.EditHistoryLocation
import com.onandor.notemanager.utils.undo.EditHistoryType
import com.onandor.notemanager.utils.undo.NoteMoveSnapshot
import com.onandor.notemanager.utils.undo.UndoableAction
import com.onandor.notemanager.utils.undo.UndoableActionHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID
import java.util.regex.Matcher
import javax.inject.Inject
import kotlin.math.abs

data class AddEditNoteUiState(
    val title: TextFieldValue = TextFieldValue(""),
    val content: TextFieldValue = TextFieldValue(""),
    val location: NoteLocation = NoteLocation.NOTES,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val modificationDate: LocalDateTime = LocalDateTime.now(),
    val modificationDateString: String = "",
    val pinned: Boolean = false,
    val pinHash: String = "",
    val addedLabels: List<Label> = emptyList(),
    val labels: List<Label> = emptyList(),
    val snackbarMessageResource: Int? = null,
    val editLabelsDialogOpen: Boolean = false,
    val changePinDialogOpen: Boolean = false,
    val newNote: Boolean = false,
    val titleLinkRanges: List<IntRange> = emptyList(),
    val contentLinkRanges: List<IntRange> = emptyList(),
    val clickedLink: String? = null,
    val linkConfirmDialogOpen: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val deleteConfirmDialogOpen: Boolean = false
)

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: INoteRepository,
    private val labelRepository: ILabelRepository,
    private val addEditResultState: AddEditResultState,
    private val savedStateHandle: SavedStateHandle,
    private val navManager: INavigationManager,
    private val undoableActionHolder: UndoableActionHolder
) : ViewModel() {

    private val dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
    private var noteId = savedStateHandle
        .get<String>(NavDestinationArgs.NOTE_ID_ARG)
        .let { if (it != null) UUID.fromString(it) else null }
    private val labelId = savedStateHandle
        .get<String>(NavDestinationArgs.LABEL_ID_ARG)
        .let { if (it != null) UUID.fromString(it) else null }
    private var modified: Boolean = false
    private var savedByUser: Boolean = false
    private val editHistory: EditHistory = EditHistory()
    private var selectedRange: TextRange? = null

    private val _labelsAsync = labelRepository.getLabelsStream()
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Label>>> { emit(AsyncResult.Error("")) }

    private val _uiState = MutableStateFlow(AddEditNoteUiState())
    val uiState: StateFlow<AddEditNoteUiState> = combine(
        _uiState, _labelsAsync
    ) { uiState, labelsAsync ->
        when(labelsAsync) {
            AsyncResult.Loading -> {
                uiState
            }
            is AsyncResult.Error -> {
                uiState.copy(snackbarMessageResource = R.string.addeditnote_labels_loading_error)
            }
            is AsyncResult.Success -> {
                uiState.copy(labels = labelsAsync.data)
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AddEditNoteUiState()
        )

    private val saveTimer = object: CountDownTimer(Long.MAX_VALUE, 1000) {
        var secondsUntilSave = 4
        var running = false

        fun reset() {
            secondsUntilSave = 4
            if (!running) {
                running = true
                this.start()
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            secondsUntilSave--
            if (secondsUntilSave == 0) {
                running = false
                saveNote()
                this.cancel()
            }
        }

        override fun onFinish() { }
    }

    init {
        if (noteId != null) {
            loadNote(noteId!!)
        }
        else {
            _uiState.update {
                it.copy(
                    newNote = true,
                    modificationDateString = dtf.format(LocalDateTime.now())
                )
            }
            if (labelId != null) {
                viewModelScope.launch {
                    val startingLabel = labelRepository.getLabel(labelId) ?: return@launch
                    val addedLabels = listOf(startingLabel)
                    _uiState.update { it.copy(addedLabels = addedLabels) }
                }
            }
        }
        viewModelScope.launch {
            labelRepository.synchronize()
                .onSuccess {
                    if (labelId == null) {
                        return@launch
                    }
                    val startingLabel = labelRepository.getLabel(labelId)
                    if (startingLabel == null) {
                        navManager.navigateBack()
                        return@launch
                    }
                    val addedLabels = listOf(startingLabel)
                    _uiState.update { it.copy(addedLabels = addedLabels) }
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveTimer.cancel()
    }

    private fun findLinkRanges(content: String): List<IntRange> {
        val linkRanges = mutableListOf<IntRange>()
        val matcher: Matcher = Patterns.WEB_URL.matcher(content)
        while (matcher.find()) {
            linkRanges.add(IntRange(matcher.start(1), matcher.end() - 1))
        }
        return linkRanges
    }

    private fun updateFullUiState(note: Note) {
        val titleLinkRanges = findLinkRanges(note.title)
        val contentLinkRanges = findLinkRanges(note.content)
        _uiState.update {
            it.copy(
                title = TextFieldValue(note.title),
                content = TextFieldValue(note.content),
                location = note.location,
                creationDate = note.creationDate,
                modificationDate = note.modificationDate,
                modificationDateString = dtf.format(note.modificationDate),
                pinned = note.pinned,
                pinHash = note.pinHash,
                addedLabels = note.labels,
                titleLinkRanges = titleLinkRanges,
                contentLinkRanges = contentLinkRanges
            )
        }
    }

    private fun loadNote(noteId: UUID) {
        viewModelScope.launch {
            noteRepository.getNote(noteId).let { note ->
                if (note == null)
                    return@launch
                updateFullUiState(note)
            }
            labelRepository.synchronize()
                .onFailure {
                    return@launch
                }
            noteRepository.synchronizeSingle(noteId)
                .onSuccess {
                    noteRepository.getNote(noteId).let { note ->
                        if (note == null)
                            return@launch
                        if (note.modificationDate == _uiState.value.modificationDate)
                            return@launch
                        updateFullUiState(note)
                    }
                }
                .onFailure {
                    addEditResultState.set(AddEditResults.DELETED_AFTER_SYNC)
                    navManager.navigateBack()
                }
        }
    }

    fun finishEditing() {
        savedByUser = true
        if (_uiState.value.title.text.isEmpty() and _uiState.value.content.text.isEmpty()) {
            if (noteId == null) {
                addEditResultState.set(AddEditResults.DISCARDED)
                return
            }
            viewModelScope.launch {
                noteRepository.deleteNote(noteId!!)
            }
            addEditResultState.set(AddEditResults.DISCARDED)
            return
        }

        if (!modified)
            return

        saveNote()
        addEditResultState.set(AddEditResults.SAVED)
        undoableActionHolder.clear()
    }

    private fun saveNote() {
        viewModelScope.launch {
            if (noteId == null) {
                createNewNote()
            }
            else {
                updateExistingNote()
            }
        }
        _uiState.update {
            it.copy(
                modificationDate = LocalDateTime.now(),
                modificationDateString = dtf.format(it.modificationDate)
            )
        }
    }

    private fun createNewNote() {
        viewModelScope.launch {
            noteId = noteRepository.createNote(
                title = _uiState.value.title.text,
                content = _uiState.value.content.text,
                labels = _uiState.value.addedLabels,
                location = _uiState.value.location,
                pinned = _uiState.value.pinned,
                pinHash = _uiState.value.pinHash
            )
        }
    }

    private fun updateExistingNote() {
        if (noteId == null) {
            throw RuntimeException("AddEditNoteViewModel.updateExistingNote(): cannot update nonexistent note")
        }
        viewModelScope.launch {
            noteRepository.updateNote(
                noteId = noteId!!,
                title = _uiState.value.title.text,
                content = _uiState.value.content.text,
                labels = _uiState.value.addedLabels,
                location = _uiState.value.location,
                pinned = _uiState.value.pinned,
                pinHash = _uiState.value.pinHash
            )
        }
    }

    private fun getNewLinkRanges(
        linkRanges: List<IntRange>,
        change: Int,
        idx: Int
    ): List<IntRange> {
        if (linkRanges.isEmpty())
            return linkRanges

        val newLinkRanges: MutableList<IntRange> = mutableListOf()
        linkRanges.forEach { range ->
            val newRange = if (idx >= range.first && idx <= range.last) {
                IntRange(range.first, range.last + change)
            } else if (idx <= range.first) {
                IntRange(range.first + change, range.last + change)
            } else {
                range
            }
            if (!newRange.isEmpty())
                newLinkRanges.add(newRange)
        }
        return newLinkRanges
    }

    private fun getClickedLink(linkRanges: List<IntRange>, pos: Int, text: String): String? {
        val linkRange: IntRange? = linkRanges.find { pos in it }
        var clickedLink = if (linkRange != null) text.substring(linkRange) else null
        if (clickedLink != null && clickedLink.take(8) != "https://" && clickedLink.take(7) != "http://") {
            clickedLink = "https://$clickedLink"
        }
        return clickedLink
    }

    private fun saveSelectedRangeEditToHistory(
        oldText: String,
        newText: String,
        newSelection: TextRange,
        location: EditHistoryLocation
    ) {
        if (selectedRange == null)
            return

        // Delete the selected text
        editHistory.delete(
            pos = selectedRange!!.max - 1,
            text = oldText.substring(
                startIndex = selectedRange!!.min,
                endIndex = selectedRange!!.max
            ).reversed(),
            location = location
        )
        val change = newText.length - oldText.length
        if (abs(change) != selectedRange!!.length || change >= 0) {
            // Selected text was replaced with new text, add itt in place
            editHistory.insert(
                pos = selectedRange!!.min,
                text = newText.substring(
                    startIndex = selectedRange!!.min,
                    endIndex = newSelection.start
                ),
                location = location
            )
        }
    }

    private fun saveEditToHistory(
        oldText: String,
        newText: String,
        newSelection: TextRange,
        location: EditHistoryLocation
    ) {
        val changeLength = abs(newText.length - oldText.length)
        if (newText.length > oldText.length) {
            editHistory.insert(
                pos = newSelection.start - changeLength,
                text = newText.substring(
                    startIndex = newSelection.start - changeLength,
                    endIndex = newSelection.start
                ),
                location = location
            )
        } else {
            editHistory.delete(
                pos = newSelection.start,
                text = oldText.substring(
                    startIndex = newSelection.start,
                    endIndex = newSelection.start + changeLength
                ),
                location = location
            )
        }
    }

    fun updateTitle(newTitle: TextFieldValue, isUndoRedo: Boolean = false) {
        var titleLinkRanges = _uiState.value.titleLinkRanges
        val oldTitle = _uiState.value.title

        if (oldTitle.text.length != newTitle.text.length) {
            modified = true
            saveTimer.reset()

            val changeSize = abs(newTitle.text.length - oldTitle.text.length)
            titleLinkRanges = getNewLinkRanges(
                linkRanges = titleLinkRanges,
                change = newTitle.text.length - oldTitle.text.length,
                idx = newTitle.selection.start - changeSize
            )

            if (!isUndoRedo) {
                if (selectedRange != null) {
                    saveSelectedRangeEditToHistory(
                        oldText = oldTitle.text,
                        newText = newTitle.text,
                        newSelection = newTitle.selection,
                        location = EditHistoryLocation.Title
                    )
                } else {
                    saveEditToHistory(
                        oldText = oldTitle.text,
                        newText = newTitle.text,
                        newSelection = newTitle.selection,
                        location = EditHistoryLocation.Title
                    )
                }
                selectedRange = null
            }
        } else if (!isUndoRedo && (oldTitle.selection.start != newTitle.selection.start
                    || oldTitle.selection.end != newTitle.selection.end)) {
            editHistory.cursorMoved()
            selectedRange = if (newTitle.selection.length > 0) newTitle.selection else null
        }
        val clickedLink = getClickedLink(
            linkRanges = titleLinkRanges,
            pos = newTitle.selection.start,
            text = newTitle.text
        )
        _uiState.update {
            it.copy(
                title = newTitle,
                clickedLink = clickedLink,
                titleLinkRanges = titleLinkRanges,
                canUndo = editHistory.canUndo(),
                canRedo = editHistory.canRedo()
            )
        }
    }

    fun updateContent(newContent: TextFieldValue, isUndoRedo: Boolean = false) {
        var contentLinkRanges = _uiState.value.contentLinkRanges
        val oldContent = _uiState.value.content

        if (oldContent.text.length != newContent.text.length) {
            modified = true
            saveTimer.reset()

            val changeSize = abs(newContent.text.length - oldContent.text.length)
            contentLinkRanges = getNewLinkRanges(
                linkRanges = contentLinkRanges,
                change = newContent.text.length - oldContent.text.length,
                idx = newContent.selection.start - changeSize
            )

            if (!isUndoRedo) {
                if (selectedRange != null) {
                    saveSelectedRangeEditToHistory(
                        oldText = oldContent.text,
                        newText = newContent.text,
                        newSelection = newContent.selection,
                        location = EditHistoryLocation.Content
                    )
                } else {
                    saveEditToHistory(
                        oldText = oldContent.text,
                        newText = newContent.text,
                        newSelection = newContent.selection,
                        location = EditHistoryLocation.Content
                    )
                }
                selectedRange = null
            }
        } else if (!isUndoRedo && (oldContent.selection.start != newContent.selection.start
                    || oldContent.selection.end != newContent.selection.end)) {
            editHistory.cursorMoved()
            selectedRange = if (newContent.selection.length > 0) newContent.selection else null
        }
        val clickedLink = getClickedLink(
            linkRanges = contentLinkRanges,
            pos = newContent.selection.start,
            text = newContent.text
        )
        _uiState.update {
            it.copy(
                content = newContent,
                clickedLink = clickedLink,
                contentLinkRanges = contentLinkRanges,
                canUndo = editHistory.canUndo(),
                canRedo = editHistory.canRedo()
            )
        }
    }

    fun archiveNote() {
        savedByUser = true
        if (noteId == null && _uiState.value.title.text.isEmpty() && _uiState.value.content.text.isEmpty()) {
            addEditResultState.set(AddEditResults.DISCARDED)
            return
        }
        else {
            viewModelScope.launch {
                _uiState.update { it.copy(location = NoteLocation.ARCHIVE) }
                runBlocking { saveNote() }
                val noteMoveSnapshot = NoteMoveSnapshot(
                    id = noteId!!,
                    location = NoteLocation.NOTES,
                    pinned = uiState.value.pinned,
                    pinHash = uiState.value.pinHash
                )
                undoableActionHolder.set(UndoableAction.NoteMove(listOf(noteMoveSnapshot)))
            }
        }
        addEditResultState.set(AddEditResults.ARCHIVED)
    }

    fun unArchiveNote() {
        savedByUser = true
        if (noteId == null) {
            throw RuntimeException("AddEditNoteViewModel.unArchiveNote(): cannot unarchive nonexistent note")
        }
        viewModelScope.launch {
            _uiState.update { it.copy(location = NoteLocation.NOTES) }
            runBlocking { saveNote() }
            val noteMoveSnapshot = NoteMoveSnapshot(
                id = noteId!!,
                location = NoteLocation.ARCHIVE,
                pinned = uiState.value.pinned,
                pinHash = uiState.value.pinHash
            )
            undoableActionHolder.set(UndoableAction.NoteMove(listOf(noteMoveSnapshot)))
        }
        addEditResultState.set(AddEditResults.UNARCHIVED)
    }

    fun trashNote() {
        savedByUser = true
        if (noteId == null) {
            addEditResultState.set(AddEditResults.DISCARDED)
            return
        }
        viewModelScope.launch {
            val oldLocation = uiState.value.location
            _uiState.update {
                it.copy(
                    location = NoteLocation.TRASH,
                    pinned = false,
                    pinHash = ""
                )
            }
            runBlocking { saveNote() }
            val noteMoveSnapshot = NoteMoveSnapshot(
                id = noteId!!,
                location = oldLocation,
                pinned = false,
                pinHash = ""
            )
            undoableActionHolder.set(UndoableAction.NoteMove(listOf(noteMoveSnapshot)))
        }
        addEditResultState.set(AddEditResults.TRASHED)
    }

    fun deleteNote() {
        savedByUser = true
        if (noteId == null) {
            throw RuntimeException("AddEditNoteViewModel.deleteNote(): cannot delete nonexistent note")
        }
        viewModelScope.launch {
            noteRepository.deleteNote(noteId!!)
        }
        val note = Note(
            id = noteId!!,
            title = uiState.value.title.text,
            content = uiState.value.content.text,
            labels = uiState.value.addedLabels,
            location = uiState.value.location,
            pinned = uiState.value.pinned,
            pinHash = uiState.value.pinHash,
            deleted = false,
            creationDate = uiState.value.creationDate,
            modificationDate = uiState.value.modificationDate
        )
        undoableActionHolder.set(UndoableAction.NoteDelete(listOf(note)))
        addEditResultState.set(AddEditResults.DELETED)
    }

    fun navigateBack() {
        navManager.navigateBack()
    }

    fun showEditLabelsDialog() {
        _uiState.update { it.copy(editLabelsDialogOpen = true) }
    }

    fun hideEditLabelsDialog() {
        _uiState.update { it.copy(editLabelsDialogOpen = false) }
    }

    fun addRemoveLabel(label: Label, added: Boolean) {
        modified = true
        _uiState.update {
            val newLabels = it.addedLabels.toMutableList()
            if (added) {
                newLabels.add(label)
            }
            else {
                newLabels.remove(label)
            }
            it.copy(addedLabels = newLabels.sortedWith(compareBy(Label::title)))
        }
        saveTimer.reset()
    }

    fun moveCursor(textRange: TextRange) {
        _uiState.update {
            val content = it.content.copy(
                selection = textRange
            )
            it.copy(content = content)
        }
    }

    fun onPause() {
        /*
         * The purpose of this function is to save the note when the user closes the app while
         * editing. Ideally this function would get called whenever the ON_DESTROY event is
         * triggered, but I found out that it doesn't work reliably. Saving when the ON_PAUSE
         * event is triggered does work, but the downside is that ON_PAUSE also gets triggered if
         * the user simply navigates back to the previous screen, and in that case the note has
         * already been saved/deleted/whatever. So there needs to be a check for that.
         */
        if (savedByUser)
            return

        if (_uiState.value.title.text.isNotEmpty() || _uiState.value.content.text.isNotEmpty()) {
            saveNote()
        } else if (noteId != null) {
            viewModelScope.launch {
                noteRepository.deleteNote(noteId!!)
            }
        }
    }

    fun changePinned(pinned: Boolean) {
        modified = true
        _uiState.update { it.copy(pinned = pinned) }
        saveTimer.reset()
    }

    fun setPin(pin: String): Boolean {
        if (pin.length < 4)
            return false

        modified = true
        val pinHash = if (pin.isNotEmpty()) {
            BCrypt
                .withDefaults()
                .hashToString(12, pin.toCharArray())
        } else { "" }
        _uiState.update { it.copy(pinHash = pinHash) }
        closeChangePinDialog()
        saveTimer.reset()
        return true
    }

    fun removePin() {
        modified = true
        _uiState.update { it.copy(pinHash = "") }
        saveTimer.reset()
    }

    fun openChangePinDialog() {
        _uiState.update { it.copy(changePinDialogOpen = true) }
    }

    fun closeChangePinDialog() {
        _uiState.update { it.copy(changePinDialogOpen = false) }
    }

    fun openLinkConfirmDialog() {
        _uiState.update { it.copy(linkConfirmDialogOpen = true) }
    }

    fun closeLinkConfirmDialog() {
        _uiState.update { it.copy(linkConfirmDialogOpen = false) }
    }

    private fun getUndoTFV(entry: EditHistoryEntry, textFieldValue: TextFieldValue): TextFieldValue {
        val textToUndo: String
        val pos: Int
        val text: String
        val selection: TextRange
        if (entry.type == EditHistoryType.Delete) {
            textToUndo = entry.text.reversed()
            pos = entry.startIdx - (textToUndo.length - 1)
            text = StringBuilder(textFieldValue.text).insert(pos, textToUndo).toString()
            selection = TextRange(pos + textToUndo.length)
        } else {
            textToUndo = entry.text
            pos = entry.startIdx
            text = StringBuilder(textFieldValue.text)
                .removeRange(pos, pos + textToUndo.length)
                .toString()
            selection = TextRange(pos)
        }
        return TextFieldValue(
            text = text,
            selection = selection
        )
    }

    private fun getRedoTFV(entry: EditHistoryEntry, textFieldValue: TextFieldValue): TextFieldValue {
        val text: String
        val selection: TextRange
        if (entry.type == EditHistoryType.Insert) {
            text = StringBuilder(textFieldValue.text).insert(entry.startIdx, entry.text).toString()
            selection = TextRange(entry.startIdx + entry.text.length)
        } else {
            text = StringBuilder(textFieldValue.text)
                .removeRange(entry.startIdx - (entry.text.length - 1), entry.startIdx + 1)
                .toString()
            selection = TextRange(entry.startIdx - (entry.text.length - 1))
        }
        return TextFieldValue(
            text = text,
            selection = selection
        )
    }

    fun undo() {
        val entry = editHistory.undo() ?: return
        if (entry.location == EditHistoryLocation.Title) {
            val newTitle = getUndoTFV(entry, _uiState.value.title)
            updateTitle(newTitle, isUndoRedo = true)
        } else {
            val newContent = getUndoTFV(entry, _uiState.value.content)
            updateContent(newContent, isUndoRedo = true)
        }
    }

    fun redo() {
        val entry = editHistory.redo() ?: return
        if (entry.location == EditHistoryLocation.Title) {
            val newTitle = getRedoTFV(entry, _uiState.value.title)
            updateTitle(newTitle, isUndoRedo = true)
        } else {
            val newContent = getRedoTFV(entry, _uiState.value.content)
            updateContent(newContent, isUndoRedo = true)
        }
    }

    fun openDeleteConfirmDialog() {
        _uiState.update { it.copy(deleteConfirmDialogOpen = true) }
    }

    fun closeDeleteConfirmDialog() {
        _uiState.update { it.copy(deleteConfirmDialogOpen = false) }
    }

    fun confirmDelete() {
        closeDeleteConfirmDialog()
        deleteNote()
        navManager.navigateBack()
    }
}