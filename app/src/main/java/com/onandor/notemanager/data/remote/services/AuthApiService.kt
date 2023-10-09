package com.onandor.notemanager.data.remote.services

import com.onandor.notemanager.data.remote.models.AuthUser
import com.onandor.notemanager.data.remote.models.TokenPair
import com.onandor.notemanager.data.remote.models.UserDetails
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import javax.inject.Inject

class AuthApiService @Inject constructor(
    private val httpClient: HttpClient
) : IAuthApiService {

    override suspend fun register(authUser: AuthUser): UserDetails {
        return httpClient.post("auth/register") {
            setBody(authUser)
        }.body()
    }

    override suspend fun login(authUser: AuthUser): TokenPair {
        return httpClient.post("auth/login") {
            setBody(authUser)
        }.body()
    }

    override suspend fun logout(authUser: AuthUser) {
        httpClient.post("auth/logout") {
            setBody(authUser)
        }
    }

    override suspend fun deleteUser(password: String) {
        httpClient.post("auth/delete") {
            setBody(password)
        }
    }

    override suspend fun changePassword(
        installationId: String,
        oldPassword: String,
        newPassword: String
    ): HashMap<String, String> {
        return httpClient.post("auth/changePassword") {
            setBody(
                hashMapOf(
                    "deviceId" to installationId,
                    "oldPassword" to oldPassword,
                    "newPassword" to newPassword
                )
            )
        }.body()
    }
}