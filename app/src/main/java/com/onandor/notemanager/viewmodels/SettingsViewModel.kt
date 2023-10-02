package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.local.datastore.ISettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: ISettingsDataStore
) : ViewModel() {

    fun resetFirstLaunch() {
        viewModelScope.launch {
            settings.saveFirstLaunch(firstLaunch = true)
        }
    }
}