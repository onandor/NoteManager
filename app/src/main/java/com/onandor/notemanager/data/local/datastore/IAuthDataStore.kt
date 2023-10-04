package com.onandor.notemanager.data.local.datastore

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface IAuthDataStore {

    fun observeString(key: Preferences.Key<String>): Flow<String>

    suspend fun getString(key: Preferences.Key<String>): String

    fun observeInt(key: Preferences.Key<Int>): Flow<Int>

    suspend fun getInt(key: Preferences.Key<Int>): Int

    suspend fun <T> save(key: Preferences.Key<T>, value: T)
}