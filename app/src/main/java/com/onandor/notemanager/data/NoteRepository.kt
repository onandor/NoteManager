package com.onandor.notemanager.data

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.onandor.notemanager.data.local.datastore.ISettings
import com.onandor.notemanager.data.local.datastore.SettingsKeys
import com.onandor.notemanager.data.local.db.NoteDao
import com.onandor.notemanager.data.local.db.NoteLabelDao
import com.onandor.notemanager.data.mapping.toExternal
import com.onandor.notemanager.data.mapping.toLocal
import com.onandor.notemanager.data.mapping.toRemote
import com.onandor.notemanager.data.remote.models.ApiError
import com.onandor.notemanager.data.remote.models.LocalNoteNotFound
import com.onandor.notemanager.data.remote.models.NotLoggedIn
import com.onandor.notemanager.data.remote.models.NoteNotFound
import com.onandor.notemanager.data.remote.models.RemoteNote
import com.onandor.notemanager.data.remote.sources.INoteDataSource
import com.onandor.notemanager.di.ApplicationScope
import com.onandor.notemanager.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val noteLabelDao: NoteLabelDao,
    private val remoteDataSource: INoteDataSource,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope,
    private val settings: ISettings
) : INoteRepository {

    private suspend fun updateRemoteNote(note: Note) {
        val userId = settings.getInt(SettingsKeys.USER_ID)
        if (userId > 0)
            remoteDataSource.update(note.toRemote(userId))
    }

    override fun getNoteStream(noteId: UUID): Flow<Note?> {
        return noteDao.observeById(noteId).map { it?.toExternal() }
    }

    override fun getNotesStream(location: NoteLocation): Flow<List<Note>> {
        return noteDao.observeAllByLocation(location).map { notes ->
            withContext(dispatcher) {
                notes.toExternal()
            }
        }
    }

    override fun getSearchedNotesStream(location: NoteLocation, search: String, labels: List<Label>): Flow<List<Note>> {
        return noteDao.observeAllByLocationAndSearchString(location, search).map { notes ->
            withContext(dispatcher) {
                notes.toExternal().filter { note ->
                    note.labels.containsAll(labels)
                }
            }
        }
    }

    override fun getSearchedNotesStream(locations: List<NoteLocation>, search: String, labels: List<Label>): Flow<List<Note>> {
        return noteDao.observeAllByMultipleLocationsAndSearchString(locations, search).map { notes ->
            withContext(dispatcher) {
                notes.toExternal().filter { note ->
                    note.labels.containsAll(labels)
                }.sortedBy { note -> note.location }
            }
        }
    }

    override suspend fun getNote(noteId: UUID): Note? {
        return noteDao.getById(noteId)?.toExternal()
    }

    override suspend fun getNotes(): List<Note> {
        return withContext(dispatcher) {
            noteDao.getAll().toExternal()
        }
    }

    override suspend fun synchronizeSingle(noteId: UUID): Result<Unit, ApiError> {
        val userId = settings.getInt(SettingsKeys.USER_ID)
        if (userId <= 0)
            return Err(NotLoggedIn)

        val localNote = noteDao.getById(noteId)?.toExternal()?.toRemote(userId)
            ?: return Err(LocalNoteNotFound)
        remoteDataSource.synchronize(localNote)
        lateinit var remoteNote: RemoteNote
        remoteDataSource.getById(noteId)
            .onSuccess { _remoteNote ->
                remoteNote = _remoteNote
            }
            .onFailure { apiError ->
                if (apiError == NoteNotFound) {
                    noteLabelDao.deleteByNoteId(noteId)
                    noteDao.deleteById(noteId)
                }
                return Err(apiError)
            }
        if (localNote.modificationDate == remoteNote.modificationDate) {
            return Ok(Unit)
        }
        noteDao.upsert(remoteNote.toExternal().toLocal())
        val labels = remoteNote.labels.toExternal().toLocal()
        noteLabelDao.deleteByNoteIdIfLabelIdNotInList(remoteNote.id, labels.map { label -> label.id })
        labels.forEach { label ->
            noteLabelDao.insertOrIgnore(remoteNote.id, label.id)
        }
        return Ok(Unit)
    }

    override suspend fun synchronize(): Result<Unit, ApiError> {
        val userId = settings.getInt(SettingsKeys.USER_ID)
        if (userId <= 0)
            return Err(NotLoggedIn)

        val localNotes = noteDao.getAll().toExternal().toRemote(userId)
        remoteDataSource.synchronize(localNotes)
        lateinit var remoteNotes: List<RemoteNote>
        remoteDataSource.getAll()
            .onSuccess { _remoteNotes ->
                remoteNotes = _remoteNotes
            }
            .onFailure { apiError ->
                return Err(apiError)
            }
        val modifiedNotes = remoteNotes.filterNot { remoteNote ->
            localNotes.any { localNote ->
                remoteNote.id == localNote.id
                        && remoteNote.modificationDate == localNote.modificationDate
            }
        }
        val deletedNotes = localNotes.filterNot { localNote ->
            remoteNotes.any { remoteNote -> localNote.id == remoteNote.id }
        }
        noteDao.deleteAllSoftDeleted()
        deletedNotes.forEach { note ->
            noteLabelDao.deleteByNoteId(note.id)
            noteDao.deleteById(note.id)
        }
        noteDao.upsertAll(modifiedNotes.toExternal().toLocal())
        modifiedNotes.forEach { note ->
            val labels = note.labels.toExternal().toLocal()
            noteLabelDao.deleteByNoteIdIfLabelIdNotInList(note.id, labels.map { label -> label.id })
            labels.forEach { label ->
                noteLabelDao.insertOrIgnore(note.id, label.id)
            }
        }
        return Ok(Unit)
    }

    override suspend fun createNote(
        title: String,
        content: String,
        labels: List<Label>,
        location: NoteLocation,
        pinned: Boolean,
        pinHash: String,
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
            pinned = pinned,
            pinHash = pinHash,
            deleted = false,
            creationDate = creationDate,
            modificationDate = modificationDate
        )

        noteDao.upsert(note.toLocal())
        labels.forEach { label ->
            noteLabelDao.insertOrIgnore(noteId, label.id)
        }

        val userId = settings.getInt(SettingsKeys.USER_ID)
        if (userId > 0)
            remoteDataSource.create(note.toRemote(userId))
        return noteId
    }

    override suspend fun updateNote(
        noteId: UUID,
        title: String,
        content: String,
        labels: List<Label>,
        location: NoteLocation,
        pinned: Boolean,
        pinHash: String
    ) {
        val note = getNote(noteId)?.copy(
            title = title,
            content = content,
            labels = labels,
            location = location,
            pinned = pinned,
            pinHash = pinHash,
            deleted = false,
            modificationDate = LocalDateTime.now()
        ) ?: throw Exception("Note (id $noteId) not found in local database")

        updateNoteLabels(noteId, labels)
        noteDao.upsert(note.toLocal())
        updateRemoteNote(note)
    }

    override suspend fun updateNoteLabels(noteId: UUID, labels: List<Label>) {
        val note = getNote(noteId)?.copy(labels = labels)
            ?: throw Exception("Note (id $noteId) not found in local database")

        val localLabels = labels.toLocal()
        noteLabelDao.deleteByNoteIdIfLabelIdNotInList(noteId, localLabels.map { label -> label.id })
        labels.forEach { label ->
            noteLabelDao.insertOrIgnore(noteId, label.id)
        }
        updateRemoteNote(note)
    }

    override suspend fun updateNoteLocation(noteId: UUID, location: NoteLocation) {
        val note = getNote(noteId)?.copy(location = location)
            ?: throw Exception("Note (id $noteId) not found in local database")

        noteDao.upsert(note.toLocal())
        updateRemoteNote(note)
    }

    override suspend fun updateNotePinned(noteId: UUID, pinned: Boolean) {
        val note = getNote(noteId)?.copy(pinned = pinned)
            ?: throw Exception("Note (id $noteId) not found in local database")

        noteDao.upsert(note.toLocal())
        updateRemoteNote(note)
    }

    override suspend fun updateNotePinHash(noteId: UUID, pinHash: String) {
        val note = getNote(noteId)?.copy(pinHash = pinHash)
            ?: throw Exception("Note (id $noteId) not found in local database")

        noteDao.upsert(note.toLocal())
        updateRemoteNote(note)
    }

    override suspend fun deleteNote(noteId: UUID) {
        noteLabelDao.deleteByNoteId(noteId)

        var remoteDeleteFailed = false
        val userId = settings.getInt(SettingsKeys.USER_ID)
        if (userId > 0) {
            remoteDataSource.delete(noteId)
                .onSuccess {
                    noteDao.deleteById(noteId)
                }
                .onFailure {
                    remoteDeleteFailed = true
                }
        } else if (userId <= 0 || remoteDeleteFailed) {
            var note = noteDao.getById(noteId)?.note ?: return
            note = note.copy(deleted = true)
            noteDao.upsert(note)
        }
    }

    override suspend fun emptyTrash() {
        val notesWithLabels = noteDao.getAllByLocation(NoteLocation.TRASH)
        notesWithLabels.forEach { noteWithLabel ->
            noteLabelDao.deleteByNoteId(noteWithLabel.note.id)
        }

        val userId = settings.getInt(SettingsKeys.USER_ID)
        var remoteDeleteFailed = false
        if (userId > 0) {
            remoteDataSource.deleteMultiple(
                notesWithLabels.map { noteWithLabels -> noteWithLabels.note.id }
            )
                .onSuccess {
                    notesWithLabels.forEach { noteWithLabel ->
                        noteDao.deleteById(noteWithLabel.note.id)
                    }
                }
                .onFailure {
                    remoteDeleteFailed = true
                }
        } else if (userId <= 0 || remoteDeleteFailed) {
            noteDao.upsertAll(
                notesWithLabels.map { noteWithLabel ->
                    noteWithLabel.note.copy(deleted = true)
                }
            )
        }
    }

    override suspend fun deleteAllLocal() {
        noteDao.deleteAll()
        noteLabelDao.deleteAll()
    }
}