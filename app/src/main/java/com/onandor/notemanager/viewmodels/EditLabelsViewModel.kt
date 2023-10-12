package com.onandor.notemanager.viewmodels

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.R
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.utils.AsyncResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class EditLabelsUiState(
    val loading: Boolean = true,
    val labels: List<Label> = emptyList(),
    val snackbarMessageResource: Int? = null
)

@HiltViewModel
class EditLabelsViewModel @Inject constructor(
    private val labelRepository: ILabelRepository,
    private val navManager: INavigationManager
) : ViewModel() {

    private val _labelsAsync = labelRepository.getLabelsStream()
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Label>>> { emit(AsyncResult.Error("Error while loading labels.")) } // TODO: resource

    val uiState: StateFlow<EditLabelsUiState> = _labelsAsync.map { labelsAsync ->
        when(labelsAsync) {
            AsyncResult.Loading -> {
                EditLabelsUiState()
            }
            is AsyncResult.Error -> {
                EditLabelsUiState(
                    loading = false,
                    snackbarMessageResource = R.string.edit_labels_loading_error
                )
            }
            is AsyncResult.Success -> {
                EditLabelsUiState(loading = false, labels = labelsAsync.data)
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EditLabelsUiState()
        )

    fun navigateBack() {
        navManager.navigateBack()
    }

    fun labelClick(label: Label) {
        println("Label clicked")
    }
}