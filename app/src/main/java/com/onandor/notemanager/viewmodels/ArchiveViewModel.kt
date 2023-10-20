package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import com.onandor.notemanager.utils.AddEditResult
import com.onandor.notemanager.utils.AddEditResultState
import com.onandor.notemanager.utils.AddEditResults
import com.onandor.notemanager.utils.AsyncResult
import com.onandor.notemanager.utils.NoteComparison
import com.onandor.notemanager.utils.NoteComparisonField
import com.onandor.notemanager.utils.NoteSorting
import com.onandor.notemanager.utils.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchiveUiState(
    val notes: List<Note> = listOf(),
    val addEditResult: AddEditResult = AddEditResults.NONE,
    val sorting: NoteSorting = NoteSorting(NoteComparisonField.ModificationDate, Order.Descending)
)

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val noteRepository: INoteRepository,
    private val addEditResultState: AddEditResultState,
    private val navManager: INavigationManager,
    private val settings: ISettings
) : ViewModel() {

    private val sorting = combine(
        settings.observeInt(SettingsKeys.NOTE_LIST_SORT_BY),
        settings.observeInt(SettingsKeys.NOTE_LIST_ORDER)
    ) { compareByInt, orderInt ->
        if (compareByInt < 0 || orderInt < 0) {
            NoteSorting(NoteComparisonField.ModificationDate, Order.Descending)
        }
        else {
            NoteSorting(
                compareBy = NoteComparisonField.fromInt(compareByInt),
                order = Order.fromInt(orderInt)
            )
        }
    }

    private val _notesAsync = noteRepository.getNotesStream(NoteLocation.ARCHIVE)
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Note>>> { emit(AsyncResult.Error("Error while loading notes.")) } // TODO: resource

    val uiState: StateFlow<NotesUiState> = combine(
        _notesAsync, addEditResultState.result, sorting
    ) { notesAsync, addEditResult, sorting ->
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
                val sortedNotes = notesAsync.data.sortedWith(NoteComparison.comparators[sorting]!!)
                NotesUiState(
                    notes = sortedNotes,
                    addEditResult = addEditResult,
                    sorting = sorting
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

    fun noteClick(note: Note) {
        navManager.navigateTo(NavActions.addEditNote(note.id.toString()))
    }

    fun changeSorting(sorting: NoteSorting) {
        viewModelScope.launch {
            settings.save(SettingsKeys.NOTE_LIST_SORT_BY, sorting.compareBy.value)
            settings.save(SettingsKeys.NOTE_LIST_ORDER, sorting.order.value)
        }
    }
}