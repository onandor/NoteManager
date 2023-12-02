package com.onandor.notemanager.data

import com.github.michaelbull.result.Result
import com.onandor.notemanager.data.remote.models.ApiError
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
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

    suspend fun synchronize(): Result<Unit, ApiError>

    suspend fun createNote(
        title: String,
        content: String,
        labels: List<Label>,
        location: NoteLocation,
        pinned: Boolean,
        pinHash: String,
        creationDate: LocalDateTime = LocalDateTime.now(),
        modificationDate: LocalDateTime = LocalDateTime.now()
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

    suspend fun updateNoteLabels(noteId: UUID, labels: List<Label>)

    suspend fun updateNoteLocation(noteId: UUID, location: NoteLocation)

    suspend fun updateNotePinned(noteId: UUID, pinned: Boolean)

    suspend fun updateNotePinHash(noteId: UUID, pinHash: String)

    suspend fun deleteNote(noteId: UUID)

    suspend fun emptyTrash()

    suspend fun deleteAllLocal()
}