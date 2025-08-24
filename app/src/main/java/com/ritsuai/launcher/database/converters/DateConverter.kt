package com.ritsuai.launcher.database.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 * Conversor para tipos Date en la base de datos Room.
 */
class DateConverter {
    
    /**
     * Convierte un timestamp (Long) a Date
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    
    /**
     * Convierte un Date a timestamp (Long)
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

