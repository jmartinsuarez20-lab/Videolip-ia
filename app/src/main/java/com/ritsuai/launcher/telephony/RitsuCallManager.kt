package com.ritsuai.launcher.telephony

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.ritsuai.launcher.ai.RitsuAICore
import com.ritsuai.launcher.speech.TextToSpeechManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Gestor de llamadas telefónicas para Ritsu.
 * Maneja la detección, respuesta y gestión de llamadas.
 */
class RitsuCallManager(private val context: Context) {

    // Managers del sistema
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
    
    // Componentes de Ritsu
    private val aiCore = RitsuAICore.getInstance(context)
    private val ttsManager = TextToSpeechManager.getInstance(context)
    
    // Estado actual de la llamada
    private var currentCallState = TelephonyManager.CALL_STATE_IDLE
    private var incomingNumber: String? = null
    private var ritsuAnswering = false
    
    // Scope de corrutinas
    private val callScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Listener de estado del teléfono
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            handleCallStateChanged(state, phoneNumber)
        }
    }
    
    /**
     * Inicializa el gestor de llamadas
     */
    fun initialize() {
        // Registrar listener de estado del teléfono
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.registerTelephonyCallback(
                context.mainExecutor,
                object : TelephonyManager.TelephonyCallback() {
                    override fun onCallStateChanged(state: Int) {
                        handleCallStateChanged(state, null)
                    }
                }
            )
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }
    
    /**
     * Maneja cambios en el estado de la llamada
     */
    private fun handleCallStateChanged(state: Int, phoneNumber: String?) {
        // Actualizar estado actual
        val previousState = currentCallState
        currentCallState = state
        
        // Actualizar número entrante
        if (phoneNumber != null) {
            incomingNumber = phoneNumber
        }
        
        // Manejar cambios de estado
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                // Llamada entrante
                handleIncomingCall(incomingNumber)
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                // Llamada en curso
                if (previousState == TelephonyManager.CALL_STATE_RINGING) {
                    // Llamada contestada
                    handleCallAnswered()
                } else {
                    // Llamada saliente
                    handleOutgoingCall()
                }
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                // Llamada finalizada
                if (previousState == TelephonyManager.CALL_STATE_OFFHOOK) {
                    handleCallEnded()
                } else if (previousState == TelephonyManager.CALL_STATE_RINGING) {
                    // Llamada rechazada o perdida
                    handleCallRejected()
                }
            }
        }
    }
    
    /**
     * Maneja una llamada entrante
     */
    private fun handleIncomingCall(phoneNumber: String?) {
        // Notificar a Ritsu sobre la llamada entrante
        callScope.launch {
            val contactName = getContactName(phoneNumber)
            val message = "Llamada entrante de $contactName"
            
            // Generar respuesta de Ritsu
            val response = aiCore.processMessage(message, "call")
            
            // Notificar al usuario (implementación específica depende de la UI)
            // Por ejemplo, mostrar una notificación o un diálogo
        }
    }
    
    /**
     * Maneja una llamada contestada
     */
    private fun handleCallAnswered() {
        // Si Ritsu debe responder la llamada
        if (ritsuAnswering) {
            callScope.launch {
                // Esperar un momento antes de hablar
                kotlinx.coroutines.delay(1000)
                
                // Obtener saludo personalizado
                val contactName = getContactName(incomingNumber)
                val greeting = getCallGreeting(contactName)
                
                // Hablar
                ttsManager.speak(greeting)
            }
        }
    }
    
    /**
     * Maneja una llamada saliente
     */
    private fun handleOutgoingCall() {
        // Implementación específica para llamadas salientes
    }
    
    /**
     * Maneja una llamada finalizada
     */
    private fun handleCallEnded() {
        // Resetear estado
        ritsuAnswering = false
        incomingNumber = null
        
        // Notificar a Ritsu
        callScope.launch {
            aiCore.processMessage("Llamada finalizada", "call")
        }
    }
    
    /**
     * Maneja una llamada rechazada o perdida
     */
    private fun handleCallRejected() {
        // Resetear estado
        ritsuAnswering = false
        
        // Notificar a Ritsu sobre la llamada perdida
        callScope.launch {
            val contactName = getContactName(incomingNumber)
            val message = "Llamada perdida de $contactName"
            
            // Generar respuesta de Ritsu
            aiCore.processMessage(message, "call")
        }
        
        incomingNumber = null
    }
    
    /**
     * Configura a Ritsu para responder la próxima llamada
     */
    fun setRitsuToAnswerCall(answer: Boolean) {
        ritsuAnswering = answer
    }
    
    /**
     * Realiza una llamada telefónica
     *
     * @param phoneNumber Número de teléfono a llamar
     * @return true si la llamada se inició correctamente, false en caso contrario
     */
    fun makeCall(phoneNumber: String): Boolean {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$phoneNumber")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Responde una llamada entrante
     *
     * @return true si la llamada se respondió correctamente, false en caso contrario
     */
    fun answerCall(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                telecomManager.acceptRingingCall()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }
    
    /**
     * Rechaza una llamada entrante
     *
     * @return true si la llamada se rechazó correctamente, false en caso contrario
     */
    fun rejectCall(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                telecomManager.endCall()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }
    
    /**
     * Finaliza la llamada actual
     *
     * @return true si la llamada se finalizó correctamente, false en caso contrario
     */
    fun endCall(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                telecomManager.endCall()
                return true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }
    
    /**
     * Obtiene el nombre del contacto a partir del número de teléfono
     */
    private fun getContactName(phoneNumber: String?): String {
        if (phoneNumber == null) return "Desconocido"
        
        // Implementación simplificada
        // En una implementación real, se consultaría la base de datos de contactos
        return "Contacto"
    }
    
    /**
     * Obtiene un saludo personalizado para la llamada
     */
    private fun getCallGreeting(contactName: String): String {
        // Obtener nombre del usuario
        val userName = "Usuario" // En una implementación real, se obtendría de la configuración
        
        return "Hola, soy Ritsu, la asistente de $userName. ¿En qué puedo ayudarte?"
    }
    
    companion object {
        // Instancia singleton
        @Volatile
        private var INSTANCE: RitsuCallManager? = null
        
        fun getInstance(context: Context): RitsuCallManager {
            return INSTANCE ?: synchronized(this) {
                val instance = RitsuCallManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

