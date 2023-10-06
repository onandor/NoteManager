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
    val passwordConfirmation: String = "",
    val emailValid: Boolean = true,
    val passwordValid: Boolean = true,
    val passwordConfirmationValid: Boolean = true
)

enum class SignInRegisterFormType {
    SIGN_IN,
    REGISTER
}

data class SignInRegisterUiState(
    val loading: Boolean = false,
    val formType: SignInRegisterFormType = SignInRegisterFormType.SIGN_IN,
    val form: SignInRegisterForm = SignInRegisterForm("", "", ""),
    val snackbarMessageResource: Int? = null,
    val signInSuccessful: Boolean = false
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
            passwordConfirmation = "",
            emailValid = true,
            passwordValid = true,
            passwordConfirmationValid = true
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
        val emailRegex = "^[A-Za-z\\d+_.-]+@[A-Za-z\\d.-]+\$".toRegex()
        val form = _uiState.value.form.copy(
            email = email,
            emailValid = email.matches(emailRegex)
        )
        updateForm(form)
    }

    fun updatePassword(password: String) {
        val confirmationValid = password == _uiState.value.form.passwordConfirmation
        val form = _uiState.value.form.copy(
            password = password,
            passwordValid = password.isNotBlank(),
            passwordConfirmationValid = confirmationValid
        )
        updateForm(form)
    }

    fun updatePasswordConfirmation(passwordConfirmation: String) {
        val valid = _uiState.value.form.password == passwordConfirmation
        val form = _uiState.value.form.copy(
            passwordConfirmation = passwordConfirmation,
            passwordConfirmationValid = valid
        )
        updateForm(form)
    }

    fun snackbarShown() {
        _uiState.update {
            it.copy(snackbarMessageResource = null)
        }
    }

    fun signInSignaled() {
        _uiState.update {
            it.copy(signInSuccessful = false)
        }
    }

    fun signIn() {
        updateEmail(_uiState.value.form.email)
        updatePassword(_uiState.value.form.password)
        if (!_uiState.value.form.emailValid || !_uiState.value.form.passwordValid) {
            _uiState.update {
                it.copy(snackbarMessageResource = R.string.sign_in_register_error_invalid_credentials)
            }
            return
        }

        viewModelScope.launch {
            updateLoading(true)
            val authUser = AuthUser(
                email = _uiState.value.form.email,
                password = _uiState.value.form.password,
                deviceId = UUID.fromString(settings.getString(SettingsKeys.INSTALLATION_ID))
            )
            authDataSource.login(authUser)
                .onSuccess { tokenPair ->
                    settings.save(SettingsKeys.USER_EMAIL, authUser.email)
                    settings.save(SettingsKeys.USER_ID, tokenPair.userId)
                    settings.save(SettingsKeys.ACCESS_TOKEN, tokenPair.accessToken)
                    settings.save(SettingsKeys.REFRESH_TOKEN, tokenPair.refreshToken)
                    _uiState.update {
                        it.copy(signInSuccessful = true)
                    }
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
        updateEmail(_uiState.value.form.email)
        updatePassword(_uiState.value.form.password)
        updatePasswordConfirmation(_uiState.value.form.passwordConfirmation)
        if (!_uiState.value.form.emailValid || !_uiState.value.form.passwordValid
            || !_uiState.value.form.passwordConfirmationValid) {
            _uiState.update {
                it.copy(snackbarMessageResource = R.string.sign_in_register_error_invalid_credentials)
            }
            return
        }

        viewModelScope.launch {
            updateLoading(true)
            val authUser = AuthUser(
                email = _uiState.value.form.email,
                password = _uiState.value.form.password,
                deviceId = null
            )
            authDataSource.register(authUser)
                .onSuccess {
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