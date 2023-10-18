package com.onandor.notemanager.data.local.models

import androidx.room.TypeConverter
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.utils.LabelColorType
import com.onandor.notemanager.utils.labelColors
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun timestampToDate(timestamp: String?): LocalDateTime? =
        timestamp?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? = date?.toString()

    @TypeConverter
    fun noteLocationEnumToInt(noteLocation: NoteLocation): Int = noteLocation.value

    @TypeConverter
    fun intToNoteLocation(value: Int) = enumValues<NoteLocation>()[value]

    @TypeConverter
    fun labelColorTypeEnumToInt(labelColorType: LabelColorType): Int = labelColorType.value

    @TypeConverter
    fun intToLabelColorType(value: Int) = LabelColorType.fromInt(value)
}