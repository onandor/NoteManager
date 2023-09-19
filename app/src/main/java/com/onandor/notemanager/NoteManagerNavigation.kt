package com.onandor.notemanager

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.onandor.notemanager.NMDestinationsArgs.NOTE_ID_ARG
import com.onandor.notemanager.NMScreens.ADD_EDIT_NOTE_SCREEN
import com.onandor.notemanager.NMScreens.ARCHIVE_SCREEN
import com.onandor.notemanager.NMScreens.NOTE_LIST_SCREEN
import com.onandor.notemanager.NMScreens.SETTINGS_SCREEN
import com.onandor.notemanager.NMScreens.TRASH_SCREEN

private object NMScreens {
    const val NOTE_LIST_SCREEN = "noteList"
    const val ADD_EDIT_NOTE_SCREEN = "addEditNote"
    const val ARCHIVE_SCREEN = "archive"
    const val TRASH_SCREEN = "trash"
    const val SETTINGS_SCREEN = "settings"
}

object NMDestinationsArgs {
    const val NOTE_ID_ARG = "noteId"
}

object NMDestinations {
    const val NOTE_LIST_ROUTE = NOTE_LIST_SCREEN
    const val ADD_EDIT_NOTE_ROUTE = "$ADD_EDIT_NOTE_SCREEN/{$NOTE_ID_ARG}"
    const val ARCHIVE_ROUTE = ARCHIVE_SCREEN
    const val TRASH_ROUTE = TRASH_SCREEN
    const val SETTINGS_ROUTE = SETTINGS_SCREEN
}

class NMNavigationActions(private val navController: NavHostController) {

    fun navigateToNoteList() {
        navController.popBackStack()
        navController.navigate(NMDestinations.NOTE_LIST_ROUTE) {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateToAddEditNote() {
        navController.navigate(NMDestinations.ADD_EDIT_NOTE_ROUTE) {
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

    fun navigateUp() {
        navController.navigateUp()
    }
}