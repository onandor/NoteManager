package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.navigation.INavigationManager
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
import com.onandor.notemanager.utils.combine
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

data class SearchForm(
    val text: String = "",
    val labels: List<Label> = emptyList()
)

data class SearchUiState(
    val loading: Boolean = false,
    val emptySearch: Boolean = true,
    val emptyResult: Boolean = true,
    val searchForm: SearchForm = SearchForm(),
    val mainNotes: List<Note> = emptyList(),
    val archiveNotes: List<Note> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val noteRepository: INoteRepository,
    private val navManager: INavigationManager
) : ViewModel() {

    private val _mainNotesLoading = MutableStateFlow(true)
    private val _archiveNotesLoading = MutableStateFlow(true)
    private val _searchForm = MutableStateFlow(SearchForm())
    private var emptySearch = true

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val _mainNotesAsync = _searchForm
        .debounce(250)
        .onEach { _mainNotesLoading.update { true } }
        .flatMapLatest { form ->
            if (form.text.isEmpty() && form.labels.isEmpty()) {
                emptySearch = true
                flowOf(AsyncResult.Success(emptyList()))
            }
            else {
                emptySearch = false
                noteRepository.getSearchedNotesStream(
                    location = NoteLocation.NOTES,
                    search = form.text,
                    labels = form.labels
                )
                    .map { AsyncResult.Success(it) }
                    .catch<AsyncResult<List<Note>>> {
                        emit(AsyncResult.Error("Error while loading notes."))
                    }
            }
        }
        .onEach { _mainNotesLoading.update { false } }
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val _archiveNotesAsync = _searchForm
        .debounce(250)
        .onEach { _archiveNotesLoading.update { true } }
        .flatMapLatest { form ->
            if (form.text.isEmpty() && form.labels.isEmpty()) {
                emptySearch = true
                flowOf(AsyncResult.Success(emptyList()))
            }
            else {
                emptySearch = false
                noteRepository.getSearchedNotesStream(
                    location = NoteLocation.ARCHIVE,
                    search = form.text,
                    labels = form.labels
                )
                    .map { AsyncResult.Success(it) }
                    .catch<AsyncResult<List<Note>>> {
                        emit(AsyncResult.Error("Error while loading archived notes."))
                    }
            }
        }
        .onEach { _archiveNotesLoading.update { false } }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = combine(
        _uiState, _mainNotesAsync, _archiveNotesAsync, _searchForm, _mainNotesLoading, _archiveNotesLoading
    ) { uiState, mainNotesAsync, archiveNotesAsync, searchForm, mainNotesLoading, archiveNotesLoading ->
        if (mainNotesAsync == AsyncResult.Loading || archiveNotesAsync == AsyncResult.Loading) {
            uiState.copy(
                loading = true,
                emptySearch = false,
                searchForm = searchForm
            )
        }
        else if (mainNotesAsync is AsyncResult.Error || archiveNotesAsync is AsyncResult.Error) {
            uiState.copy(
                loading = false,
                emptySearch = false
            )
        }
        else {
            mainNotesAsync as AsyncResult.Success<List<Note>>
            archiveNotesAsync as AsyncResult.Success<List<Note>>
            if (searchForm.text.isEmpty() && searchForm.labels.isEmpty()) {
                uiState.copy(
                    loading = false,
                    emptySearch = true,
                    searchForm = searchForm
                )
            }
            else {
                val loading = mainNotesLoading || archiveNotesLoading
                val emptyResult = !emptySearch && mainNotesAsync.data.isEmpty() && archiveNotesAsync.data.isEmpty()
                uiState.copy(
                    loading = loading,
                    emptySearch = false,
                    emptyResult = emptyResult,
                    searchForm = searchForm,
                    mainNotes = mainNotesAsync.data,
                    archiveNotes = archiveNotesAsync.data
                )
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
            _searchForm.update { it.copy(text = newText) }
        }
    }

    fun navigateBack() {
        navManager.navigateBack()
    }
}