package com.onandor.notemanager.data.remote.models

import java.time.LocalDateTime
import java.util.UUID

data class RemoteNote(
    val id: UUID,
    val userId: Int,
    val title: String,
    val content: String,
    val labels: List<RemoteLabel>,
    val location: Int,
    val creationDate: Long,
    val modificationDate: Long
)