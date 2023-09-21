package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.data.NoteRepository
import com.onandor.notemanager.utils.AsyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashUiState(
    val notes: List<Note> = listOf()
)

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _notesAsync = noteRepository.getNotesStream(NoteLocation.TRASH)
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Note>>> { emit(AsyncResult.Error("Error while loading notes.")) } // TODO: resource

    val uiState: StateFlow<TrashUiState> = _notesAsync.map { notesAsync ->
        when(notesAsync) {
            AsyncResult.Loading -> {
                // TODO
                TrashUiState()
            }
            is AsyncResult.Error -> {
                // TODO
                TrashUiState()
            }
            is AsyncResult.Success -> {
                TrashUiState(notes = notesAsync.data)
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrashUiState()
        )

    fun emptyTrash() {
        viewModelScope.launch {
            noteRepository.emptyTrash()
            // TODO: notification
        }
    }
}