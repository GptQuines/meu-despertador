package com.despertador.app.data.db

import androidx.room.TypeConverter

object Converters {

    @TypeConverter
    fun fromDaySet(value: Set<Int>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toDaySet(value: String): Set<Int> {
        if (value.isBlank()) return emptySet()
        return value.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }
}
