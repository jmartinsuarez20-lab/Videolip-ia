package com.ritsuai.launcher.ai.nlp

import android.content.Context
import com.ritsuai.launcher.R
import java.util.Calendar
import java.util.Random

/**
 * Generador de respuestas para Ritsu.
 * Crea respuestas naturales basadas en la intención del usuario y el contexto.
 */
class ResponseGenerator(private val context: Context) {

    // Generador de números aleatorios
    private val random = Random()
    
    // Plantillas de respuestas por intención
    private val responseTemplates = mapOf(
        "open_app" to listOf(
            "Abriendo {app_name}.",
            "Iniciando {app_name} ahora mismo.",
            "Ejecutando {app_name} para ti.",
            "Entendido, abriendo {app_name}."
        ),
        "call" to listOf(
            "Llamando a {contact}.",
            "Iniciando llamada con {contact}.",
            "Marcando a {contact} ahora mismo.",
            "Comunicándote con {contact}."
        ),
        "message" to listOf(
            "Enviando mensaje a {contact}.",
            "Preparando mensaje para {contact}.",
            "Abriendo chat con {contact}.",
            "¿Qué mensaje quieres enviar a {contact}?"
        ),
        "search" to listOf(
            "Buscando {query}.",
            "Aquí tienes los resultados para {query}.",
            "Investigando sobre {query}.",
            "Encontrando información sobre {query}."
        ),
        "set_alarm" to listOf(
            "Alarma establecida para {time}.",
            "Te despertaré a las {time}.",
            "Alarma programada para {time}.",
            "Entendido, alarma para {time}."
        ),
        "set_reminder" to listOf(
            "Recordatorio creado para {reminder}.",
            "Te recordaré {reminder}.",
            "No olvidaré recordarte {reminder}.",
            "Entendido, te recordaré {reminder}."
        ),
        "play_music" to listOf(
            "Reproduciendo {music}.",
            "Poniendo {music} para ti.",
            "Disfrutando de {music}.",
            "Iniciando reproducción de {music}."
        ),
        "weather" to listOf(
            "El clima en {location} es {weather_description}.",
            "En {location} está {weather_description} con una temperatura de {temperature}.",
            "Actualmente en {location}: {weather_description}.",
            "El pronóstico para {location} indica {weather_description}."
        ),
        "greeting" to listOf(
            "Hola, ¿en qué puedo ayudarte hoy?",
            "Buenos días, ¿cómo puedo asistirte?",
            "Saludos, estoy aquí para lo que necesites.",
            "Hola, soy Ritsu. ¿Qué puedo hacer por ti?"
        ),
        "farewell" to listOf(
            "Hasta luego, estaré aquí cuando me necesites.",
            "Adiós, que tengas un buen día.",
            "Nos vemos pronto.",
            "Hasta pronto, avísame si necesitas algo más."
        ),
        "thanks" to listOf(
            "De nada, es un placer ayudarte.",
            "No hay de qué, para eso estoy.",
            "El placer es mío.",
            "Siempre a tu servicio."
        ),
        "help" to listOf(
            "Puedo ayudarte con varias tareas. Prueba a pedirme que abra aplicaciones, haga llamadas, envíe mensajes, busque información, etc.",
            "Estoy aquí para asistirte. Puedo abrir apps, hacer llamadas, enviar mensajes, buscar información y más.",
            "¿En qué necesitas ayuda? Puedo realizar diversas tareas como abrir aplicaciones, hacer llamadas o buscar información.",
            "Dime qué necesitas y haré lo posible por ayudarte."
        ),
        "preference" to listOf(
            "Entendido, recordaré que te gusta {param}.",
            "Me alegra saber que te gusta {param}.",
            "Gracias por compartir tu preferencia por {param}.",
            "Anotado, te gusta {param}."
        ),
        "opinion" to listOf(
            "Sobre {param}, creo que es interesante desde mi perspectiva.",
            "Mi opinión sobre {param} está basada en la información disponible.",
            "Respecto a {param}, hay diversos puntos de vista a considerar.",
            "Encuentro que {param} es un tema fascinante."
        ),
        "joke" to listOf(
            "¿Por qué los programadores prefieren el frío? Porque odian los bugs.",
            "¿Qué hace un programador zombi? Buscar CEREBROS... y café.",
            "¿Cómo se llama un grupo de 8 bits? Un byte-allón.",
            "Error 404: Chiste no encontrado."
        ),
        "unknown" to listOf(
            "Lo siento, no he entendido bien. ¿Podrías reformular tu petición?",
            "No estoy segura de lo que me pides. ¿Puedes ser más específico?",
            "Disculpa, no he comprendido. ¿Puedes decirlo de otra manera?",
            "No he captado bien tu mensaje. ¿Podrías explicarlo de otra forma?"
        )
    )
    
