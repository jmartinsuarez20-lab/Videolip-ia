package com.ritsuai.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.ritsuai.R
import com.ritsuai.RitsuAICore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Servicio que muestra a Ritsu como un overlay flotante en la pantalla
 */
class RitsuOverlayService : Service() {

    private val TAG = "RitsuOverlayService"
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var ritsuAICore: RitsuAICore
    
    // Corrutinas
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var animationJob: Job? = null
    
    // ID del canal de notificación
    private val CHANNEL_ID = "RitsuOverlayChannel"
    private val NOTIFICATION_ID = 1001
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar el núcleo de Ritsu
        ritsuAICore = RitsuAICore(applicationContext)
        
        // Crear canal de notificación para servicio en primer plano
        createNotificationChannel()
        
        // Iniciar servicio en primer plano
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Inicializar el administrador de ventanas
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // Crear y mostrar la vista de overlay
        setupOverlayView()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ritsu Overlay Service"
            val descriptionText = "Muestra a Ritsu como un overlay flotante"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ritsu está activa")
            .setContentText("Ritsu está ejecutándose en segundo plano")
            .setSmallIcon(R.drawable.ic_check_circle)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun setupOverlayView() {
        // Inflar la vista de overlay
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_ritsu, null)
        
        // Configurar parámetros de la ventana
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 300
        }
        
        // Configurar arrastre de la vista
        setupDragToMove(overlayView, params)
        
        // Configurar interacciones con Ritsu
        setupRitsuInteractions(overlayView)
        
        // Añadir la vista al administrador de ventanas
        windowManager.addView(overlayView, params)
        
        // Iniciar animación de idle
        startIdleAnimation()
    }
    
    private fun setupDragToMove(view: View, params: WindowManager.LayoutParams) {
        var initialX: Int = 0
        var initialY: Int = 0
        var initialTouchX: Float = 0f
        var initialTouchY: Float = 0f
        
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupRitsuInteractions(view: View) {
        val ritsuAvatar = view.findViewById<ImageView>(R.id.ivRitsuAvatar)
        val ritsuStatus = view.findViewById<TextView>(R.id.tvRitsuStatus)
        
        // Configurar clic en el avatar
        ritsuAvatar.setOnClickListener {
            // Mostrar diálogo o expandir interfaz
            ritsuStatus.text = "¿En qué puedo ayudarte?"
            
            // Animar respuesta
            animateResponse()
        }
        
        // Configurar clic largo en el avatar
        ritsuAvatar.setOnLongClickListener {
            // Mostrar menú de opciones
            ritsuStatus.text = "Menú de opciones"
            true
        }
    }
    
    private fun startIdleAnimation() {
        animationJob?.cancel()
        animationJob = serviceScope.launch {
            val ritsuAvatar = overlayView.findViewById<ImageView>(R.id.ivRitsuAvatar)
            
            while (true) {
                // Animación de respiración suave
                for (i in 0..10) {
                    val scale = 0.95f + (i * 0.005f)
                    ritsuAvatar.scaleX = scale
                    ritsuAvatar.scaleY = scale
                    delay(100)
                }
                
                for (i in 10 downTo 0) {
                    val scale = 0.95f + (i * 0.005f)
                    ritsuAvatar.scaleX = scale
                    ritsuAvatar.scaleY = scale
                    delay(100)
                }
                
                // Parpadeo ocasional
                if (Math.random() > 0.7) {
                    ritsuAvatar.alpha = 0.7f
                    delay(100)
                    ritsuAvatar.alpha = 1.0f
                }
                
                delay(500)
            }
        }
    }
    
    private fun animateResponse() {
        animationJob?.cancel()
        animationJob = serviceScope.launch {
            val ritsuAvatar = overlayView.findViewById<ImageView>(R.id.ivRitsuAvatar)
            
            // Animación de "pensando"
            for (i in 0 until 3) {
                ritsuAvatar.rotation = -5f
                delay(150)
                ritsuAvatar.rotation = 5f
                delay(150)
            }
            
            ritsuAvatar.rotation = 0f
            
            // Volver a la animación de idle
            startIdleAnimation()
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Cancelar todas las corrutinas
        animationJob?.cancel()
        
        // Eliminar la vista de overlay
        if (::overlayView.isInitialized && ::windowManager.isInitialized) {
            windowManager.removeView(overlayView)
        }
        
        // Liberar recursos del núcleo de Ritsu
        if (::ritsuAICore.isInitialized) {
            ritsuAICore.destroy()
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

