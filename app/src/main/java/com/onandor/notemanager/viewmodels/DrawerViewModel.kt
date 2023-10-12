package com.onandor.notemanager.viewmodels

import androidx.lifecycle.ViewModel
import com.onandor.notemanager.navigation.INavigationManager
import com.onandor.notemanager.navigation.NavActions
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    val navManager: INavigationManager
) : ViewModel() {

    fun navigateToNotes() {
        navManager.navigateTo(NavActions.notes())
    }

    fun navigateToArchive() {
        navManager.navigateTo(NavActions.archive())
    }

    fun navigateToTrash() {
        navManager.navigateTo(NavActions.trash())
    }

    fun navigateToSettings() {
        navManager.navigateTo(NavActions.settings())
    }

    fun navigateToUserDetails() {
        navManager.navigateTo(NavActions.userDetails())
    }

    fun navigateToEditLabels() {
        navManager.navigateTo(NavActions.editLabels())
    }
}