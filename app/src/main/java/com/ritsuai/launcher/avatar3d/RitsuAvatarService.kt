package com.ritsuai.launcher.avatar3d

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.ritsuai.launcher.R
import com.ritsuai.launcher.ai.RitsuAICore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Servicio para mostrar el avatar 3D de Ritsu como overlay.
 * Permite que Ritsu aparezca sobre cualquier aplicación.
 */
class RitsuAvatarService : Service() {

    // Tag para logs
    private val TAG = "RitsuAvatarService"
    
    // WindowManager para mostrar el overlay
    private lateinit var windowManager: WindowManager
    
    // Vista del avatar
    private lateinit var avatarView: View
    
    // Parámetros de la ventana
    private lateinit var params: WindowManager.LayoutParams
    
    // Componentes de la vista
    private lateinit var avatarImageView: ImageView
    private lateinit var speechBubbleView: TextView
    
    // Controladores
    private lateinit var animationController: RitsuAnimationController
    private lateinit var physicsEngine: RitsuPhysicsEngine
    
    // Núcleo de IA
    private lateinit var aiCore: RitsuAICore
    
    // Scope de corrutinas
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Estado del avatar
    private var isVisible = true
    private var isSpeaking = false
    private var currentMood = "neutral"
    
    // Coordenadas para arrastrar
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Creando servicio de avatar 3D")
        
        // Inicializar componentes
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        aiCore = RitsuAICore.getInstance(applicationContext)
        
        // Crear vista del avatar
        createAvatarView()
        
        // Inicializar controladores
        animationController = RitsuAnimationController(avatarImageView)
        physicsEngine = RitsuPhysicsEngine()
        
        // Mostrar avatar
        showAvatar()
        
        // Iniciar animación de idle
        animationController.playIdleAnimation()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Iniciando servicio de avatar 3D")
        
        // Procesar intent si es necesario
        intent?.let {
            when (it.action) {
                ACTION_SHOW_AVATAR -> showAvatar()
                ACTION_HIDE_AVATAR -> hideAvatar()
                ACTION_SPEAK -> {
                    val text = it.getStringExtra(EXTRA_SPEECH_TEXT) ?: ""
                    if (text.isNotEmpty()) {
                        showSpeechBubble(text)
                    }
                }
                ACTION_SET_MOOD -> {
                    val mood = it.getStringExtra(EXTRA_MOOD) ?: "neutral"
                    setMood(mood)
                }
            }
        }
        
        // Iniciar bucle de animación
        startAnimationLoop()
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        Log.d(TAG, "Destruyendo servicio de avatar 3D")
        
        // Detener corrutinas
        serviceScope.cancel()
        
