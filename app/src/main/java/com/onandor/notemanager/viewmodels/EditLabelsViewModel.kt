package com.onandor.notemanager.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.R
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.navigation.INavigationManager
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

data class AddEditLabelForm(
    val id: UUID? = null,
    val title: String = "",
    val titleValid: Boolean = true,
    val color: LabelColor = LabelColors.none
)

data class EditLabelsUiState(
    val loading: Boolean = true,
    val labels: List<Label> = emptyList(),
    val snackbarMessageResource: Int? = null,
    val addEditLabelDialogOpen: Boolean = false,
    val addEditLabelForm: AddEditLabelForm = AddEditLabelForm()
)

@HiltViewModel
class EditLabelsViewModel @Inject constructor(
    private val labelRepository: ILabelRepository,
    private val navManager: INavigationManager
) : ViewModel() {

    private val _labelsAsync = labelRepository.getLabelsStream()
        .map { AsyncResult.Success(it) }
        .catch<AsyncResult<List<Label>>> { emit(AsyncResult.Error("Error while loading labels.")) }

    private val addEditLabelDialogOpen = MutableStateFlow(false)
    private val addEditLabelForm = MutableStateFlow(AddEditLabelForm())

    val colorSelection = labelColors.toList()
        .map { pair -> pair.second }
        .filterNot { labelColor -> labelColor.type == LabelColorType.None }

    val uiState: StateFlow<EditLabelsUiState> = combine(
        _labelsAsync, addEditLabelDialogOpen, addEditLabelForm
    ) { labelsAsync, editLabelDialogOpen, addEditLabelForm ->
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
                EditLabelsUiState(
                    loading = false,
                    labels = labelsAsync.data,
                    addEditLabelDialogOpen = editLabelDialogOpen,
                    addEditLabelForm = addEditLabelForm
                )
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
        addEditLabelForm.update {
            it.copy(
                id = label.id,
                title = label.title,
                titleValid = label.title.length in 1 .. 30,
                color = label.color
            )
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
                titleValid = true,
                color = LabelColors.none
            )
        }
    }

    fun addEditLabelUpdateTitle(title: String) {
        addEditLabelForm.update {
            it.copy(
                title = title,
                titleValid = (title.length in 1..30)
            )
        }
    }

    fun addEditLabelUpdateColor(color: LabelColor) {
        addEditLabelForm.update {
            it.copy(color = color)
        }
    }

    fun saveLabel() {
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

    fun deleteLabel(label: Label) {
        viewModelScope.launch {
            labelRepository.deleteLabel(label.id)
        }
    }
}