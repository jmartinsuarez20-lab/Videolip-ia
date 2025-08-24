package com.ritsuai.services

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
import com.ritsuai.R
import com.ritsuai.RitsuAICore
import com.ritsuai.evolution.RitsuAdaptiveAvatar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Servicio para mostrar el avatar flotante de Ritsu
 */
class RitsuOverlayService : Service() {

    private val TAG = "RitsuOverlayService"
    
    // Administrador de ventanas
    private lateinit var windowManager: WindowManager
    
    // Vista del overlay
    private lateinit var overlayView: View
    
    // Parámetros de la ventana
    private lateinit var params: WindowManager.LayoutParams
    
    // Núcleo de Ritsu
    private lateinit var ritsuAICore: RitsuAICore
    
    // Avatar adaptativo
    private lateinit var adaptiveAvatar: RitsuAdaptiveAvatar
    
    // Corrutinas
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var updateJob: Job? = null
    
    // Estado
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio de overlay creado")
        
        // Inicializar el administrador de ventanas
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // Inicializar el núcleo de Ritsu
        ritsuAICore = RitsuAICore(applicationContext)
        
        // Crear la vista del overlay
        createOverlayView()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Servicio de overlay iniciado")
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    /**
     * Crea la vista del overlay
     */
    private fun createOverlayView() {
        // Inflar la vista
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_ritsu, null)
        
        // Configurar parámetros de la ventana
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }
        
        // Configurar eventos táctiles
        setupTouchListener()
        
        // Añadir la vista al administrador de ventanas
        try {
            windowManager.addView(overlayView, params)
            
            // Iniciar actualización periódica
            startPeriodicUpdate()
        } catch (e: Exception) {
            Log.e(TAG, "Error al añadir vista de overlay: ${e.message}")
        }
    }
    
    /**
     * Configura el listener de eventos táctiles
     */
    private fun setupTouchListener() {
        overlayView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Guardar posición inicial
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Calcular nueva posición
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    
                    // Actualizar posición
                    windowManager.updateViewLayout(overlayView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Si fue un toque corto (no un arrastre)
                    val isTap = Math.abs(event.rawX - initialTouchX) < 10 && 
                               Math.abs(event.rawY - initialTouchY) < 10
                    
                    if (isTap) {
                        handleTap()
                    }
                    true
                }
                else -> false
            }
        }
    }
    
    /**
     * Maneja un toque en el avatar
     */
    private fun handleTap() {
        // Aquí iría la lógica para mostrar el diálogo de chat
        Log.d(TAG, "Avatar tocado")
        
        // Cambiar el estado del avatar
        val tvStatus = overlayView.findViewById<TextView>(R.id.tvRitsuStatus)
        tvStatus.text = "¡Hola!"
        
        // Programar el cambio de vuelta después de un tiempo
        serviceScope.launch {
            kotlinx.coroutines.delay(2000)
            withContext(Dispatchers.Main) {
                tvStatus.text = "Ritsu"
            }
        }
    }
    
    /**
     * Inicia la actualización periódica del avatar
     */
    private fun startPeriodicUpdate() {
        updateJob = serviceScope.launch {
            try {
                while (true) {
                    // Actualizar cada 5 segundos
                    kotlinx.coroutines.delay(5000)
                    
                    // Actualizar avatar en el hilo principal
                    withContext(Dispatchers.Main) {
                        updateAvatarDisplay()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en actualización periódica: ${e.message}")
            }
        }
    }
    
    /**
     * Actualiza la visualización del avatar
     */
    private fun updateAvatarDisplay() {
        try {
            val ivAvatar = overlayView.findViewById<ImageView>(R.id.ivRitsuAvatar)
            val tvStatus = overlayView.findViewById<TextView>(R.id.tvRitsuStatus)
            
            // En una implementación real, aquí se cargaría la imagen del avatar actual
            // y se actualizaría el estado según la actividad de Ritsu
            
            // Por ahora, solo actualizamos el texto de estado con la hora
            val currentTime = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            
            tvStatus.text = "Activa: $currentTime"
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar avatar: ${e.message}")
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Servicio de overlay destruido")
        
        // Cancelar trabajos en segundo plano
        updateJob?.cancel()
        
        // Eliminar la vista del administrador de ventanas
        try {
            windowManager.removeView(overlayView)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar vista de overlay: ${e.message}")
        }
        
        // Liberar recursos
        ritsuAICore.destroy()
    }
}

