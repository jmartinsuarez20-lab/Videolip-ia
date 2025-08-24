package com.ritsuai.launcher.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ritsuai.launcher.database.converters.DateConverter
import com.ritsuai.launcher.database.dao.ConversationDao
import com.ritsuai.launcher.database.dao.MemoryDao
import com.ritsuai.launcher.database.entities.Conversation
import com.ritsuai.launcher.database.entities.Memory

/**
 * Base de datos principal de Ritsu AI.
 * Almacena memoria persistente, conversaciones y preferencias.
 */
@Database(
    entities = [
        Memory::class,
        Conversation::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class RitsuDatabase : RoomDatabase() {
    
    /**
     * DAO para acceder a la tabla de memoria
     */
    abstract fun memoryDao(): MemoryDao
    
    /**
     * DAO para acceder a la tabla de conversaciones
     */
    abstract fun conversationDao(): ConversationDao
}

