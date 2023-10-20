package com.onandor.notemanager.utils

import androidx.compose.runtime.compositionLocalOf

data class NoteListOptions(
    val collapsedView: Boolean = false
)

val LocalNoteListOptions = compositionLocalOf { NoteListOptions() }