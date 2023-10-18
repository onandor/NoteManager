package com.onandor.notemanager.data.mapping

import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.data.remote.models.RemoteLabel
import com.onandor.notemanager.data.remote.models.RemoteNote
import com.onandor.notemanager.utils.LabelColor
import com.onandor.notemanager.utils.LabelColorType
import com.onandor.notemanager.utils.labelColors
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun Label.toRemote(userId: Int) = RemoteLabel(
    id = id,
    userId = userId,
    title = title,
    color = color.type.value
)

fun RemoteLabel.toExternal() = Label(
    id = id,
    title = title,
    color = labelColors[LabelColorType.fromInt(color)]!!
)

@JvmName("externalToRemoteLabelList")
fun List<Label>.toRemote(userId: Int) = map { label -> label.toRemote(userId) }

@JvmName("remoteToExternalLabelList")
fun List<RemoteLabel>.toExternal() = map(RemoteLabel::toExternal)

fun Note.toRemote(userId: Int) = RemoteNote(
    id = id,
    userId = userId,
    title = title,
    content = content,
    labels = labels.toRemote(userId),
    location = location.value,
    creationDate = creationDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    modificationDate = modificationDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
)

fun RemoteNote.toExternal() = Note(
    id = id,
    title = title,
    content = content,
    labels = labels.toExternal(),
    location = NoteLocation.fromInt(location),
    creationDate = Instant
        .ofEpochMilli(creationDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime(),
    modificationDate = Instant
        .ofEpochMilli(modificationDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
)

@JvmName("externalToRemoteNoteList")
fun List<Note>.toRemote(userId: Int) = map { note -> note.toRemote(userId) }

@JvmName("remoteToExternalNoteList")
fun List<RemoteNote>.toExternal() = map(RemoteNote::toExternal)