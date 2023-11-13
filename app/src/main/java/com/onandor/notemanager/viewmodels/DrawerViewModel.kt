package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.R
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import com.onandor.notemanager.utils.AsyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DrawerUiState(
    val loading: Boolean = true,
    val labels: List<Label> = emptyList(),
    val selectedLabel: Label? = null,
    val snackbarResource: Int = 0
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

    fun navigateToNotes() {
        navManager.navigateTo(NavActions.notes())
    }

    fun navigateToArchive() {
        navManager.navigateTo(NavActions.archive())
    }

    fun navigateToTrash() {
        navManager.navigateTo(NavActions.trash())
    }

    fun navigateToSettings() {
        navManager.navigateTo(NavActions.settings())
    }

    fun navigateToUserDetails() {
        navManager.navigateTo(NavActions.userDetails())
    }

    fun navigateToEditLabels() {
        navManager.navigateTo(NavActions.editLabels())
    }
}