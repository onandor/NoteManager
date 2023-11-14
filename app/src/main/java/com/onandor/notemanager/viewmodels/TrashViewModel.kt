package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.R
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.data.NoteRepository
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import com.onandor.notemanager.utils.AddEditResultState
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
    val loading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val selectedNotes: List<Note> = emptyList(),
    val confirmationDialogOpen: Boolean = false,
    val addEditSnackbarResource: Int = 0,
    val snackbarResource: Int = 0
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
                uiState.copy(addEditSnackbarResource = addEditResult.resource)
            }
            is AsyncResult.Error -> {
                // TODO
                uiState.copy(
                    loading = false,
                    addEditSnackbarResource = addEditResult.resource
                )
            }
            is AsyncResult.Success -> {
                uiState.copy(
                    loading = false,
                    notes = notesAsync.data,
                    addEditSnackbarResource = addEditResult.resource
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrashUiState()
        )

    private fun emptyTrash() {
        viewModelScope.launch {
            noteRepository.emptyTrash()
            _uiState.update { it.copy(snackbarResource = R.string.trash_snackbar_trash_emptied) }
        }
    }

    fun dialogConfirmed() {
        if (_uiState.value.selectedNotes.isEmpty())
            emptyTrash()
        else
            deleteSelectedNotes()
        closeConfirmationDialog()
    }

    fun addEditResultSnackbarShown() {
        addEditResultState.clear()
    }

    fun snackbarShown() {
        _uiState.update { it.copy(snackbarResource = 0) }
    }

    fun noteClick(note: Note) {
        if (_uiState.value.selectedNotes.isNotEmpty())
            noteLongClick(note)
        else
            navManager.navigateTo(NavActions.editNote(note.id.toString()))
    }

    fun noteLongClick(note: Note) {
        _uiState.update {
            val newSelectedNotes = it.selectedNotes.toMutableList()
            if (it.selectedNotes.contains(note)) {
                newSelectedNotes.remove(note)
            } else {
                newSelectedNotes.add(note)
            }
            it.copy(selectedNotes = newSelectedNotes)
        }
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

    fun clearSelection() {
        _uiState.update { it.copy(selectedNotes = emptyList()) }
    }

    fun restoreSelectedNotes() {
        viewModelScope.launch {
            _uiState.value.selectedNotes.forEach { note ->
                noteRepository.updateNoteLocation(note.id, NoteLocation.NOTES)
            }
            val single = _uiState.value.selectedNotes.size == 1
            clearSelection()
            val resource = if (single)
                R.string.snackbar_selection_note_restored
            else
                R.string.snackbar_selection_notes_restored
            _uiState.update { it.copy(snackbarResource = resource) }
        }
    }

    private fun deleteSelectedNotes() {
        viewModelScope.launch {
            _uiState.value.selectedNotes.forEach { note ->
                noteRepository.deleteNote(note.id)
            }
            val single = _uiState.value.selectedNotes.size == 1
            clearSelection()
            val resource = if (single)
                R.string.snackbar_selection_note_deleted
            else
                R.string.snackbar_selection_notes_deleted
            _uiState.update { it.copy(snackbarResource = resource) }
        }
    }
}