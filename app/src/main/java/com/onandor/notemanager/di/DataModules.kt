package com.onandor.notemanager.di

import android.content.Context
import androidx.room.Room
import com.onandor.notemanager.data.INoteRepository
import com.onandor.notemanager.data.NoteRepository
import com.onandor.notemanager.data.local.db.LabelDao
import com.onandor.notemanager.data.local.db.NMDatabase
import com.onandor.notemanager.data.local.db.NoteDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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