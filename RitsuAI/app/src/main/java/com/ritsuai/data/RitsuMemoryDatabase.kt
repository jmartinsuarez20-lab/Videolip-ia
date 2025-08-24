package com.ritsuai.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos para almacenar las memorias de Ritsu
 */
@Database(entities = [RitsuMemory::class], version = 1, exportSchema = false)
abstract class RitsuMemoryDatabase : RoomDatabase() {
    
    abstract fun ritsuMemoryDao(): RitsuMemoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: RitsuMemoryDatabase? = null
        
        fun getDatabase(context: Context): RitsuMemoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RitsuMemoryDatabase::class.java,
                    "ritsu_memory_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

