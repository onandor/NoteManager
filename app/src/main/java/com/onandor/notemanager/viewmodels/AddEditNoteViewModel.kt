package com.onandor.notemanager.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.onandor.notemanager.data.INoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AddEditNoteUiState(
    val title: String = "",
    val description: String = ""
)

@HiltViewModel
class AddEditNoteViewModel @Inject constructor(
    private val noteRepository: INoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditNoteUiState())
    val uiState: StateFlow<AddEditNoteUiState> = _uiState.asStateFlow()

}