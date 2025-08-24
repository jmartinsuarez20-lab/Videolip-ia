package com.ritsuai

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gestor de memoria y aprendizaje de Ritsu
 * Maneja el almacenamiento de conversaciones, preferencias y aprendizaje continuo
 */
class RitsuMemoryManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("ritsu_memory", Context.MODE_PRIVATE)
    private val conversationHistory = mutableListOf<ConversationEntry>()
    private val userPreferences = mutableMapOf<String, Any>()
    private val learnedPatterns = mutableMapOf<String, PatternData>()
    
    private var learningLevel = 1
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    init {
        loadMemoryData()
    }
    
    private fun loadMemoryData() {
        // Cargar historial de conversaciones
        val historyJson = prefs.getString("conversation_history", "[]")
        loadConversationHistory(historyJson ?: "[]")
        
        // Cargar preferencias del usuario
        val preferencesJson = prefs.getString("user_preferences", "{}")
        loadUserPreferences(preferencesJson ?: "{}")
        
        // Cargar patrones aprendidos
        val patternsJson = prefs.getString("learned_patterns", "{}")
        loadLearnedPatterns(patternsJson ?: "{}")
        
        // Cargar nivel de aprendizaje
        learningLevel = prefs.getInt("learning_level", 1)
    }
    
    private fun loadConversationHistory(json: String) {
        try {
            val jsonArray = JSONArray(json)
            conversationHistory.clear()
            
            for (i in 0 until jsonArray.length()) {
                val entryJson = jsonArray.getJSONObject(i)
                val entry = ConversationEntry(
                    userId = entryJson.getString("userId"),
                    userInput = entryJson.getString("userInput"),
                    ritsuResponse = entryJson.getString("ritsuResponse"),
                    timestamp = entryJson.getLong("timestamp"),
                    emotion = RitsuEmotion.valueOf(entryJson.getString("emotion")),
                    context = entryJson.getString("context")
                )
                conversationHistory.add(entry)
            }
        } catch (e: Exception) {
            // Error al cargar historial, empezar limpio
        }
    }
    
    private fun loadUserPreferences(json: String) {
        try {
            val jsonObject = JSONObject(json)
            userPreferences.clear()
            
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                userPreferences[key] = jsonObject.get(key)
            }
        } catch (e: Exception) {
            // Error al cargar preferencias
        }
    }
    
    private fun loadLearnedPatterns(json: String) {
        try {
            val jsonObject = JSONObject(json)
            learnedPatterns.clear()
            
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val patternJson = jsonObject.getJSONObject(key)
                val pattern = PatternData(
                    pattern = patternJson.getString("pattern"),
                    frequency = patternJson.getInt("frequency"),
                    lastSeen = patternJson.getLong("lastSeen"),
                    associatedResponses = parseStringList(patternJson.getJSONArray("associatedResponses"))
                )
                learnedPatterns[key] = pattern
            }
        } catch (e: Exception) {
            // Error al cargar patrones
        }
    }
    
    private fun parseStringList(jsonArray: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }
    
    /**
     * Guarda una interacción para aprendizaje futuro
     */
    fun saveInteraction(userId: String, userInput: String, ritsuResponse: String, emotion: RitsuEmotion = RitsuEmotion.NORMAL) {
        val entry = ConversationEntry(
            userId = userId,
            userInput = userInput,
            ritsuResponse = ritsuResponse,
            timestamp = System.currentTimeMillis(),
            emotion = emotion,
            context = getCurrentContext()
        )
        
        conversationHistory.add(entry)
        
        // Mantener solo las últimas 1000 conversaciones
        if (conversationHistory.size > 1000) {
            conversationHistory.removeAt(0)
        }
        
        // Analizar patrones en la nueva interacción
        analyzePatterns(userInput, ritsuResponse)
        
        // Guardar en SharedPreferences
        saveMemoryData()
    }
    
    private fun getCurrentContext(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 6..11 -> "mañana"
            in 12..17 -> "tarde"
            in 18..23 -> "noche"
            else -> "madrugada"
        }
    }
    
    private fun analyzePatterns(userInput: String, ritsuResponse: String) {
        val words = userInput.lowercase().split(" ")
        
        // Analizar patrones de palabras clave
        for (word in words) {
            if (word.length > 3) { // Solo palabras significativas
                val pattern = learnedPatterns[word] ?: PatternData(
                    pattern = word,
                    frequency = 0,
                    lastSeen = 0,
                    associatedResponses = emptyList()
                )
                
                val updatedPattern = pattern.copy(
                    frequency = pattern.frequency + 1,
                    lastSeen = System.currentTimeMillis(),
                    associatedResponses = (pattern.associatedResponses + ritsuResponse).distinct().takeLast(5)
                )
                
                learnedPatterns[word] = updatedPattern
            }
        }
        
        // Incrementar nivel de aprendizaje
        if (conversationHistory.size % 50 == 0) {
            learningLevel++
        }
    }
    
    /**
     * Obtiene el contexto de conversación para generar respuestas más inteligentes
     */
    fun getConversationContext(userId: String, limit: Int = 5): List<String> {
        return conversationHistory
            .filter { it.userId == userId }
            .takeLast(limit)
            .map { "${it.userInput} -> ${it.ritsuResponse}" }
    }
    
    /**
     * Obtiene preferencias aprendidas del usuario
     */
    fun getUserPreferences(userId: String): Map<String, Any> {
        val userSpecificPrefs = mutableMapOf<String, Any>()
        
        // Analizar conversaciones para extraer preferencias
        val userConversations = conversationHistory.filter { it.userId == userId }
        
        // Preferencias de tiempo
        val morningInteractions = userConversations.count { it.context == "mañana" }
        val eveningInteractions = userConversations.count { it.context == "noche" }
        
        userSpecificPrefs["preferred_time"] = if (morningInteractions > eveningInteractions) "mañana" else "noche"
        
        // Preferencias de tono
        val formalInteractions = userConversations.count { 
            it.userInput.contains("por favor") || it.userInput.contains("gracias")
        }
        val casualInteractions = userConversations.count {
            it.userInput.contains("hola") || it.userInput.contains("hey")
        }
        
        userSpecificPrefs["preferred_tone"] = if (formalInteractions > casualInteractions) "formal" else "casual"
        
        // Temas de interés
        val topicFrequency = mutableMapOf<String, Int>()
        val topics = listOf("música", "películas", "juegos", "trabajo", "estudio", "comida", "viajes")
        
        for (topic in topics) {
            val count = userConversations.count { it.userInput.lowercase().contains(topic) }
            if (count > 0) {
                topicFrequency[topic] = count
            }
        }
        
        userSpecificPrefs["interests"] = topicFrequency.toList().sortedByDescending { it.second }.take(3)
        
        return userSpecificPrefs
    }
    
    /**
     * Procesa información de aprendizaje específica
     */
    fun processLearning(input: String) {
        val lowerInput = input.lowercase()
        
        when {
            lowerInput.contains("me gusta") -> {
                val preference = extractPreference(input, "me gusta")
                userPreferences["likes"] = (userPreferences["likes"] as? List<String> ?: emptyList()) + preference
            }
            
            lowerInput.contains("no me gusta") -> {
                val preference = extractPreference(input, "no me gusta")
                userPreferences["dislikes"] = (userPreferences["dislikes"] as? List<String> ?: emptyList()) + preference
            }
            
            lowerInput.contains("recuerda que") -> {
                val fact = input.substring(input.lowercase().indexOf("recuerda que") + 12).trim()
                userPreferences["facts"] = (userPreferences["facts"] as? List<String> ?: emptyList()) + fact
            }
            
            lowerInput.contains("mi nombre es") -> {
                val name = input.substring(input.lowercase().indexOf("mi nombre es") + 12).trim()
                userPreferences["name"] = name
            }
        }
        
        saveMemoryData()
    }
    
    private fun extractPreference(input: String, trigger: String): String {
        val index = input.lowercase().indexOf(trigger)
        return if (index != -1) {
            input.substring(index + trigger.length).trim()
        } else {
            ""
        }
    }
    
    /**
     * Genera respuestas inteligentes basadas en patrones aprendidos
     */
    fun generateIntelligentResponse(input: String): String? {
        val words = input.lowercase().split(" ")
        val relevantPatterns = mutableListOf<PatternData>()
        
        // Buscar patrones relevantes
        for (word in words) {
            learnedPatterns[word]?.let { pattern ->
                if (pattern.frequency > 2) { // Solo patrones frecuentes
                    relevantPatterns.add(pattern)
                }
            }
        }
        
        // Seleccionar la mejor respuesta basada en patrones
        if (relevantPatterns.isNotEmpty()) {
            val bestPattern = relevantPatterns.maxByOrNull { it.frequency }
            return bestPattern?.associatedResponses?.randomOrNull()
        }
        
        return null
    }
    
    /**
     * Obtiene estadísticas de aprendizaje
     */
    fun getLearningStats(): LearningStats {
        return LearningStats(
            totalConversations = conversationHistory.size,
            learningLevel = learningLevel,
            patternsLearned = learnedPatterns.size,
            userPreferences = userPreferences.size,
            averageResponseTime = calculateAverageResponseTime(),
            mostUsedWords = getMostUsedWords()
        )
    }
    
    private fun calculateAverageResponseTime(): Long {
        // Simulado - en una implementación real calcularíamos tiempos reales
        return 1500L // 1.5 segundos promedio
    }
    
    private fun getMostUsedWords(): List<Pair<String, Int>> {
        return learnedPatterns.toList()
            .sortedByDescending { it.second.frequency }
            .take(10)
            .map { it.first to it.second.frequency }
    }
    
    private fun saveMemoryData() {
        val editor = prefs.edit()
        
        // Guardar historial de conversaciones
        val historyJson = JSONArray()
        for (entry in conversationHistory.takeLast(500)) { // Solo las últimas 500
            val entryJson = JSONObject().apply {
                put("userId", entry.userId)
                put("userInput", entry.userInput)
                put("ritsuResponse", entry.ritsuResponse)
                put("timestamp", entry.timestamp)
                put("emotion", entry.emotion.name)
                put("context", entry.context)
            }
            historyJson.put(entryJson)
        }
        editor.putString("conversation_history", historyJson.toString())
        
        // Guardar preferencias
        val preferencesJson = JSONObject()
        for ((key, value) in userPreferences) {
            preferencesJson.put(key, value)
        }
        editor.putString("user_preferences", preferencesJson.toString())
        
        // Guardar patrones aprendidos
        val patternsJson = JSONObject()
        for ((key, pattern) in learnedPatterns) {
            val patternJson = JSONObject().apply {
                put("pattern", pattern.pattern)
                put("frequency", pattern.frequency)
                put("lastSeen", pattern.lastSeen)
                put("associatedResponses", JSONArray(pattern.associatedResponses))
            }
            patternsJson.put(key, patternJson)
        }
        editor.putString("learned_patterns", patternsJson.toString())
        
        // Guardar nivel de aprendizaje
        editor.putInt("learning_level", learningLevel)
        
        editor.apply()
    }
    
    fun getLearningLevel(): Int = learningLevel
    
    /**
     * Limpia datos antiguos para optimizar memoria
     */
    fun cleanupOldData() {
        val oneMonthAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        
        // Remover conversaciones muy antiguas
        conversationHistory.removeAll { it.timestamp < oneMonthAgo }
        
        // Remover patrones no utilizados recientemente
        learnedPatterns.entries.removeAll { it.value.lastSeen < oneMonthAgo }
        
        saveMemoryData()
    }
}

// Clases de datos para el sistema de memoria
data class ConversationEntry(
    val userId: String,
    val userInput: String,
    val ritsuResponse: String,
    val timestamp: Long,
    val emotion: RitsuEmotion,
    val context: String
)

data class PatternData(
    val pattern: String,
    val frequency: Int,
    val lastSeen: Long,
    val associatedResponses: List<String>
)

data class LearningStats(
    val totalConversations: Int,
    val learningLevel: Int,
    val patternsLearned: Int,
    val userPreferences: Int,
    val averageResponseTime: Long,
    val mostUsedWords: List<Pair<String, Int>>
)

