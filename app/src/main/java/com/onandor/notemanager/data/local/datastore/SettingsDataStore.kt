package com.onandor.notemanager.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.onandor.notemanager.data.local.datastore.KEYS.KEY_FIRST_LAUNCH
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

object KEYS {
    val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch")
}

class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ISettingsDataStore {

    override fun firstLaunch(): Flow<Boolean> {
        return dataStore.data
            .catch {
                emit(emptyPreferences())
            }
            .map { preference ->
                preference[KEY_FIRST_LAUNCH] ?: false
            }
    }

    override suspend fun saveFirstLaunch(firstLaunch: Boolean) {
        dataStore.edit { preference ->
            preference[KEY_FIRST_LAUNCH] = firstLaunch
        }
    }
}