package com.ritsuai.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

/**
 * Entidad que representa una memoria de Ritsu
 */
@Entity(tableName = "ritsu_memories")
@TypeConverters(DateConverter::class)
data class RitsuMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val commandType: String? = null,
    val timestamp: Date = Date(),
    val sentiment: Float = 0f,
    val isImportant: Boolean = false
)

