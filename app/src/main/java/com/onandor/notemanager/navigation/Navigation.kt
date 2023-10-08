package com.onandor.notemanager.navigation

import androidx.navigation.NavOptions
import com.onandor.notemanager.navigation.NavDestinationArgs.NOTE_ID_ARG
import com.onandor.notemanager.navigation.Screens.ADD_EDIT_NOTE_SCREEN
import com.onandor.notemanager.navigation.Screens.ARCHIVE_SCREEN
import com.onandor.notemanager.navigation.Screens.NOTES_SCREEN
import com.onandor.notemanager.navigation.Screens.ONBOARDING_SCREEN
import com.onandor.notemanager.navigation.Screens.SETTINGS_SCREEN
import com.onandor.notemanager.navigation.Screens.SIGN_IN_REGISTER_SCREEN
import com.onandor.notemanager.navigation.Screens.TRASH_SCREEN
import com.onandor.notemanager.navigation.Screens.USER_DETAILS_SCREEN

private object Screens {
    const val NOTES_SCREEN = "notes"
    const val ADD_EDIT_NOTE_SCREEN = "addEditNote"
    const val ARCHIVE_SCREEN = "archive"
    const val TRASH_SCREEN = "trash"
    const val SETTINGS_SCREEN = "settings"
    const val ONBOARDING_SCREEN = "onboarding"
    const val SIGN_IN_REGISTER_SCREEN = "signInRegister"
    const val USER_DETAILS_SCREEN = "userDetails"
}

object NavDestinations {
    const val NOTES = NOTES_SCREEN
    const val ADD_EDIT_NOTE = "${ADD_EDIT_NOTE_SCREEN}?$NOTE_ID_ARG={$NOTE_ID_ARG}"
    const val ARCHIVE = ARCHIVE_SCREEN
    const val TRASH = TRASH_SCREEN
    const val SETTINGS = SETTINGS_SCREEN
    const val ONBOARDING = ONBOARDING_SCREEN
    const val SIGN_IN_REGISTER = SIGN_IN_REGISTER_SCREEN
    const val USER_DETAILS = USER_DETAILS_SCREEN
}

object NavDestinationArgs {
    const val NOTE_ID_ARG = "noteId"
}

interface NavAction {
    val destination: String
    val navOptions: NavOptions
        get() = NavOptions.Builder()
            .setPopUpTo(0, true)
            .setLaunchSingleTop(true)
            .build()
}

object NavActions {
    fun notes() = object : NavAction {
        override val destination: String = NavDestinations.NOTES
        override val navOptions: NavOptions = NavOptions.Builder()
            .setPopUpTo(0, true)
            .setLaunchSingleTop(true)
            .build()
    }

    fun addEditNote() = object : NavAction {
        override val destination: String = ADD_EDIT_NOTE_SCREEN
    }

    fun addEditNote(noteId: String) = object : NavAction {
        override val destination: String = "${ADD_EDIT_NOTE_SCREEN}?${NOTE_ID_ARG}=$noteId"
        override val navOptions: NavOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .build()
    }

    fun archive() = object : NavAction {
        override val destination: String = NavDestinations.ARCHIVE
        override val navOptions: NavOptions = NavOptions.Builder()
            .setPopUpTo(0, true)
            .setLaunchSingleTop(true)
            .build()
    }

    fun trash() = object : NavAction {
        override val destination: String = NavDestinations.TRASH
        override val navOptions: NavOptions = NavOptions.Builder()
            .setPopUpTo(0, true)
            .setLaunchSingleTop(true)
            .build()
    }

    fun settings() = object : NavAction {
        override val destination: String = NavDestinations.SETTINGS
    }

    fun signInRegister() = object : NavAction {
        override val destination: String = NavDestinations.SIGN_IN_REGISTER
    }

    fun userDetails() = object : NavAction {
        override val destination: String = NavDestinations.USER_DETAILS
    }

    fun onboarding() = object : NavAction {
        override val destination: String = NavDestinations.ONBOARDING
    }
}