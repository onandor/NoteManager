package com.onandor.notemanager.data.remote.sources

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onandor.notemanager.data.remote.models.ApiError
import com.onandor.notemanager.data.remote.models.DefaultRequest
import com.onandor.notemanager.data.remote.models.LabelNotFound
import com.onandor.notemanager.data.remote.models.RemoteLabel
import com.onandor.notemanager.data.remote.services.ILabelApiService
import com.onandor.notemanager.utils.getApiError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import java.util.UUID
import javax.inject.Inject

class LabelDataSource @Inject constructor(
    private val labelApiService: ILabelApiService
): ILabelDataSource {

    override suspend fun getAll(): Result<List<RemoteLabel>, ApiError> {
        return try {
            Ok(labelApiService.getAll())
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun create(remoteLabel: RemoteLabel): Result<Unit, ApiError> {
        return try {
            Ok(labelApiService.create(remoteLabel))
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun update(remoteLabel: RemoteLabel): Result<Unit, ApiError> {
        return try {
            Ok(labelApiService.update(remoteLabel))
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun synchronize(remoteLabels: List<RemoteLabel>): Result<Unit, ApiError> {
        return try {
            Ok(labelApiService.synchronize(remoteLabels))
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun delete(labelId: UUID): Result<Unit, ApiError> {
        return try {
            Ok(labelApiService.delete(labelId))
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) {
                Err(LabelNotFound)
            } else {
                Err(DefaultRequest)
            }
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }
}