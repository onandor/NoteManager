package com.onandor.notemanager.data.local.datastore

import kotlinx.coroutines.flow.Flow

interface ISettingsDataStore {
    fun firstLaunch(): Flow<Boolean>

    suspend fun saveFirstLaunch(firstLaunch: Boolean)
}