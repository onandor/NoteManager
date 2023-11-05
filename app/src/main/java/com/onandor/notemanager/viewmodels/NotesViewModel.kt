package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.R
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import com.onandor.notemanager.ui.components.NoteListState
import com.onandor.notemanager.utils.AddEditResult
import com.onandor.notemanager.utils.AddEditResultState
import com.onandor.notemanager.utils.AddEditResults
import com.onandor.notemanager.utils.AsyncResult
import com.onandor.notemanager.utils.NoteComparison
import com.onandor.notemanager.utils.NoteComparisonField
import com.onandor.notemanager.utils.NoteSorting
import com.onandor.notemanager.utils.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotesUiState(
    val loading: Boolean = true,
    val notes: List<Note> = emptyList(),
    val selectedNotes: List<Note> = emptyList(),
    val noteListState: NoteListState = NoteListState(),
    val addEditSnackbarResource: Int = 0,
    val selectionSnackbarResource: Int = 0
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: INoteRepository,
    private val addEditResultState: AddEditResultState,
    private val navManager: INavigationManager,
    private val settings: ISettings
) : ViewModel() {

    private val noteListState = combine(
        settings.observeBoolean(SettingsKeys.NOTE_LIST_COLLAPSED_VIEW, false),
        settings.observeInt(SettingsKeys.NOTE_LIST_SORT_BY),
        settings.observeInt(SettingsKeys.NOTE_LIST_ORDER)
    ) { collapsed, compareByInt, orderInt ->
        val sorting = if (compareByInt < 0 || orderInt < 0) {
            NoteSorting(NoteComparisonField.ModificationDate, Order.Descending)
        }
        else {
            NoteSorting(
                compareBy = NoteComparisonField.fromInt(compareByInt),
                order = Order.fromInt(orderInt)
            )
        }
        NoteListState(
            collapsed = collapsed,
            sorting = sorting
        )
    }

    private val _notesAsync = noteRepository.getNotesStream(NoteLocation.NOTES)
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Note>>> { emit(AsyncResult.Error("Error while loading notes.")) } // TODO: resource

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = combine(
        _uiState, _notesAsync, addEditResultState.result, noteListState
    ) { uiState, notesAsync, addEditResult, noteListState ->
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
                val sortedNotes = notesAsync.data.sortedWith(NoteComparison.comparators[noteListState.sorting]!!)
                uiState.copy(
                    loading = false,
                    notes = sortedNotes,
                    addEditSnackbarResource = addEditResult.resource,
                    noteListState = noteListState
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotesUiState()
        )

    fun addEditResultSnackbarShown() {
        addEditResultState.clear()
    }

    fun selectionSnackbarShown() {
        _uiState.update { it.copy(selectionSnackbarResource = 0) }
    }

    fun addNote() {
        navManager.navigateTo(NavActions.addEditNote())
    }

    fun noteClick(note: Note) {
        if (_uiState.value.selectedNotes.isNotEmpty())
            noteLongClick(note)
        else
            navManager.navigateTo(NavActions.addEditNote(note.id.toString()))
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

    fun changeSorting(sorting: NoteSorting) {
        viewModelScope.launch {
            settings.save(SettingsKeys.NOTE_LIST_SORT_BY, sorting.compareBy.value)
            settings.save(SettingsKeys.NOTE_LIST_ORDER, sorting.order.value)
        }
    }

    fun toggleNoteListCollapsedView() {
        viewModelScope.launch {
            settings.save(SettingsKeys.NOTE_LIST_COLLAPSED_VIEW, !noteListState.first().collapsed)
        }
    }

    fun showSearch() {
        navManager.navigateTo(NavActions.search())
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedNotes = emptyList()) }
    }

    fun moveSelectedNotes(location: NoteLocation) {
        viewModelScope.launch {
            _uiState.value.selectedNotes.forEach { note ->
                noteRepository.updateNoteLocation(note.id, location)
            }
            val single = _uiState.value.selectedNotes.size == 1
            clearSelection()
            val resource = when(location) {
                NoteLocation.ARCHIVE -> {
                    if (single) R.string.snackbar_selection_note_archived
                    else R.string.snackbar_selection_notes_archived
                }
                NoteLocation.TRASH -> {
                    if (single) R.string.snackbar_selection_note_trashed
                    else R.string.snackbar_selection_notes_trashed
                }
                else -> 0
            }
            _uiState.update {
                it.copy(selectionSnackbarResource = resource)
            }
        }
    }
}