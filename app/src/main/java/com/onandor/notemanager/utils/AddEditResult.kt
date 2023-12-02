package com.onandor.notemanager.utils

import androidx.annotation.StringRes
import com.onandor.notemanager.R

object AddEditResultTypes {
    const val NONE = 0
    const val SAVED = 1
    const val ARCHIVED = 2
    const val UNARCHIVED = 3
    const val TRASHED = 4
    const val DELETED = 5
    const val DISCARDED = 6
    const val DELETED_AFTER_SYNC = 7
}

data class AddEditResult(
    val type: Int,
    @StringRes val resource: Int
)

object AddEditResults {
    val NONE = AddEditResult(AddEditResultTypes.NONE, 0)
    val SAVED = AddEditResult(AddEditResultTypes.SAVED, R.string.addeditresult_saved)
    val ARCHIVED = AddEditResult(AddEditResultTypes.ARCHIVED, R.string.addeditresult_archived)
    val UNARCHIVED = AddEditResult(AddEditResultTypes.UNARCHIVED, R.string.addeditresult_unarchived)
    val TRASHED = AddEditResult(AddEditResultTypes.TRASHED, R.string.addeditresult_trashed)
    val DELETED = AddEditResult(AddEditResultTypes.DELETED, R.string.addeditresult_deleted)
    val DISCARDED = AddEditResult(AddEditResultTypes.DISCARDED, R.string.addeditresult_discarded)
    val DELETED_AFTER_SYNC = AddEditResult(AddEditResultTypes.DELETED_AFTER_SYNC, R.string.addeditresult_deleted_after_sync)
}

