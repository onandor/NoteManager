package com.onandor.notemanager.data.local.datastore

import kotlinx.coroutines.flow.Flow

interface IAuthDataStore {
    fun observeAccessToken(): Flow<String>

    suspend fun getAccessToken(): String

    suspend fun saveAccessToken(accessToken: String)

    fun observeRefreshToken(): Flow<String>

    suspend fun getRefreshToken(): String

    suspend fun saveRefreshToken(refreshToken: String)
}