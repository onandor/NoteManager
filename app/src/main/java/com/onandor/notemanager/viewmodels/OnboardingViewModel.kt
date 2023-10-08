package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: ISettings,
    private val navManager: INavigationManager
) : ViewModel() {

    fun completeFirstLaunch() {
        viewModelScope.launch {
            settings.save(SettingsKeys.FIRST_LAUNCH, false)
        }
    }

    fun skip() {
        navManager.navigateTo(NavActions.notes())
    }

    fun signIn() {
        navManager.navigateTo(NavActions.signInRegister())
    }
}