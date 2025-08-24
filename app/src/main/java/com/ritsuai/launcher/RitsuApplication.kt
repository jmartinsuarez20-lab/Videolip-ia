package com.ritsuai.launcher

import android.app.Application
import android.content.Intent
import androidx.room.Room
import com.ritsuai.launcher.avatar3d.RitsuAvatarService
import com.ritsuai.launcher.database.RitsuDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Clase de aplicación principal para Ritsu AI.
 * Inicializa componentes clave y servicios al inicio de la aplicación.
 */
class RitsuApplication : Application() {

    // Scope de corrutinas para operaciones en segundo plano
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Instancia de la base de datos
    lateinit var database: RitsuDatabase
        private set
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar la base de datos
        database = Room.databaseBuilder(
            applicationContext,
            RitsuDatabase::class.java,
            "ritsu_database"
        ).build()
        
        // Iniciar servicios esenciales
        applicationScope.launch {
            startRitsuServices()
        }
    }
    
    /**
     * Inicia los servicios esenciales de Ritsu
     */
    private fun startRitsuServices() {
        // Iniciar el servicio de avatar 3D
        try {
            val avatarServiceIntent = Intent(this, RitsuAvatarService::class.java)
            startService(avatarServiceIntent)
        } catch (e: Exception) {
            // El servicio podría no iniciarse si no se tienen los permisos necesarios
            e.printStackTrace()
        }
    }
    
    companion object {
        // Constantes de la aplicación
        const val SPECIAL_MODE_CODE = "262456"
        const val TAG = "RitsuAI"
    }
}

