package com.onandor.notemanager.data

import java.time.LocalDateTime

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val labels: List<Label>,
    val location: NoteLocation,
    val creationDate: LocalDateTime,
    val modificationDate: LocalDateTime
)