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
    val NOTE_LIST_COLLAPSED_VIEW = SettingsKey(false, booleanPreferencesKey("note_list_collapsed_view"))
    val NOTE_LIST_SORT_BY = SettingsKey(false, intPreferencesKey("note_list_sort_by"))
    val NOTE_LIST_ORDER = SettingsKey(false, intPreferencesKey("note_list_order"))
    val THEME_TYPE = SettingsKey(false, intPreferencesKey("theme_type"))
}

class Settings @Inject constructor(
    @DefaultDataStore private val defaultDataStore: DataStore<Preferences>,
    @EncryptedDataStore private val encryptedDataStore: DataStore<Preferences>
) : ISettings {

    private fun <T> getDataStore(key: SettingsKey<T>): DataStore<Preferences> {
        return if (key.encrypted) encryptedDataStore else defaultDataStore
    }

    override fun observeString(key: SettingsKey<String>): Flow<String> {
        val dataStore = getDataStore(key)
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[key.dataKey] ?: ""
            }
    }

    override suspend fun getString(key: SettingsKey<String>): String {
        val dataStore = getDataStore(key)
        val preferences = dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .first()
        return preferences[key.dataKey] ?: ""
    }

    override fun observeInt(key: SettingsKey<Int>): Flow<Int> {
        val dataStore = getDataStore(key)
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[key.dataKey] ?: -1
            }
    }

    override suspend fun getInt(key: SettingsKey<Int>): Int {
        val dataStore = getDataStore(key)
        val preferences = dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .first()
        return preferences[key.dataKey] ?: -1
    }

    override fun observeBoolean(key: SettingsKey<Boolean>, defaultValue: Boolean): Flow<Boolean> {
        val dataStore = getDataStore(key)
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->

                preferences[key.dataKey] ?: defaultValue
            }
    }

    override suspend fun getBoolean(key: SettingsKey<Boolean>, defaultValue: Boolean): Boolean {
        val dataStore = getDataStore(key)
        val preferences = dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .first()
        return preferences[key.dataKey] ?: defaultValue
    }

    override suspend fun <T> save(key: SettingsKey<T>, value: T) {
        val dataStore = getDataStore(key)
        dataStore.edit { preferences ->
            preferences[key.dataKey] = value
        }
    }

    override suspend fun <T> remove(key: SettingsKey<T>) {
        val dataStore = getDataStore(key)
        dataStore.edit { preferences ->
            preferences.remove(key.dataKey)
        }
    }
}