package com.onandor.notemanager.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes")
    fun observeAll(): Flow<List<LocalNote>>

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun observerById(noteId: String): Flow<LocalNote>

    @Query("SELECT * FROM notes")
    suspend fun getAll(): List<LocalNote>

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

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteById(noteId: String)
}