package com.ritsuai.launcher.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ritsuai.launcher.database.entities.Memory

/**
 * DAO para la entidad Memory.
 * Proporciona métodos para acceder a la memoria de Ritsu.
 */
@Dao
interface MemoryDao {
    
    /**
     * Inserta una nueva memoria
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: Memory): Long
    
    /**
     * Actualiza una memoria existente
     */
    @Update
    suspend fun update(memory: Memory)
    
    /**
     * Elimina una memoria
     */
    @Delete
    suspend fun delete(memory: Memory)
    
    /**
     * Obtiene todas las memorias
     */
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    suspend fun getAllMemories(): List<Memory>
    
    /**
     * Obtiene memorias por clave
     */
    @Query("SELECT * FROM memories WHERE `key` = :key ORDER BY timestamp DESC")
    suspend fun getMemoriesByKey(key: String): List<Memory>
    
    /**
     * Obtiene la memoria más reciente por clave
     */
    @Query("SELECT * FROM memories WHERE `key` = :key ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMemoryByKey(key: String): Memory?
    
    /**
     * Busca memorias por valor
     */
    @Query("SELECT * FROM memories WHERE value LIKE :query ORDER BY timestamp DESC")
    suspend fun searchMemory(query: String): List<Memory>
    
    /**
     * Obtiene memorias por fuente
     */
    @Query("SELECT * FROM memories WHERE source = :source ORDER BY timestamp DESC")
    suspend fun getMemoriesBySource(source: String): List<Memory>
    
    /**
     * Elimina todas las memorias
     */
    @Query("DELETE FROM memories")
    suspend fun deleteAllMemories()
    
    /**
     * Elimina memorias por clave
     */
    @Query("DELETE FROM memories WHERE `key` = :key")
    suspend fun deleteMemoriesByKey(key: String)
    
    /**
     * Elimina memorias antiguas
     */
    @Query("DELETE FROM memories WHERE timestamp < :timestamp")
    suspend fun deleteOldMemories(timestamp: Long)
}

