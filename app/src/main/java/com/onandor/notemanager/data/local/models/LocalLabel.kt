package com.onandor.notemanager.data.local.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "labels"
)
data class LocalLabel(
    @PrimaryKey var name: String,
    var color: String
)