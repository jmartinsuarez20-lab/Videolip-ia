package com.ritsuai.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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
    fun getAllMemoriesFlow(): Flow<List<RitsuMemory>>
    
    @Query("SELECT * FROM ritsu_memories ORDER BY timestamp DESC")
    suspend fun getAllMemories(): List<RitsuMemory>
    
    @Query("SELECT * FROM ritsu_memories WHERE id = :id")
    suspend fun getMemoryById(id: Long): RitsuMemory?
    
    @Query("SELECT * FROM ritsu_memories WHERE commandType = :commandType ORDER BY timestamp DESC")
    suspend fun getMemoriesByCommandType(commandType: String): List<RitsuMemory>
    
    @Query("SELECT * FROM ritsu_memories WHERE isImportant = 1 ORDER BY timestamp DESC")
    suspend fun getImportantMemories(): List<RitsuMemory>
    
    @Query("SELECT * FROM ritsu_memories WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMemories(query: String): List<RitsuMemory>
    
    @Query("SELECT * FROM ritsu_memories ORDER BY relevanceScore DESC LIMIT :limit")
    suspend fun getMostRelevantMemories(limit: Int): List<RitsuMemory>
    
    @Query("DELETE FROM ritsu_memories WHERE timestamp < :timestamp AND isImportant = 0")
    suspend fun deleteOldMemories(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM ritsu_memories")
    suspend fun getMemoryCount(): Int
}

