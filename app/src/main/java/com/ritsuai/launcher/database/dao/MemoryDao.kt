package com.ritsuai.launcher.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ritsuai.launcher.database.entities.Memory
import kotlinx.coroutines.flow.Flow

/**
 * DAO para acceder a la tabla de memoria.
 */
@Dao
interface MemoryDao {
    
    /**
     * Inserta un nuevo recuerdo en la base de datos
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: Memory): Long
    
    /**
     * Actualiza un recuerdo existente
     */
    @Update
    suspend fun update(memory: Memory)
    
    /**
     * Elimina un recuerdo
     */
    @Delete
    suspend fun delete(memory: Memory)
    
    /**
     * Obtiene un recuerdo por su clave
     */
    @Query("SELECT * FROM memories WHERE `key` = :key LIMIT 1")
    suspend fun getByKey(key: String): Memory?
    
    /**
     * Obtiene todos los recuerdos de una categoría
     */
    @Query("SELECT * FROM memories WHERE category = :category ORDER BY importance DESC, updatedAt DESC")
    fun getByCategory(category: String): Flow<List<Memory>>
    
    /**
     * Obtiene los recuerdos más importantes
     */
    @Query("SELECT * FROM memories ORDER BY importance DESC LIMIT :limit")
    fun getMostImportant(limit: Int): Flow<List<Memory>>
    
    /**
     * Obtiene los recuerdos más recientes
     */
    @Query("SELECT * FROM memories ORDER BY updatedAt DESC LIMIT :limit")
    fun getMostRecent(limit: Int): Flow<List<Memory>>
    
    /**
     * Obtiene los recuerdos más accedidos
     */
    @Query("SELECT * FROM memories ORDER BY accessCount DESC LIMIT :limit")
    fun getMostAccessed(limit: Int): Flow<List<Memory>>
    
    /**
     * Incrementa el contador de accesos de un recuerdo
     */
    @Query("UPDATE memories SET accessCount = accessCount + 1 WHERE `key` = :key")
    suspend fun incrementAccessCount(key: String)
    
    /**
     * Busca recuerdos por texto
     */
    @Query("SELECT * FROM memories WHERE `key` LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<Memory>>
}

