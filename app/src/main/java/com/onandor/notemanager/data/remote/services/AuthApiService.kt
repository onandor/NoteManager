package com.onandor.notemanager.data.remote.services

import com.onandor.notemanager.data.remote.models.AuthUser
import com.onandor.notemanager.data.remote.models.TokenPair
import com.onandor.notemanager.data.remote.models.UserDetails
import com.onandor.notemanager.data.remote.services.AuthEndpoints.CHANGE_PASSWORD
import com.onandor.notemanager.data.remote.services.AuthEndpoints.DELETE
import com.onandor.notemanager.data.remote.services.AuthEndpoints.LOGIN
import com.onandor.notemanager.data.remote.services.AuthEndpoints.LOGOUT
import com.onandor.notemanager.data.remote.services.AuthEndpoints.REGISTER
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

private const val AUTH_ROUTE = "auth"
private object AuthEndpoints {
    const val REGISTER = "/register"
    const val LOGIN = "/login"
    const val LOGOUT = "/logout"
    const val DELETE = "/delete"
    const val CHANGE_PASSWORD = "/changePassword"
}

class AuthApiService @Inject constructor(
    private val httpClient: HttpClient
) : IAuthApiService {

    override suspend fun register(authUser: AuthUser): UserDetails {
        return httpClient.post("$AUTH_ROUTE$REGISTER") {
            setBody(authUser)
        }.body()
    }

    override suspend fun login(authUser: AuthUser): TokenPair {
        return httpClient.post("$AUTH_ROUTE$LOGIN") {
            setBody(authUser)
        }.body()
    }

    override suspend fun logout(authUser: AuthUser) {
        httpClient.post("$AUTH_ROUTE$LOGOUT") {
            setBody(authUser)
        }
    }

    override suspend fun deleteUser(password: String) {
        httpClient.post("$AUTH_ROUTE$DELETE") {
            setBody(password)
        }
    }

    override suspend fun changePassword(
        installationId: String,
        oldPassword: String,
        newPassword: String
    ): HashMap<String, String> {
        return httpClient.post("$AUTH_ROUTE$CHANGE_PASSWORD") {
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