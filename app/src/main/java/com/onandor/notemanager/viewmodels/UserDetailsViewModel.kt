package com.onandor.notemanager.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.data.remote.models.AuthUser
import com.onandor.notemanager.data.remote.sources.IAuthDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class UserDetailsUiState(
    val loadingRequest: Boolean = false,
    val loggedIn: Boolean = false,
    val email: String = ""
)

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val settings: ISettings,
    private val authDataSource: IAuthDataSource
) : ViewModel() {

    private val userId = settings.observeInt(SettingsKeys.USER_ID)
    private val email = settings.observeString(SettingsKeys.USER_EMAIL)
    private val loadingRequest = MutableStateFlow(false)

    val uiState: StateFlow<UserDetailsUiState> = combine(
        userId, email, loadingRequest
    ) { userId, email, loadingRequest ->
        UserDetailsUiState(
            loadingRequest = loadingRequest,
            loggedIn = userId > 0,
            email = email
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserDetailsUiState()
        )

    private fun updateLoadingRequest(_loadingRequest: Boolean) {
        loadingRequest.value = _loadingRequest
    }

    fun logOut() {
        viewModelScope.launch {
            updateLoadingRequest(true)
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
            updateLoadingRequest(false)
        }
    }

    fun deleteUser() {

    }
}