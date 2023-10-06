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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class UserDetailsForm(
    val oldPassword: String = "",
    val newPassword: String = "",
    val newPasswordConfirmation: String = ""
)

data class UserDetailsUiState(
    val loadingRequest: Boolean = false,
    val loggedIn: Boolean = false,
    val email: String = "",
    val openDialog: UserDetailsDialogType = UserDetailsDialogType.NONE,
    val userDetailsForm: UserDetailsForm = UserDetailsForm(),
    val snackbarMessageResource: Int? = null
)

enum class UserDetailsDialogType {
    NONE,
    DELETE_USER,
    CHANGE_PASSWORD
}

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    private val settings: ISettings,
    private val authDataSource: IAuthDataSource
) : ViewModel() {

    private val userId = settings.observeInt(SettingsKeys.USER_ID)
    private val email = settings.observeString(SettingsKeys.USER_EMAIL)
    private val loadingRequest = MutableStateFlow(false)
    private val openDialog = MutableStateFlow(UserDetailsDialogType.NONE)
    private val userDetailsForm = MutableStateFlow(UserDetailsForm())
    private val snackbarMessageResource: MutableStateFlow<Int?> = MutableStateFlow(null)

    val uiState: StateFlow<UserDetailsUiState> = combine(
        userId,
        email,
        loadingRequest,
        userDetailsForm,
        openDialog,
        snackbarMessageResource
    ) { userId, email, loadingRequest, userDetailsForm, openDialog, snackbarMessageResource ->
        UserDetailsUiState(
            loadingRequest = loadingRequest,
            loggedIn = userId > 0,
            email = email,
            openDialog = openDialog,
            userDetailsForm = userDetailsForm,
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

    fun updateOldPassword(oldPassword: String) {
        userDetailsForm.update {
            it.copy(oldPassword = oldPassword)
        }
    }

    fun updateNewPassword(newPassword: String) {
        userDetailsForm.update {
            it.copy(newPassword = newPassword)
        }
    }

    fun updateNewPasswordConfirmation(newPasswordConfirmation: String) {
        userDetailsForm.update {
            it.copy(newPasswordConfirmation = newPasswordConfirmation)
        }
    }

    fun openDialog(dialogType: UserDetailsDialogType) {
        openDialog.value = dialogType
    }

    fun dismissDialog() {
        openDialog.value = UserDetailsDialogType.NONE
        userDetailsForm.update {
            it.copy(
                oldPassword = "",
                newPassword = "",
                newPasswordConfirmation = ""
            )
        }
    }

    fun deleteUser() {
        openDialog.value = UserDetailsDialogType.NONE
        viewModelScope.launch {
            loadingRequest.value = true
            authDataSource.deleteUser(userDetailsForm.value.oldPassword)
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
            updateOldPassword("")
            loadingRequest.value = false
        }
    }

    fun snackbarShown() {
        snackbarMessageResource.value = null
    }

    fun changePassword() {
        openDialog.value = UserDetailsDialogType.NONE
        viewModelScope.launch {
            loadingRequest.value = true
            authDataSource.changePassword(
                userDetailsForm.value.oldPassword,
                userDetailsForm.value.newPassword
            )
                .onSuccess {
                    snackbarMessageResource.value = R.string.user_details_snackbar_password_changed
                }
                .onFailure { error ->
                    snackbarMessageResource.value = error.messageResource
                }
            updateOldPassword("")
            updateNewPassword("")
            updateNewPasswordConfirmation("")
            loadingRequest.value = false
        }
    }
}