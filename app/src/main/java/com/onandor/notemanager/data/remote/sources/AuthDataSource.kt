package com.onandor.notemanager.data.remote.sources

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.onandor.notemanager.data.remote.models.ApiError
import com.onandor.notemanager.data.remote.models.AuthUser
import com.onandor.notemanager.data.remote.models.EmailTaken
import com.onandor.notemanager.data.remote.models.InvalidCredentials
import com.onandor.notemanager.data.remote.models.InvalidPassword
import com.onandor.notemanager.data.remote.models.TokenPair
import com.onandor.notemanager.data.remote.models.UserDetails
import com.onandor.notemanager.data.remote.services.IAuthApiService
import com.onandor.notemanager.utils.getApiError
import io.ktor.client.plugins.ClientRequestException
import javax.inject.Inject

class AuthDataSource @Inject constructor(
    private val authApiService: IAuthApiService
) : IAuthDataSource {

    override suspend fun register(authUser: AuthUser): Result<UserDetails, ApiError> {
        return try {
            Ok(authApiService.register(authUser))
        } catch (e: ClientRequestException) {
            Err(EmailTaken)
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun login(authUser: AuthUser): Result<TokenPair, ApiError> {
        return try {
            Ok(authApiService.login(authUser))
        } catch (e: ClientRequestException) {
            Err(InvalidCredentials)
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun logout(authUser: AuthUser): Result<Unit, ApiError> {
        return try {
            Ok(authApiService.logout(authUser))
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun deleteUser(password: String): Result<Unit, ApiError> {
        return try {
            Ok(authApiService.deleteUser(password))
        } catch (e: ClientRequestException) {
            Err(InvalidPassword)
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }

    override suspend fun changePassword(
        installationId: String,
        oldPassword: String,
        newPassword: String
    ): Result<String, ApiError> {
        return try {
            Ok(authApiService.changePassword(installationId, oldPassword, newPassword)
                .getValue("refreshToken"))
        } catch (e: ClientRequestException) {
            Err(InvalidPassword)
        } catch (e: Exception) {
            Err(e.getApiError())
        }
    }
}