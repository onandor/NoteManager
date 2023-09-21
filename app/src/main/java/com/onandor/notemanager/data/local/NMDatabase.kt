package com.onandor.notemanager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [LocalNote::class, LocalLabel::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NMDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    abstract fun labelDao(): LabelDao
}