package com.ritsuai.launcher.services

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.ritsuai.launcher.ai.RitsuAICore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Servicio de escucha de notificaciones para Ritsu.
 * Permite a Ritsu interceptar y procesar notificaciones del sistema.
 */
class RitsuNotificationListenerService : NotificationListenerService() {

    // Tag para logs
    private val TAG = "RitsuNotification"
    
    // Scope de corrutinas
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Núcleo de IA
    private lateinit var aiCore: RitsuAICore
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar núcleo de IA
        aiCore = RitsuAICore.getInstance(applicationContext)
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        
        Log.d(TAG, "Servicio de notificaciones conectado")
        
        // Notificar al núcleo de IA
        serviceScope.launch {
            aiCore.processMessage("Servicio de notificaciones conectado", "system")
        }
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        // Procesar notificación
        processNotification(sbn, true)
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        
        // Procesar notificación removida
        processNotification(sbn, false)
    }
    
    /**
     * Procesa una notificación
     *
     * @param sbn Notificación
     * @param isPosted true si la notificación fue publicada, false si fue removida
     */
    private fun processNotification(sbn: StatusBarNotification, isPosted: Boolean) {
        // Obtener información de la notificación
        val packageName = sbn.packageName
        val notification = sbn.notification
        
        // Verificar si es una notificación de interés
        if (isNotificationOfInterest(packageName)) {
            // Extraer información
            val title = extractNotificationTitle(notification)
            val text = extractNotificationText(notification)
            
            Log.d(TAG, "Notificación ${if (isPosted) "publicada" else "removida"}: $packageName - $title - $text")
            
            // Notificar al núcleo de IA sobre notificaciones importantes
            if (isPosted && isImportantNotification(packageName, title, text)) {
                serviceScope.launch {
                    val message = "Notificación de $packageName: $title - $text"
                    aiCore.processMessage(message, "notification")
                }
            }
        }
    }
    
    /**
     * Extrae el título de una notificación
     */
    private fun extractNotificationTitle(notification: Notification): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val extras = notification.extras
            return extras.getString(Notification.EXTRA_TITLE) ?: ""
        }
        return ""
    }
    
    /**
     * Extrae el texto de una notificación
     */
    private fun extractNotificationText(notification: Notification): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val extras = notification.extras
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            
            return if (bigText.isNotEmpty()) bigText else text
        }
        return ""
    }
    
    /**
     * Verifica si una notificación es de interés para Ritsu
     */
    private fun isNotificationOfInterest(packageName: String): Boolean {
        return packageName in listOf(
            "com.whatsapp",
            "com.facebook.orca",
            "com.google.android.apps.messaging",
            "com.android.dialer",
            "com.google.android.dialer",
            "com.android.calendar",
            "com.google.android.calendar",
            "com.android.email",
            "com.google.android.gm"
        )
    }
    
    /**
     * Verifica si una notificación es importante
     */
    private fun isImportantNotification(packageName: String, title: String, text: String): Boolean {
        // Llamadas telefónicas
        if (packageName.contains("dialer") && (title.contains("Llamada") || text.contains("Llamada"))) {
            return true
        }
        
        // Mensajes de WhatsApp
        if (packageName == "com.whatsapp" && !title.isEmpty() && !text.isEmpty()) {
            return true
        }
        
        // Mensajes de Messenger
        if (packageName == "com.facebook.orca" && !title.isEmpty() && !text.isEmpty()) {
            return true
        }
        
        // Mensajes SMS
        if (packageName == "com.google.android.apps.messaging" && !title.isEmpty() && !text.isEmpty()) {
            return true
        }
        
        // Correos electrónicos importantes
        if ((packageName == "com.android.email" || packageName == "com.google.android.gm") && 
            (title.contains("Urgente") || title.contains("Importante"))) {
            return true
        }
        
        // Eventos de calendario próximos
        if ((packageName == "com.android.calendar" || packageName == "com.google.android.calendar") && 
            text.contains("minutos")) {
            return true
        }
        
        return false
    }
    
    companion object {
        // Instancia del servicio
        private var instance: RitsuNotificationListenerService? = null
        
        /**
         * Obtiene la instancia del servicio
         */
        fun getInstance(): RitsuNotificationListenerService? {
            return instance
        }
        
        /**
         * Inicia el servicio
         */
        fun start(context: android.content.Context) {
            val intent = Intent(context, RitsuNotificationListenerService::class.java)
            context.startService(intent)
        }
    }
}

