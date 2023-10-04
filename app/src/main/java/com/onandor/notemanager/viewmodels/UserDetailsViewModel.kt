package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import com.onandor.notemanager.data.local.datastore.ISettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    val _uiState: MutableStateFlow<UserDetailsUiState> = MutableStateFlow(UserDetailsUiState())
    val uiState: StateFlow<UserDetailsUiState> = _uiState

    fun logOut() {

    }

    fun deleteUser() {

    }
}