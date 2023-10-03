package com.onandor.notemanager.data.local.datastore

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface ISettingsDataStore {
    fun getFirstLaunch(): Flow<Boolean>

    suspend fun saveFirstLaunch(firstLaunch: Boolean)

    fun getInstallationId(): Flow<UUID>

    suspend fun saveInstallationId(installationId: UUID)
}