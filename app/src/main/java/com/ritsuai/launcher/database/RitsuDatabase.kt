package com.ritsuai.launcher.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ritsuai.launcher.database.converters.DateConverter
import com.ritsuai.launcher.database.dao.ConversationDao
import com.ritsuai.launcher.database.dao.MemoryDao
import com.ritsuai.launcher.database.entities.Conversation
import com.ritsuai.launcher.database.entities.Memory

/**
 * Base de datos principal de Ritsu.
 * Almacena conversaciones, memoria y preferencias.
 */
@Database(
    entities = [
        Conversation::class,
        Memory::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class RitsuDatabase : RoomDatabase() {

    // DAOs
    abstract fun conversationDao(): ConversationDao
    abstract fun memoryDao(): MemoryDao
    
    companion object {
        // Nombre de la base de datos
        private const val DATABASE_NAME = "ritsu_db"
        
        // Instancia singleton
        @Volatile
        private var INSTANCE: RitsuDatabase? = null
        
        fun getInstance(context: Context): RitsuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RitsuDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}

