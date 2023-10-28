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
import javax.inject.Inject

data class SearchForm(
    val text: String = "",
    val labels: List<Label> = emptyList()
)

data class SearchUiState(
    val loading: Boolean = true,
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

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val _mainNotesAsync = _searchForm
        .debounce(500)
        .onEach { _mainNotesLoading.update { true } }
        .flatMapLatest { form ->
            noteRepository.getSearchedNotesStream(
                location = NoteLocation.NOTES,
                search = form.text,
                labels = form.labels
            )
                .map { AsyncResult.Success(it) }
                .catch<AsyncResult<List<Note>>> { emit(AsyncResult.Error("Error while loading notes.")) }
        }
        .onEach { _mainNotesLoading.update { false } }
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val _archiveNotesAsync = _searchForm
        .debounce(500)
        .onEach { _archiveNotesLoading.update { true } }
        .flatMapLatest { form ->
            noteRepository.getSearchedNotesStream(
                location = NoteLocation.ARCHIVE,
                search = form.text,
                labels = form.labels
            )
                .map { AsyncResult.Success(it) }
                .catch<AsyncResult<List<Note>>> { emit(AsyncResult.Error("Error while loading notes in archive.")) }
        }
        .onEach { _archiveNotesLoading.update { false } }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = combine(
        _uiState, _mainNotesAsync, _archiveNotesAsync, _searchForm, _mainNotesLoading, _archiveNotesLoading
    ) { uiState, notesInNotesAsync, notesInArchiveAsync, searchForm, mainNotesLoading, archiveNotesLoading ->
        if (notesInNotesAsync == AsyncResult.Loading
            || notesInArchiveAsync == AsyncResult.Loading
            || mainNotesLoading || archiveNotesLoading) {
            uiState.copy(
                loading = true,
                searchForm = searchForm
            )
        }
        else if (notesInNotesAsync is AsyncResult.Error || notesInArchiveAsync is AsyncResult.Error) {
            uiState.copy(loading = false)
        }
        else {
            notesInNotesAsync as AsyncResult.Success<List<Note>>
            notesInArchiveAsync as AsyncResult.Success<List<Note>>
            uiState.copy(
                loading = false,
                searchForm = searchForm,
                mainNotes = notesInNotesAsync.data,
                archiveNotes = notesInArchiveAsync.data
            )
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

    fun goBack() {
        navManager.navigateBack()
    }
}