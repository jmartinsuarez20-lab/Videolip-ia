package com.ritsuai.launcher.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad para almacenar conversaciones entre el usuario y Ritsu.
 */
@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * Mensaje del usuario o de Ritsu
     */
    val message: String,
    
    /**
     * Indica si el mensaje es del usuario (true) o de Ritsu (false)
     */
    val isUserMessage: Boolean,
    
    /**
     * Contexto de la conversación (chat general, llamada, WhatsApp, etc.)
     */
    val context: String,
    
    /**
     * Identificador de la sesión de conversación
     */
    val sessionId: String,
    
    /**
     * Fecha y hora del mensaje
     */
    val timestamp: Date = Date(),
    
    /**
     * Sentimiento detectado en el mensaje (neutral, positivo, negativo)
     */
    val sentiment: String = "neutral",
    
    /**
     * Intención detectada en el mensaje (pregunta, comando, charla, etc.)
     */
    val intent: String = "chat"
)

