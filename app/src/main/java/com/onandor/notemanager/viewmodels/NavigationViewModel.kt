package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.navigation.INavigationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor(
    val navigationManager: INavigationManager,
    private val settings: ISettings
) : ViewModel() {

    private val _noteListCollapsedView = MutableStateFlow(false)
    val noteListCollapsedView = _noteListCollapsedView.asStateFlow()

    init {
        viewModelScope.launch {
            val collapsed = settings.getBoolean(SettingsKeys.NOTE_LIST_COLLAPSED_VIEW, false)
            _noteListCollapsedView.update { collapsed }
        }
    }

    fun toggleNoteListCollapsedView() {
        _noteListCollapsedView.update { !it }
        viewModelScope.launch {
            settings.save(SettingsKeys.NOTE_LIST_COLLAPSED_VIEW, _noteListCollapsedView.value)
        }
    }
}