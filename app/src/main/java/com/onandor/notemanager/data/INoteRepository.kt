package com.onandor.notemanager.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface INoteRepository {

    fun getNoteStream(noteId: String): Flow<Note?>

    fun getNotesStream(location: NoteLocation): Flow<List<Note>>

    suspend fun getNote(noteId: String): Note?

    suspend fun getNotes(): List<Note>

    suspend fun createNote(
        title: String,
        content: String,
        labels: List<Label>,
        location: NoteLocation,
        creationDate: LocalDateTime,
        modificationDate: LocalDateTime
    ): String

    suspend fun updateNote(
        noteId: String,
        title: String,
        content: String,
        labels: List<Label>,
        location: NoteLocation,
        modificationDate: LocalDateTime
    )

    suspend fun updateNoteTitleAndContent(
        noteId: String,
        title: String,
        content: String,
        modificationDate: LocalDateTime
    )

    suspend fun updateNoteLabels(noteId: String, labels: List<Label>)

    suspend fun updateNoteLocation(noteId: String, location: NoteLocation)

    suspend fun deleteNote(noteId: String)

    suspend fun emptyTrash()
}