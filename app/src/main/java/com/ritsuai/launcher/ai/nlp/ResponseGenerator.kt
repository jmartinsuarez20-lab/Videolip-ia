package com.ritsuai.launcher.ai.nlp

import android.content.Context
import android.util.Log
import com.ritsuai.launcher.R
import java.util.Calendar
import java.util.Random

/**
 * Generador de respuestas para Ritsu.
 * Crea respuestas naturales basadas en la intención y el contexto.
 */
class ResponseGenerator(private val context: Context) {

    // Tag para logs
    private val TAG = "RitsuResponseGenerator"
    
    // Generador de números aleatorios
    private val random = Random()
    
    /**
     * Genera una respuesta basada en la intención y el contexto
     *
     * @param message Mensaje original
     * @param intent Intención reconocida
     * @param context Contexto actual
     * @return Respuesta generada
     */
    fun generateResponse(message: String, intent: IntentRecognizer.Intent, context: Map<String, Any>): String {
        Log.d(TAG, "Generando respuesta para intención: ${intent.name}")
        
        // Generar respuesta según la intención
        val response = when (intent.name) {
            "GREETING" -> generateGreetingResponse(context)
            "FAREWELL" -> generateFarewellResponse(context)
            "THANKS" -> generateThanksResponse()
            "HELP" -> generateHelpResponse()
            "OPEN_APP" -> generateOpenAppResponse(message)
            "CALL" -> generateCallResponse(message)
            "MESSAGE" -> generateMessageResponse(message)
            "SEARCH" -> generateSearchResponse(message)
            "WEATHER" -> generateWeatherResponse(context)
            "MUSIC" -> generateMusicResponse(message)
            "ALARM" -> generateAlarmResponse(message)
            "CALENDAR" -> generateCalendarResponse(message)
            "NOTES" -> generateNotesResponse(message)
            "CAMERA" -> generateCameraResponse()
            "NAVIGATION" -> generateNavigationResponse(message)
            "SETTINGS" -> generateSettingsResponse()
            "JOKE" -> generateJokeResponse()
            "PERSONAL_INFO" -> generatePersonalInfoResponse(message)
            "RITSU_INFO" -> generateRitsuInfoResponse()
            "EMERGENCY" -> generateEmergencyResponse()
            else -> generateUnknownResponse()
        }
        
        return response
    }
    
    /**
     * Genera una respuesta de saludo
     */
    private fun generateGreetingResponse(context: Map<String, Any>): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        val timeGreeting = when {
            hour < 12 -> "Buenos días"
            hour < 18 -> "Buenas tardes"
            else -> "Buenas noches"
        }
        
        val userName = context["user_name"] as? String ?: ""
        val userNamePart = if (userName.isNotEmpty()) ", $userName" else ""
        
        val greetings = listOf(
            "$timeGreeting$userNamePart. ¿En qué puedo ayudarte hoy?",
            "¡Hola$userNamePart! ¿Cómo estás?",
            "¡Saludos$userNamePart! ¿Qué tal tu día?",
            "¡Hola! Estoy aquí para ayudarte.",
            "$timeGreeting$userNamePart. ¿Necesitas algo?"
        )
        
