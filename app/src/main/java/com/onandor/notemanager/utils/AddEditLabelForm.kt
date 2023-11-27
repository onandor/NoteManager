package com.onandor.notemanager.utils

import java.time.LocalDateTime
import java.util.UUID

data class AddEditLabelForm(
    val id: UUID? = null,
    val title: String = "",
    val titleValid: Boolean = false,
    val color: LabelColor = LabelColors.none,
    val creationDate: LocalDateTime = LocalDateTime.now(),
    val modificationDate: LocalDateTime = LocalDateTime.now()
)