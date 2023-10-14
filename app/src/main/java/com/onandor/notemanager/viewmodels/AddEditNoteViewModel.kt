package com.onandor.notemanager.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavDestinationArgs
import com.onandor.notemanager.utils.AddEditResultState
import com.onandor.notemanager.utils.AddEditResults
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

data class AddEditNoteUiState(
    val title: String = "",
    val content: String = "",
    val noteLocation: NoteLocation = NoteLocation.NOTES
)

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: INoteRepository,
    private val addEditResultState: AddEditResultState,
    private val savedStateHandle: SavedStateHandle,
    private val navManager: INavigationManager
) : ViewModel() {

    private val _noteId: String = savedStateHandle[NavDestinationArgs.NOTE_ID_ARG] ?: ""
    private val noteId: UUID? = if (_noteId.isNotEmpty()) UUID.fromString(_noteId) else null
    private var modified: Boolean = false

    private val _uiState = MutableStateFlow(AddEditNoteUiState())
    val uiState: StateFlow<AddEditNoteUiState> = _uiState.asStateFlow()

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
                        noteLocation = note.location
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
                labels = listOf(),
                location = NoteLocation.NOTES
            )
        }
    }

    private fun updateExistingNote() {
        if (noteId == null) {
            throw RuntimeException("AddEditNoteViewModel.updateExistingNote(): cannot update nonexistent note")
        }
        viewModelScope.launch {
            noteRepository.updateNoteTitleAndContent(
                noteId = noteId,
                title = _uiState.value.title,
                content = _uiState.value.content
            )
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
}