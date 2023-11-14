package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.R
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import com.onandor.notemanager.navigation.NavDestinations
import com.onandor.notemanager.utils.AsyncResult
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

data class DrawerUiState(
    val loading: Boolean = true,
    val labels: List<Label> = emptyList(),
    val selectedLabel: Label? = null,
    val snackbarResource: Int = 0,
    val currentRoute: String = ""
)

@HiltViewModel
class DrawerViewModel @Inject constructor(
    val navManager: INavigationManager,
    val labelRepository: ILabelRepository
) : ViewModel() {

    private val _labelsAsync = labelRepository.getLabelsStream()
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Label>>> { emit(AsyncResult.Error("Error while loading labels.")) }

    private val _uiState = MutableStateFlow(DrawerUiState())
    val uiState: StateFlow<DrawerUiState> = combine(
        _uiState, _labelsAsync
    ) { uiState, labelsAsync ->
        when(labelsAsync) {
            AsyncResult.Loading -> {
                uiState
            }
            is AsyncResult.Error -> {
                uiState.copy(
                    loading = false,
                    snackbarResource = R.string.error_while_loading_labels
                )
            }
            is AsyncResult.Success -> {
                uiState.copy(
                    loading = false,
                    labels = labelsAsync.data
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DrawerUiState()
        )

    private fun clearSelectedLabel(delay: Long = 250) {
        viewModelScope.launch {
            delay(delay)
            _uiState.update { it.copy(selectedLabel = null) }
        }
    }

    fun changeCurrentRoute(newCurrentRoute: String) {
        if (newCurrentRoute != NavDestinations.LABEL_SEARCH)
            clearSelectedLabel(0)
        _uiState.update { it.copy(currentRoute = newCurrentRoute) }
    }

    fun navigateToNotes() {
        clearSelectedLabel()
        navManager.navigateTo(NavActions.notes())
    }

    fun navigateToArchive() {
        clearSelectedLabel()
        navManager.navigateTo(NavActions.archive())
    }

    fun navigateToTrash() {
        clearSelectedLabel()
        navManager.navigateTo(NavActions.trash())
    }

    fun navigateToSettings() {
        clearSelectedLabel()
        navManager.navigateTo(NavActions.settings())
    }

    fun navigateToUserDetails() {
        clearSelectedLabel()
        navManager.navigateTo(NavActions.userDetails())
    }

    fun navigateToEditLabels() {
        clearSelectedLabel()
        navManager.navigateTo(NavActions.editLabels())
    }

    fun navigateToLabelSearch(label: Label) {
        val selectedLabel = _uiState.value.selectedLabel
        if (selectedLabel != null && selectedLabel.id == label.id)
            return

        val popCurrent = _uiState.value.currentRoute == NavDestinations.LABEL_SEARCH
        navManager.navigateTo(NavActions.labelSearch(label.id.toString()), popCurrent = popCurrent)
        viewModelScope.launch {
            delay(250)
            _uiState.update { it.copy(selectedLabel = label) }
        }
    }
}