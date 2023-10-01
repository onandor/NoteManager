package com.onandor.notemanager.data

import com.onandor.notemanager.data.local.db.NoteDao
import com.onandor.notemanager.di.ApplicationScope
import com.onandor.notemanager.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val localDataSource: NoteDao,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope
) : INoteRepository {
    override fun getNoteStream(noteId: String): Flow<Note?> {
        return localDataSource.observeById(noteId).map { it.toExternal() }
    }

    override fun getNotesStream(location: NoteLocation): Flow<List<Note>> {
        return localDataSource.observeAllByLocation(location).map { notes ->
            withContext(dispatcher) {
                notes.toExternal()
            }
        }
    }

    override suspend fun getNote(noteId: String): Note? {
        return localDataSource.getById(noteId)?.toExternal()
    }

    override suspend fun getNotes(): List<Note> {
        return withContext(dispatcher) {
            localDataSource.getAll().toExternal()
        }
    }

    override suspend fun createNote(
        title: String,
        content: String,
        labels: List<Label>,
        location: NoteLocation,
        creationDate: LocalDateTime,
        modificationDate: LocalDateTime
    ): String {
        val noteId = withContext(dispatcher) {
            UUID.randomUUID().toString()
        }
        val note = Note(
            id = noteId,
            title = title,
            content = content,
            labels = labels,
            location = location,
            creationDate = creationDate,
            modificationDate = modificationDate
        )

        localDataSource.upsert(note.toLocal())
        return noteId
    }

    override suspend fun updateNote(
        noteId: String,
        title: String,
        content: String,
        labels: List<Label>,
        location: NoteLocation,
        modificationDate: LocalDateTime
    ) {
        val note = getNote(noteId)?.copy(
            title = title,
            content = content,
            labels = labels,
            location = location,
            modificationDate = modificationDate
        ) ?: throw Exception("Note (id $noteId) not found in local database")

        localDataSource.upsert(note.toLocal())
    }

    override suspend fun updateNoteTitleAndContent(
        noteId: String,
        title: String,
        content: String,
        modificationDate: LocalDateTime
    ) {
        val note = getNote(noteId)?.copy(
            title = title,
            content = content,
            modificationDate = modificationDate
        ) ?: throw Exception("Note (id $noteId) not found in local database")

        localDataSource.upsert(note.toLocal())
    }

    override suspend fun updateNoteLabels(noteId: String, labels: List<Label>) {
        val note = getNote(noteId)?.copy(labels = labels)
            ?: throw Exception("Note (id $noteId) not found in local database")

        localDataSource.upsert(note.toLocal())
    }

    override suspend fun updateNoteLocation(noteId: String, location: NoteLocation) {
        val note = getNote(noteId)?.copy(location = location)
            ?: throw Exception("Note (id $noteId) not found in local database")

        localDataSource.upsert(note.toLocal())
    }

    override suspend fun deleteNote(noteId: String,) {
        localDataSource.deleteById(noteId)
    }

    override suspend fun emptyTrash() {
        localDataSource.deleteByLocation(NoteLocation.TRASH)
    }
}