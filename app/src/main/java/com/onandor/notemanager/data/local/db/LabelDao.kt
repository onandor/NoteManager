package com.onandor.notemanager.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.onandor.notemanager.data.local.models.LocalLabel
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface LabelDao {

    @Query("SELECT * FROM labels WHERE id = :labelId")
    fun observeById(labelId: UUID): Flow<LocalLabel?>

    @Query("SELECT * FROM labels")
    fun observeAll(): Flow<List<LocalLabel>>

    @Query("SELECT * FROM labels")
    suspend fun getAll(): List<LocalLabel>

    @Query("SELECT * FROM labels WHERE id = :labelId")
    suspend fun getById(labelId: UUID): LocalLabel?

    @Query("SELECT * FROM labels WHERE title = :labelTitle")
    suspend fun getByTitle(labelTitle: String): LocalLabel?

    @Upsert
    suspend fun upsert(label: LocalLabel)

    @Upsert
    suspend fun upsertAll(labels: List<LocalLabel>)

    @Query("DELETE FROM labels WHERE id = :labelId")
    suspend fun deleteById(labelId: UUID)

    @Query("DELETE FROM labels")
    suspend fun deleteAll()

    @Query("DELETE FROM labels WHERE deleted")
    suspend fun deleteAllSoftDeleted()
}