package com.onandor.notemanager.data.remote.sources

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
        val response = httpClient.post("auth/register") {
            setBody(authUser)
        }
        return response.body()
    }

    override suspend fun login(authUser: AuthUser): TokenPair {
        val response = httpClient.post("auth/login") {
            setBody(authUser)
        }
        return response.body()
    }

}