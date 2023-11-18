package com.onandor.notemanager.utils.undo

import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import java.util.UUID

data class NoteMoveSnapshot(
    val id: UUID,
    val location: NoteLocation,
    val pinned: Boolean,
    val pinHash: String
)

sealed class UndoableAction {

    data class NoteMove(val noteMoveSnapshots: List<NoteMoveSnapshot>) : UndoableAction()

    data class NoteDelete(val notes: List<Note>) : UndoableAction()
}

fun createNoteMoveSnapshot(note: Note): NoteMoveSnapshot {
    return NoteMoveSnapshot(
        id = note.id,
        location = note.location,
        pinned = note.pinned,
        pinHash = note.pinHash
    )
}
