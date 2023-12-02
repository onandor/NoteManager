package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onandor.notemanager.R
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.utils.AddEditLabelForm
import com.onandor.notemanager.utils.AsyncResult
import com.onandor.notemanager.utils.LabelColor
import com.onandor.notemanager.utils.LabelColorType
import com.onandor.notemanager.utils.LabelColors
import com.onandor.notemanager.utils.labelColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EditLabelsUiState(
    val loading: Boolean = true,
    val labels: List<Label> = emptyList(),
    val snackbarMessageResource: Int? = null,
    val addEditLabelDialogOpen: Boolean = false,
    val addEditLabelForm: AddEditLabelForm = AddEditLabelForm(),
    val deleteDialogOpen: Boolean = false
)

@HiltViewModel
class EditLabelsViewModel @Inject constructor(
    private val labelRepository: ILabelRepository,
    private val navManager: INavigationManager
) : ViewModel() {

    private val _labelsAsync = labelRepository.getLabelsStream()
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Label>>> { emit(AsyncResult.Error("")) }

    private val addEditLabelDialogOpen = MutableStateFlow(false)
    private val addEditLabelForm = MutableStateFlow(AddEditLabelForm())
    private val deleteDialogOpen = MutableStateFlow(false)
    private var labelToDelete: Label? = null

    val colorSelection = labelColors.toList()
        .map { pair -> pair.second }
        .filterNot { labelColor -> labelColor.type == LabelColorType.None }

    private val _uiState = MutableStateFlow(EditLabelsUiState())
    val uiState: StateFlow<EditLabelsUiState> = combine(
        _uiState, _labelsAsync, addEditLabelDialogOpen, addEditLabelForm, deleteDialogOpen
    ) { uiState, labelsAsync, editLabelDialogOpen, addEditLabelForm, deleteDialogOpen ->
        when(labelsAsync) {
            AsyncResult.Loading -> {
                uiState
            }
            is AsyncResult.Error -> {
                uiState.copy(
                    loading = false,
                    snackbarMessageResource = R.string.error_while_loading_labels
                )
            }
            is AsyncResult.Success -> {
                uiState.copy(
                    loading = false,
                    labels = labelsAsync.data,
                    addEditLabelDialogOpen = editLabelDialogOpen,
                    addEditLabelForm = addEditLabelForm,
                    deleteDialogOpen = deleteDialogOpen
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EditLabelsUiState()
        )

    init {
        viewModelScope.launch {
            labelRepository.synchronize()
        }
    }

    fun navigateBack() {
        navManager.navigateBack()
    }

    fun labelClick(label: Label) {
        addEditLabelForm.update {
            it.copy(
                id = label.id,
                title = label.title,
                titleValid = label.title.isNotEmpty(),
                color = label.color
            )
        }
        viewModelScope.launch {
            labelRepository.synchronizeSingle(label.id)
                .onSuccess {
                    val updatedLabel = labelRepository.getLabel(label.id) ?: return@launch
                    if (label.modificationDate == updatedLabel.modificationDate)
                        return@launch
                    addEditLabelForm.update {
                        it.copy(
                            title = updatedLabel.title,
                            titleValid = updatedLabel.title.isNotEmpty(),
                            color = updatedLabel.color
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(snackbarMessageResource = R.string.apierror_label_not_found) }
                    hideAddEditLabelDialog()
                    labelRepository.synchronize()
                    return@launch
                }
        }
        showAddEditLabelDialog()
    }

    fun showAddEditLabelDialog() {
        addEditLabelDialogOpen.update { true }
    }

    fun hideAddEditLabelDialog() {
        addEditLabelDialogOpen.update { false }
        addEditLabelForm.update {
            it.copy(
                id = null,
                title = "",
                titleValid = false,
                color = LabelColors.none
            )
        }
    }

    fun addEditLabelUpdateTitle(title: String) {
        if (title.length > 30)
            return

        addEditLabelForm.update {
            it.copy(
                title = title,
                titleValid = title.isNotEmpty()
            )
        }
    }

    fun addEditLabelUpdateColor(color: LabelColor) {
        addEditLabelForm.update {
            it.copy(color = color)
        }
    }

    fun saveLabel() {
        if (!addEditLabelForm.value.titleValid)
            return

        if (addEditLabelForm.value.id == null) {
            viewModelScope.launch {
                labelRepository.createLabel(
                    title = addEditLabelForm.value.title,
                    color = addEditLabelForm.value.color
                )
            }
        }
        else {
            viewModelScope.launch {
                labelRepository.updateLabel(
                    labelId = addEditLabelForm.value.id!!,
                    title = addEditLabelForm.value.title,
                    color = addEditLabelForm.value.color
                )
            }
        }
    }

    fun showConfirmDeleteLabel(label: Label) {
        labelToDelete = label
        deleteDialogOpen.update { true }
    }

    fun cancelDeleteLabel() {
        labelToDelete = null
        deleteDialogOpen.update { false }
    }

    fun deleteLabel() {
        if (labelToDelete == null)
            return

        viewModelScope.launch {
            labelRepository.deleteLabel(labelToDelete!!.id)
            labelToDelete = null
            deleteDialogOpen.update { false }
        }
    }

    fun snackbarShown() {
        _uiState.update { it.copy(snackbarMessageResource = null) }
    }
}