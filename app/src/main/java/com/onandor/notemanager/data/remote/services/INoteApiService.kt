package com.onandor.notemanager.data.remote.services

import com.onandor.notemanager.data.remote.models.RemoteNote
import java.util.UUID

interface INoteApiService {

    suspend fun getAll(): List<RemoteNote>
    suspend fun getById(noteId: UUID): RemoteNote

    suspend fun create(remoteNote: RemoteNote)

    suspend fun update(remoteNote: RemoteNote)

    suspend fun delete(noteId: UUID)

    suspend fun deleteMultiple(noteIds: List<UUID>)
}