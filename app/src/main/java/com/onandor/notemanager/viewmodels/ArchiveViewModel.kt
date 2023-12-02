package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.github.michaelbull.result.onFailure
import com.onandor.notemanager.R
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import com.onandor.notemanager.ui.components.NoteListState
import com.onandor.notemanager.utils.AddEditResultState
import com.onandor.notemanager.utils.AsyncResult
import com.onandor.notemanager.utils.NoteComparison
import com.onandor.notemanager.utils.NoteComparisonField
import com.onandor.notemanager.utils.NoteSorting
import com.onandor.notemanager.utils.Order
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchiveUiState(
    val loading: Boolean = true,
    val synchronizing: Boolean = false,
    val notes: List<Note> = emptyList(),
    val selectedNotes: List<Note> = emptyList(),
    val noteListState: NoteListState = NoteListState(),
    val addEditSnackbarResource: Int = 0,
    val selectionSnackbarResource: Int = 0,
    val pinEntryDialogOpen: Boolean = false,
    val showUndoableAddEditSnackbar: Boolean = false,
    val showUndoableSelectionSnackbar: Boolean = false
)

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val noteRepository: INoteRepository,
    private val labelRepository: ILabelRepository,
    private val addEditResultState: AddEditResultState,
    private val navManager: INavigationManager,
    private val settings: ISettings,
    private val undoableActionHolder: UndoableActionHolder
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

    private val _notesAsync = noteRepository.getNotesStream(NoteLocation.ARCHIVE)
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Note>>> { emit(AsyncResult.Error("Error while loading notes.")) } // TODO: resource

    private var lockedNote: Note? = null

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = combine(
        _uiState, _notesAsync, addEditResultState.result, noteListState
    ) { uiState, notesAsync, addEditResult, noteListState ->
        when(notesAsync) {
            AsyncResult.Loading -> {
                uiState.copy(addEditSnackbarResource = addEditResult.resource)
            }
            is AsyncResult.Error -> {
                uiState.copy(
                    loading = false,
                    addEditSnackbarResource = addEditResult.resource,
                    selectionSnackbarResource = R.string.error_while_loading_notes
                )
            }
            is AsyncResult.Success -> {
                val sortedNotes = notesAsync.data
                    .sortedWith(NoteComparison.comparators[noteListState.sorting]!!)
                    .sortedWith(compareByDescending(Note::pinned))
                uiState.copy(
                    loading = false,
                    notes = sortedNotes,
                    addEditSnackbarResource = addEditResult.resource,
                    showUndoableAddEditSnackbar = undoableActionHolder.action != null,
                    noteListState = noteListState
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ArchiveUiState()
        )

    init {
        viewModelScope.launch {
            labelRepository.synchronize()
            noteRepository.synchronize()
        }
    }

    fun addEditResultSnackbarShown() {
        addEditResultState.clear()
    }

    fun selectionSnackbarShown() {
        _uiState.update { it.copy(selectionSnackbarResource = 0, showUndoableSelectionSnackbar = false) }
    }

    fun noteClick(note: Note) {
        if (_uiState.value.selectedNotes.isNotEmpty()) {
            noteLongClick(note)
        }
        else if (note.pinHash.isNotEmpty()) {
            lockedNote = note
            openPinEntryDialog()
        }
        else {
            navManager.navigateTo(NavActions.editNote(note.id.toString()))
        }
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

    fun navigateToSearch() {
        navManager.navigateTo(NavActions.search())
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedNotes = emptyList()) }
    }

    fun moveSelectedNotes(location: NoteLocation) {
        viewModelScope.launch {
            val noteMoveSnapshots: MutableList<NoteMoveSnapshot> = mutableListOf()
            _uiState.value.selectedNotes.forEach { note ->
                noteMoveSnapshots.add(createNoteMoveSnapshot(note))
                if (location == NoteLocation.TRASH) {
                    if (note.pinned)
                        noteRepository.updateNotePinned(note.id, false)
                    if (note.pinHash.isNotEmpty())
                        noteRepository.updateNotePinHash(note.id, "")
                }
                noteRepository.updateNoteLocation(note.id, location)
            }
            val single = _uiState.value.selectedNotes.size == 1
            clearSelection()
            val resource = when(location) {
                NoteLocation.NOTES -> {
                    if (single) R.string.snackbar_selection_note_unarchived
                    else R.string.snackbar_selection_notes_unarchived
                }
                NoteLocation.TRASH -> {
                    if (single) R.string.snackbar_selection_note_trashed
                    else R.string.snackbar_selection_notes_trashed
                }
                else -> 0
            }
            _uiState.update {
                it.copy(selectionSnackbarResource = resource, showUndoableSelectionSnackbar = true)
            }
            undoableActionHolder.set(UndoableAction.NoteMove(noteMoveSnapshots))
        }
    }

    fun changeSelectedNotesPinning(pinned: Boolean) {
        viewModelScope.launch {
            _uiState.value.selectedNotes.forEach { note ->
                noteRepository.updateNotePinned(note.id, pinned)
            }
            val single = _uiState.value.selectedNotes.size == 1
            clearSelection()
            val resource = when (pinned) {
                true -> {
                    if (single) R.string.snackbar_selection_note_pinned
                    else R.string.snackbar_selection_notes_pinned
                }
                false -> {
                    if (single) R.string.snackbar_selection_note_unpinned
                    else R.string.snackbar_selection_notes_unpinned
                }
            }
            _uiState.update {
                it.copy(selectionSnackbarResource = resource)
            }
        }
    }

    private fun openPinEntryDialog() {
        _uiState.update { it.copy(pinEntryDialogOpen = true) }
    }
    fun closePinEntryDialog() {
        _uiState.update { it.copy(pinEntryDialogOpen = false) }
        lockedNote = null
    }
    fun confirmPinEntry(pin: String): Boolean {
        if (lockedNote == null) {
            closePinEntryDialog()
            return true
        }
        val hashResult = BCrypt.verifyer().verify(pin.toCharArray(), lockedNote!!.pinHash)
        if (!hashResult.verified)
            return false
        _uiState.update { it.copy(pinEntryDialogOpen = false) }
        navManager.navigateTo(NavActions.editNote(lockedNote!!.id.toString()))
        return true
    }

    fun undoLastAction() {
        val action = undoableActionHolder.pop() ?: return
        when (action) {
            is UndoableAction.NoteMove -> {
                viewModelScope.launch {
                    action.noteMoveSnapshots.forEach { (noteId, location, pinned, pinHash) ->
                        noteRepository.updateNoteLocation(noteId, location)
                        if (pinned)
                            noteRepository.updateNotePinned(noteId, true)
                        if (pinHash.isNotEmpty())
                            noteRepository.updateNotePinHash(noteId, pinHash)
                    }
                }
            }
            /* It is not possible to delete notes here, only to move them into the trash */
            is UndoableAction.NoteDelete -> {}
        }
    }

    fun clearLastUndoableAction() {
        undoableActionHolder.clear()
    }

    fun synchronize() {
        _uiState.update { it.copy(synchronizing = true) }
        viewModelScope.launch {
            labelRepository.synchronize()
                .onFailure {
                    // TODO: toast
                    delay(200)
                    _uiState.update { it.copy(synchronizing = false) }
                    return@launch
                }
            noteRepository.synchronize()
                .onFailure {
                    // TODO: toast
                }
            delay(200)
            _uiState.update { it.copy(synchronizing = false) }
        }
    }
}