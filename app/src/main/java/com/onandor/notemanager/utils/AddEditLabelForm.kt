package com.onandor.notemanager.utils

import java.util.UUID

data class AddEditLabelForm(
    val id: UUID? = null,
    val title: String = "",
    val titleValid: Boolean = false,
    val color: LabelColor = LabelColors.none
)