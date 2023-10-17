package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.ui.theme.ThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: ISettings,
    private val navManager: INavigationManager
) : ViewModel() {

    fun resetFirstLaunch() {
        viewModelScope.launch {
            settings.save(SettingsKeys.FIRST_LAUNCH, true)
        }
    }

    fun navigateBack() {
        navManager.navigateBack()
    }

    fun saveThemeType(themeType: ThemeType) {
        viewModelScope.launch {
            settings.save(SettingsKeys.THEME_TYPE, themeType.value)
        }
    }
}