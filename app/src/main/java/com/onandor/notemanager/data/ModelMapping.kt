package com.onandor.notemanager.data

import com.onandor.notemanager.data.local.models.LocalLabel
import com.onandor.notemanager.data.local.models.LocalNote
import com.onandor.notemanager.data.local.models.LocalNoteWithLabels

fun Label.toLocal() = LocalLabel(
    id = id,
    title = title,
    color = color
)

fun LocalLabel.toExternal() = Label(
    id = id,
    title = title,
    color = color
)

@JvmName("toExternalLabelList")
fun List<LocalLabel>.toExternal() = map(LocalLabel::toExternal)

@JvmName("toLocalLabelList")
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

@JvmName("toLocalNoteList")
fun List<Note>.toLocal() = map(Note::toLocal)

@JvmName("toLocalNoteListWithLabels")
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

@JvmName("toExternalNoteList")
fun List<LocalNote>.toExternal() = map(LocalNote::toExternal)

@JvmName("toExternalNoteListWithLabels")
fun List<LocalNoteWithLabels>.toExternal() = map(LocalNoteWithLabels::toExternal)