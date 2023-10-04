package com.onandor.notemanager.data.remote.services

import com.onandor.notemanager.data.remote.models.AuthUser
import com.onandor.notemanager.data.remote.models.TokenPair
import com.onandor.notemanager.data.remote.models.UserDetails
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
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
}