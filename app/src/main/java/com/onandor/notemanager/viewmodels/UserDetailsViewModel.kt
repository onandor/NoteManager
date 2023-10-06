package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onandor.notemanager.R
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.data.remote.models.AuthUser
import com.onandor.notemanager.data.remote.sources.IAuthDataSource
import com.onandor.notemanager.utils.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class UserDetailsUiState(
    val loadingRequest: Boolean = false,
    val loggedIn: Boolean = false,
    val email: String = "",
    val deleteUserDialogOpen: Boolean = false,
    val passwordConfirmation: String = "",
    val snackbarMessageResource: Int? = null
)

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val settings: ISettings,
    private val authDataSource: IAuthDataSource
) : ViewModel() {

    private val userId = settings.observeInt(SettingsKeys.USER_ID)
    private val email = settings.observeString(SettingsKeys.USER_EMAIL)
    private val loadingRequest = MutableStateFlow(false)
    private val deleteUserDialogOpen = MutableStateFlow(false)
    private val passwordConfirmation = MutableStateFlow("")
    private val snackbarMessageResource: MutableStateFlow<Int?> = MutableStateFlow(null)

    val uiState: StateFlow<UserDetailsUiState> = combine(
        userId, email, loadingRequest, passwordConfirmation, deleteUserDialogOpen, snackbarMessageResource
    ) { userId, email, loadingRequest, passwordConfirmation, deleteUserDialogOpen, snackbarMessageResource ->
        UserDetailsUiState(
            loadingRequest = loadingRequest,
            loggedIn = userId > 0,
            email = email,
            deleteUserDialogOpen = deleteUserDialogOpen,
            passwordConfirmation = passwordConfirmation,
            snackbarMessageResource = snackbarMessageResource
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserDetailsUiState()
        )

    fun logOut() {
        viewModelScope.launch {
            loadingRequest.value = true
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
            loadingRequest.value = false
        }
    }

    fun updatePasswordConfirmation(_passwordConfirmation: String) {
        passwordConfirmation.value = _passwordConfirmation
    }

    fun openDeleteUserDialog() {
        deleteUserDialogOpen.value = true
    }

    fun dismissDeleteUserDialog() {
        deleteUserDialogOpen.value = false
        passwordConfirmation.value = ""
    }

    fun deleteUser() {
        deleteUserDialogOpen.value = false
        viewModelScope.launch {
            loadingRequest.value = true
            authDataSource.deleteUser(passwordConfirmation.value)
                .onSuccess {
                    settings.remove(SettingsKeys.USER_ID)
                    settings.remove(SettingsKeys.USER_EMAIL)
                    settings.remove(SettingsKeys.ACCESS_TOKEN)
                    settings.remove(SettingsKeys.REFRESH_TOKEN)
                    snackbarMessageResource.value = R.string.user_details_snackbar_user_deleted
                }
                .onFailure { error ->
                    snackbarMessageResource.value = error.messageResource
                }
            passwordConfirmation.value = ""
            loadingRequest.value = false
        }
    }

    fun snackbarShown() {
        snackbarMessageResource.value = null
    }
}