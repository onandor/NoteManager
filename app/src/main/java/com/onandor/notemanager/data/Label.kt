package com.onandor.notemanager.data

import java.util.UUID

data class Label (
    val id: UUID,
    val title: String,
    val color: String
)