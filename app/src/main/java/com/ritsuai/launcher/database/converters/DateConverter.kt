package com.ritsuai.launcher.database.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Conversor de fechas para Room.
 * Permite almacenar objetos Date en la base de datos.
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