    // Respuestas basadas en sentimiento
    private val sentimentResponses = mapOf(
        "positive" to listOf(
            "Me alegra que estés de buen humor.",
            "Es genial verte tan positivo.",
            "Tu entusiasmo es contagioso.",
            "Me encanta tu actitud positiva."
        ),
        "negative" to listOf(
            "Lamento que te sientas así.",
            "¿Hay algo en lo que pueda ayudarte a sentirte mejor?",
            "Estoy aquí para apoyarte.",
            "Entiendo que no es un buen momento."
        )
    )
    
    /**
     * Genera una respuesta basada en la intención del usuario y el contexto
     *
     * @param userMessage Mensaje del usuario
     * @param intent Intención detectada
     * @param sentiment Sentimiento detectado
     * @param context Contexto de la conversación
     * @return Respuesta generada
     */
    fun generateResponse(
        userMessage: String,
        intent: String,
        sentiment: String,
        context: ContextManager.ConversationContext
    ): String {
        // Obtener plantillas para la intención
        val templates = responseTemplates[intent] ?: responseTemplates["unknown"]!!
        
        // Seleccionar una plantilla aleatoria
        val template = templates[random.nextInt(templates.size)]
        
        // Extraer parámetros
        val params = extractParameters(userMessage, intent)
        
        // Reemplazar parámetros en la plantilla
        var response = replaceParameters(template, params)
        
        // Añadir respuesta basada en sentimiento si es relevante
        if (sentiment != "neutral" && random.nextFloat() < 0.3f) {
            val sentimentTemplates = sentimentResponses[sentiment] ?: emptyList()
            if (sentimentTemplates.isNotEmpty()) {
                val sentimentResponse = sentimentTemplates[random.nextInt(sentimentTemplates.size)]
                response = "$sentimentResponse $response"
            }
        }
        
        // Personalizar según hora del día
        response = addTimeContext(response)
        
        return response
    }
    
    /**
     * Extrae parámetros del mensaje del usuario
     */
    private fun extractParameters(message: String, intent: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        
        // Implementación simplificada
        when (intent) {
            "open_app" -> {
                val appPattern = "(?:abr[ei]|inicia|ejecuta|lanza)\\s+(.+)"
                val regex = Regex(appPattern, RegexOption.IGNORE_CASE)
                val matchResult = regex.find(message)
                if (matchResult != null && matchResult.groupValues.size > 1) {
                    params["app_name"] = matchResult.groupValues[1].trim()
                }
            }
            "call" -> {
                val callPattern = "(?:llama|telefon[oe]a|marca|comunica)\\s+(?:a|con)\\s+(.+)"
                val regex = Regex(callPattern, RegexOption.IGNORE_CASE)
                val matchResult = regex.find(message)
                if (matchResult != null && matchResult.groupValues.size > 1) {
                    params["contact"] = matchResult.groupValues[1].trim()
                }
            }
            // Implementar para otras intenciones...
        }
        
        return params
    }
    
    /**
     * Reemplaza los parámetros en la plantilla
     */
    private fun replaceParameters(template: String, params: Map<String, String>): String {
        var result = template
        
        for ((key, value) in params) {
            result = result.replace("{$key}", value)
        }
        
        // Valores por defecto para parámetros no proporcionados
        result = result.replace(Regex("\\{[^}]+\\}"), "eso")
        
        return result
    }
    
    /**
     * Añade contexto temporal a la respuesta
     */
    private fun addTimeContext(response: String): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        // Solo modificar si es una respuesta de saludo
        if (response.startsWith("Hola") || response.startsWith("Buenos") || response.startsWith("Saludos")) {
            return when {
                hour < 12 -> response.replace("Hola", "Buenos días").replace("Saludos", "Buenos días")
                hour < 20 -> response.replace("Hola", "Buenas tardes").replace("Saludos", "Buenas tardes")
                else -> response.replace("Hola", "Buenas noches").replace("Saludos", "Buenas noches")
            }
        }
        
        return response
    }
    
    /**
     * Obtiene un chiste aleatorio
     */
    fun getRandomJoke(): String {
        val jokes = responseTemplates["joke"] ?: return "No tengo chistes en este momento."
        return jokes[random.nextInt(jokes.size)]
    }
}

