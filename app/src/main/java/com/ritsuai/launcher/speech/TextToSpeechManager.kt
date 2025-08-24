package com.ritsuai.launcher.speech

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

/**
 * Gestor de síntesis de voz para Ritsu.
 * Permite a Ritsu hablar con el usuario.
 */
class TextToSpeechManager private constructor(private val context: Context) {

    // Motor de síntesis de voz
    private var textToSpeech: TextToSpeech? = null
    
    // Estado de inicialización
    private var isInitialized = false
    private val initializationDeferred = CompletableDeferred<Boolean>()
    
    // Configuración de voz
    private var speechRate = 1.0f
    private var pitch = 1.0f
    private var volume = 1.0f
    
    // Locale para el idioma
    private var locale = Locale("es", "ES")
    
    init {
        initialize()
    }
    
    /**
     * Inicializa el motor de síntesis de voz
     */
    private fun initialize() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Configurar idioma
                val result = textToSpeech?.setLanguage(locale)
                
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Idioma no soportado: $locale")
                    isInitialized = false
                    initializationDeferred.complete(false)
                } else {
                    // Configurar voz femenina si está disponible
                    setFemaleVoice()
                    
                    // Configurar parámetros
                    textToSpeech?.setSpeechRate(speechRate)
                    textToSpeech?.setPitch(pitch)
                    
                    isInitialized = true
                    initializationDeferred.complete(true)
                }
            } else {
                Log.e(TAG, "Error al inicializar TextToSpeech: $status")
                isInitialized = false
                initializationDeferred.complete(false)
            }
        }
        
        // Configurar listener de progreso
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                // Inicio de la síntesis
            }
            
            override fun onDone(utteranceId: String?) {
                // Fin de la síntesis
            }
            
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                // Error en la síntesis
                Log.e(TAG, "Error en la síntesis de voz: $utteranceId")
            }
            
            override fun onError(utteranceId: String?, errorCode: Int) {
                super.onError(utteranceId, errorCode)
                // Error en la síntesis con código
                Log.e(TAG, "Error en la síntesis de voz: $utteranceId, código: $errorCode")
            }
        })
    }
    
    /**
     * Configura una voz femenina si está disponible
     */
    private fun setFemaleVoice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val voices = textToSpeech?.voices
            if (voices != null) {
                // Buscar voces femeninas en español
                val femaleVoices = voices.filter { voice ->
                    voice.name.contains("female", ignoreCase = true) &&
                            voice.locale.language == "es"
                }
                
                if (femaleVoices.isNotEmpty()) {
                    // Usar la primera voz femenina encontrada
                    textToSpeech?.voice = femaleVoices.first()
                } else {
                    // Buscar cualquier voz en español
                    val spanishVoices = voices.filter { voice ->
                        voice.locale.language == "es"
                    }
                    
                    if (spanishVoices.isNotEmpty()) {
                        textToSpeech?.voice = spanishVoices.first()
                    }
                }
            }
        }
    }
    
    /**
     * Habla un texto
     *
     * @param text Texto a hablar
     * @param queueMode Modo de cola (QUEUE_ADD o QUEUE_FLUSH)
     * @return true si se inició la síntesis correctamente, false en caso contrario
     */
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "TextToSpeech no inicializado")
            return false
        }
        
        val utteranceId = UUID.randomUUID().toString()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val result = textToSpeech?.speak(text, queueMode, null, utteranceId)
            return result == TextToSpeech.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            val params = HashMap<String, String>().apply {
                put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                put(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.toString())
            }
            
            @Suppress("DEPRECATION")
            val result = textToSpeech?.speak(text, queueMode, params)
            return result == TextToSpeech.SUCCESS
        }
    }
    
    /**
     * Detiene la síntesis de voz
     */
    fun stop() {
        textToSpeech?.stop()
    }
    
    /**
     * Configura la velocidad de habla
     *
     * @param rate Velocidad (0.0 - 2.0)
     */
    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.1f, 2.0f)
        textToSpeech?.setSpeechRate(speechRate)
    }
    
    /**
     * Configura el tono de voz
     *
     * @param pitch Tono (0.0 - 2.0)
     */
    fun setPitch(pitch: Float) {
        this.pitch = pitch.coerceIn(0.1f, 2.0f)
        textToSpeech?.setPitch(this.pitch)
    }
    
    /**
     * Configura el volumen
     *
     * @param volume Volumen (0.0 - 1.0)
     */
    fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0.0f, 1.0f)
    }
    
    /**
     * Configura el idioma
     *
     * @param locale Locale del idioma
     * @return true si el idioma es soportado, false en caso contrario
     */
    fun setLanguage(locale: Locale): Boolean {
        val result = textToSpeech?.setLanguage(locale)
        
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e(TAG, "Idioma no soportado: $locale")
            return false
        }
        
        this.locale = locale
        return true
    }
    
    /**
     * Verifica si el motor de síntesis está hablando
     *
     * @return true si está hablando, false en caso contrario
     */
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }
    
    /**
     * Libera recursos
     */
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
    
    /**
     * Espera a que el motor de síntesis esté inicializado
     *
     * @return true si se inicializó correctamente, false en caso contrario
     */
    suspend fun awaitInitialization(): Boolean {
        return withContext(Dispatchers.Default) {
            initializationDeferred.await()
        }
    }
    
    companion object {
        private const val TAG = "TextToSpeechManager"
        
        // Instancia singleton
        @Volatile
        private var INSTANCE: TextToSpeechManager? = null
        
        fun getInstance(context: Context): TextToSpeechManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TextToSpeechManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

