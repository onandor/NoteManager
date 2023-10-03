package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onandor.notemanager.data.local.datastore.ISettingsDataStore
import com.onandor.notemanager.data.remote.models.AuthUser
import com.onandor.notemanager.data.remote.sources.IAuthApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    val formType: SignInRegisterFormType = SignInRegisterFormType.SIGN_IN,
    val form: SignInRegisterForm = SignInRegisterForm("", "", "")
)

@HiltViewModel
class SignInRegisterViewModel @Inject constructor(
    private val settings: ISettingsDataStore,
    private val authApiService: IAuthApiService
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

    fun signIn() {
        viewModelScope.launch {
            val authUser = AuthUser(
                email = _uiState.value.form.email,
                password = _uiState.value.form.password,
                deviceId = runBlocking { settings.getInstallationId() }.first()
            )

            val tokenPair = authApiService.login(authUser)
            println("access: ${tokenPair.accessToken}, refresh: ${tokenPair.refreshToken}")
        }
    }

    fun register() {
        viewModelScope.launch {
            val authUser = AuthUser(
                email = _uiState.value.form.email,
                password = _uiState.value.form.password,
                deviceId = null
            )
            val userDetails = authApiService.register(authUser)
            println("id: ${userDetails.id}, email: ${userDetails.email}")
        }
    }
}