package com.onandor.notemanager.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.onandor.notemanager.data.local.models.LocalLabel
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {

    @Query("SELECT * FROM labels")
    fun observeAll(): Flow<List<LocalLabel>>

    @Query("SELECT * FROM labels")
    suspend fun getAll(): List<LocalLabel>

    @Query("SELECT * FROM labels WHERE name = :labelName")
    suspend fun getByName(labelName: String): LocalLabel?

    @Upsert
    suspend fun upsert(label: LocalLabel)

    @Upsert
    suspend fun upsertAll(labels: List<LocalLabel>)

    @Query("DELETE FROM labels WHERE name = :labelName")
    suspend fun deleteByName(labelName: String)
}