package com.ritsuai.launcher.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad que representa una memoria de Ritsu.
 * Almacena información aprendida sobre el usuario y su entorno.
 *
 * @property id Identificador único de la memoria
 * @property key Clave de la memoria (categoría)
 * @property value Valor de la memoria
 * @property source Fuente de la memoria (conversation, system, etc.)
 * @property timestamp Fecha y hora de creación
 * @property confidence Nivel de confianza (0.0 - 1.0)
 */
@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val value: String,
    val source: String,
    val timestamp: Date,
    val confidence: Float = 1.0f
)

