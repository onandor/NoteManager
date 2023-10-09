package com.onandor.notemanager.data.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "labels"
)
data class LocalLabel(
    @PrimaryKey var id: UUID,
    var title: String,
    var color: String
)