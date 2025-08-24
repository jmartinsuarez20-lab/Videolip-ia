package com.ritsuai.launcher.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ritsuai.launcher.database.entities.Conversation
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * DAO para acceder a la tabla de conversaciones.
 */
@Dao
interface ConversationDao {
    
    /**
     * Inserta un nuevo mensaje en la conversación
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: Conversation): Long
    
    /**
     * Elimina un mensaje de la conversación
     */
    @Delete
    suspend fun delete(conversation: Conversation)
    
    /**
     * Obtiene todos los mensajes de una sesión de conversación
     */
    @Query("SELECT * FROM conversations WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getConversationBySession(sessionId: String): Flow<List<Conversation>>
    
    /**
     * Obtiene las conversaciones más recientes agrupadas por sesión
     */
    @Query("SELECT * FROM conversations WHERE id IN (SELECT MAX(id) FROM conversations GROUP BY sessionId) ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentConversations(limit: Int): Flow<List<Conversation>>
    
    /**
     * Obtiene los mensajes de una sesión en un rango de tiempo
     */
    @Query("SELECT * FROM conversations WHERE sessionId = :sessionId AND timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp ASC")
    fun getConversationInTimeRange(sessionId: String, startDate: Date, endDate: Date): Flow<List<Conversation>>
    
    /**
     * Busca mensajes por contenido
     */
    @Query("SELECT * FROM conversations WHERE message LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMessages(query: String): Flow<List<Conversation>>
    
    /**
     * Obtiene los últimos N mensajes de una sesión
     */
    @Query("SELECT * FROM conversations WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    fun getLastMessages(sessionId: String, limit: Int): Flow<List<Conversation>>
    
    /**
     * Elimina todos los mensajes de una sesión
     */
    @Query("DELETE FROM conversations WHERE sessionId = :sessionId")
    suspend fun deleteConversation(sessionId: String)
    
    /**
     * Elimina conversaciones anteriores a una fecha
     */
    @Query("DELETE FROM conversations WHERE timestamp < :date")
    suspend fun deleteConversationsOlderThan(date: Date)
}

