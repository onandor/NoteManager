package com.onandor.notemanager.data.mapping

import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.local.models.LocalLabel
import com.onandor.notemanager.data.local.models.LocalNote
import com.onandor.notemanager.data.local.models.LocalNoteWithLabels
import com.onandor.notemanager.utils.labelColors

fun Label.toLocal() = LocalLabel(
    id = id,
    title = title,
    color = color.type
)

fun LocalLabel.toExternal() = Label(
    id = id,
    title = title,
    color = labelColors[color]!!
)

@JvmName("localToExternalLabelList")
fun List<LocalLabel>.toExternal() = map(LocalLabel::toExternal)

@JvmName("externalToLocalLabelList")
fun List<Label>.toLocal() = map(Label::toLocal)

fun Note.toLocal() = LocalNote(
    id = id,
    title = title,
    content = content,
    location = location,
    creationDate = creationDate,
    modificationDate = modificationDate
)

fun Note.toLocalWithLabels() = LocalNoteWithLabels(
    note = this.toLocal(),
    labels = labels.toLocal()
)

@JvmName("externalToLocalNoteList")
fun List<Note>.toLocal() = map(Note::toLocal)

@JvmName("externalToLocalNoteListWithLabels")
fun List<Note>.toLocalWithLabels() = map(Note::toLocalWithLabels)

fun LocalNote.toExternal() = Note(
    id = id,
    title = title,
    content = content,
    labels = emptyList(),
    location = location,
    creationDate = creationDate,
    modificationDate = modificationDate
)

fun LocalNoteWithLabels.toExternal() = note.toExternal().copy(labels = labels.toExternal())

@JvmName("localToExternalNoteList")
fun List<LocalNote>.toExternal() = map(LocalNote::toExternal)

@JvmName("localToExternalNoteListWithLabels")
fun List<LocalNoteWithLabels>.toExternal() = map(LocalNoteWithLabels::toExternal)