package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import com.onandor.notemanager.data.local.datastore.ISettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
    private val settings: ISettingsDataStore
) : ViewModel() {

    private val _uiState: MutableStateFlow<SignInRegisterUiState>
        = MutableStateFlow(SignInRegisterUiState())
    val uiState: StateFlow<SignInRegisterUiState> = _uiState

    fun changeFormType(formType: SignInRegisterFormType) {
        _uiState.update {
            it.copy(formType = formType)
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
}