package com.onandor.notemanager.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.onandor.notemanager.data.local.datastore.SettingsKeys.FIRST_LAUNCH
import com.onandor.notemanager.data.local.datastore.SettingsKeys.INSTALLATION_ID
import com.onandor.notemanager.di.DefaultDataStore
import com.onandor.notemanager.di.EncryptedDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

private object SettingsKeys {
    val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    val INSTALLATION_ID = stringPreferencesKey("installation_id")
}

class SettingsDataStore @Inject constructor(
    @DefaultDataStore private val dataStore: DataStore<Preferences>,
    @EncryptedDataStore private val encryptedDataStore: DataStore<Preferences>
) : ISettingsDataStore {

    override fun getFirstLaunch(): Flow<Boolean> {
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                preferences[FIRST_LAUNCH] ?: true
            }
    }

    override suspend fun saveFirstLaunch(firstLaunch: Boolean) {
        dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH] = firstLaunch
        }
    }

    override fun getInstallationId(): Flow<UUID> {
        return encryptedDataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preferences ->
                val idString = preferences[INSTALLATION_ID]
                if (idString == null)
                     UUID.fromString("")
                else
                    UUID.fromString(idString)
            }
    }

    override suspend fun saveInstallationId(installationId: UUID) {
        encryptedDataStore.edit { preferences ->
            preferences[INSTALLATION_ID] = installationId.toString()
        }
    }
}