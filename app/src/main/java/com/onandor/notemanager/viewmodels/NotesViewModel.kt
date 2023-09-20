package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.utils.AsyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class NoteListUiState(
    val notes: List<Note> = listOf()
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: INoteRepository
) : ViewModel() {

    private val _notesAsync = noteRepository.getNotesStream()
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Note>>> { emit(AsyncResult.Error("Error while loading notes.")) } // TODO: resource

    val uiState: StateFlow<NoteListUiState> = _notesAsync.map { notesAsync ->
        when(notesAsync) {
            AsyncResult.Loading -> {
                // TODO
                NoteListUiState()
            }
            is AsyncResult.Error -> {
                // TODO
                NoteListUiState()
            }
            is AsyncResult.Success -> {
                NoteListUiState(notes = notesAsync.data)
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NoteListUiState()
        )
}