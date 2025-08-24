package com.ritsuai.launcher.ai.nlp

import android.content.Context
import android.util.Log
import java.util.Locale

/**
 * Reconocedor de intenciones para Ritsu.
 * Analiza mensajes y determina la intención del usuario.
 */
class IntentRecognizer(private val context: Context) {

    // Tag para logs
    private val TAG = "RitsuIntentRecognizer"
    
    // Patrones de intenciones en español
    private val intentPatterns = mapOf(
        Intent.GREETING to listOf(
            "hola", "buenos días", "buenas tardes", "buenas noches", "qué tal", "cómo estás"
        ),
        Intent.FAREWELL to listOf(
            "adiós", "hasta luego", "chao", "nos vemos", "hasta pronto", "bye"
        ),
        Intent.THANKS to listOf(
            "gracias", "te lo agradezco", "muchas gracias", "agradecido", "thank you"
        ),
        Intent.HELP to listOf(
            "ayuda", "ayúdame", "necesito ayuda", "cómo funciona", "qué puedes hacer"
        ),
        Intent.OPEN_APP to listOf(
            "abre", "abrir", "inicia", "iniciar", "ejecuta", "lanza"
        ),
        Intent.CALL to listOf(
            "llama", "llamar", "haz una llamada", "marca", "teléfono"
        ),
        Intent.MESSAGE to listOf(
            "mensaje", "envía", "enviar", "escribe", "whatsapp", "sms", "texto"
        ),
        Intent.SEARCH to listOf(
            "busca", "buscar", "encuentra", "encontrar", "google", "internet"
        ),
        Intent.WEATHER to listOf(
            "clima", "tiempo", "temperatura", "lluvia", "sol", "pronóstico"
        ),
        Intent.MUSIC to listOf(
            "música", "canción", "reproduce", "reproducir", "spotify", "audio"
        ),
        Intent.ALARM to listOf(
            "alarma", "despertador", "recordatorio", "recuérdame", "avísame"
        ),
        Intent.CALENDAR to listOf(
            "calendario", "agenda", "cita", "evento", "reunión", "programar"
        ),
        Intent.NOTES to listOf(
            "nota", "notas", "apunta", "apuntar", "escribe", "recordar"
        ),
        Intent.CAMERA to listOf(
            "cámara", "foto", "fotografía", "selfie", "graba", "video"
        ),
        Intent.NAVIGATION to listOf(
            "navega", "navegación", "mapa", "ruta", "dirección", "cómo llegar"
        ),
        Intent.SETTINGS to listOf(
            "configuración", "ajustes", "preferencias", "opciones", "configura"
        ),
        Intent.JOKE to listOf(
            "chiste", "broma", "hazme reír", "cuéntame algo gracioso", "diviérteme"
        ),
        Intent.PERSONAL_INFO to listOf(
            "mi nombre es", "me llamo", "tengo", "años", "vivo en", "trabajo en"
        ),
        Intent.RITSU_INFO to listOf(
            "quién eres", "qué eres", "cómo te llamas", "cuál es tu nombre", "sobre ti"
        ),
        Intent.EMERGENCY to listOf(
            "emergencia", "ayuda urgente", "socorro", "peligro", "accidente", "urgencia"
        )
    )
    
    /**
     * Reconoce la intención de un mensaje
     *
     * @param message Mensaje a analizar
     * @return Intención reconocida
     */
    fun recognizeIntent(message: String): Intent {
        val lowerMessage = message.toLowerCase(Locale.getDefault())
        
        // Buscar coincidencias en patrones
        for ((intent, patterns) in intentPatterns) {
            for (pattern in patterns) {
                if (lowerMessage.contains(pattern)) {
                    Log.d(TAG, "Intención reconocida: $intent (patrón: $pattern)")
                    return intent
                }
            }
        }
        
        // Análisis más detallado para intenciones específicas
        if (containsAppName(lowerMessage)) {
            return Intent.OPEN_APP
        }
        
        if (containsContactName(lowerMessage)) {
            if (lowerMessage.contains("llama") || lowerMessage.contains("llamar")) {
                return Intent.CALL
            } else if (lowerMessage.contains("mensaje") || lowerMessage.contains("envía")) {
                return Intent.MESSAGE
            }
        }
        
        // Si no se reconoce ninguna intención específica, devolver UNKNOWN
        Log.d(TAG, "Intención no reconocida: UNKNOWN")
        return Intent.UNKNOWN
    }
    
    /**
     * Verifica si un mensaje contiene un nombre de aplicación
     */
    private fun containsAppName(message: String): Boolean {
        // En una implementación real, se verificaría contra la lista de aplicaciones instaladas
        // Para este ejemplo, usamos una lista predefinida
        val commonApps = listOf(
            "whatsapp", "facebook", "instagram", "twitter", "youtube", "gmail", "maps",
            "chrome", "spotify", "netflix", "amazon", "tiktok", "telegram"
        )
        
        return commonApps.any { app -> message.contains(app) }
    }
    
    /**
     * Verifica si un mensaje contiene un nombre de contacto
     */
    private fun containsContactName(message: String): Boolean {
        // En una implementación real, se verificaría contra la lista de contactos
        // Para este ejemplo, devolvemos false
        return false
    }
    
    /**
     * Clase que representa una intención
     */
    class Intent(val name: String, val confidence: Float = 1.0f) {
        companion object {
            // Intenciones básicas
            val GREETING = Intent("GREETING")
            val FAREWELL = Intent("FAREWELL")
            val THANKS = Intent("THANKS")
            val HELP = Intent("HELP")
            
            // Intenciones de aplicaciones
            val OPEN_APP = Intent("OPEN_APP")
            val CALL = Intent("CALL")
            val MESSAGE = Intent("MESSAGE")
            val SEARCH = Intent("SEARCH")
            val WEATHER = Intent("WEATHER")
            val MUSIC = Intent("MUSIC")
            val ALARM = Intent("ALARM")
            val CALENDAR = Intent("CALENDAR")
            val NOTES = Intent("NOTES")
            val CAMERA = Intent("CAMERA")
            val NAVIGATION = Intent("NAVIGATION")
            val SETTINGS = Intent("SETTINGS")
            
            // Intenciones de conversación
            val JOKE = Intent("JOKE")
            val PERSONAL_INFO = Intent("PERSONAL_INFO")
            val RITSU_INFO = Intent("RITSU_INFO")
            
            // Intenciones especiales
            val EMERGENCY = Intent("EMERGENCY")
            
            // Intención desconocida
            val UNKNOWN = Intent("UNKNOWN", 0.5f)
        }
    }
}

