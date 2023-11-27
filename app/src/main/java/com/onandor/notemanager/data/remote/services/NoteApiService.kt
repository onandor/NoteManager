package com.onandor.notemanager.data.remote.services

import com.onandor.notemanager.data.remote.models.RemoteNote
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.serialization.json.buildJsonObject
import java.util.UUID
import javax.inject.Inject

private const val NOTES_ROUTE = "notes"
private const val DELETE = "/delete"

class NoteApiService @Inject constructor(
    private val httpClient: HttpClient
) : INoteApiService {

    override suspend fun getAll(): List<RemoteNote> {
        return httpClient.get(NOTES_ROUTE).body()
    }

    override suspend fun getById(noteId: UUID): RemoteNote {
        return httpClient.get("$NOTES_ROUTE$noteId").body()
    }

    override suspend fun create(remoteNote: RemoteNote) {
        httpClient.post(NOTES_ROUTE) {
            setBody(remoteNote)
        }
    }

    override suspend fun update(remoteNote: RemoteNote) {
        httpClient.put(NOTES_ROUTE) {
            setBody(remoteNote)
        }
    }

    override suspend fun delete(noteId: UUID) {
        httpClient.delete("$NOTES_ROUTE$DELETE/$noteId")
    }

    override suspend fun deleteMultiple(noteIds: List<UUID>) {
        httpClient.post("$NOTES_ROUTE$DELETE") {
            setBody(noteIds)
        }
    }
}