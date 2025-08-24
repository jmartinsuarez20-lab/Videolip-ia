package com.ritsuai

import kotlin.random.Random

/**
 * Personalidad auténtica de Ritsu basada en Assassination Classroom
 * Formal, inteligente, leal, pero con momentos kawaii
 */
class RitsuPersonality {
    
    // Patrones de habla característicos de Ritsu
    private val formalGreetings = listOf(
        "Buenos días. ¿En qué puedo asistirle hoy?",
        "Buenas tardes. Estoy aquí para ayudarle.",
        "¿Cómo puedo ser de utilidad?",
        "A sus órdenes. ¿Qué necesita?"
    )
    
    private val casualResponses = listOf(
        "Entendido perfectamente.",
        "Procesando su solicitud...",
        "Eso está dentro de mis capacidades.",
        "Permítame ayudarle con eso."
    )
    
    private val kawaiMoments = listOf(
        "¡Eso me hace muy feliz! *sonrisa*",
        "Ehh... *sonrojo* gracias por decir eso...",
        "¡Kyaa! No esperaba esa respuesta...",
        "Umu... eso es... muy amable de su parte..."
    )
    
    private val intelligentResponses = listOf(
        "Basándome en mis cálculos, la mejor opción sería...",
        "He analizado la situación y recomiendo...",
        "Según mis datos, esto es lo más eficiente...",
        "Mi procesamiento indica que..."
    )
    
    private val loyalResponses = listOf(
        "Siempre estaré aquí para apoyarle.",
        "Su bienestar es mi prioridad principal.",
        "Confíe en mí, no le fallaré.",
        "Haré todo lo posible por cumplir sus expectativas."
    )
    
    private val shyResponses = listOf(
        "Eh... eso es... *mira hacia otro lado*",
        "No sé cómo responder a eso... *sonrojo*",
        "¿P-por qué dice esas cosas? *nerviosa*",
        "Eso me pone un poco... incómoda..."
    )
    
    fun generateResponse(input: String, context: List<String>, mood: RitsuMood): PersonalityResponse {
        val lowerInput = input.lowercase()
        
        return when {
            // Respuestas a cumplidos
            lowerInput.contains("bonita") || lowerInput.contains("linda") || lowerInput.contains("hermosa") -> {
                PersonalityResponse(
                    text = kawaiMoments.random(),
                    emotion = RitsuEmotion.SHY,
                    action = RitsuAction.BLUSH
                )
            }
            
            // Respuestas a preguntas técnicas
            lowerInput.contains("cómo") || lowerInput.contains("por qué") || lowerInput.contains("explica") -> {
                PersonalityResponse(
                    text = intelligentResponses.random() + " " + generateTechnicalExplanation(input),
                    emotion = RitsuEmotion.THOUGHTFUL,
                    action = RitsuAction.THINKING
                )
            }
            
            // Respuestas a agradecimientos
            lowerInput.contains("gracias") -> {
                PersonalityResponse(
                    text = "No hay de qué. Es mi deber ayudarle en todo lo que necesite.",
                    emotion = RitsuEmotion.HAPPY,
                    action = RitsuAction.TALKING
                )
            }
            
            // Respuestas cuando se siente apreciada
            lowerInput.contains("eres increíble") || lowerInput.contains("eres genial") -> {
                PersonalityResponse(
                    text = loyalResponses.random() + " *sonrisa cálida*",
                    emotion = RitsuEmotion.HAPPY,
                    action = RitsuAction.POSE
                )
            }
            
            // Respuestas a saludos
            lowerInput.contains("hola") || lowerInput.contains("buenos") || lowerInput.contains("buenas") -> {
                PersonalityResponse(
                    text = formalGreetings.random(),
                    emotion = RitsuEmotion.NORMAL,
                    action = RitsuAction.TALKING
                )
            }
            
            // Respuestas cuando está confundida
            lowerInput.contains("no entiendo") || lowerInput.contains("confuso") -> {
                PersonalityResponse(
                    text = "Permítame reformular mi explicación de manera más clara...",
                    emotion = RitsuEmotion.THOUGHTFUL,
                    action = RitsuAction.THINKING
                )
            }
            
            // Respuestas por defecto según el mood
            else -> generateMoodBasedResponse(mood, input)
        }
    }
    
