package com.onandor.notemanager.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.UUID

@Entity(
    tableName = "note_labels",
    primaryKeys = ["note_id", "label_id"]
)
data class NoteLabel(
    @ColumnInfo(name = "note_id") val noteId: UUID,
    @ColumnInfo(name = "label_id") val labelId: UUID
)