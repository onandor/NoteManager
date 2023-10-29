package com.onandor.notemanager.navigation

import kotlinx.coroutines.flow.StateFlow

interface INavigationManager {

    val navActions: StateFlow<NavAction?>
    fun navigateTo(navAction: NavAction?, popCurrent: Boolean = false)
    fun navigateBack()
    fun setInitialBackStackAction(navAction: NavAction)
}