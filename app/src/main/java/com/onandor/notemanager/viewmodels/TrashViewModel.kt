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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashUiState(
    val notes: List<Note> = listOf(),
    val addEditResult: AddEditResult = AddEditResults.NONE,
    val confirmationDialogOpen: Boolean = false
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

    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = combine(
        _uiState, _notesAsync, addEditResultState.result
    ) { uiState, notesAsync, addEditResult ->
        when(notesAsync) {
            AsyncResult.Loading -> {
                // TODO
                uiState.copy(addEditResult = addEditResult)
            }
            is AsyncResult.Error -> {
                // TODO
                uiState.copy(addEditResult = addEditResult)
            }
            is AsyncResult.Success -> {
                uiState.copy(
                    notes = notesAsync.data,
                    addEditResult = addEditResult
                )
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
        closeConfirmationDialog()
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

    fun openConfirmationDialog() {
        _uiState.update { it.copy(confirmationDialogOpen = true) }
    }

    fun closeConfirmationDialog() {
        _uiState.update { it.copy(confirmationDialogOpen = false) }
    }
}