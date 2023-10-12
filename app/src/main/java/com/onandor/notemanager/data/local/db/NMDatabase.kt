package com.onandor.notemanager.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.onandor.notemanager.data.local.models.Converters
import com.onandor.notemanager.data.local.models.LocalLabel
import com.onandor.notemanager.data.local.models.LocalNote
import com.onandor.notemanager.data.local.models.NoteLabel

@Database(
    version = 1,
    exportSchema = false,
    entities = [
        LocalNote::class,
        LocalLabel::class,
        NoteLabel::class
    ]
)
@TypeConverters(Converters::class)
abstract class NMDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    abstract fun labelDao(): LabelDao

    abstract fun noteLabelDao(): NoteLabelDao
}