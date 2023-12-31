package com.onandor.notemanager.data.remote.sources

import com.github.michaelbull.result.Result
import com.onandor.notemanager.data.remote.models.ApiError
import com.onandor.notemanager.data.remote.models.AuthUser
import com.onandor.notemanager.data.remote.models.TokenPair
import com.onandor.notemanager.data.remote.models.UserDetails

interface IAuthDataSource {

    suspend fun register(authUser: AuthUser): Result<UserDetails, ApiError>

    suspend fun login(authUser: AuthUser): Result<TokenPair, ApiError>

    suspend fun logout(authUser: AuthUser): Result<Unit, ApiError>

    suspend fun deleteUser(password: String): Result<Unit, ApiError>

    suspend fun changePassword(installationId: String, oldPassword: String, newPassword: String): Result<String, ApiError>
}