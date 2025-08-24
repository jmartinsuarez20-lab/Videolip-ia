package com.ritsuai.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.graphics.Bitmap
import com.ritsuai.*
import kotlinx.coroutines.*

/**
 * Servicio que mantiene el avatar de Ritsu flotando sobre todas las aplicaciones
 * Maneja las animaciones, interacciones táctiles y actualizaciones visuales
 */
class RitsuOverlayService : Service() {
    
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var avatarImageView: ImageView
    private lateinit var ritsuCore: RitsuAICore
    private lateinit var avatarManager: RitsuAvatarManager
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isMinimized = false
    private var currentAnimation: AnimatorSet? = null
    
    // Posición del avatar
    private var avatarX = 100f
    private var avatarY = 100f
    
    companion object {
        const val CHANNEL_ID = "RitsuOverlayChannel"
        const val NOTIFICATION_ID = 1
        var instance: RitsuOverlayService? = null
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        ritsuCore = RitsuAICore(this)
        avatarManager = RitsuAvatarManager(this)
        
        createNotificationChannel()
        createOverlayView()
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Iniciar animaciones idle
        startIdleAnimations()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Ritsu AI Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene a Ritsu visible en pantalla"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Ritsu AI")
            .setContentText("Ritsu está activa y lista para ayudarte")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }
    
    private fun createOverlayView() {
        overlayView = LayoutInflater.from(this).inflate(
            android.R.layout.simple_list_item_1, // Placeholder, usaremos custom view
            null
        )
        
        // Crear vista personalizada para Ritsu
        val avatarLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(200, 400)
        }
        
        avatarImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 400)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        
        avatarLayout.addView(avatarImageView)
        overlayView = avatarLayout
        
        // Configurar parámetros de la ventana overlay
        val layoutParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            )
        }
        
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = avatarX.toInt()
        layoutParams.y = avatarY.toInt()
        
        // Configurar interacciones táctiles
        setupTouchListener(layoutParams)
        
        // Agregar vista al window manager
        windowManager.addView(overlayView, layoutParams)
        
        // Actualizar avatar inicial
        updateAvatarAppearance()
    }
    
    private fun setupTouchListener(layoutParams: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var lastTapTime = 0L
        
        overlayView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    
                    // Detectar doble tap
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < 300) {
                        handleDoubleTap()
                    }
                    lastTapTime = currentTime
                    
                    true
                }
                
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    
                    windowManager.updateViewLayout(overlayView, layoutParams)
                    true
                }
                
                MotionEvent.ACTION_UP -> {
                    // Detectar tap simple
                    val deltaX = Math.abs(event.rawX - initialTouchX)
                    val deltaY = Math.abs(event.rawY - initialTouchY)
                    
                    if (deltaX < 10 && deltaY < 10) {
                        handleSingleTap()
                    }
                    
                    // Guardar nueva posición
                    avatarX = layoutParams.x.toFloat()
                    avatarY = layoutParams.y.toFloat()
                    
                    true
                }
                
                else -> false
            }
        }
    }
    
    private fun handleSingleTap() {
        scope.launch {
            val response = ritsuCore.processInput("Usuario tocó el avatar", "user")
            
            // Mostrar reacción de Ritsu
            showReaction(response.emotion, response.action)
            
            if (response.voiceEnabled) {
                ritsuCore.speak(response.text)
            }
        }
    }
    
    private fun handleDoubleTap() {
        if (isMinimized) {
            maximizeAvatar()
        } else {
            minimizeAvatar()
        }
    }
    
    private fun minimizeAvatar() {
        isMinimized = true
        
        val scaleX = ObjectAnimator.ofFloat(overlayView, "scaleX", 1f, 0.3f)
        val scaleY = ObjectAnimator.ofFloat(overlayView, "scaleY", 1f, 0.3f)
        val alpha = ObjectAnimator.ofFloat(overlayView, "alpha", 1f, 0.7f)
        
        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 300
            start()
        }
    }
    
    private fun maximizeAvatar() {
        isMinimized = false
        
        val scaleX = ObjectAnimator.ofFloat(overlayView, "scaleX", 0.3f, 1f)
        val scaleY = ObjectAnimator.ofFloat(overlayView, "scaleY", 0.3f, 1f)
        val alpha = ObjectAnimator.ofFloat(overlayView, "alpha", 0.7f, 1f)
        
        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 300
            start()
        }
    }
    
    private fun showReaction(emotion: RitsuEmotion, action: RitsuAction) {
        // Actualizar expresión del avatar
        avatarManager.changeExpression(emotion)
        updateAvatarAppearance()
        
        // Animación según la acción
        when (action) {
            RitsuAction.BLUSH -> animateBlush()
            RitsuAction.POSE -> animatePose()
            RitsuAction.THINKING -> animateThinking()
            RitsuAction.WORKING -> animateWorking()
            else -> animateIdle()
        }
    }
    
    private fun animateBlush() {
        val shake = ObjectAnimator.ofFloat(overlayView, "translationX", 0f, -10f, 10f, 0f)
        shake.duration = 200
        shake.repeatCount = 2
        shake.start()
    }
    
    private fun animatePose() {
        val bounce = ObjectAnimator.ofFloat(overlayView, "translationY", 0f, -20f, 0f)
        bounce.duration = 400
        bounce.start()
    }
    
    private fun animateThinking() {
        val rotation = ObjectAnimator.ofFloat(overlayView, "rotation", 0f, -5f, 5f, 0f)
        rotation.duration = 1000
        rotation.repeatCount = 3
        rotation.start()
    }
    
    private fun animateWorking() {
        val pulse = ObjectAnimator.ofFloat(overlayView, "scaleX", 1f, 1.1f, 1f)
        val pulseY = ObjectAnimator.ofFloat(overlayView, "scaleY", 1f, 1.1f, 1f)
        
        AnimatorSet().apply {
            playTogether(pulse, pulseY)
            duration = 500
            repeatCount = 2
            start()
        }
    }
    
    private fun animateIdle() {
        // Animación sutil de respiración
        val breathe = ObjectAnimator.ofFloat(overlayView, "scaleY", 1f, 1.02f, 1f)
        breathe.duration = 2000
        breathe.repeatCount = ObjectAnimator.INFINITE
        breathe.start()
    }
    
    private fun startIdleAnimations() {
        scope.launch {
            while (true) {
                delay(5000) // Cada 5 segundos
                
                if (!isMinimized && currentAnimation?.isRunning != true) {
                    // Animación idle aleatoria
                    when ((0..3).random()) {
                        0 -> animateIdle()
                        1 -> animateBlink()
                        2 -> animateLookAround()
                        3 -> animateStretch()
                    }
                }
            }
        }
    }
    
    private fun animateBlink() {
        val blink = ObjectAnimator.ofFloat(overlayView, "scaleY", 1f, 0.9f, 1f)
        blink.duration = 150
        blink.start()
    }
    
    private fun animateLookAround() {
        val look = ObjectAnimator.ofFloat(overlayView, "rotation", 0f, 10f, -10f, 0f)
        look.duration = 2000
        look.start()
    }
    
    private fun animateStretch() {
        val stretch = ObjectAnimator.ofFloat(overlayView, "scaleY", 1f, 1.1f, 1f)
        stretch.duration = 1000
        stretch.start()
    }
    
    /**
     * Actualiza la apariencia visual del avatar
     */
    fun updateAvatarAppearance() {
        scope.launch(Dispatchers.IO) {
            val avatarBitmap = avatarManager.generateAvatarBitmap()
            
            withContext(Dispatchers.Main) {
                avatarImageView.setImageBitmap(avatarBitmap)
            }
        }
    }
    
    /**
     * Cambia la ropa del avatar
     */
    fun changeClothing(outfit: ClothingOutfit) {
        avatarManager.changeClothing(outfit)
        updateAvatarAppearance()
        
        // Animación de cambio de ropa
        val fadeOut = ObjectAnimator.ofFloat(overlayView, "alpha", 1f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(overlayView, "alpha", 0f, 1f)
        
        fadeOut.duration = 200
        fadeIn.duration = 200
        
        fadeOut.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                updateAvatarAppearance()
                fadeIn.start()
            }
        })
        
        fadeOut.start()
    }
    
    /**
     * Hace que Ritsu se mueva a una posición específica
     */
    fun moveToPosition(x: Float, y: Float) {
        val layoutParams = overlayView.layoutParams as WindowManager.LayoutParams
        
        val moveX = ObjectAnimator.ofInt(layoutParams.x, x.toInt())
        val moveY = ObjectAnimator.ofInt(layoutParams.y, y.toInt())
        
        moveX.addUpdateListener { animation ->
            layoutParams.x = animation.animatedValue as Int
            windowManager.updateViewLayout(overlayView, layoutParams)
        }
        
        moveY.addUpdateListener { animation ->
            layoutParams.y = animation.animatedValue as Int
            windowManager.updateViewLayout(overlayView, layoutParams)
        }
        
        AnimatorSet().apply {
            playTogether(moveX, moveY)
            duration = 1000
            start()
        }
        
        avatarX = x
        avatarY = y
    }
    
    /**
     * Hace que Ritsu "hable" con animación de boca
     */
    fun speakWithAnimation(text: String) {
        scope.launch {
            // Cambiar expresión a hablando
            avatarManager.changeExpression(RitsuEmotion.NORMAL)
            updateAvatarAppearance()
            
            // Animación de habla
            val talkAnimation = ObjectAnimator.ofFloat(overlayView, "scaleX", 1f, 1.05f, 1f)
            talkAnimation.duration = 200
            talkAnimation.repeatCount = text.length / 10 // Aproximado
            talkAnimation.start()
            
            // Hablar con TTS
            ritsuCore.speak(text)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        
        try {
            windowManager.removeView(overlayView)
        } catch (e: Exception) {
            // Vista ya removida
        }
        
        currentAnimation?.cancel()
        scope.cancel()
        ritsuCore.cleanup()
    }
}

