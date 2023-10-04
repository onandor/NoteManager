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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SignInRegisterForm(
    val email: String = "",
    val password: String = "",
    val passwordConfirmation: String = ""
)

enum class SignInRegisterFormType {
    SIGN_IN,
    REGISTER
}

data class SignInRegisterUiState(
    val loading: Boolean = false,
    val formType: SignInRegisterFormType = SignInRegisterFormType.SIGN_IN,
    val form: SignInRegisterForm = SignInRegisterForm("", "", ""),
    val snackbarMessageResource: Int? = null
)

@HiltViewModel
class SignInRegisterViewModel @Inject constructor(
    private val settings: ISettings,
    private val authDataSource: IAuthDataSource
) : ViewModel() {

    private val _uiState: MutableStateFlow<SignInRegisterUiState>
        = MutableStateFlow(SignInRegisterUiState())
    val uiState: StateFlow<SignInRegisterUiState> = _uiState

    fun changeFormType(formType: SignInRegisterFormType) {
        val form = _uiState.value.form.copy(
            email = "",
            password = "",
            passwordConfirmation = ""
        )
        _uiState.update {
            it.copy(
                formType = formType,
                form = form
            )
        }
    }

    private fun updateForm(form: SignInRegisterForm) {
        _uiState.update {
            it.copy(form = form)
        }
    }

    fun updateEmail(email: String) {
        val form = _uiState.value.form.copy(
            email = email
        )
        updateForm(form)
    }

    fun updatePassword(password: String) {
        val form = _uiState.value.form.copy(
            password = password
        )
        updateForm(form)
    }

    fun updatePasswordConfirmation(passwordConfirmation: String) {
        val form = _uiState.value.form.copy(
            passwordConfirmation = passwordConfirmation
        )
        updateForm(form)
    }

    fun snackbarShown() {
        _uiState.update {
            it.copy(snackbarMessageResource = null)
        }
    }

    fun signIn() {
        viewModelScope.launch {
            updateLoading(true)
            val authUser = AuthUser(
                email = _uiState.value.form.email,
                password = _uiState.value.form.password,
                deviceId = UUID.fromString(settings.getString(SettingsKeys.INSTALLATION_ID))
            )
            authDataSource.login(authUser)
                .onSuccess { tokenPair ->
                    settings.save(SettingsKeys.ACCESS_TOKEN, tokenPair.accessToken)
                    settings.save(SettingsKeys.REFRESH_TOKEN, tokenPair.refreshToken)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(snackbarMessageResource = error.messageResource)
                    }
                }
            updateLoading(false)
        }
    }

    fun register() {
        viewModelScope.launch {
            updateLoading(true)
            val authUser = AuthUser(
                email = _uiState.value.form.email,
                password = _uiState.value.form.password,
                deviceId = null
            )
            authDataSource.register(authUser)
                .onSuccess { userDetails ->
                    settings.save(SettingsKeys.USER_ID, userDetails.id)
                    settings.save(SettingsKeys.USER_EMAIL, userDetails.email)
                    _uiState.update {
                        it.copy(
                            snackbarMessageResource = R.string.sign_in_register_registration_ok,
                        )
                    }
                    changeFormType(SignInRegisterFormType.SIGN_IN)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(snackbarMessageResource = error.messageResource)
                    }
                }
            updateLoading(false)
        }
    }

    private fun updateLoading(loading: Boolean) {
        _uiState.update {
            it.copy(loading = loading)
        }
    }
}