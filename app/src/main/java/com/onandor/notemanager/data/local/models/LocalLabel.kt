package com.onandor.notemanager.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.onandor.notemanager.utils.LabelColorType
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "labels"
)
data class LocalLabel(
    @PrimaryKey var id: UUID,
    var title: String,
    var color: LabelColorType,
    var deleted: Boolean,
    @ColumnInfo(name="creation_date") var creationDate: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name="modification_date") var modificationDate: LocalDateTime = LocalDateTime.now()
)