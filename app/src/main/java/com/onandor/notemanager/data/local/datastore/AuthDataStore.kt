package com.onandor.notemanager.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.onandor.notemanager.di.EncryptedDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

object AuthStoreKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val USER_ID = intPreferencesKey("user_id")
    val USER_EMAIL = stringPreferencesKey("user_email")
}

class AuthDataStore @Inject constructor(
    @EncryptedDataStore private val dataStore: DataStore<Preferences>
) : IAuthDataStore {

    override fun observeString(key: Preferences.Key<String>): Flow<String> {
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[key] ?: ""
            }
    }

    override suspend fun getString(key: Preferences.Key<String>): String {
        val preferences = dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .first()
        return preferences[key] ?: ""
    }

    override fun observeInt(key: Preferences.Key<Int>): Flow<Int> {
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[key] ?: -1
            }
    }

    override suspend fun getInt(key: Preferences.Key<Int>): Int {
        val preferences = dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .first()
        return preferences[key] ?: -1
    }

    override suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key] = value
        }
    }
}