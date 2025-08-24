package com.ritsuai.evolution

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Sistema que permite a Ritsu adaptar su apariencia visual basado en
 * preferencias del usuario y su nivel de evolución.
 */
class RitsuAdaptiveAvatar(private val context: Context) {
    
    private val TAG = "RitsuAdaptiveAvatar"
    
    // Directorio para guardar avatares personalizados
    private val avatarDirectory by lazy {
        File(context.filesDir, "ritsu_avatars").apply {
            if (!exists()) mkdirs()
        }
    }
    
    // Preferencias para guardar configuración de avatar
    private val avatarPrefs by lazy {
        context.getSharedPreferences("ritsu_avatar_prefs", Context.MODE_PRIVATE)
    }
    
    /**
     * Obtiene el avatar actual de Ritsu
     */
    suspend fun getCurrentAvatar(): Bitmap = withContext(Dispatchers.IO) {
        val currentAvatarPath = avatarPrefs.getString("current_avatar", null)
        
        if (currentAvatarPath != null) {
            val avatarFile = File(currentAvatarPath)
            if (avatarFile.exists()) {
                return@withContext BitmapFactory.decodeFile(avatarFile.absolutePath)
            }
        }
        
        // Si no hay avatar personalizado, devolver el predeterminado
        return@withContext BitmapFactory.decodeResource(context.resources, 
            context.resources.getIdentifier("ritsu_default_avatar", "drawable", context.packageName))
    }
    
    /**
     * Guarda un nuevo avatar personalizado
     */
    suspend fun saveCustomAvatar(avatarBitmap: Bitmap, name: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val avatarFile = File(avatarDirectory, "${System.currentTimeMillis()}_$name.png")
            FileOutputStream(avatarFile).use { outputStream ->
                avatarBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            
            // Actualizar preferencias
            avatarPrefs.edit {
                putString("current_avatar", avatarFile.absolutePath)
                putString("current_avatar_name", name)
                apply()
            }
            
            Log.d(TAG, "Avatar personalizado guardado: $name")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar avatar personalizado: ${e.message}")
            return@withContext false
        }
    }
    
    /**
     * Obtiene todos los avatares guardados
     */
    suspend fun getSavedAvatars(): List<AvatarInfo> = withContext(Dispatchers.IO) {
        val avatarFiles = avatarDirectory.listFiles() ?: return@withContext emptyList()
        
        return@withContext avatarFiles.map { file ->
            val name = file.nameWithoutExtension.substringAfter("_")
            AvatarInfo(file.absolutePath, name, file.lastModified())
        }.sortedByDescending { it.createdAt }
    }
    
    /**
     * Cambia al avatar especificado
     */
    fun switchToAvatar(avatarPath: String, avatarName: String) {
        avatarPrefs.edit {
            putString("current_avatar", avatarPath)
            putString("current_avatar_name", avatarName)
            apply()
        }
        Log.d(TAG, "Cambiado a avatar: $avatarName")
    }
    
    /**
     * Adapta el avatar basado en el nivel de evolución
     */
    suspend fun adaptAvatarToEvolutionLevel(evolutionLevel: Int): Boolean {
        // En una implementación real, aquí se generaría un avatar más avanzado
        // basado en el nivel de evolución
        
        val evolutionAvatarName = "ritsu_evolution_level_$evolutionLevel"
        val evolutionAvatarResId = context.resources.getIdentifier(
            evolutionAvatarName, "drawable", context.packageName)
        
        if (evolutionAvatarResId != 0) {
            try {
                val evolutionBitmap = BitmapFactory.decodeResource(context.resources, evolutionAvatarResId)
                return saveCustomAvatar(evolutionBitmap, "Evolución Nivel $evolutionLevel")
            } catch (e: Exception) {
                Log.e(TAG, "Error al adaptar avatar a nivel de evolución: ${e.message}")
            }
        }
        
        return false
    }
    
    /**
     * Elimina un avatar guardado
     */
    suspend fun deleteAvatar(avatarPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(avatarPath)
            if (file.exists()) {
                val deleted = file.delete()
                
                // Si el avatar eliminado era el actual, cambiar al predeterminado
                if (deleted && avatarPrefs.getString("current_avatar", "") == avatarPath) {
                    avatarPrefs.edit {
                        remove("current_avatar")
                        remove("current_avatar_name")
                        apply()
                    }
                }
                
                return@withContext deleted
            }
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar avatar: ${e.message}")
            return@withContext false
        }
    }
    
    /**
     * Clase de datos para información de avatares guardados
     */
    data class AvatarInfo(
        val path: String,
        val name: String,
        val createdAt: Long
    )
}

