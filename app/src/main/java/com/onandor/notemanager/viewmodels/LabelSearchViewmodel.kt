package com.onandor.notemanager.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.onandor.notemanager.R
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import com.onandor.notemanager.navigation.NavDestinationArgs
import com.onandor.notemanager.ui.components.NoteListState
import com.onandor.notemanager.utils.AddEditLabelForm
import com.onandor.notemanager.utils.AddEditResultState
import com.onandor.notemanager.utils.AsyncResult
import com.onandor.notemanager.utils.LabelColor
import com.onandor.notemanager.utils.LabelColorType
import com.onandor.notemanager.utils.NoteComparison
import com.onandor.notemanager.utils.NoteComparisonField
import com.onandor.notemanager.utils.NoteSorting
import com.onandor.notemanager.utils.Order
import com.onandor.notemanager.utils.combine
import com.onandor.notemanager.utils.labelColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class LabelSearchUiState(
    val loading: Boolean = true,
    val searchedLabel: AddEditLabelForm = AddEditLabelForm(),
    val mainNotes: List<Note> = emptyList(),
    val archiveNotes: List<Note> = emptyList(),
    val selectedNotes: List<Note> = emptyList(),
    val noteListState: NoteListState = NoteListState(),
    val addEditSnackbarResource: Int = 0,
    val snackbarResource: Int = 0,
    val pinEntryDialogOpen: Boolean = false,
    val editLabelDialogOpen: Boolean = false,
    val deleteDialogOpen: Boolean = false,
    val editLabelForm: AddEditLabelForm = AddEditLabelForm()
)

