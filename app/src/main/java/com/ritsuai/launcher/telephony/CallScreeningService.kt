package com.ritsuai.launcher.telephony

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection
import android.util.Log
import androidx.annotation.RequiresApi
import com.ritsuai.launcher.ai.RitsuAICore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Servicio de filtrado de llamadas para Ritsu.
 * Permite a Ritsu filtrar llamadas entrantes y detectar spam.
 */
@RequiresApi(Build.VERSION_CODES.N)
class CallScreeningService : CallScreeningService() {

    // Tag para logs
    private val TAG = "RitsuCallScreening"
    
    // Núcleo de IA
    private lateinit var aiCore: RitsuAICore
    
    // Gestor de llamadas
    private lateinit var callManager: RitsuCallManager
    
    // Scope de corrutinas
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar componentes
        aiCore = RitsuAICore.getInstance(applicationContext)
        callManager = RitsuCallManager.getInstance(applicationContext)
    }
    
    override fun onScreenCall(callDetails: Call.Details) {
        // Obtener información de la llamada
        val phoneNumber = callDetails.handle?.schemeSpecificPart
        val isIncoming = callDetails.callDirection == Call.Details.DIRECTION_INCOMING
        
        Log.d(TAG, "Llamada ${if (isIncoming) "entrante" else "saliente"}: $phoneNumber")
        
        if (isIncoming) {
            // Verificar si es spam
            serviceScope.launch {
                val isSpam = isSpamCall(phoneNumber)
                
                if (isSpam) {
                    // Rechazar llamada de spam
                    rejectSpamCall(callDetails)
                } else {
                    // Permitir llamada normal
                    allowCall(callDetails)
                }
            }
        } else {
            // Permitir llamadas salientes
            allowCall(callDetails)
        }
    }
    
    /**
     * Verifica si una llamada es spam
     */
    private suspend fun isSpamCall(phoneNumber: String?): Boolean {
        if (phoneNumber == null) return false
        
        // Verificar en lista negra local
        if (isInBlacklist(phoneNumber)) {
            return true
        }
        
        // Verificar con IA
        val message = "¿Es spam el número $phoneNumber?"
        val response = withContext(Dispatchers.Default) {
            aiCore.processMessage(message, "spam_check")
        }
        
        // Analizar respuesta
        return response.contains("spam", ignoreCase = true) ||
               response.contains("telemarketing", ignoreCase = true) ||
               response.contains("no deseado", ignoreCase = true)
    }
    
    /**
     * Verifica si un número está en la lista negra
     */
    private fun isInBlacklist(phoneNumber: String): Boolean {
        // En una implementación real, se verificaría en una base de datos
        // Para este ejemplo, usamos una lista predefinida
        val blacklist = listOf(
            "1234567890",
            "0987654321",
            "5555555555"
        )
        
        return blacklist.any { phoneNumber.contains(it) }
    }
    
    /**
     * Permite una llamada
     */
    private fun allowCall(callDetails: Call.Details) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val response = CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
            
            respondToCall(callDetails, response)
        }
    }
    
    /**
     * Rechaza una llamada de spam
     */
    private fun rejectSpamCall(callDetails: Call.Details) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val response = CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
            
            respondToCall(callDetails, response)
            
            // Registrar en log
            Log.d(TAG, "Llamada de spam rechazada: ${callDetails.handle?.schemeSpecificPart}")
        }
    }
}

