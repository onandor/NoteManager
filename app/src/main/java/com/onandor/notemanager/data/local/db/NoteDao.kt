package com.onandor.notemanager.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.data.local.models.LocalLabel
import com.onandor.notemanager.data.local.models.LocalNote
import com.onandor.notemanager.data.local.models.LocalNoteWithLabels
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes")
    fun observeAll(): Flow<List<LocalNoteWithLabels>>

    @Query("SELECT * FROM notes WHERE location = :noteLocation")
    fun observeAllByLocation(noteLocation: NoteLocation): Flow<List<LocalNoteWithLabels>>

    @Query("""
        SELECT * FROM notes
        WHERE (location = :noteLocation
        AND (:searchString LIKE ""
            OR (title LIKE '%' || :searchString || '%'
            OR content LIKE '%' || :searchString || '%')))
        """)
    fun observeAllByLocationAndSearchString(noteLocation: NoteLocation, searchString: String):
            Flow<List<LocalNoteWithLabels>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun observeById(noteId: UUID): Flow<LocalNoteWithLabels?>

    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<LocalNoteWithLabels>

    @Query("SELECT * FROM notes WHERE location = :noteLocation")
    suspend fun getAllByLocation(noteLocation: NoteLocation): List<LocalNoteWithLabels>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getById(noteId: UUID): LocalNoteWithLabels?

    @Upsert
    suspend fun upsert(note: LocalNote)

    @Upsert
    suspend fun upsertAll(notes: List<LocalNote>)

    @Query("UPDATE notes SET title = :title, content = :content, modification_date = :modificationDate WHERE id = :noteId")
    suspend fun updateTitleAndContent(
        noteId: UUID,
        title: String,
        content: String,
        modificationDate: LocalDateTime
    )

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteById(noteId: UUID)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}