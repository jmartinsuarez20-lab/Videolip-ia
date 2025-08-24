package com.ritsuai.launcher.ai.nlp

import android.util.Log
import java.util.LinkedList

/**
 * Gestor de contexto para Ritsu.
 * Mantiene el contexto de la conversación para generar respuestas coherentes.
 */
class ContextManager {

    // Tag para logs
    private val TAG = "RitsuContextManager"
    
    // Historial de mensajes recientes
    private val messageHistory = LinkedList<HistoryItem>()
    
    // Contexto actual
    private val currentContext = mutableMapOf<String, Any>()
    
    // Tamaño máximo del historial
    private val MAX_HISTORY_SIZE = 10
    
    init {
        // Inicializar contexto con valores por defecto
        currentContext["user_name"] = ""
        currentContext["current_app"] = ""
        currentContext["current_activity"] = ""
        currentContext["last_intent"] = ""
        currentContext["conversation_topic"] = ""
        currentContext["mood"] = "neutral"
    }
    
    /**
     * Actualiza el contexto con un nuevo mensaje
     *
     * @param message Mensaje recibido
     * @param intent Intención reconocida
     * @param source Fuente del mensaje (user, system, notification, etc.)
     */
    fun updateContext(message: String, intent: IntentRecognizer.Intent, source: String) {
        Log.d(TAG, "Actualizando contexto con mensaje: $message (intención: ${intent.name}, fuente: $source)")
        
        // Agregar mensaje al historial
        addToHistory(message, intent.name, source)
        
        // Actualizar última intención
        currentContext["last_intent"] = intent.name
        
        // Actualizar contexto según la intención
        when (intent.name) {
            "GREETING" -> {
                currentContext["conversation_topic"] = "greeting"
            }
            "FAREWELL" -> {
                currentContext["conversation_topic"] = "farewell"
            }
            "PERSONAL_INFO" -> {
                updatePersonalInfo(message)
            }
            "OPEN_APP" -> {
                // Extraer nombre de la aplicación
                val appName = extractAppName(message)
                if (appName.isNotEmpty()) {
                    currentContext["current_app"] = appName
                }
            }
            "CALL", "MESSAGE" -> {
                // Extraer contacto
                val contact = extractContactName(message)
                if (contact.isNotEmpty()) {
                    currentContext["current_contact"] = contact
                }
            }
            "WEATHER" -> {
                currentContext["conversation_topic"] = "weather"
            }
            "MUSIC" -> {
                // Extraer música
                val music = extractMusicQuery(message)
                if (music.isNotEmpty()) {
                    currentContext["current_music"] = music
                }
                currentContext["conversation_topic"] = "music"
            }
            "ALARM", "CALENDAR" -> {
                // Extraer tiempo
                val time = extractTime(message)
                if (time.isNotEmpty()) {
                    currentContext["current_time"] = time
                }
                
                // Extraer fecha
                val date = extractDate(message)
                if (date.isNotEmpty()) {
                    currentContext["current_date"] = date
                }
                
                currentContext["conversation_topic"] = intent.name.toLowerCase()
            }
        }
        
        // Actualizar estado de ánimo si es necesario
        updateMood(message)
        
        Log.d(TAG, "Contexto actualizado: $currentContext")
    }
    
    /**
     * Agrega un mensaje al historial
     */
    private fun addToHistory(message: String, intent: String, source: String) {
        val item = HistoryItem(message, intent, source, System.currentTimeMillis())
        messageHistory.addFirst(item)
        
        // Limitar tamaño del historial
        if (messageHistory.size > MAX_HISTORY_SIZE) {
            messageHistory.removeLast()
        }
    }
    
    /**
     * Actualiza información personal en el contexto
     */
    private fun updatePersonalInfo(message: String) {
        // En una implementación real, se usaría NLP para extraer información
        // Para este ejemplo, usamos reglas simples
        
        val lowerMessage = message.toLowerCase()
        
        // Extraer nombre
        if (lowerMessage.contains("me llamo") || lowerMessage.contains("mi nombre es")) {
            val parts = lowerMessage.split("me llamo", "mi nombre es")
            if (parts.size > 1) {
                val name = parts[1].trim().split(" ")[0].capitalize()
                if (name.isNotEmpty()) {
                    currentContext["user_name"] = name
                }
            }
        }
    }
    
