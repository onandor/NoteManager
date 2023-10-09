package com.onandor.notemanager.data

import com.onandor.notemanager.data.local.models.LabelList
import com.onandor.notemanager.data.local.models.LocalLabel
import com.onandor.notemanager.data.local.models.LocalNote

fun Label.toLocal() = LocalLabel(
    id = id,
    title = title,
    color = color
)

fun List<Label>.toLocal() = LabelList(map(Label::toLocal))

fun LocalLabel.toExternal() = Label(
    id = id,
    title = title,
    color = color
)

fun LabelList.toExternal() = labelList.map(LocalLabel::toExternal)

fun List<LocalLabel>.toExternal() = map(LocalLabel::toExternal)

fun Note.toLocal() = LocalNote(
    id = id,
    title = title,
    content = content,
    labelList = labels.toLocal(),
    location = location,
    creationDate = creationDate,
    modificationDate = modificationDate
)

fun List<Note>.toLocal() = map(Note::toLocal)

fun LocalNote.toExternal() = Note(
    id = id,
    title = title,
    content = content,
    labels = labelList.toExternal(),
    location = location,
    creationDate = creationDate,
    modificationDate = modificationDate
)

fun List<LocalNote>.toExternal() = map(LocalNote::toExternal)