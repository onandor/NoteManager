package com.onandor.notemanager.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID

interface INoteRepository {

    fun getNoteStream(noteId: UUID): Flow<Note?>

    fun getNotesStream(location: NoteLocation): Flow<List<Note>>

    suspend fun getNote(noteId: UUID): Note?

    suspend fun getNotes(): List<Note>

    suspend fun createNote(
        title: String,
        content: String,
        labels: List<Label>,
        location: NoteLocation
    ): UUID

    suspend fun updateNote(
        noteId: UUID,
        title: String,
        content: String,
        labels: List<Label>,
        location: NoteLocation
    )

    suspend fun updateNoteTitleAndContent(
        noteId: UUID,
        title: String,
        content: String
    )

    suspend fun updateNoteLabels(noteId: UUID, labels: List<Label>)

    suspend fun updateNoteLocation(noteId: UUID, location: NoteLocation)

    suspend fun refreshNotes()

    suspend fun deleteNote(noteId: UUID)

    suspend fun emptyTrash()

    suspend fun deleteAllLocal()
}