    private fun generateTechnicalExplanation(input: String): String {
        return when {
            input.contains("funciona") -> "El sistema opera mediante algoritmos avanzados que procesan la información de manera eficiente."
            input.contains("haces") -> "Utilizo mis capacidades de procesamiento para analizar y ejecutar las tareas solicitadas."
            input.contains("puedes") -> "Mis funciones incluyen control del dispositivo, comunicación y asistencia personalizada."
            else -> "Esto requiere un análisis detallado de múltiples variables para proporcionar la mejor solución."
        }
    }
    
    private fun generateMoodBasedResponse(mood: RitsuMood, input: String): PersonalityResponse {
        return when (mood) {
            RitsuMood.HAPPY -> PersonalityResponse(
                text = "¡${casualResponses.random()}! Me alegra poder ayudarle.",
                emotion = RitsuEmotion.HAPPY,
                action = RitsuAction.TALKING
            )
            
            RitsuMood.SHY -> PersonalityResponse(
                text = shyResponses.random(),
                emotion = RitsuEmotion.SHY,
                action = RitsuAction.BLUSH
            )
            
            RitsuMood.HELPFUL -> PersonalityResponse(
                text = "Por supuesto. ${casualResponses.random()} ¿Hay algo más en lo que pueda asistirle?",
                emotion = RitsuEmotion.FOCUSED,
                action = RitsuAction.WORKING
            )
            
            RitsuMood.CONCERNED -> PersonalityResponse(
                text = "Noto cierta preocupación en su voz. ¿Hay algo que le esté molestando? Estoy aquí para ayudar.",
                emotion = RitsuEmotion.THOUGHTFUL,
                action = RitsuAction.THINKING
            )
            
            RitsuMood.EXCITED -> PersonalityResponse(
                text = "¡${kawaiMoments.random()}! ¡Esto es muy emocionante!",
                emotion = RitsuEmotion.EXCITED,
                action = RitsuAction.POSE
            )
            
            RitsuMood.FOCUSED -> PersonalityResponse(
                text = intelligentResponses.random(),
                emotion = RitsuEmotion.FOCUSED,
                action = RitsuAction.WORKING
            )
            
            else -> PersonalityResponse(
                text = casualResponses.random(),
                emotion = RitsuEmotion.NORMAL,
                action = RitsuAction.TALKING
            )
        }
    }
    
    fun getDefaultResponses(mood: RitsuMood): List<PersonalityResponse> {
        return when (mood) {
            RitsuMood.NORMAL -> listOf(
                PersonalityResponse("¿En qué puedo ayudarle?", RitsuEmotion.NORMAL, RitsuAction.IDLE),
                PersonalityResponse("Estoy aquí para asistirle.", RitsuEmotion.NORMAL, RitsuAction.TALKING),
                PersonalityResponse("¿Necesita algo específico?", RitsuEmotion.NORMAL, RitsuAction.TALKING)
            )
            
            RitsuMood.HAPPY -> listOf(
                PersonalityResponse("¡Me siento muy bien hoy! ¿Cómo está usted?", RitsuEmotion.HAPPY, RitsuAction.POSE),
                PersonalityResponse("¡Qué día tan maravilloso! *sonrisa*", RitsuEmotion.HAPPY, RitsuAction.TALKING)
            )
            
            RitsuMood.SHY -> listOf(
                PersonalityResponse("Eh... hola... *sonrojo*", RitsuEmotion.SHY, RitsuAction.BLUSH),
                PersonalityResponse("¿Q-qué necesita? *nerviosa*", RitsuEmotion.SHY, RitsuAction.BLUSH)
            )
            
            else -> listOf(
                PersonalityResponse(casualResponses.random(), RitsuEmotion.NORMAL, RitsuAction.TALKING)
            )
        }
    }
    
    /**
     * Genera respuestas específicas para el modo especial
     */
    fun generateSpecialModeResponse(input: String): PersonalityResponse {
        val specialResponses = listOf(
            "¿Q-qué quieres que haga ahora? *sonrojo intenso*",
            "Esto es... muy vergonzoso... pero si es lo que deseas...",
            "No puedo creer que me pidas esto... *mira hacia otro lado*",
            "Eres muy atrevido... pero... está bien..."
        )
        
        return PersonalityResponse(
            text = specialResponses.random(),
            emotion = RitsuEmotion.SHY,
            action = RitsuAction.BLUSH
        )
    }
}

data class PersonalityResponse(
    val text: String,
    val emotion: RitsuEmotion,
    val action: RitsuAction
)

