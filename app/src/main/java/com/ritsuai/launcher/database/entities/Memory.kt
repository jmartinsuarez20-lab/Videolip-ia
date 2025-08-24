package com.ritsuai.launcher.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad para almacenar la memoria persistente de Ritsu.
 * Almacena información aprendida sobre el usuario y sus preferencias.
 */
@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * Clave única para identificar este recuerdo
     */
    val key: String,
    
    /**
     * Valor asociado con la clave
     */
    val value: String,
    
    /**
     * Categoría del recuerdo (preferencia, contacto, hábito, etc.)
     */
    val category: String,
    
    /**
     * Nivel de importancia (0-10)
     */
    val importance: Int = 5,
    
    /**
     * Fecha de creación
     */
    val createdAt: Date = Date(),
    
    /**
     * Fecha de última actualización
     */
    val updatedAt: Date = Date(),
    
    /**
     * Contador de accesos a este recuerdo
     */
    val accessCount: Int = 0
)

