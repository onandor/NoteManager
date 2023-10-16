package com.onandor.notemanager.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class AddEditNoteUiState(
    val title: String = "",
    val content: String = "",
    val location: NoteLocation = NoteLocation.NOTES,
    val modificationDate: LocalDateTime = LocalDateTime.now(),
    val labels: List<Label> = emptyList(),
    val remainingLabels: List<Label> = emptyList(),
    val snackbarMessageResource: Int? = null,
    val editLabelsDialogOpen: Boolean = false
)

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: INoteRepository,
    private val labelRepository: ILabelRepository,
    private val addEditResultState: AddEditResultState,
    private val savedStateHandle: SavedStateHandle,
    private val navManager: INavigationManager
) : ViewModel() {

    private val _noteId: String = savedStateHandle[NavDestinationArgs.NOTE_ID_ARG] ?: ""
    private val noteId: UUID? = if (_noteId.isNotEmpty()) UUID.fromString(_noteId) else null
    private var modified: Boolean = false

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
                val remainingLabels = labelsAsync.data.filterNot { label ->
                    uiState.labels.any { noteLabel -> noteLabel.id == label.id }
                }
                uiState.copy(remainingLabels = remainingLabels)
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AddEditNoteUiState()
        )

    init {
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    private fun loadNote(noteId: UUID) {
        viewModelScope.launch {
            noteRepository.getNote(noteId).let { note ->
                if (note == null)
                    return@launch

                _uiState.update {
                    it.copy(
                        title = note.title,
                        content = note.content,
                        location = note.location,
                        modificationDate = note.modificationDate,
                        labels = note.labels
                    )
                }
            }
        }
    }

    fun saveNote() {
        if (_uiState.value.title.isEmpty() and _uiState.value.content.isEmpty()) {
            if (noteId == null) {
                addEditResultState.set(AddEditResults.DISCARDED)
                return
            }
            viewModelScope.launch {
                noteRepository.deleteNote(noteId)
            }
            addEditResultState.set(AddEditResults.DISCARDED)
            return
        }

        if (!modified)
            return

        viewModelScope.launch {
            if (noteId == null) {
                createNewNote()
            }
            else {
                updateExistingNote()
            }
        }
        addEditResultState.set(AddEditResults.SAVED)
    }

    private fun createNewNote() {
        viewModelScope.launch {
            noteRepository.createNote(
                title = _uiState.value.title,
                content = _uiState.value.content,
                labels = _uiState.value.labels,
                location = NoteLocation.NOTES
            )
        }
    }

    private fun updateExistingNote() {
        if (noteId == null) {
            throw RuntimeException("AddEditNoteViewModel.updateExistingNote(): cannot update nonexistent note")
        }
        viewModelScope.launch {
            // TODO: do it in one action
            noteRepository.updateNoteTitleAndContent(
                noteId = noteId,
                title = _uiState.value.title,
                content = _uiState.value.content
            )
            noteRepository.updateNoteLabels(noteId, _uiState.value.labels)
        }
    }

    private fun createAndArchiveNewNote() {
        viewModelScope.launch {
            val noteId = noteRepository.createNote(
                title = _uiState.value.title,
                content = _uiState.value.content,
                labels = listOf(),
                location = NoteLocation.NOTES
            )
            noteRepository.updateNoteLocation(
                noteId = noteId,
                location = NoteLocation.ARCHIVE
            )
        }
    }

    fun updateTitle(newTitle: String) {
        modified = true
        _uiState.update {
            it.copy(
                title = newTitle
            )
        }
    }

    fun updateContent(newContent: String) {
        modified = true
        _uiState.update {
            it.copy(
                content = newContent
            )
        }
    }

    fun archiveNote() {
        if (noteId == null) {
            if (_uiState.value.title.isEmpty() and _uiState.value.content.isEmpty()) {
                addEditResultState.set(AddEditResults.DISCARDED)
                return
            }
            createAndArchiveNewNote()
        }
        else {
            viewModelScope.launch {
                noteRepository.updateNoteLocation(noteId, NoteLocation.ARCHIVE)
            }
        }
        addEditResultState.set(AddEditResults.ARCHIVED)
    }

    fun unArchiveNote() {
        if (noteId == null) {
            throw RuntimeException("AddEditNoteViewModel.unArchiveNote(): cannot unarchive nonexistent note")
        }
        viewModelScope.launch {
            noteRepository.updateNoteLocation(noteId, NoteLocation.NOTES)
        }
        addEditResultState.set(AddEditResults.UNARCHIVED)
    }

    fun trashNote() {
        if (noteId == null) {
            addEditResultState.set(AddEditResults.DISCARDED)
            return
        }
        viewModelScope.launch {
            noteRepository.updateNoteLocation(noteId, NoteLocation.TRASH)
        }
        addEditResultState.set(AddEditResults.TRASHED)
    }

    fun deleteNote() {
        if (noteId == null) {
            throw RuntimeException("AddEditNoteViewModel.deleteNote(): cannot delete nonexistent note")
        }
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
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

    fun addLabel(label: Label) {
        modified = true
        val newLabels = _uiState.value.labels.toMutableList()
        newLabels.add(label)
        _uiState.update { it.copy(labels = newLabels) }
    }

    fun removeLabel(label: Label) {
        modified = true
        val newLabels = _uiState.value.labels.toMutableList()
        newLabels.remove(label)
        _uiState.update { it.copy(labels = newLabels) }
    }
}