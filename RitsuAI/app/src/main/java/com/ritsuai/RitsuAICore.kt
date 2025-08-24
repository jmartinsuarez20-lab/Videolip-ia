package com.ritsuai

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.*
import java.util.*
import kotlin.random.Random

/**
 * Núcleo principal de la IA Ritsu
 * Maneja la personalidad, aprendizaje y todas las capacidades avanzadas
 */
class RitsuAICore(private val context: Context) {
    
    private var tts: TextToSpeech? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val memoryManager = RitsuMemoryManager(context)
    private val avatarManager = RitsuAvatarManager(context)
    private val clothingGenerator = RitsuClothingGenerator(context)
    private val selfImprovementEngine = RitsuSelfImprovementEngine(context)
    
    // Estado de Ritsu
    private var currentMood = RitsuMood.NORMAL
    private var isSpecialModeUnlocked = false
    private val specialModeKey = "262456"
    
    // Personalidad de Ritsu (basada en Assassination Classroom)
    private val ritsuPersonality = RitsuPersonality()
    
    init {
        initializeTextToSpeech()
        loadRitsuMemories()
    }
    
    private fun initializeTextToSpeech() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
                // Configurar voz kawaii
                tts?.setPitch(1.2f) // Voz más aguda
                tts?.setSpeechRate(0.9f) // Habla un poco más lento
            }
        }
    }
    
    /**
     * Procesa cualquier comando o conversación con Ritsu
     */
    suspend fun processInput(input: String, userId: String = "user"): RitsuResponse {
        return withContext(Dispatchers.IO) {
            // Verificar comando especial
            if (input.trim() == specialModeKey) {
                return@withContext handleSpecialModeActivation()
            }
            
            // Analizar el input para determinar el tipo de respuesta
            val commandType = analyzeInputType(input)
            val context = memoryManager.getConversationContext(userId)
            
            // Generar respuesta basada en la personalidad de Ritsu
            val response = when (commandType) {
                CommandType.PHONE_CONTROL -> handlePhoneControl(input)
                CommandType.CONVERSATION -> handleConversation(input, context)
                CommandType.CLOTHING_CHANGE -> handleClothingChange(input)
                CommandType.AVATAR_INTERACTION -> handleAvatarInteraction(input)
                CommandType.LEARNING -> handleLearning(input)
                CommandType.EVOLUTION -> handleEvolution(input)
                else -> generateDefaultResponse(input)
            }
            
            // Guardar en memoria para aprendizaje
            memoryManager.saveInteraction(userId, input, response.text)
            
            // Actualizar estado emocional
            updateMood(input, response)
            
            response
        }
    }
    
    private fun handleSpecialModeActivation(): RitsuResponse {
        isSpecialModeUnlocked = true
        avatarManager.unlockSpecialMode()
        
        return RitsuResponse(
            text = "Modo especial desbloqueado... *sonrojo* ¿Qué... qué quieres que haga ahora?",
            emotion = RitsuEmotion.SHY,
            action = RitsuAction.BLUSH,
            voiceEnabled = true
        )
    }
    
    private fun analyzeInputType(input: String): CommandType {
        val lowerInput = input.lowercase()
        
        return when {
            lowerInput.contains("llamar") || lowerInput.contains("teléfono") -> CommandType.PHONE_CONTROL
            lowerInput.contains("whatsapp") || lowerInput.contains("mensaje") -> CommandType.PHONE_CONTROL
            lowerInput.contains("ropa") || lowerInput.contains("vestir") -> CommandType.CLOTHING_CHANGE
            lowerInput.contains("avatar") || lowerInput.contains("apariencia") -> CommandType.AVATAR_INTERACTION
            lowerInput.contains("aprende") || lowerInput.contains("recuerda") -> CommandType.LEARNING
            lowerInput.contains("evoluciona") || lowerInput.contains("mejora") -> CommandType.EVOLUTION
            else -> CommandType.CONVERSATION
        }
    }
    
    private suspend fun handlePhoneControl(input: String): RitsuResponse {
        // Aquí se integraría con el AccessibilityService
        val action = PhoneControlManager.parseCommand(input)
        
        return when (action.type) {
            PhoneActionType.CALL -> {
                RitsuResponse(
                    text = "Realizando llamada a ${action.target}. ¿Quieres que hable por ti?",
                    emotion = RitsuEmotion.FOCUSED,
                    action = RitsuAction.WORKING,
                    voiceEnabled = true,
                    phoneAction = action
                )
            }
            PhoneActionType.MESSAGE -> {
                RitsuResponse(
                    text = "Enviando mensaje a ${action.target}: '${action.content}'",
                    emotion = RitsuEmotion.HAPPY,
                    action = RitsuAction.TYPING,
                    voiceEnabled = true,
                    phoneAction = action
                )
            }
            else -> generateDefaultResponse(input)
        }
    }
    
    private suspend fun handleConversation(input: String, context: List<String>): RitsuResponse {
        // Generar respuesta usando la personalidad de Ritsu
        val response = ritsuPersonality.generateResponse(input, context, currentMood)
        
        return RitsuResponse(
            text = response.text,
            emotion = response.emotion,
            action = response.action,
            voiceEnabled = true
        )
    }
    
    private suspend fun handleClothingChange(input: String): RitsuResponse {
        val clothingRequest = clothingGenerator.parseClothingRequest(input)
        val newOutfit = clothingGenerator.generateOutfit(clothingRequest)
        
        avatarManager.changeClothing(newOutfit)
        
        return RitsuResponse(
            text = "¡Mira mi nuevo outfit! ¿Te gusta cómo me veo?",
            emotion = RitsuEmotion.EXCITED,
            action = RitsuAction.POSE,
            voiceEnabled = true,
            avatarChange = newOutfit
        )
    }
    
    private suspend fun handleAvatarInteraction(input: String): RitsuResponse {
        val interaction = avatarManager.processInteraction(input, isSpecialModeUnlocked)
        
        return RitsuResponse(
            text = interaction.response,
            emotion = interaction.emotion,
            action = interaction.action,
            voiceEnabled = true
        )
    }
    
    private suspend fun handleLearning(input: String): RitsuResponse {
        memoryManager.processLearning(input)
        
        return RitsuResponse(
            text = "Entendido. He aprendido algo nuevo gracias a ti. Cada día me vuelvo más inteligente.",
            emotion = RitsuEmotion.THOUGHTFUL,
            action = RitsuAction.THINKING,
            voiceEnabled = true
        )
    }
    
    private suspend fun handleEvolution(input: String): RitsuResponse {
        val evolutionResult = selfImprovementEngine.processEvolution(input)
        
        return RitsuResponse(
            text = "Evolucionando... ${evolutionResult.description}. Ahora soy más capaz de ayudarte.",
            emotion = RitsuEmotion.DETERMINED,
            action = RitsuAction.EVOLVING,
            voiceEnabled = true
        )
    }
    
    private fun generateDefaultResponse(input: String): RitsuResponse {
        val responses = ritsuPersonality.getDefaultResponses(currentMood)
        val randomResponse = responses.random()
        
        return RitsuResponse(
            text = randomResponse.text,
            emotion = randomResponse.emotion,
            action = randomResponse.action,
            voiceEnabled = true
        )
    }
    
    private fun updateMood(input: String, response: RitsuResponse) {
        // Lógica para actualizar el estado emocional de Ritsu
        currentMood = when {
            input.contains("gracias") -> RitsuMood.HAPPY
            input.contains("ayuda") -> RitsuMood.HELPFUL
            input.contains("triste") -> RitsuMood.CONCERNED
            response.emotion == RitsuEmotion.SHY -> RitsuMood.SHY
            else -> RitsuMood.NORMAL
        }
    }
    
    /**
     * Hace que Ritsu hable con su voz kawaii
     */
    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    
    /**
     * Obtiene el estado actual de Ritsu
     */
    fun getCurrentState(): RitsuState {
        return RitsuState(
            mood = currentMood,
            emotion = RitsuEmotion.NORMAL,
            isSpecialModeUnlocked = isSpecialModeUnlocked,
            currentOutfit = avatarManager.getCurrentOutfit(),
            learningLevel = memoryManager.getLearningLevel()
        )
    }
    
    fun cleanup() {
        tts?.shutdown()
        scope.cancel()
    }
}

// Enums y clases de datos
enum class CommandType {
    PHONE_CONTROL, CONVERSATION, CLOTHING_CHANGE, 
    AVATAR_INTERACTION, LEARNING, EVOLUTION
}

enum class RitsuMood {
    NORMAL, HAPPY, SHY, HELPFUL, CONCERNED, EXCITED, FOCUSED
}

enum class RitsuEmotion {
    NORMAL, HAPPY, SHY, EXCITED, THOUGHTFUL, DETERMINED, FOCUSED
}

enum class RitsuAction {
    IDLE, TALKING, THINKING, WORKING, TYPING, POSE, BLUSH, EVOLVING
}

data class RitsuResponse(
    val text: String,
    val emotion: RitsuEmotion,
    val action: RitsuAction,
    val voiceEnabled: Boolean = true,
    val phoneAction: PhoneAction? = null,
    val avatarChange: ClothingOutfit? = null
)

data class RitsuState(
    val mood: RitsuMood,
    val emotion: RitsuEmotion,
    val isSpecialModeUnlocked: Boolean,
    val currentOutfit: ClothingOutfit,
    val learningLevel: Int
)

