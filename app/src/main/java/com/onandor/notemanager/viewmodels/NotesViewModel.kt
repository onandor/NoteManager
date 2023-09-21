package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.utils.AsyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class NotesUiState(
    val notes: List<Note> = listOf()
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: INoteRepository
) : ViewModel() {

    private val _notesAsync = noteRepository.getNotesStream(NoteLocation.NOTES)
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Note>>> { emit(AsyncResult.Error("Error while loading notes.")) } // TODO: resource

    val uiState: StateFlow<NotesUiState> = _notesAsync.map { notesAsync ->
        when(notesAsync) {
            AsyncResult.Loading -> {
                // TODO
                NotesUiState()
            }
            is AsyncResult.Error -> {
                // TODO
                NotesUiState()
            }
            is AsyncResult.Success -> {
                NotesUiState(notes = notesAsync.data)
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotesUiState()
        )
}