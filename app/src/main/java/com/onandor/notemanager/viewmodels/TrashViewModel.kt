package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.data.NoteRepository
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import com.onandor.notemanager.utils.AddEditResult
import com.onandor.notemanager.utils.AddEditResultState
import com.onandor.notemanager.utils.AddEditResults
import com.onandor.notemanager.utils.AsyncResult
import com.onandor.notemanager.utils.NoteComparison
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashUiState(
    val notes: List<Note> = listOf(),
    val addEditResult: AddEditResult = AddEditResults.NONE
)

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val addEditResultState: AddEditResultState,
    private val navManager: INavigationManager
) : ViewModel() {

    private val _notesAsync = noteRepository.getNotesStream(NoteLocation.TRASH)
        .map { it.sortedWith(NoteComparison.comparators[NoteComparison.modificationDateDescending]!!) }
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Note>>> { emit(AsyncResult.Error("Error while loading notes.")) } // TODO: resource

    val uiState: StateFlow<NotesUiState> = combine(
        _notesAsync, addEditResultState.result
    ) { notesAsync, addEditResult ->
        when(notesAsync) {
            AsyncResult.Loading -> {
                // TODO
                NotesUiState(addEditResult = addEditResult)
            }
            is AsyncResult.Error -> {
                // TODO
                NotesUiState(addEditResult = addEditResult)
            }
            is AsyncResult.Success -> {
                NotesUiState(notes = notesAsync.data, addEditResult = addEditResult)
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotesUiState()
        )

    fun emptyTrash() {
        viewModelScope.launch {
            noteRepository.emptyTrash()
            // TODO: notification
        }
    }
    fun addEditResultSnackbarShown() {
        addEditResultState.clear()
    }

    fun noteClick(note: Note) {
        navManager.navigateTo(NavActions.addEditNote(note.id.toString()))
    }

    fun navigateBack() {
        navManager.navigateBack()
    }
}