@HiltViewModel
class LabelSearchViewmodel @Inject constructor(
    private val noteRepository: INoteRepository,
    private val labelRepository: ILabelRepository,
    private val addEditResultState: AddEditResultState,
    private val navManager: INavigationManager,
    private val settings: ISettings,
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val labelId = savedStateHandle
        .get<String>(NavDestinationArgs.LABEL_ID_ARG)
        .let { if (it != null) UUID.fromString(it) else null }
    private val _searchedLabel = MutableStateFlow(AddEditLabelForm())
    private var lockedNote: Note? = null

    private val _editLabelForm = MutableStateFlow(AddEditLabelForm())
    val colorSelection = labelColors.toList()
        .map { pair -> pair.second }
        .filterNot { labelColor -> labelColor.type == LabelColorType.None }

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

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _notesAsync = _searchedLabel
        .flatMapLatest { searchedLabel ->
            if (searchedLabel.id == null) {
                flowOf(AsyncResult.Success(Pair(emptyList(), emptyList())))
            } else {
                val label = Label(
                    id = searchedLabel.id,
                    title = searchedLabel.title,
                    color = searchedLabel.color
                )
                noteRepository.getSearchedNotesStream(
                    locations = listOf(NoteLocation.NOTES, NoteLocation.ARCHIVE),
                    search = "",
                    labels = listOf(label)
                )
                    .map { notes -> AsyncResult.Success(notes.partition { it.location == NoteLocation.NOTES }) }
                    .catch<AsyncResult<Pair<List<Note>, List<Note>>>> {
                        emit(AsyncResult.Error("Error while loading main notes."))
                    }
            }
        }

    private val _uiState = MutableStateFlow(LabelSearchUiState())
    val uiState = combine(
        _uiState, _notesAsync, _searchedLabel, addEditResultState.result, noteListState, _editLabelForm
    ) { uiState, notesAsync, searchedLabel, addEditResult, noteListState, labelEditForm ->
        when(notesAsync) {
            AsyncResult.Loading -> {
                uiState.copy(
                    addEditSnackbarResource = addEditResult.resource,
                    searchedLabel = searchedLabel
                )
            }
            is AsyncResult.Error -> {
                uiState.copy(
                    loading = false,
                    searchedLabel = searchedLabel,
                    addEditSnackbarResource = addEditResult.resource,
                    snackbarResource = R.string.error_while_loading_notes
                )
            }
            is AsyncResult.Success -> {
                val sortedMainNotes = notesAsync.data.first
                    .sortedWith(NoteComparison.comparators[noteListState.sorting]!!)
                    .sortedWith(compareByDescending(Note::pinned))
                val sortedArchiveNotes = notesAsync.data.second
                    .sortedWith(NoteComparison.comparators[noteListState.sorting]!!)
                    .sortedWith(compareByDescending(Note::pinned))

                uiState.copy(
                    loading = false,
                    searchedLabel = searchedLabel,
                    mainNotes = sortedMainNotes,
                    archiveNotes = sortedArchiveNotes,
                    addEditSnackbarResource = addEditResult.resource,
                    noteListState = noteListState,
                    editLabelForm = labelEditForm
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LabelSearchUiState()
        )

    init {
        if (labelId == null) {
            _uiState.update { it.copy(snackbarResource = R.string.error_while_loading_notes) }
        } else {
            viewModelScope.launch {
                val label = labelRepository.getLabel(labelId)
                if (label == null) {
                    _uiState.update { it.copy(snackbarResource = R.string.error_while_loading_notes) }
                } else {
                    _searchedLabel.update {
                        it.copy(
                            id = label.id,
                            title = label.title,
                            color = label.color
                        )
                    }
                    _editLabelForm.update {
                        it.copy(
                            id = label.id,
                            title = label.title,
                            titleValid = true,
                            color = label.color
                        )
                    }
                }
            }
        }
    }

    fun noteClick(note: Note) {
        if (_uiState.value.selectedNotes.isNotEmpty()) {
            noteLongClick(note)
        } else if (note.pinHash.isNotEmpty()) {
            lockedNote = note
            openPinEntryDialog()
        } else {
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
            _uiState.value.selectedNotes.forEach { note ->
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
                it.copy(snackbarResource = resource)
            }
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
                it.copy(snackbarResource = resource)
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

    fun updateLabelTitle(newTitle: String) {
        if (newTitle.length > 30)
            return

        _editLabelForm.update {
            it.copy(
                title = newTitle,
                titleValid = newTitle.isNotEmpty()
            )
        }
    }

    fun updateLabelColor(newColor: LabelColor) {
        _editLabelForm.update { it.copy(color = newColor) }
    }

    fun saveLabel() {
        if (_searchedLabel.value.id == null)
            return

        _searchedLabel.update {
            it.copy(
                title = _editLabelForm.value.title,
                color = _editLabelForm.value.color
            )
        }
        viewModelScope.launch {
            labelRepository.updateLabel(
                labelId = _editLabelForm.value.id!!,
                title = _editLabelForm.value.title,
                color = _editLabelForm.value.color
            )
        }
    }

    fun deleteLabel() {
        _uiState.update { it.copy(deleteDialogOpen = false) }
        if (labelId == null)
            return

        viewModelScope.launch {
            labelRepository.deleteLabel(labelId)
        }
        navManager.navigateBack()
    }

    fun openEditLabelDialog() {
        _uiState.update { it.copy(editLabelDialogOpen = true) }
    }

    fun closeEditLabelDialog() {
        _uiState.update { it.copy(editLabelDialogOpen = false) }
    }

    fun openDeleteDialog() {
        _uiState.update { it.copy(deleteDialogOpen = true) }
    }

    fun closeDeleteDialog() {
        _uiState.update { it.copy(deleteDialogOpen = false) }
    }

    fun navigateBack() {
        navManager.navigateBack()
    }

    fun addNote() {
        if (labelId == null)
            return

        navManager.navigateTo(NavActions.addNote(labelId.toString()))
    }

    fun snackbarShown() {
        _uiState.update { it.copy(snackbarResource = 0) }
    }

    fun addEditResultSnackbarShown() {
        addEditResultState.clear()
    }
}