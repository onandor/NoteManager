package com.onandor.notemanager.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.onandor.notemanager.data.local.datastore.AuthKeys.ACCESS_TOKEN
import com.onandor.notemanager.data.local.datastore.AuthKeys.REFRESH_TOKEN
import com.onandor.notemanager.di.EncryptedDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private object AuthKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
}

class AuthDataStore @Inject constructor(
    @EncryptedDataStore private val dataStore: DataStore<Preferences>
) : IAuthDataStore {

    override fun observeAccessToken(): Flow<String> {
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[ACCESS_TOKEN] ?: ""
            }
    }

    override suspend fun getAccessToken(): String {
        val preferences = dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .first()
        return preferences[ACCESS_TOKEN] ?: ""
    }

    override suspend fun saveAccessToken(accessToken: String) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
        }
    }

    override fun observeRefreshToken(): Flow<String> {
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[REFRESH_TOKEN] ?: ""
            }
    }

    override suspend fun getRefreshToken(): String {
        val preferences = dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .first()
        return preferences[REFRESH_TOKEN] ?: ""
    }

    override suspend fun saveRefreshToken(refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }
}