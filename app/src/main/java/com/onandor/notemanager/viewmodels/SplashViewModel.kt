package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.NMDestinations
import com.onandor.notemanager.data.local.datastore.ISettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashUiState(
    val isLoading: Boolean = true,
    val startDestination: String = NMDestinations.NOTES_ROUTE
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val settingsDataStore: ISettingsDataStore
) : ViewModel() {

    private val _uiState: MutableStateFlow<SplashUiState> = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        viewModelScope.launch {
            settingsDataStore.firstLaunch().collect { firstLaunch ->
                val startDestination = firstLaunch.let {
                    if (it)
                        NMDestinations.ONBOARDING_ROUTE
                    else
                        NMDestinations.NOTES_ROUTE
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}