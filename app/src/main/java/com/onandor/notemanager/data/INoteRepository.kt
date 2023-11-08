package com.onandor.notemanager.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface INoteRepository {

    fun getNoteStream(noteId: UUID): Flow<Note?>

    fun getNotesStream(location: NoteLocation): Flow<List<Note>>

    fun getSearchedNotesStream(
        location: NoteLocation,
        search: String = "",
        labels: List<Label> = emptyList()
    ): Flow<List<Note>>

    fun getSearchedNotesStream(
        locations: List<NoteLocation>,
        search: String = "",
        labels: List<Label> = emptyList()
    ): Flow<List<Note>>

    suspend fun getNote(noteId: UUID): Note?

    suspend fun getNotes(): List<Note>

    suspend fun createNote(
        title: String,
        content: String,
        labels: List<Label>,
        location: NoteLocation,
        pinned: Boolean,
        pinHash: String
    ): UUID

    suspend fun updateNote(
        noteId: UUID,
        title: String,
        content: String,
        labels: List<Label>,
        location: NoteLocation,
        pinned: Boolean,
        pinHash: String
    )

    suspend fun updateNoteTitleAndContent(
        noteId: UUID,
        title: String,
        content: String
    )

    suspend fun updateNoteLabels(noteId: UUID, labels: List<Label>)

    suspend fun updateNoteLocation(noteId: UUID, location: NoteLocation)

    suspend fun updateNotePinned(noteId: UUID, pinned: Boolean)

    suspend fun updateNotePinHash(noteId: UUID, pinHash: String)

    suspend fun refreshNotes()

    suspend fun deleteNote(noteId: UUID)

    suspend fun emptyTrash()

    suspend fun deleteAllLocal()
}