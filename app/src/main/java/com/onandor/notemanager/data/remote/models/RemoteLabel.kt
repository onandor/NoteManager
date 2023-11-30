package com.onandor.notemanager.data.remote.models

import java.util.UUID

data class RemoteLabel(
    val id: UUID,
    val userId: Int,
    val title: String,
    val color: Int,
    val deleted: Boolean,
    val creationDate: Long,
    val modificationDate: Long
)