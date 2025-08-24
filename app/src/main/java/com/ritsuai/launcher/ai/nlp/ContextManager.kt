package com.ritsuai.launcher.ai.nlp

import java.util.Date
import java.util.LinkedList

/**
 * Gestor de contexto conversacional.
 * Mantiene el contexto de la conversación para generar respuestas coherentes.
 */
class ContextManager {

    // Historial de mensajes recientes
    private val messageHistory = LinkedList<ContextMessage>()
    
    // Tamaño máximo del historial
    private val MAX_HISTORY_SIZE = 10
    
    // Contexto actual
    private var currentContext = ConversationContext()
    
    /**
     * Actualiza el contexto con un nuevo mensaje
     *
     * @param message Mensaje del usuario
     * @param intent Intención detectada
     * @param sentiment Sentimiento detectado
     */
    fun updateContext(message: String, intent: String, sentiment: String) {
        // Añadir mensaje al historial
        addToHistory(message, intent, sentiment)
        
        // Actualizar contexto actual
        updateCurrentContext(message, intent, sentiment)
    }
    
    /**
     * Añade un mensaje al historial
     */
    private fun addToHistory(message: String, intent: String, sentiment: String) {
        val contextMessage = ContextMessage(
            message = message,
            intent = intent,
            sentiment = sentiment,
            timestamp = Date()
        )
        
        // Añadir al principio
        messageHistory.addFirst(contextMessage)
        
        // Limitar tamaño
        if (messageHistory.size > MAX_HISTORY_SIZE) {
            messageHistory.removeLast()
        }
    }
    
    /**
     * Actualiza el contexto actual
     */
    private fun updateCurrentContext(message: String, intent: String, sentiment: String) {
        // Actualizar tema de conversación
        if (intent != "unknown" && intent != "greeting" && intent != "farewell" && intent != "thanks") {
            currentContext.topic = intent
        }
        
        // Actualizar estado emocional
        if (sentiment != "neutral") {
            currentContext.emotionalState = sentiment
        }
        
        // Actualizar última intención
        currentContext.lastIntent = intent
        
        // Actualizar última interacción
        currentContext.lastInteraction = Date()
        
        // Incrementar contador de turnos
        currentContext.turnCount++
    }
    
    /**
     * Obtiene el contexto actual
     *
     * @return Contexto actual de la conversación
     */
    fun getCurrentContext(): ConversationContext {
        return currentContext
    }
    
    /**
     * Obtiene el historial de mensajes
     *
     * @return Lista de mensajes recientes
     */
    fun getMessageHistory(): List<ContextMessage> {
        return messageHistory.toList()
    }
    
    /**
     * Reinicia el contexto
     */
    fun resetContext() {
        messageHistory.clear()
        currentContext = ConversationContext()
    }
    
    /**
     * Clase que representa un mensaje en el contexto
     */
    data class ContextMessage(
        val message: String,
        val intent: String,
        val sentiment: String,
        val timestamp: Date
    )
    
    /**
     * Clase que representa el contexto de la conversación
     */
    data class ConversationContext(
        var topic: String = "general",
        var emotionalState: String = "neutral",
        var lastIntent: String = "unknown",
        var lastInteraction: Date = Date(),
        var turnCount: Int = 0
    )
}

