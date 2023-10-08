package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SignedOutViewModel @Inject constructor(
    private val navManager: INavigationManager
) : ViewModel() {

    private val _showLearnMore: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showLearnMore: StateFlow<Boolean> = _showLearnMore.asStateFlow()

    fun signIn() {
        navManager.navigateTo(NavActions.signInRegister())
    }

    fun dismiss() {
        navManager.navigateBack()
    }

    fun showLearnMore() {
        _showLearnMore.value = true
    }
}