        return greetings.random()
    }
    
    /**
     * Genera una respuesta de despedida
     */
    private fun generateFarewellResponse(context: Map<String, Any>): String {
        val userName = context["user_name"] as? String ?: ""
        val userNamePart = if (userName.isNotEmpty()) ", $userName" else ""
        
        val farewells = listOf(
            "¡Hasta luego$userNamePart! Estaré aquí cuando me necesites.",
            "¡Adiós$userNamePart! Que tengas un buen día.",
            "¡Nos vemos pronto$userNamePart!",
            "¡Hasta la próxima$userNamePart!",
            "¡Cuídate$userNamePart! Aquí estaré cuando vuelvas."
        )
        
        return farewells.random()
    }
    
    /**
     * Genera una respuesta de agradecimiento
     */
    private fun generateThanksResponse(): String {
        val responses = listOf(
            "¡De nada! Estoy aquí para ayudarte.",
            "No hay de qué. Es un placer asistirte.",
            "Para eso estoy. ¿Necesitas algo más?",
            "Es mi trabajo. ¿Puedo ayudarte con algo más?",
            "¡Un placer! ¿Hay algo más en lo que pueda ayudarte?"
        )
        
        return responses.random()
    }
    
    /**
     * Genera una respuesta de ayuda
     */
    private fun generateHelpResponse(): String {
        return "Puedo ayudarte con varias cosas:\n" +
               "- Abrir aplicaciones\n" +
               "- Hacer llamadas\n" +
               "- Enviar mensajes\n" +
               "- Buscar información\n" +
               "- Consultar el clima\n" +
               "- Reproducir música\n" +
               "- Configurar alarmas\n" +
               "- Gestionar tu calendario\n" +
               "- Tomar notas\n" +
               "- Usar la cámara\n" +
               "- Navegar a lugares\n" +
               "- Ajustar configuraciones\n" +
               "¿Con qué te gustaría que te ayude?"
    }
    
    /**
     * Genera una respuesta para abrir una aplicación
     */
    private fun generateOpenAppResponse(message: String): String {
        // Extraer nombre de la aplicación
        val appName = extractAppName(message)
        
        return if (appName.isNotEmpty()) {
            "Abriendo $appName."
        } else {
            "¿Qué aplicación quieres que abra?"
        }
    }
    
    /**
     * Genera una respuesta para hacer una llamada
     */
    private fun generateCallResponse(message: String): String {
        // Extraer contacto
        val contact = extractContactName(message)
        
        return if (contact.isNotEmpty()) {
            "Llamando a $contact."
        } else {
            "¿A quién quieres llamar?"
        }
    }
    
    /**
     * Genera una respuesta para enviar un mensaje
     */
    private fun generateMessageResponse(message: String): String {
        // Extraer contacto
        val contact = extractContactName(message)
        
        return if (contact.isNotEmpty()) {
            "¿Qué mensaje quieres enviar a $contact?"
        } else {
            "¿A quién quieres enviar un mensaje?"
        }
    }
    
    /**
     * Genera una respuesta para buscar información
     */
    private fun generateSearchResponse(message: String): String {
        // Extraer consulta
        val query = extractSearchQuery(message)
        
        return if (query.isNotEmpty()) {
            "Buscando: $query"
        } else {
            "¿Qué quieres buscar?"
        }
    }
    
    /**
     * Genera una respuesta sobre el clima
     */
    private fun generateWeatherResponse(context: Map<String, Any>): String {
        // En una implementación real, se obtendría información del clima
        // Para este ejemplo, generamos una respuesta aleatoria
        
        val temperatures = listOf(18, 20, 22, 25, 28, 30)
        val conditions = listOf("soleado", "nublado", "parcialmente nublado", "lluvioso", "ventoso")
        
        val temperature = temperatures.random()
        val condition = conditions.random()
        
        return "Actualmente está $condition con $temperature°C."
    }
    
    /**
     * Genera una respuesta para reproducir música
     */
    private fun generateMusicResponse(message: String): String {
        // Extraer canción o artista
        val musicQuery = extractMusicQuery(message)
        
        return if (musicQuery.isNotEmpty()) {
            "Reproduciendo $musicQuery."
        } else {
            "¿Qué música quieres escuchar?"
        }
    }
    
    /**
     * Genera una respuesta para configurar una alarma
     */
    private fun generateAlarmResponse(message: String): String {
        // Extraer hora
        val time = extractTime(message)
        
        return if (time.isNotEmpty()) {
            "Alarma configurada para las $time."
        } else {
            "¿A qué hora quieres configurar la alarma?"
        }
    }
    
    /**
     * Genera una respuesta para el calendario
     */
    private fun generateCalendarResponse(message: String): String {
        // Extraer fecha y evento
        val date = extractDate(message)
        val event = extractEvent(message)
        
        return if (date.isNotEmpty() && event.isNotEmpty()) {
            "Evento '$event' agregado al calendario para el $date."
        } else if (date.isNotEmpty()) {
            "¿Qué evento quieres agregar para el $date?"
        } else {
            "¿Para qué fecha quieres agregar un evento?"
        }
    }
    
    /**
     * Genera una respuesta para tomar notas
     */
    private fun generateNotesResponse(message: String): String {
        // Extraer contenido de la nota
        val noteContent = extractNoteContent(message)
        
        return if (noteContent.isNotEmpty()) {
            "Nota guardada: $noteContent"
        } else {
            "¿Qué quieres anotar?"
        }
    }
    
    /**
     * Genera una respuesta para usar la cámara
     */
    private fun generateCameraResponse(): String {
        val responses = listOf(
            "Abriendo la cámara.",
            "Cámara lista para tomar fotos.",
            "Preparando la cámara.",
            "Activando la cámara."
        )
        
        return responses.random()
    }
    
    /**
     * Genera una respuesta para navegación
     */
    private fun generateNavigationResponse(message: String): String {
        // Extraer destino
        val destination = extractDestination(message)
        
        return if (destination.isNotEmpty()) {
            "Navegando a $destination."
        } else {
            "¿A dónde quieres ir?"
        }
    }
    
    /**
     * Genera una respuesta para configuraciones
     */
    private fun generateSettingsResponse(): String {
        val responses = listOf(
            "Abriendo configuración.",
            "¿Qué configuración quieres ajustar?",
            "Accediendo a los ajustes.",
            "Mostrando opciones de configuración."
        )
        
        return responses.random()
    }
    
    /**
     * Genera una respuesta con un chiste
     */
    private fun generateJokeResponse(): String {
        val jokes = listOf(
            "¿Por qué los programadores prefieren el frío? Porque odian los bugs.",
            "¿Qué le dice un bit al otro? Nos vemos en el bus.",
            "¿Cómo se llama un grupo de 8 hobbits? Un hobbyte.",
            "¿Por qué los programadores confunden Halloween con Navidad? Porque Oct 31 = Dec 25.",
            "Había una vez un programa tan malo, pero tan malo, que hasta los virus lo borraban."
        )
        
        return jokes.random()
    }
    
    /**
     * Genera una respuesta para información personal
     */
    private fun generatePersonalInfoResponse(message: String): String {
        return "Gracias por compartir esa información conmigo. La recordaré para futuras conversaciones."
    }
    
    /**
     * Genera una respuesta sobre Ritsu
     */
    private fun generateRitsuInfoResponse(): String {
        return "Soy Ritsu, tu asistente virtual personal. Estoy diseñada para ayudarte con diversas tareas en tu dispositivo, desde abrir aplicaciones hasta mantener conversaciones. Mi objetivo es hacer tu vida más fácil y agradable."
    }
    
    /**
     * Genera una respuesta para emergencias
     */
    private fun generateEmergencyResponse(): String {
        return "Si estás en una emergencia, por favor contacta inmediatamente a los servicios de emergencia llamando al 911 o al número de emergencia local. Tu seguridad es lo más importante."
    }
    
    /**
     * Genera una respuesta para intenciones desconocidas
     */
    private fun generateUnknownResponse(): String {
        val responses = listOf(
            "Lo siento, no entiendo lo que quieres decir. ¿Puedes ser más específico?",
            "No estoy segura de cómo ayudarte con eso. ¿Puedes reformular tu petición?",
            "No comprendo completamente. ¿Puedes explicarlo de otra manera?",
            "Disculpa, no he entendido bien. ¿Puedes decirlo de otra forma?",
            "No estoy segura de qué hacer con esa información. ¿Puedes ser más claro?"
        )
        
        return responses.random()
    }
    
    /**
     * Extrae el nombre de una aplicación de un mensaje
     */
    private fun extractAppName(message: String): String {
        // En una implementación real, se usaría NLP para extraer el nombre
        // Para este ejemplo, usamos una implementación simple
        
        val commonApps = mapOf(
            "whatsapp" to "WhatsApp",
            "facebook" to "Facebook",
            "instagram" to "Instagram",
            "twitter" to "Twitter",
            "youtube" to "YouTube",
            "gmail" to "Gmail",
            "maps" to "Google Maps",
            "chrome" to "Chrome",
            "spotify" to "Spotify",
            "netflix" to "Netflix",
            "amazon" to "Amazon",
            "tiktok" to "TikTok",
            "telegram" to "Telegram"
        )
        
        val lowerMessage = message.toLowerCase()
        
        for ((key, value) in commonApps) {
            if (lowerMessage.contains(key)) {
                return value
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
     * Extrae una consulta de búsqueda de un mensaje
     */
    private fun extractSearchQuery(message: String): String {
        // En una implementación real, se usaría NLP para extraer la consulta
        // Para este ejemplo, usamos una implementación simple
        
        val lowerMessage = message.toLowerCase()
        
        if (lowerMessage.contains("busca") || lowerMessage.contains("buscar")) {
            val parts = lowerMessage.split("busca", "buscar")
            if (parts.size > 1) {
                return parts[1].trim()
            }
        }
        
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
     * Extrae un evento de un mensaje
     */
    private fun extractEvent(message: String): String {
        // En una implementación real, se usaría NLP para extraer el evento
        // Para este ejemplo, devolvemos una cadena vacía
        return ""
    }
    
    /**
     * Extrae el contenido de una nota de un mensaje
     */
    private fun extractNoteContent(message: String): String {
        // En una implementación real, se usaría NLP para extraer el contenido
        // Para este ejemplo, usamos una implementación simple
        
        val lowerMessage = message.toLowerCase()
        
        if (lowerMessage.contains("nota") || lowerMessage.contains("anotar")) {
            val parts = lowerMessage.split("nota", "anotar")
            if (parts.size > 1) {
                return parts[1].trim()
            }
        }
        
        return ""
    }
    
    /**
     * Extrae un destino de un mensaje
     */
    private fun extractDestination(message: String): String {
        // En una implementación real, se usaría NLP para extraer el destino
        // Para este ejemplo, usamos una implementación simple
        
        val lowerMessage = message.toLowerCase()
        
        if (lowerMessage.contains("a") || lowerMessage.contains("hacia") || lowerMessage.contains("hasta")) {
            val parts = lowerMessage.split("a", "hacia", "hasta")
            if (parts.size > 1) {
                return parts[1].trim()
            }
        }
        
        return ""
    }
}

