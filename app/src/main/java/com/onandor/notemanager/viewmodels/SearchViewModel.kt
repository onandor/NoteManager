package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val labels: List<Label> = emptyList(),
    val searchLabels: List<Label> = emptyList(),
    val searchByLabelsDialogOpen: Boolean = false
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
                // TODO: snackbar
                uiState.copy(
                    loading = false,
                    emptySearch = false
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
            it.copy(searchLabels = newSearchLabels)
        }
    }

    fun confirmSearchLabels() {
        _searchForm.update { it.copy(labels = _uiState.value.searchLabels, noDebounce = true) }
        _uiState.update { it.copy(searchByLabelsDialogOpen = false) }
    }

    fun noteClick(note: Note) {
        navManager.navigateTo(NavActions.addEditNote(note.id.toString()), popCurrent = true)
    }

    fun navigateBack() {
        navManager.navigateBack()
    }

    fun changeSearchByLabelsDialogOpen(open: Boolean) {
        _uiState.update { it.copy(searchByLabelsDialogOpen = open) }
    }
}