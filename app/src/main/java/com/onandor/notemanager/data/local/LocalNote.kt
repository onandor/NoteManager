package com.onandor.notemanager.data.local

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "notes"
)
data class LocalNote(
    @PrimaryKey val id: String,
    var title: String,
    var content: String,
    @ColumnInfo(name = "creation_date") var creationDate: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "modification_date")var modificationDate: LocalDateTime = LocalDateTime.now()
)