package com.onandor.notemanager.data.remote.services

import com.onandor.notemanager.data.remote.models.RemoteLabel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import java.util.UUID
import javax.inject.Inject

private const val LABELS_ROUTE = "labels"

class LabelApiService @Inject constructor(
    private val httpClient: HttpClient
) : ILabelApiService {

    override suspend fun getAll(): List<RemoteLabel> {
        return httpClient.get(LABELS_ROUTE).body()
    }

    override suspend fun create(remoteLabel: RemoteLabel) {
        httpClient.post(LABELS_ROUTE) {
            setBody(remoteLabel)
        }
    }

    override suspend fun update(remoteLabel: RemoteLabel) {
        httpClient.put(LABELS_ROUTE) {
            setBody(remoteLabel)
        }
    }

    override suspend fun delete(labelId: UUID) {
        httpClient.delete("$LABELS_ROUTE/$labelId")
    }
}