    /**
     * Actualiza el estado de ánimo en el contexto
     */
    private fun updateMood(message: String) {
        // En una implementación real, se analizaría el sentimiento del mensaje
        // Para este ejemplo, usamos reglas simples
        
        val lowerMessage = message.toLowerCase()
        
        val newMood = when {
            lowerMessage.contains("feliz") || lowerMessage.contains("alegre") || lowerMessage.contains("gracias") -> "happy"
            lowerMessage.contains("triste") || lowerMessage.contains("mal") || lowerMessage.contains("llorar") -> "sad"
            lowerMessage.contains("enojado") || lowerMessage.contains("molesto") || lowerMessage.contains("furioso") -> "angry"
            lowerMessage.contains("tranquilo") || lowerMessage.contains("relajado") || lowerMessage.contains("calma") -> "relaxed"
            lowerMessage.contains("cansado") || lowerMessage.contains("sueño") || lowerMessage.contains("dormir") -> "sleepy"
            else -> currentContext["mood"] as String
        }
        
        if (newMood != currentContext["mood"]) {
            currentContext["mood"] = newMood
        }
    }
    
    /**
     * Extrae el nombre de una aplicación de un mensaje
     */
    private fun extractAppName(message: String): String {
        // En una implementación real, se usaría NLP para extraer el nombre
        // Para este ejemplo, usamos una implementación simple
        
        val commonApps = listOf(
            "whatsapp", "facebook", "instagram", "twitter", "youtube", "gmail", "maps",
            "chrome", "spotify", "netflix", "amazon", "tiktok", "telegram"
        )
        
        val lowerMessage = message.toLowerCase()
        
        for (app in commonApps) {
            if (lowerMessage.contains(app)) {
                return app.capitalize()
            }
        }
        
        return ""
    }
    
    /**
     * Extrae el nombre de un contacto de un mensaje
     */
    private fun extractContactName(message: String): String {
        // En una implementación real, se usaría NLP y la lista de contactos
        // Para este ejemplo, devolvemos una cadena vacía
        return ""
    }
    
    /**
     * Extrae una consulta de música de un mensaje
     */
    private fun extractMusicQuery(message: String): String {
        // En una implementación real, se usaría NLP para extraer la consulta
        // Para este ejemplo, usamos una implementación simple
        
        val lowerMessage = message.toLowerCase()
        
        if (lowerMessage.contains("reproduce") || lowerMessage.contains("reproducir")) {
            val parts = lowerMessage.split("reproduce", "reproducir")
            if (parts.size > 1) {
                return parts[1].trim()
            }
        }
        
        return ""
    }
    
    /**
     * Extrae una hora de un mensaje
     */
    private fun extractTime(message: String): String {
        // En una implementación real, se usaría NLP para extraer la hora
        // Para este ejemplo, usamos una implementación simple
        
        val lowerMessage = message.toLowerCase()
        
        // Buscar patrones como "7:30", "7:30 am", "7 am", etc.
        val timeRegex = Regex("\\d{1,2}:\\d{2}(\\s*[ap]m)?|\\d{1,2}(\\s*[ap]m)")
        val match = timeRegex.find(lowerMessage)
        
        return match?.value ?: ""
    }
    
    /**
     * Extrae una fecha de un mensaje
     */
    private fun extractDate(message: String): String {
        // En una implementación real, se usaría NLP para extraer la fecha
        // Para este ejemplo, usamos una implementación simple
        
        val lowerMessage = message.toLowerCase()
        
        // Buscar patrones como "mañana", "el lunes", "el 15 de mayo", etc.
        val dateKeywords = listOf("hoy", "mañana", "pasado mañana", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
        
        for (keyword in dateKeywords) {
            if (lowerMessage.contains(keyword)) {
                return keyword
            }
        }
        
        return ""
    }
    
    /**
     * Obtiene el contexto actual
     */
    fun getCurrentContext(): Map<String, Any> {
        return currentContext.toMap()
    }
    
    /**
     * Obtiene el historial de mensajes
     */
    fun getMessageHistory(): List<HistoryItem> {
        return messageHistory.toList()
    }
    
    /**
     * Establece un valor en el contexto
     */
    fun setContextValue(key: String, value: Any) {
        currentContext[key] = value
    }
    
    /**
     * Obtiene un valor del contexto
     */
    fun getContextValue(key: String): Any? {
        return currentContext[key]
    }
    
    /**
     * Clase que representa un elemento del historial
     */
    data class HistoryItem(
        val message: String,
        val intent: String,
        val source: String,
        val timestamp: Long
    )
}

