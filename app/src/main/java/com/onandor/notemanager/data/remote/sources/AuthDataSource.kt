package com.onandor.notemanager.data.remote.sources

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onandor.notemanager.data.remote.models.ApiError
import com.onandor.notemanager.data.remote.models.AuthUser
import com.onandor.notemanager.data.remote.models.DisposableError
import com.onandor.notemanager.data.remote.models.EmailTaken
import com.onandor.notemanager.data.remote.models.InvalidCredentials
import com.onandor.notemanager.data.remote.models.InvalidPassword
import com.onandor.notemanager.data.remote.models.InvalidRefreshTokenException
import com.onandor.notemanager.data.remote.models.LoggedOutError
import com.onandor.notemanager.data.remote.models.ServerError
import com.onandor.notemanager.data.remote.models.ServerUnreachable
import com.onandor.notemanager.data.remote.models.TokenPair
import com.onandor.notemanager.data.remote.models.UserDetails
import com.onandor.notemanager.data.remote.services.IAuthApiService
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import javax.inject.Inject

class AuthDataSource @Inject constructor(
    private val authApiService: IAuthApiService
) : IAuthDataSource {

    override suspend fun register(authUser: AuthUser): Result<UserDetails, ApiError> {
        return try {
            Ok(authApiService.register(authUser))
        } catch (e: ClientRequestException) {
            Err(EmailTaken)
        } catch (e: ServerResponseException) {
            Err(ServerError)
        } catch (e: ConnectTimeoutException) {
            Err(ServerUnreachable)
        }
    }

    override suspend fun login(authUser: AuthUser): Result<TokenPair, ApiError> {
        return try {
            Ok(authApiService.login(authUser))
        } catch (e: ClientRequestException) {
            Err(InvalidCredentials)
        } catch (e: ServerResponseException) {
            Err(ServerError)
        } catch (e: ConnectTimeoutException) {
            Err(ServerUnreachable)
        }
    }

    override suspend fun logout(authUser: AuthUser): Result<Unit, ApiError> {
        return try {
            Ok(authApiService.logout(authUser))
        } catch (e: ClientRequestException) {
            Err(DisposableError)
        } catch (e: ConnectTimeoutException) {
            Err(DisposableError)
        } catch (e: InvalidRefreshTokenException) {
            Err(LoggedOutError)
        }
    }

    override suspend fun deleteUser(password: String): Result<Unit, ApiError> {
        return try {
            Ok(authApiService.deleteUser(password))
        } catch (e: ClientRequestException) {
            Err(InvalidPassword)
        } catch (e: ConnectTimeoutException) {
            Err(ServerUnreachable)
        } catch (e: InvalidRefreshTokenException) {
            Err(LoggedOutError)
        }
    }

    override suspend fun changePassword(
        oldPassword: String,
        newPassword: String
    ): Result<Unit, ApiError> {
        return try {
            Ok(authApiService.changePassword(oldPassword, newPassword))
        } catch (e: ClientRequestException) {
            Err(InvalidPassword)
        } catch (e: ConnectTimeoutException) {
            Err(ServerUnreachable)
        } catch (e: InvalidRefreshTokenException) {
            Err(LoggedOutError)
        }
    }
}