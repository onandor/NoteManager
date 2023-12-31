package com.onandor.notemanager.data

import java.time.LocalDateTime
import java.util.UUID

data class Note(
    val id: UUID,
    val title: String,
    val content: String,
    val labels: List<Label>,
    val location: NoteLocation,
    val pinned: Boolean,
    val pinHash: String,
    val deleted: Boolean,
    val creationDate: LocalDateTime,
    val modificationDate: LocalDateTime
)