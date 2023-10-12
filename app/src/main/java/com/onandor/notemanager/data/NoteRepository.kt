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

    override fun getNoteStream(noteId: UUID): Flow<Note?> {
        return localDataSource.observeById(noteId).map { it?.toExternal() }
    }

    override fun getNotesStream(location: NoteLocation): Flow<List<Note>> {
        return localDataSource.observeAllByLocation(location).map { notes ->
            withContext(dispatcher) {
                notes.toExternal()
            }
        }
    }

    override suspend fun getNote(noteId: UUID): Note? {
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
    ): UUID {
        val noteId = withContext(dispatcher) {
            UUID.randomUUID()
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
        noteId: UUID,
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
        noteId: UUID,
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

    override suspend fun updateNoteLabels(noteId: UUID, labels: List<Label>) {
        val note = getNote(noteId)?.copy(labels = labels)
            ?: throw Exception("Note (id $noteId) not found in local database")

        localDataSource.upsert(note.toLocal())
    }

    override suspend fun updateNoteLocation(noteId: UUID, location: NoteLocation) {
        val note = getNote(noteId)?.copy(location = location)
            ?: throw Exception("Note (id $noteId) not found in local database")

        localDataSource.upsert(note.toLocal())
    }

    override suspend fun deleteNote(noteId: UUID) {
        localDataSource.deleteById(noteId)
    }

    override suspend fun emptyTrash() {
        localDataSource.deleteByLocation(NoteLocation.TRASH)
    }

    override suspend fun deleteAllLocal() {
        localDataSource.deleteAll()
    }
}