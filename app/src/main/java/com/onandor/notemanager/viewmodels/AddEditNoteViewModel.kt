package com.onandor.notemanager.viewmodels

import android.os.CountDownTimer
import android.util.Patterns
import android.util.Range
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.onandor.notemanager.R
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavDestinationArgs
import com.onandor.notemanager.utils.AddEditResultState
import com.onandor.notemanager.utils.AddEditResults
import com.onandor.notemanager.utils.AsyncResult
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
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID
import java.util.regex.Matcher
import javax.inject.Inject

data class AddEditNoteUiState(
    val title: TextFieldValue = TextFieldValue(""),
    val content: TextFieldValue = TextFieldValue(""),
    val location: NoteLocation = NoteLocation.NOTES,
    val modificationDate: String = "",
    val pinned: Boolean = false,
    val pinHash: String = "",
    val addedLabels: List<Label> = emptyList(),
    val labels: List<Label> = emptyList(),
    val snackbarMessageResource: Int? = null,
    val editLabelsDialogOpen: Boolean = false,
    val changePinDialogOpen: Boolean = false,
    val newNote: Boolean = false,
    val linkRanges: List<Range<Int>> = emptyList()
)

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: INoteRepository,
    private val labelRepository: ILabelRepository,
    private val addEditResultState: AddEditResultState,
    private val savedStateHandle: SavedStateHandle,
    private val navManager: INavigationManager
) : ViewModel() {

    private val dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
    private val _noteId: String = savedStateHandle[NavDestinationArgs.NOTE_ID_ARG] ?: ""
    private var noteId: UUID? = if (_noteId.isNotEmpty()) UUID.fromString(_noteId) else null
    private var modified: Boolean = false
    private var savedByUser: Boolean = false

    private val _labelsAsync = labelRepository.getLabelsStream()
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Label>>> { emit(AsyncResult.Error("Error while loading labels.")) } // TODO: resource

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
                    modificationDate = dtf.format(LocalDateTime.now())
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        saveTimer.cancel()
    }

    private fun findLinkRanges(content: String): List<Range<Int>> {
        val linkRanges = mutableListOf<Range<Int>>()
        val matcher: Matcher = Patterns.WEB_URL.matcher(content)
        while (matcher.find()) {
            linkRanges.add(Range(matcher.start(1), matcher.end()))
        }
        return linkRanges
    }

    private fun loadNote(noteId: UUID) {
        viewModelScope.launch {
            noteRepository.getNote(noteId).let { note ->
                if (note == null)
                    return@launch

                val linkRanges = findLinkRanges(note.content)
                _uiState.update {
                    it.copy(
                        title = TextFieldValue(note.title),
                        content = TextFieldValue(note.content),
                        location = note.location,
                        modificationDate = dtf.format(note.modificationDate),
                        pinned = note.pinned,
                        pinHash = note.pinHash,
                        addedLabels = note.labels,
                        linkRanges = linkRanges
                    )
                }
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
        _uiState.update { it.copy(modificationDate = dtf.format(LocalDateTime.now())) }
    }

    private fun createNewNote() {
        viewModelScope.launch {
            noteId = noteRepository.createNote(
                title = _uiState.value.title.text,
                content = _uiState.value.content.text,
                labels = _uiState.value.addedLabels,
                location = NoteLocation.NOTES,
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

    private fun createAndArchiveNewNote() {
        viewModelScope.launch {
            val noteId = noteRepository.createNote(
                title = _uiState.value.title.text,
                content = _uiState.value.content.text,
                labels = listOf(),
                location = NoteLocation.NOTES,
                pinned = _uiState.value.pinned,
                pinHash = _uiState.value.pinHash
            )
            noteRepository.updateNoteLocation(
                noteId = noteId,
                location = NoteLocation.ARCHIVE
            )
        }
    }

    fun updateTitle(newTitle: TextFieldValue) {
        if (_uiState.value.title.text != newTitle.text) {
            modified = true
            saveTimer.reset()
        }
        _uiState.update {
            it.copy(
                title = newTitle
            )
        }
    }

    fun updateContent(newContent: TextFieldValue) {
        if (_uiState.value.content.text != newContent.text) {
            modified = true
            saveTimer.reset()
        }
        _uiState.update {
            it.copy(
                content = newContent
            )
        }
    }

    fun archiveNote() {
        savedByUser = true
        if (noteId == null) {
            if (_uiState.value.title.text.isEmpty() and _uiState.value.content.text.isEmpty()) {
                addEditResultState.set(AddEditResults.DISCARDED)
                return
            }
            createAndArchiveNewNote()
        }
        else {
            viewModelScope.launch {
                noteRepository.updateNoteLocation(noteId!!, NoteLocation.ARCHIVE)
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
            noteRepository.updateNoteLocation(noteId!!, NoteLocation.NOTES)
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
            noteRepository.updateNotePinned(noteId!!, false)
            noteRepository.updateNotePinHash(noteId!!, "")
            noteRepository.updateNoteLocation(noteId!!, NoteLocation.TRASH)
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
        if (!savedByUser)
            saveNote()
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
}