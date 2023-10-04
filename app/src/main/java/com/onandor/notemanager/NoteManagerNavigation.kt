package com.onandor.notemanager

import androidx.navigation.NavHostController
import com.onandor.notemanager.NMDestinationsArgs.NOTE_ID_ARG
import com.onandor.notemanager.NMScreens.ADD_EDIT_NOTE_SCREEN
import com.onandor.notemanager.NMScreens.ARCHIVE_SCREEN
import com.onandor.notemanager.NMScreens.NOTES_SCREEN
import com.onandor.notemanager.NMScreens.ONBOARDING_SCREEN
import com.onandor.notemanager.NMScreens.SETTINGS_SCREEN
import com.onandor.notemanager.NMScreens.SIGN_IN_REGISTER_SCREEN
import com.onandor.notemanager.NMScreens.TRASH_SCREEN
import com.onandor.notemanager.NMScreens.USER_DETAILS_SCREEN

private object NMScreens {
    const val NOTES_SCREEN = "notes"
    const val ADD_EDIT_NOTE_SCREEN = "addEditNote"
    const val ARCHIVE_SCREEN = "archive"
    const val TRASH_SCREEN = "trash"
    const val SETTINGS_SCREEN = "settings"
    const val ONBOARDING_SCREEN = "onboarding"
    const val SIGN_IN_REGISTER_SCREEN = "signInRegister"
    const val USER_DETAILS_SCREEN = "userDetails"
}

object NMDestinationsArgs {
    const val NOTE_ID_ARG = "noteId"
}

object NMDestinations {
    const val NOTES_ROUTE = NOTES_SCREEN
    const val ADD_EDIT_NOTE_ROUTE = "$ADD_EDIT_NOTE_SCREEN?$NOTE_ID_ARG={$NOTE_ID_ARG}"
    const val ARCHIVE_ROUTE = ARCHIVE_SCREEN
    const val TRASH_ROUTE = TRASH_SCREEN
    const val SETTINGS_ROUTE = SETTINGS_SCREEN
    const val ONBOARDING_ROUTE = ONBOARDING_SCREEN
    const val SIGN_IN_REGISTER_ROUTE = SIGN_IN_REGISTER_SCREEN
    const val USER_DETAILS_ROUTE = USER_DETAILS_SCREEN
}

class NMNavigationActions(private val navController: NavHostController) {

    fun navigateToNotes() {
        navController.popBackStack()
        navController.navigate(NMDestinations.NOTES_ROUTE) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToAddEditNote(noteId: String?) {
        navController.navigate(
            if (noteId == null) ADD_EDIT_NOTE_SCREEN else "$ADD_EDIT_NOTE_SCREEN?$NOTE_ID_ARG=$noteId"
        ) {
            launchSingleTop = true
        }
    }

    fun navigateToArchive() {
        navController.popBackStack()
        navController.navigate(NMDestinations.ARCHIVE_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToTrash() {
        navController.popBackStack()
        navController.navigate(NMDestinations.TRASH_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToSettings() {
        navController.navigate(NMDestinations.SETTINGS_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToSignInRegister() {
        navController.navigate(NMDestinations.SIGN_IN_REGISTER_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateToUserDetails() {
        navController.navigate(NMDestinations.USER_DETAILS_ROUTE) {
            launchSingleTop = true
        }
    }

    fun navigateUp() {
        navController.navigateUp()
    }
}