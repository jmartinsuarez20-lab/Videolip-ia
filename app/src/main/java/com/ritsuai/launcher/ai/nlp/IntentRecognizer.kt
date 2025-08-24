package com.ritsuai.launcher.ai.nlp

import android.content.Context
import java.util.Locale

/**
 * Reconocedor de intenciones para el procesamiento de lenguaje natural.
 * Identifica la intención del usuario a partir de su mensaje.
 */
class IntentRecognizer(private val context: Context) {

    // Patrones de intenciones
    private val intentPatterns = mapOf(
        "open_app" to listOf(
            "abr[ei]\\s+(.+)",
            "inicia\\s+(.+)",
            "ejecuta\\s+(.+)",
            "lanza\\s+(.+)"
        ),
        "call" to listOf(
            "llama\\s+a\\s+(.+)",
            "telefon[oe]a\\s+a\\s+(.+)",
            "marca\\s+a\\s+(.+)",
            "comunica\\s+con\\s+(.+)"
        ),
        "message" to listOf(
            "envia\\s+(?:un\\s+)?mensaje\\s+a\\s+(.+)",
            "escribe\\s+(?:un\\s+)?mensaje\\s+a\\s+(.+)",
            "manda\\s+(?:un\\s+)?mensaje\\s+a\\s+(.+)",
            "whatsapp\\s+a\\s+(.+)"
        ),
        "search" to listOf(
            "busca\\s+(.+)",
            "encuentra\\s+(.+)",
            "investiga\\s+(.+)",
            "google\\s+(.+)"
        ),
        "set_alarm" to listOf(
            "(?:pon|establece|crea)\\s+(?:una\\s+)?alarma\\s+(?:para|a)\\s+(.+)",
            "despierta(?:me)?\\s+a\\s+(.+)"
        ),
        "set_reminder" to listOf(
            "(?:pon|establece|crea)\\s+(?:un\\s+)?recordatorio\\s+(?:para|de)\\s+(.+)",
            "recuerda(?:me)?\\s+(.+)"
        ),
        "play_music" to listOf(
            "(?:pon|reproduce)\\s+música",
            "(?:pon|reproduce)\\s+(.+)",
            "escuchar\\s+(.+)"
        ),
        "weather" to listOf(
            "(?:cómo\\s+está|cuál\\s+es)\\s+el\\s+clima",
            "(?:va\\s+a\\s+llover|temperatura)\\s+(?:en\\s+)?(.+)?"
        ),
        "greeting" to listOf(
            "hola",
            "buenos\\s+días",
            "buenas\\s+tardes",
            "buenas\\s+noches",
            "qué\\s+tal",
            "cómo\\s+estás"
        ),
        "farewell" to listOf(
            "adiós",
            "hasta\\s+luego",
            "nos\\s+vemos",
            "chao",
            "bye"
        ),
        "thanks" to listOf(
            "gracias",
            "te\\s+lo\\s+agradezco",
            "muy\\s+amable"
        ),
        "help" to listOf(
            "ayuda",
            "ayúdame",
            "necesito\\s+ayuda",
            "cómo\\s+(?:puedo|hago)\\s+(.+)"
        ),
        "preference" to listOf(
            "me\\s+gusta\\s+(.+)",
            "prefiero\\s+(.+)",
            "me\\s+encanta\\s+(.+)"
        ),
        "opinion" to listOf(
            "qué\\s+(?:opinas|piensas)\\s+(?:de|sobre)\\s+(.+)",
            "cuál\\s+es\\s+tu\\s+opinión\\s+(?:de|sobre)\\s+(.+)"
        ),
        "joke" to listOf(
            "cuéntame\\s+(?:un\\s+)?chiste",
            "dime\\s+(?:un\\s+)?chiste",
            "hazme\\s+reír"
        ),
        "unknown" to listOf()
    )
    
    // Patrones de sentimientos
    private val sentimentPatterns = mapOf(
        "positive" to listOf(
            "feliz", "contento", "alegre", "genial", "excelente", "fantástico",
            "maravilloso", "increíble", "bueno", "bien", "me gusta", "me encanta"
        ),
        "negative" to listOf(
            "triste", "enojado", "molesto", "frustrado", "terrible", "horrible",
            "malo", "fatal", "pésimo", "odio", "detesto", "no me gusta"
        ),
        "neutral" to listOf()
    )
    
    /**
     * Reconoce la intención del usuario a partir de su mensaje
     *
     * @param message Mensaje del usuario
     * @return Intención reconocida
     */
    fun recognizeIntent(message: String): String {
        val normalizedMessage = message.lowercase(Locale.getDefault())
        
        // Buscar coincidencias con patrones de intenciones
        for ((intent, patterns) in intentPatterns) {
            if (intent == "unknown") continue
            
            for (pattern in patterns) {
                val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                if (regex.containsMatchIn(normalizedMessage)) {
                    return intent
                }
            }
        }
        
        // Si no se encuentra ninguna coincidencia, devolver "unknown"
        return "unknown"
    }
    
    /**
     * Analiza el sentimiento del mensaje del usuario
     *
     * @param message Mensaje del usuario
     * @return Sentimiento detectado (positive, negative, neutral)
     */
    fun analyzeSentiment(message: String): String {
        val normalizedMessage = message.lowercase(Locale.getDefault())
        
        // Contar palabras positivas y negativas
        var positiveCount = 0
        var negativeCount = 0
        
        // Buscar palabras positivas
        for (word in sentimentPatterns["positive"] ?: emptyList()) {
            if (normalizedMessage.contains(word)) {
                positiveCount++
            }
        }
        
        // Buscar palabras negativas
        for (word in sentimentPatterns["negative"] ?: emptyList()) {
            if (normalizedMessage.contains(word)) {
                negativeCount++
            }
        }
        
        // Determinar sentimiento
        return when {
            positiveCount > negativeCount -> "positive"
            negativeCount > positiveCount -> "negative"
            else -> "neutral"
        }
    }
    
    /**
     * Extrae parámetros de la intención
     *
     * @param message Mensaje del usuario
     * @param intent Intención reconocida
     * @return Mapa de parámetros extraídos
     */
    fun extractParameters(message: String, intent: String): Map<String, String> {
        val params = mutableMapOf<String, String>()
        val normalizedMessage = message.lowercase(Locale.getDefault())
        
        // Buscar coincidencias con patrones de la intención
        val patterns = intentPatterns[intent] ?: return params
        
        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val matchResult = regex.find(normalizedMessage)
            
            if (matchResult != null && matchResult.groupValues.size > 1) {
                // El primer grupo capturado es el parámetro principal
                val param = matchResult.groupValues[1].trim()
                
                when (intent) {
                    "open_app" -> params["app_name"] = param
                    "call" -> params["contact"] = param
                    "message" -> params["contact"] = param
                    "search" -> params["query"] = param
                    "set_alarm" -> params["time"] = param
                    "set_reminder" -> params["reminder"] = param
                    "play_music" -> params["music"] = param
                    "weather" -> params["location"] = param
                    else -> params["param"] = param
                }
                
                break
            }
        }
        
        return params
    }
}

