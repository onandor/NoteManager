package com.onandor.notemanager.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.onandor.notemanager.di.DefaultDataStore
import com.onandor.notemanager.di.EncryptedDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class SettingsKey<T>(
    val encrypted: Boolean,
    val dataKey: Preferences.Key<T>
)

object SettingsKeys {
    val ACCESS_TOKEN = SettingsKey(true, stringPreferencesKey("access_token"))
    val REFRESH_TOKEN = SettingsKey(true, stringPreferencesKey("refresh_token"))
    val USER_ID = SettingsKey(true, intPreferencesKey("user_id"))
    val USER_EMAIL = SettingsKey(true, stringPreferencesKey("user_email"))
    val FIRST_LAUNCH = SettingsKey(false, booleanPreferencesKey("first_launch"))
    val INSTALLATION_ID = SettingsKey(true, stringPreferencesKey("installation_id"))
}

class Settings @Inject constructor(
    @DefaultDataStore private val defaultDataStore: DataStore<Preferences>,
    @EncryptedDataStore private val encryptedDataStore: DataStore<Preferences>
) : ISettings {

    override fun observeString(key: SettingsKey<String>): Flow<String> {
        val dataStore = if (key.encrypted) encryptedDataStore else defaultDataStore
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[key.dataKey] ?: ""
            }
    }

    override suspend fun getString(key: SettingsKey<String>): String {
        val dataStore = if (key.encrypted) encryptedDataStore else defaultDataStore
        val preferences = dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .first()
        return preferences[key.dataKey] ?: ""
    }

    override fun observeInt(key: SettingsKey<Int>): Flow<Int> {
        val dataStore = if (key.encrypted) encryptedDataStore else defaultDataStore
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[key.dataKey] ?: -1
            }
    }

    override suspend fun getInt(key: SettingsKey<Int>): Int {
        val dataStore = if (key.encrypted) encryptedDataStore else defaultDataStore
        val preferences = dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .first()
        return preferences[key.dataKey] ?: -1
    }

    override fun observeBoolean(key: SettingsKey<Boolean>, defaultValue: Boolean): Flow<Boolean> {
        val dataStore = if (key.encrypted) encryptedDataStore else defaultDataStore
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->

                preferences[key.dataKey] ?: defaultValue
            }
    }

    override suspend fun getBoolean(key: SettingsKey<Boolean>, defaultValue: Boolean): Boolean {
        val dataStore = if (key.encrypted) encryptedDataStore else defaultDataStore
        val preferences = dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .first()
        return preferences[key.dataKey] ?: defaultValue
    }

    override suspend fun <T> save(key: SettingsKey<T>, value: T) {
        val dataStore = if (key.encrypted) encryptedDataStore else defaultDataStore
        dataStore.edit { preferences ->
            preferences[key.dataKey] = value
        }
    }
}