        // Eliminar vista
        if (::avatarView.isInitialized && ::windowManager.isInitialized) {
            windowManager.removeView(avatarView)
        }
    }
    
    /**
     * Crea la vista del avatar
     */
    private fun createAvatarView() {
        // Inflar layout
        avatarView = LayoutInflater.from(this).inflate(R.layout.overlay_ritsu_3d, null)
        
        // Obtener componentes
        avatarImageView = avatarView.findViewById(R.id.avatar_image)
        speechBubbleView = avatarView.findViewById(R.id.speech_bubble)
        
        // Configurar parámetros de la ventana
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        // Posición inicial
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 100
        
        // Configurar eventos táctiles
        setupTouchEvents()
    }
    
    /**
     * Configura eventos táctiles para arrastrar el avatar
     */
    private fun setupTouchEvents() {
        avatarView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Guardar posición inicial
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    
                    // Iniciar física de arrastre
                    physicsEngine.startDrag()
                    
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Calcular nueva posición
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    
                    params.x = (initialX + dx).toInt()
                    params.y = (initialY + dy).toInt()
                    
                    // Actualizar posición
                    windowManager.updateViewLayout(avatarView, params)
                    
                    // Actualizar física
                    physicsEngine.updateDrag(dx.toFloat(), dy.toFloat())
                    
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Finalizar arrastre con velocidad
                    val velocityX = event.rawX - initialTouchX
                    val velocityY = event.rawY - initialTouchY
                    
                    physicsEngine.endDrag(velocityX, velocityY)
                    
                    // Aplicar inercia
                    applyInertia()
                    
                    true
                }
                else -> false
            }
        }
        
        // Configurar clic en el avatar
        avatarImageView.setOnClickListener {
            // Alternar visibilidad de la burbuja de diálogo
            if (speechBubbleView.visibility == View.VISIBLE) {
                speechBubbleView.visibility = View.GONE
            } else {
                // Generar mensaje aleatorio
                serviceScope.launch {
                    val message = aiCore.processMessage("Hola", "tap")
                    showSpeechBubble(message)
                }
            }
        }
    }
    
    /**
     * Aplica inercia después de arrastrar
     */
    private fun applyInertia() {
        serviceScope.launch {
            val (velocityX, velocityY) = physicsEngine.getVelocity()
            var currentVelocityX = velocityX
            var currentVelocityY = velocityY
            
            // Aplicar inercia durante un tiempo
            val startTime = System.currentTimeMillis()
            val duration = 500L // ms
            
            while (System.currentTimeMillis() - startTime < duration) {
                // Actualizar posición
                params.x += currentVelocityX.toInt() / 10
                params.y += currentVelocityY.toInt() / 10
                
                // Aplicar fricción
                currentVelocityX *= 0.9f
                currentVelocityY *= 0.9f
                
                // Actualizar vista
                windowManager.updateViewLayout(avatarView, params)
                
                // Esperar
                delay(16) // ~60fps
            }
        }
    }
    
    /**
     * Muestra el avatar
     */
    private fun showAvatar() {
        if (!isVisible && ::avatarView.isInitialized) {
            try {
                windowManager.addView(avatarView, params)
                isVisible = true
            } catch (e: Exception) {
                Log.e(TAG, "Error al mostrar avatar", e)
            }
        }
    }
    
    /**
     * Oculta el avatar
     */
    private fun hideAvatar() {
        if (isVisible && ::avatarView.isInitialized) {
            try {
                windowManager.removeView(avatarView)
                isVisible = false
            } catch (e: Exception) {
                Log.e(TAG, "Error al ocultar avatar", e)
            }
        }
    }
    
    /**
     * Muestra una burbuja de diálogo con texto
     */
    private fun showSpeechBubble(text: String) {
        if (::speechBubbleView.isInitialized) {
            speechBubbleView.text = text
            speechBubbleView.visibility = View.VISIBLE
            
            // Iniciar animación de habla
            isSpeaking = true
            animationController.playSpeakingAnimation()
            
            // Ocultar burbuja después de un tiempo
            serviceScope.launch {
                // Calcular duración basada en la longitud del texto
                val duration = 2000L + text.length * 50L
                
                delay(duration)
                
                // Ocultar burbuja
                speechBubbleView.visibility = View.GONE
                
                // Detener animación de habla
                isSpeaking = false
                animationController.playIdleAnimation()
            }
        }
    }
    
    /**
     * Establece el estado de ánimo del avatar
     */
    private fun setMood(mood: String) {
        if (currentMood != mood) {
            currentMood = mood
            
            // Actualizar animación según el estado de ánimo
            when (mood) {
                "happy" -> animationController.playHappyAnimation()
                "sad" -> animationController.playSadAnimation()
                "angry" -> animationController.playAngryAnimation()
                "relaxed" -> animationController.playRelaxedAnimation()
                "sleepy" -> animationController.playSleepyAnimation()
                else -> animationController.playIdleAnimation()
            }
        }
    }
    
    /**
     * Inicia el bucle de animación
     */
    private fun startAnimationLoop() {
        serviceScope.launch {
            while (true) {
                // Obtener estado de ánimo actual
                val mood = aiCore.getCurrentMood()
                
                // Actualizar estado de ánimo si ha cambiado
                if (mood != currentMood) {
                    setMood(mood)
                }
                
                // Esperar
                delay(1000)
            }
        }
    }
    
    companion object {
        // Acciones
        const val ACTION_SHOW_AVATAR = "com.ritsuai.launcher.action.SHOW_AVATAR"
        const val ACTION_HIDE_AVATAR = "com.ritsuai.launcher.action.HIDE_AVATAR"
        const val ACTION_SPEAK = "com.ritsuai.launcher.action.SPEAK"
        const val ACTION_SET_MOOD = "com.ritsuai.launcher.action.SET_MOOD"
        
        // Extras
        const val EXTRA_SPEECH_TEXT = "com.ritsuai.launcher.extra.SPEECH_TEXT"
        const val EXTRA_MOOD = "com.ritsuai.launcher.extra.MOOD"
    }
}

