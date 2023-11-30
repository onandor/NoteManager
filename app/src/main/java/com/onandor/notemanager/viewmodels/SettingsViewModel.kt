package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.ILabelRepository
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKey
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.ui.theme.ThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: ISettings,
    private val navManager: INavigationManager,
    private val noteRepository: INoteRepository,
    private val labelRepository: ILabelRepository
) : ViewModel() {

    val currentTheme = settings.observeInt(SettingsKeys.THEME_TYPE).map { themeSetting ->
        if (themeSetting < 0)
            ThemeType.SYSTEM
        else
            ThemeType.fromInt(themeSetting)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeType.SYSTEM
    )

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

    fun deleteAllLocalData() {
        runBlocking {
            labelRepository.deleteAllLocal()
            noteRepository.deleteAllLocal()
        }
    }
}