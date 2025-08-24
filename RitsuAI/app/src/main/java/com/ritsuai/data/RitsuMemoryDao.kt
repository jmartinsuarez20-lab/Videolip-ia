package com.ritsuai.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * DAO para acceder a las memorias de Ritsu
 */
@Dao
interface RitsuMemoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: RitsuMemory): Long
    
    @Update
    suspend fun update(memory: RitsuMemory)
    
    @Delete
    suspend fun delete(memory: RitsuMemory)
    
    @Query("SELECT * FROM ritsu_memories ORDER BY timestamp DESC")
    suspend fun getAllMemories(): List<RitsuMemory>
    
    @Query("SELECT * FROM ritsu_memories ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMemories(limit: Int): List<RitsuMemory>
    
    @Query("SELECT * FROM ritsu_memories WHERE commandType = :type ORDER BY timestamp DESC")
    suspend fun getMemoriesByType(type: String): List<RitsuMemory>
    
    @Query("SELECT * FROM ritsu_memories WHERE content LIKE '%' || :keyword || '%' ORDER BY timestamp DESC")
    suspend fun searchMemories(keyword: String): List<RitsuMemory>
    
    @Query("SELECT COUNT(*) FROM ritsu_memories")
    suspend fun getTotalMemoryCount(): Int
    
    @Query("SELECT * FROM ritsu_memories WHERE isImportant = 1 ORDER BY timestamp DESC")
    suspend fun getImportantMemories(): List<RitsuMemory>
    
    @Query("DELETE FROM ritsu_memories WHERE timestamp < :timestamp")
    suspend fun deleteOldMemories(timestamp: Long)
}

