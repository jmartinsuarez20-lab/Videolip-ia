package com.ritsuai.launcher.ai

import android.content.Context
import android.util.Log
import com.ritsuai.launcher.ai.nlp.ContextManager
import com.ritsuai.launcher.ai.nlp.IntentRecognizer
import com.ritsuai.launcher.ai.nlp.ResponseGenerator
import com.ritsuai.launcher.database.RitsuDatabase
import com.ritsuai.launcher.database.entities.Conversation
import com.ritsuai.launcher.database.entities.Memory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Núcleo de inteligencia artificial de Ritsu.
 * Procesa mensajes, reconoce intenciones y genera respuestas.
 */
class RitsuAICore private constructor(private val context: Context) {

    // Tag para logs
    private val TAG = "RitsuAICore"
    
    // Componentes de NLP
    private val intentRecognizer = IntentRecognizer(context)
    private val contextManager = ContextManager()
    private val responseGenerator = ResponseGenerator(context)
    
    // Base de datos
    private val database = RitsuDatabase.getInstance(context)
    private val conversationDao = database.conversationDao()
    private val memoryDao = database.memoryDao()
    
    // Estado actual
    private var currentMood = "neutral"
    private var userName = "Usuario"
    
    /**
     * Procesa un mensaje y genera una respuesta
     *
     * @param message Mensaje a procesar
     * @param source Fuente del mensaje (user, system, notification, etc.)
     * @return Respuesta generada
     */
    suspend fun processMessage(message: String, source: String = "user"): String {
        return withContext(Dispatchers.Default) {
            Log.d(TAG, "Procesando mensaje: $message (fuente: $source)")
            
            try {
                // Reconocer intención
                val intent = intentRecognizer.recognizeIntent(message)
                Log.d(TAG, "Intención reconocida: ${intent.name}")
                
                // Actualizar contexto
                contextManager.updateContext(message, intent, source)
                
                // Generar respuesta
                val response = responseGenerator.generateResponse(message, intent, contextManager.getCurrentContext())
                
                // Guardar conversación
                saveConversation(message, response, source, intent.name)
                
                // Extraer y guardar memoria si es necesario
                extractAndSaveMemory(message, intent)
                
                // Actualizar estado de ánimo si es necesario
                updateMood(message, intent)
                
                response
            } catch (e: Exception) {
                Log.e(TAG, "Error al procesar mensaje", e)
                "Lo siento, ha ocurrido un error al procesar tu mensaje."
            }
        }
    }
    
    /**
     * Guarda una conversación en la base de datos
     */
    private suspend fun saveConversation(message: String, response: String, source: String, intent: String) {
        val conversation = Conversation(
            id = 0, // Auto-generado
            message = message,
            response = response,
            source = source,
            intent = intent,
            timestamp = Date(),
            isImportant = isImportantConversation(intent)
        )
        
        conversationDao.insert(conversation)
    }
    
    /**
     * Extrae y guarda memoria de un mensaje
     */
    private suspend fun extractAndSaveMemory(message: String, intent: IntentRecognizer.Intent) {
        // Solo extraer memoria de ciertos tipos de intenciones
        if (intent.name in listOf("PERSONAL_INFO", "PREFERENCE", "CONTACT_INFO", "SCHEDULE")) {
            // Extraer información relevante
            val memoryValue = extractMemoryValue(message, intent)
            
            if (memoryValue.isNotEmpty()) {
                val memory = Memory(
                    id = 0, // Auto-generado
                    key = intent.name.toLowerCase(),
                    value = memoryValue,
                    source = "conversation",
                    timestamp = Date(),
                    confidence = 0.8f // Valor por defecto
                )
                
                memoryDao.insert(memory)
            }
        }
    }
    
    /**
     * Extrae valor de memoria de un mensaje
     */
    private fun extractMemoryValue(message: String, intent: IntentRecognizer.Intent): String {
        // En una implementación real, se usaría NLP para extraer información
        // Para este ejemplo, simplemente devolvemos el mensaje
        return message
    }
    
    /**
     * Actualiza el estado de ánimo de Ritsu
     */
    private fun updateMood(message: String, intent: IntentRecognizer.Intent) {
        // En una implementación real, se analizaría el sentimiento del mensaje
        // Para este ejemplo, usamos reglas simples
        
        val newMood = when {
            message.contains("feliz") || message.contains("alegre") || message.contains("gracias") -> "happy"
            message.contains("triste") || message.contains("mal") || message.contains("llorar") -> "sad"
            message.contains("enojado") || message.contains("molesto") || message.contains("furioso") -> "angry"
            message.contains("tranquilo") || message.contains("relajado") || message.contains("calma") -> "relaxed"
            message.contains("cansado") || message.contains("sueño") || message.contains("dormir") -> "sleepy"
            else -> currentMood
        }
        
        if (newMood != currentMood) {
            currentMood = newMood
            Log.d(TAG, "Nuevo estado de ánimo: $currentMood")
        }
    }
    
    /**
     * Verifica si una conversación es importante
     */
    private fun isImportantConversation(intent: String): Boolean {
        return intent in listOf(
            "EMERGENCY",
            "PERSONAL_INFO",
            "CONTACT_INFO",
            "SCHEDULE",
            "REMINDER"
        )
    }
    
    /**
     * Obtiene el estado de ánimo actual de Ritsu
     */
    fun getCurrentMood(): String {
        return currentMood
    }
    
    /**
     * Establece el nombre del usuario
     */
    fun setUserName(name: String) {
        userName = name
    }
    
    /**
     * Obtiene el nombre del usuario
     */
    fun getUserName(): String {
        return userName
    }
    
    /**
     * Busca en la memoria de Ritsu
     */
    suspend fun searchMemory(query: String): List<Memory> {
        return memoryDao.searchMemory("%$query%")
    }
    
    /**
     * Obtiene las conversaciones recientes
     */
    suspend fun getRecentConversations(limit: Int = 10): List<Conversation> {
        return conversationDao.getRecentConversations(limit)
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

