package com.ritsuai.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.ritsuai.services.RitsuOverlayService

/**
 * Receptor para iniciar Ritsu automáticamente al encender el dispositivo
 */
class BootCompletedReceiver : BroadcastReceiver() {
    
    private val TAG = "BootCompletedReceiver"
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Dispositivo iniciado, verificando si Ritsu debe iniciarse automáticamente")
            
            // Verificar si el inicio automático está habilitado en las preferencias
            val prefs = context.getSharedPreferences("ritsu_settings", Context.MODE_PRIVATE)
            val autoStart = prefs.getBoolean("auto_start_on_boot", false)
            
            if (autoStart) {
                // Verificar si tenemos permiso de overlay
                if (checkOverlayPermission(context)) {
                    Log.d(TAG, "Iniciando Ritsu automáticamente")
                    val serviceIntent = Intent(context, RitsuOverlayService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } else {
                    Log.d(TAG, "No se puede iniciar Ritsu automáticamente: falta permiso de overlay")
                }
            } else {
                Log.d(TAG, "Inicio automático deshabilitado en preferencias")
            }
        }
    }
    
    private fun checkOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
}

