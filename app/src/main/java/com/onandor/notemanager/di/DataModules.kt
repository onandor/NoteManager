package com.onandor.notemanager.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.NoteRepository
import com.onandor.notemanager.data.local.datastore.AuthDataStore
import com.onandor.notemanager.data.local.datastore.IAuthDataStore
import com.onandor.notemanager.data.local.datastore.ISettingsDataStore
import com.onandor.notemanager.data.local.datastore.SettingsDataStore
import com.onandor.notemanager.data.local.db.LabelDao
import com.onandor.notemanager.data.local.db.NMDatabase
import com.onandor.notemanager.data.local.db.NoteDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.osipxd.security.crypto.createEncrypted
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindNoteRepository(repository: NoteRepository): INoteRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): NMDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            NMDatabase::class.java,
            name = "NoteManager.db"
        ).build()
    }

    @Provides
    fun provideNoteDao(database: NMDatabase): NoteDao = database.noteDao()

    @Provides
    fun provideLabelDao(database: NMDatabase): LabelDao = database.labelDao()
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDataStore

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class EncryptedDataStore

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Singleton
    @Provides
    @DefaultDataStore
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            produceFile = { context.preferencesDataStoreFile("settings") }
        )
    }

    @Singleton
    @Provides
    @EncryptedDataStore
    fun provideEncryptedDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createEncrypted {
            EncryptedFile.Builder(
                context.dataStoreFile("settings_crypt.preferences_pb"),
                context,
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
        }
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class SettingsDataStoreModule {

    @Binds
    abstract fun bindSettingsDataStore(settingsDataStore: SettingsDataStore): ISettingsDataStore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthDataStoreModule {

    @Binds
    abstract fun bindAuthDataStore(authDataStore: AuthDataStore): IAuthDataStore
}