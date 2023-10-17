package com.onandor.notemanager.viewmodels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.R
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.utils.AsyncResult
import com.onandor.notemanager.viewmodels.ColorSelection.BLUE
import com.onandor.notemanager.viewmodels.ColorSelection.DARK_ORANGE
import com.onandor.notemanager.viewmodels.ColorSelection.GREEN
import com.onandor.notemanager.viewmodels.ColorSelection.LIGHT_RED
import com.onandor.notemanager.viewmodels.ColorSelection.ORANGE
import com.onandor.notemanager.viewmodels.ColorSelection.PINK
import com.onandor.notemanager.viewmodels.ColorSelection.PURPLE
import com.onandor.notemanager.viewmodels.ColorSelection.RED
import com.onandor.notemanager.viewmodels.ColorSelection.YELLOW
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
    val color: Color? = null
)

data class EditLabelsUiState(
    val loading: Boolean = true,
    val labels: List<Label> = emptyList(),
    val snackbarMessageResource: Int? = null,
    val addEditLabelDialogOpen: Boolean = false,
    val addEditLabelForm: AddEditLabelForm = AddEditLabelForm()
)

private fun String.toColor(): Color? {
    return if (this.isEmpty())
        null
    else
        Color(android.graphics.Color.parseColor(this))
}

private fun Color?.toHexString(): String {
    if (this == null)
        return ""

    var red: String = (this.component1() * 255).toInt().toString(16)
    red = if (red.length < 2) "0$red" else red
    var green: String = (this.component2() * 255).toInt().toString(16)
    green = if (green.length < 2) "0$green" else green
    var blue: String = (this.component3() * 255).toInt().toString(16)
    blue = if (blue.length < 2) "0$blue" else blue
    return "#$red$green$blue"
}

private object ColorSelection {
    val YELLOW = "#FFFF00".toColor()!!
    val ORANGE = "#FFA500".toColor()!!
    val DARK_ORANGE = "#E06F1F".toColor()!!
    val RED = "#FF0000".toColor()!!
    val LIGHT_RED = "#FF1A40".toColor()!!
    val PINK = "#FF00FF".toColor()!!
    val PURPLE = "#8A2BE2".toColor()!!
    val BLUE = "#0000FF".toColor()!!
    val GREEN = "#00FF00".toColor()!!
}

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

    val colorSelection = listOf(YELLOW, ORANGE, DARK_ORANGE, RED, LIGHT_RED, PINK, PURPLE, BLUE, GREEN)
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
                color = label.color.toColor()
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
                title = "",
                titleValid = true,
                color = null
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

    fun addEditLabelUpdateColor(color: Color?) {
        addEditLabelForm.update {
            it.copy(color = color)
        }
    }

    fun saveLabel() {
        if (addEditLabelForm.value.id == null) {
            viewModelScope.launch {
                labelRepository.createLabel(
                    title = addEditLabelForm.value.title,
                    color = addEditLabelForm.value.color.toHexString()
                )
            }
        }
        else {
            viewModelScope.launch {
                labelRepository.updateLabel(
                    labelId = addEditLabelForm.value.id!!,
                    title = addEditLabelForm.value.title,
                    color = addEditLabelForm.value.color.toHexString()
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