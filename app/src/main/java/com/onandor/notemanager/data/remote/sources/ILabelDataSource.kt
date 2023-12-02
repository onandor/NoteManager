package com.onandor.notemanager.data.remote.sources

import com.github.michaelbull.result.Result
import com.onandor.notemanager.data.remote.models.ApiError
import com.onandor.notemanager.data.remote.models.RemoteLabel
import java.util.UUID

interface ILabelDataSource {

    suspend fun getAll(): Result<List<RemoteLabel>, ApiError>

    suspend fun getById(labelId: UUID): Result<RemoteLabel, ApiError>

    suspend fun create(remoteLabel: RemoteLabel): Result<Unit, ApiError>

    suspend fun update(remoteLabel: RemoteLabel): Result<Unit, ApiError>

    suspend fun synchronize(remoteLabel: RemoteLabel): Result<Unit, ApiError>

    suspend fun synchronize(remoteLabels: List<RemoteLabel>): Result<Unit, ApiError>

    suspend fun delete(labelId: UUID): Result<Unit, ApiError>
}