package com.ritsuai.launcher.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad que representa una conversación.
 * Almacena mensajes intercambiados entre el usuario y Ritsu.
 *
 * @property id Identificador único de la conversación
 * @property message Mensaje del usuario
 * @property response Respuesta de Ritsu
 * @property source Fuente del mensaje (user, system, notification, etc.)
 * @property intent Intención reconocida
 * @property timestamp Fecha y hora de la conversación
 * @property isImportant Indica si la conversación es importante
 */
@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val message: String,
    val response: String,
    val source: String,
    val intent: String,
    val timestamp: Date,
    val isImportant: Boolean = false
)

