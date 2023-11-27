package com.onandor.notemanager.data.remote.services

import com.onandor.notemanager.data.remote.models.RemoteLabel
import java.util.UUID

interface ILabelApiService {

    suspend fun getAll(): List<RemoteLabel>

    suspend fun create(remoteLabel: RemoteLabel)

    suspend fun update(remoteLabel: RemoteLabel)

    suspend fun delete(labelId: UUID)
}