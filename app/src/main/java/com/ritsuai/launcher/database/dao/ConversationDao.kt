package com.ritsuai.launcher.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ritsuai.launcher.database.entities.Conversation

/**
 * DAO para la entidad Conversation.
 * Proporciona métodos para acceder a las conversaciones almacenadas.
 */
@Dao
interface ConversationDao {
    
    /**
     * Inserta una nueva conversación
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: Conversation): Long
    
    /**
     * Actualiza una conversación existente
     */
    @Update
    suspend fun update(conversation: Conversation)
    
    /**
     * Elimina una conversación
     */
    @Delete
    suspend fun delete(conversation: Conversation)
    
    /**
     * Obtiene todas las conversaciones
     */
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    suspend fun getAllConversations(): List<Conversation>
    
    /**
     * Obtiene las conversaciones recientes
     */
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentConversations(limit: Int): List<Conversation>
    
    /**
     * Obtiene las conversaciones importantes
     */
    @Query("SELECT * FROM conversations WHERE isImportant = 1 ORDER BY timestamp DESC")
    suspend fun getImportantConversations(): List<Conversation>
    
    /**
     * Busca conversaciones por texto
     */
    @Query("SELECT * FROM conversations WHERE message LIKE :query OR response LIKE :query ORDER BY timestamp DESC")
    suspend fun searchConversations(query: String): List<Conversation>
    
    /**
     * Obtiene conversaciones por intención
     */
    @Query("SELECT * FROM conversations WHERE intent = :intent ORDER BY timestamp DESC")
    suspend fun getConversationsByIntent(intent: String): List<Conversation>
    
    /**
     * Obtiene conversaciones por fuente
     */
    @Query("SELECT * FROM conversations WHERE source = :source ORDER BY timestamp DESC")
    suspend fun getConversationsBySource(source: String): List<Conversation>
    
    /**
     * Elimina todas las conversaciones
     */
    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
    
    /**
     * Elimina conversaciones antiguas
     */
    @Query("DELETE FROM conversations WHERE timestamp < :timestamp AND isImportant = 0")
    suspend fun deleteOldConversations(timestamp: Long)
}

