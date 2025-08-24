package com.ritsuai.evolution

import android.content.Context
import android.util.Log
import com.ritsuai.data.RitsuMemory
import com.ritsuai.data.RitsuMemoryDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Motor de auto-mejora de Ritsu que le permite evolucionar con el tiempo
 * basado en las interacciones con el usuario.
 */
class RitsuSelfImprovementEngine(private val context: Context) {
    
    private val TAG = "RitsuSelfImprovement"
    private val isLearning = AtomicBoolean(false)
    private val memoryDatabase by lazy { RitsuMemoryDatabase.getDatabase(context) }
    private val memoryDao by lazy { memoryDatabase.ritsuMemoryDao() }
    
    // Nivel de evolución actual de Ritsu
    private var evolutionLevel = 1
    
    // Umbral de memorias para evolucionar
    private val EVOLUTION_THRESHOLD = 50
    
    /**
     * Inicia el proceso de aprendizaje basado en las interacciones recientes
     */
    fun startLearningProcess() {
        if (isLearning.getAndSet(true)) {
            Log.d(TAG, "Ya hay un proceso de aprendizaje en curso")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Analizar memorias recientes
                val recentMemories = memoryDao.getRecentMemories(100)
                
                // Identificar patrones de comportamiento
                val patterns = identifyPatterns(recentMemories)
                
                // Adaptar comportamiento basado en patrones
                adaptBehavior(patterns)
                
                // Verificar si es momento de evolucionar
                checkForEvolution()
                
                Log.d(TAG, "Proceso de aprendizaje completado con éxito")
            } catch (e: Exception) {
                Log.e(TAG, "Error en el proceso de aprendizaje: ${e.message}")
            } finally {
                isLearning.set(false)
            }
        }
    }
    
    /**
     * Identifica patrones en las memorias recientes
     */
    private fun identifyPatterns(memories: List<RitsuMemory>): Map<String, Int> {
        val patterns = mutableMapOf<String, Int>()
        
        // Analizar tipos de comandos frecuentes
        memories.forEach { memory ->
            val commandType = memory.commandType ?: "UNKNOWN"
            patterns[commandType] = (patterns[commandType] ?: 0) + 1
        }
        
        // Analizar horarios de uso
        memories.groupBy { it.timestamp.hour }.forEach { (hour, memoriesAtHour) ->
            patterns["HOUR_$hour"] = memoriesAtHour.size
        }
        
        return patterns
    }
    
    /**
     * Adapta el comportamiento de Ritsu basado en los patrones identificados
     */
    private suspend fun adaptBehavior(patterns: Map<String, Int>) {
        withContext(Dispatchers.IO) {
            // Guardar preferencias de comportamiento adaptadas
            val sharedPrefs = context.getSharedPreferences("ritsu_behavior", Context.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                // Guardar comandos más frecuentes para sugerencias
                val sortedCommands = patterns.filter { it.key.startsWith("COMMAND_") }
                    .entries.sortedByDescending { it.value }.take(5)
                
                sortedCommands.forEachIndexed { index, entry ->
                    putString("frequent_command_$index", entry.key.removePrefix("COMMAND_"))
                }
                
                // Adaptar horarios activos
                val activeHours = patterns.filter { it.key.startsWith("HOUR_") }
                    .entries.sortedByDescending { it.value }.take(5)
                    .map { it.key.removePrefix("HOUR_").toInt() }
                
                putString("active_hours", activeHours.joinToString(","))
                
                // Guardar nivel de evolución
                putInt("evolution_level", evolutionLevel)
                
                apply()
            }
        }
    }
    
    /**
     * Verifica si Ritsu debe evolucionar basado en la cantidad de interacciones
     */
    private suspend fun checkForEvolution() {
        val totalMemories = memoryDao.getTotalMemoryCount()
        
        // Calcular el nivel de evolución basado en la cantidad de memorias
        val newLevel = (totalMemories / EVOLUTION_THRESHOLD) + 1
        
        if (newLevel > evolutionLevel) {
            evolutionLevel = newLevel
            Log.d(TAG, "¡Ritsu ha evolucionado al nivel $evolutionLevel!")
            
            // Notificar al sistema sobre la evolución
            notifyEvolution(evolutionLevel)
        }
    }
    
    /**
     * Notifica al sistema sobre la evolución de Ritsu
     */
    private fun notifyEvolution(level: Int) {
        // Guardar el nuevo nivel de evolución
        val sharedPrefs = context.getSharedPreferences("ritsu_evolution", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putInt("current_level", level)
            putLong("last_evolution_timestamp", System.currentTimeMillis())
            apply()
        }
        
        // Aquí se podrían enviar eventos a otros componentes del sistema
    }
    
    /**
     * Obtiene el nivel actual de evolución de Ritsu
     */
    fun getCurrentEvolutionLevel(): Int {
        val sharedPrefs = context.getSharedPreferences("ritsu_evolution", Context.MODE_PRIVATE)
        return sharedPrefs.getInt("current_level", 1)
    }
    
    /**
     * Reinicia el proceso de evolución (solo para depuración)
     */
    fun resetEvolution() {
        evolutionLevel = 1
        val sharedPrefs = context.getSharedPreferences("ritsu_evolution", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putInt("current_level", 1)
            putLong("last_evolution_timestamp", 0)
            apply()
        }
        Log.d(TAG, "Evolución reiniciada al nivel 1")
    }
}

