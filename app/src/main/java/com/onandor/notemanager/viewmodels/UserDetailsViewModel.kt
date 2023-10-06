package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.data.remote.models.AuthUser
import com.onandor.notemanager.data.remote.sources.IAuthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class UserDetailsUiState(
    val loggedIn: Boolean = false,
    val email: String = "",
    val noteCount: Int = 0
)

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val settings: ISettings,
    private val authDataSource: IAuthDataSource
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
            val authUser = AuthUser(
                email = "",
                password = "",
                deviceId = UUID.fromString(settings.getString(SettingsKeys.INSTALLATION_ID))
            )
            authDataSource.logout(authUser)
            settings.remove(SettingsKeys.USER_ID)
            settings.remove(SettingsKeys.USER_EMAIL)
            settings.remove(SettingsKeys.ACCESS_TOKEN)
            settings.remove(SettingsKeys.REFRESH_TOKEN)
        }
    }

    fun deleteUser() {

    }
}