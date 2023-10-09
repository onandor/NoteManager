package com.onandor.notemanager.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.data.local.models.LabelList
import com.onandor.notemanager.data.local.models.LocalNote
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes")
    fun observeAll(): Flow<List<LocalNote>>

    @Query("SELECT * FROM notes WHERE location = :noteLocation")
    fun observeAllByLocation(noteLocation: NoteLocation): Flow<List<LocalNote>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun observeById(noteId: String): Flow<LocalNote?>

    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<LocalNote>

    @Query("SELECT * FROM notes WHERE location = :noteLocation")
    suspend fun getAllByLocation(noteLocation: NoteLocation): List<LocalNote>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getById(noteId: String): LocalNote?

    @Upsert
    suspend fun upsert(note: LocalNote)

    @Upsert
    suspend fun upsertAll(notes: List<LocalNote>)

    @Query("UPDATE notes SET title = :title, content = :content, modification_date = :modificationDate WHERE id = :noteId")
    suspend fun updateTitleAndContent(
        noteId: String,
        title: String,
        content: String,
        modificationDate: LocalDateTime
    )

    @Query("UPDATE notes SET label_list = :labelList, modification_date = :modificationDate WHERE id = :noteId")
    suspend fun updateLabels(
        noteId: String,
        labelList: LabelList,
        modificationDate: LocalDateTime
    )

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteById(noteId: String)

    @Query("DELETE FROM notes WHERE location = :location")
    suspend fun deleteByLocation(location: NoteLocation)

    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}