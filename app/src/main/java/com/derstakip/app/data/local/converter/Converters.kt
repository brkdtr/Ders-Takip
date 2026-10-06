package com.derstakip.app.data.local.converter

import androidx.room.TypeConverter
import java.util.Date

/**
 * Room TypeConverters for mapping complex Kotlin types to SQLite primitives.
 */
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        if (list == null) return null
        return list.joinToString(separator = ",")
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        if (value.isBlank()) return emptyList()
        return value.split(",").map { it.trim() }
    }

    @TypeConverter
    fun fromIntList(list: List<Int>?): String? {
        if (list == null) return null
        return list.joinToString(separator = ",")
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int>? {
        if (value == null) return null
        if (value.isBlank()) return emptyList()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
}
