package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.onandor.notemanager.R
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import com.onandor.notemanager.utils.AsyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

data class SearchForm(
    val text: String = "",
    val labels: List<Label> = emptyList(),
    val noDebounce: Boolean = false
)

data class SearchUiState(
    val loading: Boolean = false,
    val emptySearch: Boolean = true,
    val emptyResult: Boolean = true,
    val searchForm: SearchForm = SearchForm(),
    val mainNotes: List<Note> = emptyList(),
    val archiveNotes: List<Note> = emptyList(),
    val selectedNotes: List<Note> = emptyList(),
    val labels: List<Label> = emptyList(),
    val searchLabels: List<Label> = emptyList(),
    val searchByLabelsDialogOpen: Boolean = false,
    val snackbarResource: Int = 0,
    val pinEntryDialogOpen: Boolean = false
)

private data class AsyncData(
    val mainNotes: List<Note> = emptyList(),
    val archiveNotes: List<Note> = emptyList(),
    val labels: List<Label> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val noteRepository: INoteRepository,
    private val labelRepository: ILabelRepository,
    private val navManager: INavigationManager
) : ViewModel() {

    private val _notesLoading = MutableStateFlow(true)
    private val _searchForm = MutableStateFlow(SearchForm())
    private var emptySearch = true

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val _notesAsync = _searchForm
        .debounce {
            if (_searchForm.value.noDebounce) 0 else 250
        }
        .onEach { _notesLoading.update { true } }
        .flatMapLatest { form ->
            if (form.text.isEmpty() && form.labels.isEmpty()) {
                emptySearch = true
                flowOf(AsyncResult.Success(Pair(emptyList(), emptyList())))
            }
            else {
                emptySearch = false
                noteRepository.getSearchedNotesStream(
                    locations = listOf(NoteLocation.NOTES, NoteLocation.ARCHIVE),
                    search = form.text,
                    labels = form.labels
                )
                    .map { notes ->
                        AsyncResult.Success(notes.partition { it.location == NoteLocation.NOTES })
                    }
                    .catch<AsyncResult<Pair<List<Note>, List<Note>>>> {
                        emit(AsyncResult.Error("Error while loading notes."))
                    }
            }
        }
        .onEach { _notesLoading.update { false } }

    private val _labelsAsync = labelRepository.getLabelsStream()
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Label>>> { emit(AsyncResult.Error("Error while loading labels.")) }

    private val _notesAndLabelsAsync = combine(_notesAsync, _labelsAsync) { notesAsync, labelsAsync ->
        if (notesAsync == AsyncResult.Loading || labelsAsync == AsyncResult.Loading) {
            AsyncResult.Loading
        }
        else if (notesAsync is AsyncResult.Error) {
            notesAsync
        }
        else if (labelsAsync is AsyncResult.Error) {
            labelsAsync
        }
        else {
            notesAsync as AsyncResult.Success
            labelsAsync as AsyncResult.Success
            AsyncResult.Success(
                AsyncData(
                    mainNotes = notesAsync.data.first,
                    archiveNotes = notesAsync.data.second,
                    labels = labelsAsync.data
                )
            )
        }
    }

    private var lockedNote: Note? = null

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = combine(
        _uiState, _notesAndLabelsAsync, _searchForm, _notesLoading
    ) { uiState, notesAndLabelsAsync, searchForm, notesLoading ->
        when (notesAndLabelsAsync) {
            AsyncResult.Loading -> {
                uiState.copy(
                    loading = true,
                    emptySearch = false,
                    searchForm = searchForm
                )
            }
            is AsyncResult.Error -> {
                uiState.copy(
                    loading = false,
                    emptySearch = false,
                    snackbarResource = R.string.error_while_loading_notes
                )
            }
            is AsyncResult.Success -> {
                if (searchForm.text.isEmpty() && searchForm.labels.isEmpty()) {
                    uiState.copy(
                        loading = false,
                        emptySearch = true,
                        searchForm = searchForm,
                        labels = notesAndLabelsAsync.data.labels
                    )
                }
                else {
                    val emptyResult = !emptySearch && notesAndLabelsAsync.data.mainNotes.isEmpty()
                            && notesAndLabelsAsync.data.archiveNotes.isEmpty()
                    uiState.copy(
                        loading = notesLoading,
                        emptySearch = false,
                        emptyResult = emptyResult,
                        searchForm = searchForm,
                        mainNotes = notesAndLabelsAsync.data.mainNotes,
                        archiveNotes = notesAndLabelsAsync.data.archiveNotes,
                        labels = notesAndLabelsAsync.data.labels
                    )
                }
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SearchUiState()
        )

    fun updateSearchText(newText: String) {
        viewModelScope.launch {
            _searchForm.update { it.copy(text = newText, noDebounce = false) }
        }
    }

    fun addRemoveSearchLabel(label: Label, add: Boolean) {
        _uiState.update {
            val newSearchLabels = it.searchLabels.toMutableList()
            if (add) {
                newSearchLabels.add(label)
            }
            else {
                newSearchLabels.remove(label)
            }
            it.copy(searchLabels = newSearchLabels.sortedWith(compareBy(Label::title)))
        }
    }

    fun confirmSearchLabels() {
        _searchForm.update { it.copy(labels = _uiState.value.searchLabels, noDebounce = true) }
        _uiState.update { it.copy(searchByLabelsDialogOpen = false) }
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
            navManager.navigateTo(NavActions.addEditNote(note.id.toString()), popCurrent = true)
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

    fun navigateBack() {
        navManager.navigateBack()
    }

    fun changeSearchByLabelsDialogOpen(open: Boolean) {
        _uiState.update { it.copy(searchByLabelsDialogOpen = open) }
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
                NoteLocation.NOTES -> {
                    if (single) R.string.snackbar_selection_note_unarchived
                    else R.string.snackbar_selection_notes_unarchived
                }
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

    fun snackbarShown() {
        _uiState.update { it.copy(snackbarResource = 0) }
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
        navManager.navigateTo(NavActions.addEditNote(lockedNote!!.id.toString()), popCurrent = true)
        return true
    }
}