package com.onandor.notemanager.data.local.db

import androidx.room.Dao
import androidx.room.Query
import java.util.UUID

@Dao
interface NoteLabelDao {

    @Query("INSERT OR IGNORE INTO note_labels VALUES (:noteId, :labelId)")
    suspend fun insertOrIgnore(noteId: UUID, labelId: UUID)

    @Query("DELETE FROM note_labels WHERE note_id = :noteId")
    suspend fun deleteByNoteId(noteId: UUID)

    @Query("DELETE FROM note_labels WHERE label_id = :labelId")
    suspend fun deleteByLabelId(labelId: UUID)

    @Query("DELETE FROM note_labels WHERE note_id = :noteId AND label_id = :labelId")
    suspend fun deleteByNoteIdAndLabelId(noteId: UUID, labelId: UUID)

    @Query("DELETE FROM note_labels WHERE label_id NOT IN (:labelIds) AND note_id = :noteId")
    suspend fun deleteByNoteIdIfLabelIdNotInList(noteId: UUID, labelIds: List<UUID>)

    @Query("DELETE FROM note_labels")
    suspend fun deleteAll()
}