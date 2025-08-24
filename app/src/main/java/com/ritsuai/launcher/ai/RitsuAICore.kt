package com.ritsuai.launcher.ai

import android.content.Context
import com.ritsuai.launcher.ai.nlp.ContextManager
import com.ritsuai.launcher.ai.nlp.IntentRecognizer
import com.ritsuai.launcher.ai.nlp.ResponseGenerator
import com.ritsuai.launcher.database.RitsuDatabase
import com.ritsuai.launcher.database.entities.Conversation
import com.ritsuai.launcher.database.entities.Memory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

/**
 * Núcleo de inteligencia artificial de Ritsu.
 * Gestiona el procesamiento de lenguaje natural, la generación de respuestas
 * y la memoria persistente.
 */
class RitsuAICore(private val context: Context) {

    // Componentes de NLP
    private val intentRecognizer = IntentRecognizer(context)
    private val contextManager = ContextManager()
    private val responseGenerator = ResponseGenerator(context)
    
    // Base de datos
    private val database = RitsuDatabase.getInstance(context)
    
    // Scope de corrutinas
    private val aiScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // ID de sesión actual
    private var currentSessionId = UUID.randomUUID().toString()
    
    /**
     * Procesa un mensaje del usuario y genera una respuesta
     *
     * @param userMessage Mensaje del usuario
     * @param conversationContext Contexto de la conversación (chat, llamada, WhatsApp, etc.)
     * @return Respuesta generada
     */
    suspend fun processMessage(userMessage: String, conversationContext: String = "chat"): String {
        return withContext(Dispatchers.Default) {
            // Guardar mensaje del usuario en la base de datos
            saveMessage(userMessage, true, conversationContext)
            
            // Reconocer intención
            val intent = intentRecognizer.recognizeIntent(userMessage)
            
            // Analizar sentimiento
            val sentiment = intentRecognizer.analyzeSentiment(userMessage)
            
            // Actualizar contexto
            contextManager.updateContext(userMessage, intent, sentiment)
            
            // Generar respuesta
            val response = responseGenerator.generateResponse(
                userMessage,
                intent,
                sentiment,
                contextManager.getCurrentContext()
            )
            
            // Guardar respuesta en la base de datos
            saveMessage(response, false, conversationContext, intent, sentiment)
            
            // Aprender de la interacción
            learnFromInteraction(userMessage, response, intent, sentiment)
            
            response
        }
    }
    
    /**
     * Guarda un mensaje en la base de datos
     */
    private suspend fun saveMessage(
        message: String,
        isUserMessage: Boolean,
        context: String,
        intent: String = "chat",
        sentiment: String = "neutral"
    ) {
        withContext(Dispatchers.IO) {
            val conversation = Conversation(
                message = message,
                isUserMessage = isUserMessage,
                context = context,
                sessionId = currentSessionId,
                timestamp = Date(),
                sentiment = sentiment,
                intent = intent
            )
            
            database.conversationDao().insert(conversation)
        }
    }
    
    /**
     * Aprende de la interacción entre el usuario y Ritsu
     */
    private fun learnFromInteraction(
        userMessage: String,
        response: String,
        intent: String,
        sentiment: String
    ) {
        aiScope.launch {
            // Ejemplo: si el usuario menciona una preferencia, guardarla
            if (intent == "preference" && userMessage.contains("me gusta")) {
                val preference = extractPreference(userMessage)
                if (preference.isNotEmpty()) {
                    saveMemory("preference_${preference.hashCode()}", preference, "preference", 7)
                }
            }
            
            // Ejemplo: si el usuario menciona un contacto frecuente
            if (intent == "contact" && userMessage.contains("llama a")) {
                val contact = extractContact(userMessage)
                if (contact.isNotEmpty()) {
                    saveMemory("contact_${contact.hashCode()}", contact, "contact", 6)
                }
            }
            
            // Ejemplo: aprender del sentimiento del usuario
            if (sentiment != "neutral") {
                val key = "sentiment_${Date().time}"
                saveMemory(key, "$sentiment: $userMessage", "sentiment", 4)
            }
        }
    }
    
    /**
     * Extrae una preferencia del mensaje del usuario
     */
    private fun extractPreference(message: String): String {
        // Implementación simplificada
        val pattern = "me gusta (.+?)(?:\\.|,|$)"
        val regex = Regex(pattern, RegexOption.IGNORE_CASE)
        val matchResult = regex.find(message)
        return matchResult?.groupValues?.getOrNull(1)?.trim() ?: ""
    }
    
    /**
     * Extrae un contacto del mensaje del usuario
     */
    private fun extractContact(message: String): String {
        // Implementación simplificada
        val pattern = "llama a (.+?)(?:\\.|,|$)"
        val regex = Regex(pattern, RegexOption.IGNORE_CASE)
        val matchResult = regex.find(message)
        return matchResult?.groupValues?.getOrNull(1)?.trim() ?: ""
    }
    
    /**
     * Guarda un recuerdo en la memoria persistente
     */
    private suspend fun saveMemory(key: String, value: String, category: String, importance: Int) {
        withContext(Dispatchers.IO) {
            // Verificar si ya existe
            val existingMemory = database.memoryDao().getByKey(key)
            
            if (existingMemory != null) {
                // Actualizar existente
                val updatedMemory = existingMemory.copy(
                    value = value,
                    importance = importance,
                    updatedAt = Date(),
                    accessCount = existingMemory.accessCount + 1
                )
                database.memoryDao().update(updatedMemory)
            } else {
                // Crear nuevo
                val memory = Memory(
                    key = key,
                    value = value,
                    category = category,
                    importance = importance
                )
                database.memoryDao().insert(memory)
            }
        }
    }
    
    /**
     * Recupera un recuerdo de la memoria persistente
     */
    suspend fun getMemory(key: String): String? {
        return withContext(Dispatchers.IO) {
            val memory = database.memoryDao().getByKey(key)
            if (memory != null) {
                // Incrementar contador de accesos
                database.memoryDao().incrementAccessCount(key)
                memory.value
            } else {
                null
            }
        }
    }
    
    /**
     * Recupera recuerdos por categoría
     */
    suspend fun getMemoriesByCategory(category: String): List<Memory> {
        return withContext(Dispatchers.IO) {
            database.memoryDao().getByCategorySync(category)
        }
    }
    
    /**
     * Inicia una nueva sesión de conversación
     */
    fun startNewSession() {
        currentSessionId = UUID.randomUUID().toString()
        contextManager.resetContext()
    }
    
    /**
     * Obtiene el ID de la sesión actual
     */
    fun getCurrentSessionId(): String {
        return currentSessionId
    }
    
    companion object {
        // Instancia singleton
        @Volatile
        private var INSTANCE: RitsuAICore? = null
        
        fun getInstance(context: Context): RitsuAICore {
            return INSTANCE ?: synchronized(this) {
                val instance = RitsuAICore(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

