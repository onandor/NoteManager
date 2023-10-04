package com.onandor.notemanager.data.local.datastore

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface ISettings {

    fun observeString(key: SettingsKey<String>): Flow<String>

    suspend fun getString(key: SettingsKey<String>): String

    fun observeInt(key: SettingsKey<Int>): Flow<Int>

    suspend fun getInt(key: SettingsKey<Int>): Int

    fun observeBoolean(key: SettingsKey<Boolean>, defaultValue: Boolean): Flow<Boolean>

    suspend fun getBoolean(key: SettingsKey<Boolean>, defaultValue: Boolean): Boolean

    suspend fun <T> save(key: SettingsKey<T>, value: T)

    suspend fun <T> remove(key: SettingsKey<T>)
}