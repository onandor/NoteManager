package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.onandor.notemanager.R
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import com.onandor.notemanager.utils.AddEditResultState
import com.onandor.notemanager.utils.AsyncResult
import com.onandor.notemanager.utils.NoteComparison
import com.onandor.notemanager.utils.undo.NoteMoveSnapshot
import com.onandor.notemanager.utils.undo.UndoableAction
import com.onandor.notemanager.utils.undo.UndoableActionHolder
import com.onandor.notemanager.utils.undo.createNoteMoveSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    val synchronizing: Boolean = false,
    val notes: List<Note> = emptyList(),
    val selectedNotes: List<Note> = emptyList(),
    val confirmationDialogOpen: Boolean = false,
    val addEditSnackbarResource: Int = 0,
    val snackbarResource: Int = 0,
    val showUndoableAddEditSnackbar: Boolean = false,
    val showUndoableSnackbar: Boolean = false,
    val syncToastResource: Int = 0
)
@HiltViewModel
class TrashViewModel @Inject constructor(
    private val noteRepository: INoteRepository,
    private val labelRepository: ILabelRepository,
    private val addEditResultState: AddEditResultState,
    private val navManager: INavigationManager,
    private val undoableActionHolder: UndoableActionHolder
) : ViewModel() {

    private val _notesAsync = noteRepository.getNotesStream(NoteLocation.TRASH)
        .map { it.sortedWith(NoteComparison.comparators[NoteComparison.modificationDateDescending]!!) }
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Note>>> { emit(AsyncResult.Error("")) }

    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = combine(
        _uiState, _notesAsync, addEditResultState.result
    ) { uiState, notesAsync, addEditResult ->
        when(notesAsync) {
            AsyncResult.Loading -> {
                uiState.copy(addEditSnackbarResource = addEditResult.resource)
            }
            is AsyncResult.Error -> {
                uiState.copy(
                    loading = false,
                    addEditSnackbarResource = addEditResult.resource
                )
            }
            is AsyncResult.Success -> {
                uiState.copy(
                    loading = false,
                    notes = notesAsync.data,
                    addEditSnackbarResource = addEditResult.resource,
                    showUndoableAddEditSnackbar = undoableActionHolder.action != null
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrashUiState()
        )

    init {
        viewModelScope.launch {
            labelRepository.synchronize()
            noteRepository.synchronize()
        }
    }

    private fun emptyTrash() {
        val noteSnapshots: List<Note> = uiState.value.notes.toList()
        viewModelScope.launch {
            noteRepository.emptyTrash()
            _uiState.update { it.copy(snackbarResource = R.string.trash_snackbar_trash_emptied, showUndoableSnackbar = true) }
        }
        undoableActionHolder.set(UndoableAction.NoteDelete(noteSnapshots))
    }

    fun dialogConfirmed() {
        if (uiState.value.selectedNotes.isEmpty())
            emptyTrash()
        else
            deleteSelectedNotes()
        closeConfirmationDialog()
    }

    fun addEditResultSnackbarShown() {
        addEditResultState.clear()
    }

    fun snackbarShown() {
        _uiState.update { it.copy(snackbarResource = 0, showUndoableSnackbar = false) }
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
            val noteMoveSnapshots: MutableList<NoteMoveSnapshot> = mutableListOf()
            _uiState.value.selectedNotes.forEach { note ->
                noteMoveSnapshots.add(createNoteMoveSnapshot(note))
                noteRepository.updateNoteLocation(note.id, NoteLocation.NOTES)
            }
            val single = _uiState.value.selectedNotes.size == 1
            clearSelection()
            val resource = if (single)
                R.string.snackbar_selection_note_restored
            else
                R.string.snackbar_selection_notes_restored
            _uiState.update { it.copy(snackbarResource = resource, showUndoableSnackbar = true) }
            undoableActionHolder.set(UndoableAction.NoteMove(noteMoveSnapshots))
        }
    }

    private fun deleteSelectedNotes() {
        viewModelScope.launch {
            val noteSnapshots: MutableList<Note> = mutableListOf()
            _uiState.value.selectedNotes.forEach { note ->
                noteSnapshots.add(note)
                noteRepository.deleteNote(note.id)
            }
            val single = _uiState.value.selectedNotes.size == 1
            clearSelection()
            val resource = if (single)
                R.string.snackbar_selection_note_deleted
            else
                R.string.snackbar_selection_notes_deleted
            _uiState.update { it.copy(snackbarResource = resource, showUndoableSnackbar = true) }
            undoableActionHolder.set(UndoableAction.NoteDelete(noteSnapshots))
        }
    }

    fun undoLastAction() {
        val action = undoableActionHolder.pop() ?: return
        when (action) {
            is UndoableAction.NoteMove -> {
                viewModelScope.launch {
                    action.noteMoveSnapshots.forEach { (noteId, location, _, _) ->
                        noteRepository.updateNoteLocation(noteId, location)
                    }
                }
            }
            is UndoableAction.NoteDelete -> {
                viewModelScope.launch {
                    action.notes.forEach { note ->
                        noteRepository.createNote(
                            title = note.title,
                            content = note.content,
                            labels = note.labels,
                            location = note.location,
                            pinned = note.pinned,
                            pinHash = note.pinHash,
                            creationDate = note.creationDate,
                            modificationDate = note.modificationDate
                        )
                    }
                }
            }
        }
    }

    fun clearLastUndoableAction() {
        undoableActionHolder.clear()
    }

    fun synchronize() {
        _uiState.update { it.copy(synchronizing = true) }
        viewModelScope.launch {
            labelRepository.synchronize()
                .onFailure { apiError ->
                    delay(200)
                    _uiState.update {
                        it.copy(
                            synchronizing = false,
                            syncToastResource = apiError.messageResource
                        )
                    }
                    return@launch
                }
            noteRepository.synchronize()
                .onFailure { apiError ->
                    _uiState.update { it.copy(syncToastResource = apiError.messageResource) }
                }
            delay(200)
            _uiState.update { it.copy(synchronizing = false) }
        }
    }

    fun syncToastShown() {
        _uiState.update { it.copy(syncToastResource = 0) }
    }
}