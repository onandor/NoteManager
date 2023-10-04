package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserDetailsUiState(
    val loggedIn: Boolean = false,
    val email: String = "",
    val noteCount: Int = 0
)

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val settings: ISettings
) : ViewModel() {

    private val userId = settings.observeInt(SettingsKeys.USER_ID)
    private val email = settings.observeString(SettingsKeys.USER_EMAIL)

    val uiState: StateFlow<UserDetailsUiState> = combine(
        userId, email
    ) { userId, email ->
        UserDetailsUiState(
            loggedIn = userId > 0,
            email = email,
            noteCount = 0
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserDetailsUiState()
        )

    fun logOut() {
        viewModelScope.launch {
            settings.remove(SettingsKeys.USER_ID)
            settings.remove(SettingsKeys.USER_EMAIL)
            settings.remove(SettingsKeys.ACCESS_TOKEN)
            settings.remove(SettingsKeys.REFRESH_TOKEN)
        }
    }

    fun deleteUser() {

    }
}