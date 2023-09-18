package com.onandor.notemanager.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.onandor.notemanager.data.Label
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun timestampToDate(timestamp: String?): LocalDateTime? {
        return timestamp?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun labelListToJsonString(labelList: LabelList): String {
        return Gson().toJson(labelList)
    }

    @TypeConverter
    fun jsonStringToLabelList(jsonString: String): LabelList {
        return Gson().fromJson(jsonString, LabelList::class.java)
    }
}