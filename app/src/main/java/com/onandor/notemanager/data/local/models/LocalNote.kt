package com.onandor.notemanager.data.local.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.onandor.notemanager.data.NoteLocation
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "notes"
)
data class LocalNote(
    @PrimaryKey val id: UUID,
    var title: String,
    var content: String,
    var location: NoteLocation,
    var pinned: Boolean,
    @ColumnInfo(name = "pin_hash") var pinHash: String,
    @ColumnInfo(name = "creation_date") var creationDate: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "modification_date") var modificationDate: LocalDateTime = LocalDateTime.now()
)

data class LocalNoteWithLabels(
    @Embedded val note: LocalNote,
    @Relation(
        entity = LocalLabel::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteLabel::class,
            parentColumn = "note_id",
            entityColumn = "label_id"
        )
    )
    val labels: List<LocalLabel>
)