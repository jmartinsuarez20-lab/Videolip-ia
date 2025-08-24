package com.ritsuai.launcher.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ritsuai.launcher.R
import com.ritsuai.launcher.ai.RitsuAICore
import com.ritsuai.launcher.databinding.ActivityVoiceCommandBinding
import com.ritsuai.launcher.speech.SpeechRecognitionManager
import com.ritsuai.launcher.speech.TextToSpeechManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Actividad para comandos de voz.
 * Permite al usuario interactuar con Ritsu mediante la voz.
 */
class VoiceCommandActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVoiceCommandBinding
    
    // Componentes de Ritsu
    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private lateinit var textToSpeechManager: TextToSpeechManager
    private lateinit var aiCore: RitsuAICore
    
    // Animaciones
    private lateinit var pulseAnimation: Animation
    
    // Código de solicitud para permisos
    private val PERMISSION_REQUEST_CODE = 100
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar binding
        binding = ActivityVoiceCommandBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar componentes
        speechRecognitionManager = SpeechRecognitionManager.getInstance(this)
        textToSpeechManager = TextToSpeechManager.getInstance(this)
        aiCore = RitsuAICore.getInstance(this)
        
        // Inicializar animaciones
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation)
        
        // Configurar botón de escucha
        binding.listenButton.setOnClickListener {
            startListening()
        }
        
        // Configurar botón de cancelar
        binding.cancelButton.setOnClickListener {
            cancelListening()
        }
        
        // Observar estado del reconocimiento
        lifecycleScope.launch {
            speechRecognitionManager.recognitionState.collect { state ->
                updateUiForState(state)
            }
        }
        
        // Observar resultados parciales
        lifecycleScope.launch {
            speechRecognitionManager.partialResults.collect { text ->
                binding.recognizedText.text = text
            }
        }
        
        // Verificar permisos
        checkPermissions()
    }
    
    override fun onResume() {
        super.onResume()
        
        // Inicializar reconocimiento de voz
        speechRecognitionManager.initialize()
    }
    
    override fun onPause() {
        super.onPause()
        
        // Detener reconocimiento
        speechRecognitionManager.cancel()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Liberar recursos
        speechRecognitionManager.destroy()
    }
    
    /**
     * Inicia el reconocimiento de voz
     */
    private fun startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            // Mostrar UI de escucha
            binding.listenButton.visibility = View.GONE
            binding.cancelButton.visibility = View.VISIBLE
            binding.listeningIndicator.visibility = View.VISIBLE
            binding.listeningIndicator.startAnimation(pulseAnimation)
            binding.statusText.text = getString(R.string.listening)
            
            // Iniciar reconocimiento
            speechRecognitionManager.startListening { result ->
                processVoiceCommand(result)
            }
        } else {
            // Solicitar permiso
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    /**
     * Cancela el reconocimiento de voz
     */
    private fun cancelListening() {
        // Detener reconocimiento
        speechRecognitionManager.cancel()
        
        // Actualizar UI
        binding.listenButton.visibility = View.VISIBLE
        binding.cancelButton.visibility = View.GONE
        binding.listeningIndicator.clearAnimation()
        binding.listeningIndicator.visibility = View.GONE
        binding.statusText.text = getString(R.string.tap_to_speak)
    }
    
    /**
     * Procesa un comando de voz
     */
    private fun processVoiceCommand(command: String) {
        // Mostrar comando reconocido
        binding.recognizedText.text = command
        
        // Actualizar UI
        binding.listenButton.visibility = View.VISIBLE
        binding.cancelButton.visibility = View.GONE
        binding.listeningIndicator.clearAnimation()
        binding.listeningIndicator.visibility = View.GONE
        binding.statusText.text = getString(R.string.processing)
        
        // Procesar comando con IA
        lifecycleScope.launch {
            val response = aiCore.processMessage(command)
            
            // Mostrar respuesta
            binding.responseText.text = response
            binding.statusText.text = getString(R.string.tap_to_speak)
            
            // Hablar respuesta
            textToSpeechManager.speak(response)
        }
    }
    
    /**
     * Actualiza la UI según el estado del reconocimiento
     */
    private fun updateUiForState(state: SpeechRecognitionManager.RecognitionState) {
        when (state) {
            is SpeechRecognitionManager.RecognitionState.Idle -> {
                binding.statusText.text = getString(R.string.tap_to_speak)
                binding.listenButton.visibility = View.VISIBLE
                binding.cancelButton.visibility = View.GONE
                binding.listeningIndicator.clearAnimation()
                binding.listeningIndicator.visibility = View.GONE
            }
            is SpeechRecognitionManager.RecognitionState.Starting -> {
                binding.statusText.text = getString(R.string.starting)
            }
            is SpeechRecognitionManager.RecognitionState.Ready -> {
                binding.statusText.text = getString(R.string.ready)
            }
            is SpeechRecognitionManager.RecognitionState.Listening -> {
                binding.statusText.text = getString(R.string.listening)
                binding.listenButton.visibility = View.GONE
                binding.cancelButton.visibility = View.VISIBLE
                binding.listeningIndicator.visibility = View.VISIBLE
                binding.listeningIndicator.startAnimation(pulseAnimation)
            }
            is SpeechRecognitionManager.RecognitionState.Processing -> {
                binding.statusText.text = getString(R.string.processing)
                binding.listeningIndicator.clearAnimation()
            }
            is SpeechRecognitionManager.RecognitionState.Error -> {
                binding.statusText.text = state.message
                binding.listenButton.visibility = View.VISIBLE
                binding.cancelButton.visibility = View.GONE
                binding.listeningIndicator.clearAnimation()
                binding.listeningIndicator.visibility = View.GONE
            }
        }
    }
    
    /**
     * Verifica y solicita permisos necesarios
     */
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                startListening()
            } else {
                // Permiso denegado
                binding.statusText.text = getString(R.string.permission_denied)
            }
        }
    }
}

