package com.onandor.notemanager.data.remote.sources

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.ResultBinding
import com.onandor.notemanager.data.remote.models.ApiError
import com.onandor.notemanager.data.remote.models.RemoteNote
import java.util.UUID

interface INoteDataSource {

    suspend fun getAll(): Result<List<RemoteNote>, ApiError>

    suspend fun getById(noteId: UUID): Result<RemoteNote, ApiError>

    suspend fun create(remoteNote: RemoteNote): Result<Unit, ApiError>

    suspend fun update(remoteNote: RemoteNote): Result<Unit, ApiError>

    suspend fun synchronize(remoteNote: RemoteNote): Result<Unit, ApiError>

    suspend fun synchronize(remoteNotes: List<RemoteNote>): Result<Unit, ApiError>

    suspend fun delete(noteId: UUID): Result<Unit, ApiError>

    suspend fun deleteMultiple(noteIds: List<UUID>): Result<Unit, ApiError>
}