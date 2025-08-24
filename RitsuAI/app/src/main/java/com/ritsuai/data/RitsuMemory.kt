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
    
    // Contenido de la memoria
    val content: String,
    
    // Tipo de comando asociado a esta memoria
    val commandType: String,
    
    // Timestamp de cuando se creó la memoria
    val timestamp: Date = Date(),
    
    // Indica si esta memoria es importante para el aprendizaje
    val isImportant: Boolean = false,
    
    // Puntuación de relevancia (0-100)
    val relevanceScore: Int = 50,
    
    // Contexto adicional (puede ser JSON para datos estructurados)
    val context: String? = null,
    
    // Resultado de la acción (éxito, fracaso, etc.)
    val result: String? = null,
    
    // Feedback del usuario (positivo, negativo, neutral)
    val userFeedback: String? = null,
    
    // Nivel de evolución cuando se creó esta memoria
    val evolutionLevel: Int = 1
)

