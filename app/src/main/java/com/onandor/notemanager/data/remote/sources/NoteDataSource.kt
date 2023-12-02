package com.onandor.notemanager.data.remote.sources

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onandor.notemanager.data.remote.models.ApiError
import com.onandor.notemanager.data.remote.models.DefaultRequest
import com.onandor.notemanager.data.remote.models.NoteNotFound
import com.onandor.notemanager.data.remote.models.RemoteNote
import com.onandor.notemanager.data.remote.services.INoteApiService
import com.onandor.notemanager.utils.getApiError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import java.util.UUID
import javax.inject.Inject

class NoteDataSource @Inject constructor(
    private val noteApiService: INoteApiService
) : INoteDataSource {

    override suspend fun getAll(): Result<List<RemoteNote>, ApiError> {
        return try {
            Ok(noteApiService.getAll())
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }
    override suspend fun getById(noteId: UUID): Result<RemoteNote, ApiError> {
        return try {
            Ok(noteApiService.getById(noteId))
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound)
                Err(NoteNotFound)
            else
                Err(DefaultRequest)
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun create(remoteNote: RemoteNote): Result<Unit, ApiError> {
        return try {
            Ok(noteApiService.create(remoteNote))
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun update(remoteNote: RemoteNote): Result<Unit, ApiError> {
        return try {
            Ok(noteApiService.update(remoteNote))
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun synchronize(remoteNote: RemoteNote): Result<Unit, ApiError> {
        return try {
            Ok(noteApiService.synchronize(remoteNote))
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun synchronize(remoteNotes: List<RemoteNote>): Result<Unit, ApiError> {
        return try {
            Ok(noteApiService.synchronize(remoteNotes))
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun delete(noteId: UUID): Result<Unit, ApiError> {
        return try {
            Ok(noteApiService.delete(noteId))
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound)
                Err(NoteNotFound)
            else
                Err(DefaultRequest)
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun deleteMultiple(noteIds: List<UUID>): Result<Unit, ApiError> {
        return try {
            Ok(noteApiService.deleteMultiple(noteIds))
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }
}