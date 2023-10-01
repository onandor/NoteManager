package com.onandor.notemanager.data.local.models

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.onandor.notemanager.data.NoteLocation
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun timestampToDate(timestamp: String?): LocalDateTime? =
        timestamp?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? = date?.toString()

    @TypeConverter
    fun labelListToJsonString(labelList: LabelList): String = Gson().toJson(labelList)

    @TypeConverter
    fun jsonStringToLabelList(jsonString: String): LabelList =
        Gson().fromJson(jsonString, LabelList::class.java)

    @TypeConverter
    fun noteLocationEnumToInt(noteLocation: NoteLocation): Int = noteLocation.value

    @TypeConverter
    fun intToNoteLocation(value: Int) = enumValues<NoteLocation>()[value]
}