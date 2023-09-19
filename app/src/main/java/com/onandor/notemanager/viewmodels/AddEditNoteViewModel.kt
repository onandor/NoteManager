package com.onandor.notemanager.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.NMDestinationsArgs
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.NoteLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import java.time.LocalDateTime
import javax.inject.Inject

data class AddEditNoteUiState(
    val title: String = "",
    val content: String = ""
)

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: INoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: String? = savedStateHandle[NMDestinationsArgs.NOTE_ID_ARG]

    private val _uiState = MutableStateFlow(AddEditNoteUiState())
    val uiState: StateFlow<AddEditNoteUiState> = _uiState.asStateFlow()

    init {
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    private fun loadNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.getNote(noteId).let { note ->
                if (note == null)
                    return@launch

                _uiState.update {
                    it.copy(
                        title = note.title,
                        content = note.content
                    )
                }
            }
        }
    }

    fun saveNote() {
        viewModelScope.launch {
            if (noteId == null) {
                createNewNote()
            }
            else {
                updateExistingNote()
            }
        }
    }

    private fun createNewNote() {
        viewModelScope.launch {
            noteRepository.createNote(
                title = _uiState.value.title,
                content = _uiState.value.content,
                labels = listOf(),
                location = NoteLocation.NOTES,
                creationDate = LocalDateTime.now(),
                modificationDate = LocalDateTime.now()
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
                content = _uiState.value.content,
                modificationDate = LocalDateTime.now()
            )
        }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update {
            it.copy(
                title = newTitle
            )
        }
    }

    fun updateContent(newContent: String) {
        _uiState.update {
            it.copy(
                content = newContent
            )
        }
    }
}