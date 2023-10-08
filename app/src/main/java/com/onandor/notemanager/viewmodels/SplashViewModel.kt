package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import com.onandor.notemanager.navigation.NavDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SplashUiState(
    val isLoading: Boolean = true,
    val startDestination: String = NavDestinations.NOTES
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val settings: ISettings,
    private val navManager: INavigationManager
) : ViewModel() {

    private val _uiState: MutableStateFlow<SplashUiState> = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        viewModelScope.launch {
            val firstLaunch = settings.getBoolean(SettingsKeys.FIRST_LAUNCH, true)
            var startDestination = ""
            if (firstLaunch) {
                startDestination = NavDestinations.ONBOARDING
                settings.save(SettingsKeys.INSTALLATION_ID, UUID.randomUUID().toString())
            }
            else {
                startDestination = NavDestinations.NOTES
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    startDestination = startDestination
                )
            }
            if (startDestination == NavDestinations.ONBOARDING)
                navManager.navigateTo(NavActions.onboarding())
            else
                navManager.navigateTo(NavActions.notes())
        }
    }
}