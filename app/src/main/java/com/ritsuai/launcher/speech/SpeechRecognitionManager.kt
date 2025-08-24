package com.ritsuai.launcher.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

/**
 * Gestor de reconocimiento de voz para Ritsu.
 * Permite a Ritsu entender comandos hablados del usuario.
 */
class SpeechRecognitionManager(private val context: Context) {

    // Reconocedor de voz
    private var speechRecognizer: SpeechRecognizer? = null
    
    // Estado del reconocimiento
    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState
    
    // Resultados parciales
    private val _partialResults = MutableStateFlow<String>("")
    val partialResults: StateFlow<String> = _partialResults
    
    // Callback para resultados
    private var resultCallback: ((String) -> Unit)? = null
    
    // Idioma de reconocimiento
    private var locale = Locale("es", "ES")
    
    /**
     * Inicializa el reconocedor de voz
     */
    fun initialize() {
        // Verificar si el dispositivo soporta reconocimiento de voz
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.e(TAG, "Reconocimiento de voz no disponible en este dispositivo")
            return
        }
        
        // Crear reconocedor
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        
        // Configurar listener
        speechRecognizer?.setRecognitionListener(createRecognitionListener())
    }
    
    /**
     * Crea el listener para el reconocedor de voz
     */
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _recognitionState.value = RecognitionState.Ready
            }
            
            override fun onBeginningOfSpeech() {
                _recognitionState.value = RecognitionState.Listening
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // Actualizar nivel de volumen si es necesario
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {
                // No implementado
            }
            
            override fun onEndOfSpeech() {
                _recognitionState.value = RecognitionState.Processing
            }
            
            override fun onError(error: Int) {
                val errorMessage = getErrorMessage(error)
                Log.e(TAG, "Error en reconocimiento de voz: $errorMessage")
                _recognitionState.value = RecognitionState.Error(errorMessage)
            }
            
            override fun onResults(results: Bundle?) {
                _recognitionState.value = RecognitionState.Idle
                
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    _partialResults.value = text
                    resultCallback?.invoke(text)
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    _partialResults.value = text
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {
                // No implementado
            }
        }
    }
    
    /**
     * Inicia el reconocimiento de voz
     *
     * @param callback Callback para recibir el resultado
     */
    fun startListening(callback: (String) -> Unit) {
        if (speechRecognizer == null) {
            initialize()
        }
        
        resultCallback = callback
        _partialResults.value = ""
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000)
        }
        
        try {
            speechRecognizer?.startListening(intent)
            _recognitionState.value = RecognitionState.Starting
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar reconocimiento de voz", e)
            _recognitionState.value = RecognitionState.Error("Error al iniciar reconocimiento")
        }
    }
    
    /**
     * Detiene el reconocimiento de voz
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        _recognitionState.value = RecognitionState.Idle
    }
    
    /**
     * Cancela el reconocimiento de voz
     */
    fun cancel() {
        speechRecognizer?.cancel()
        _recognitionState.value = RecognitionState.Idle
    }
    
    /**
     * Libera recursos
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        _recognitionState.value = RecognitionState.Idle
    }
    
    /**
     * Configura el idioma de reconocimiento
     *
     * @param locale Locale del idioma
     */
    fun setLanguage(locale: Locale) {
        this.locale = locale
    }
    
    /**
     * Obtiene un mensaje de error a partir del código
     */
    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
            SpeechRecognizer.ERROR_CLIENT -> "Error de cliente"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permisos insuficientes"
            SpeechRecognizer.ERROR_NETWORK -> "Error de red"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Tiempo de espera de red agotado"
            SpeechRecognizer.ERROR_NO_MATCH -> "No se encontraron coincidencias"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Reconocedor ocupado"
            SpeechRecognizer.ERROR_SERVER -> "Error del servidor"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No se detectó voz"
            else -> "Error desconocido"
        }
    }
    
    /**
     * Estados del reconocimiento de voz
     */
    sealed class RecognitionState {
        object Idle : RecognitionState()
        object Starting : RecognitionState()
        object Ready : RecognitionState()
        object Listening : RecognitionState()
        object Processing : RecognitionState()
        data class Error(val message: String) : RecognitionState()
    }
    
    companion object {
        private const val TAG = "SpeechRecognitionMgr"
        
        // Instancia singleton
        @Volatile
        private var INSTANCE: SpeechRecognitionManager? = null
        
        fun getInstance(context: Context): SpeechRecognitionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SpeechRecognitionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

