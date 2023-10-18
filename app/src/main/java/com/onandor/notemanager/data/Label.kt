package com.onandor.notemanager.data

import com.onandor.notemanager.utils.LabelColor
import java.util.UUID

data class Label (
    val id: UUID,
    val title: String,
    val color: LabelColor
)