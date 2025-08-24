package com.ritsuai.launcher.avatar3d

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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.ritsuai.launcher.R
import com.ritsuai.launcher.databinding.OverlayRitsu3dBinding
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Servicio para mostrar el avatar 3D de Ritsu como overlay sobre otras aplicaciones.
 */
class RitsuAvatarService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var binding: OverlayRitsu3dBinding
    private lateinit var sceneView: SceneView
    private var modelNode: ModelNode? = null
    
    // Parámetros de la ventana de overlay
    private lateinit var layoutParams: WindowManager.LayoutParams
    
    // Gestor de animaciones
    private lateinit var animationController: RitsuAnimationController
    
    // Gestor de física
    private lateinit var physicsEngine: RitsuPhysicsEngine
    
    // Scope de corrutinas
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // ID del canal de notificación
    private val CHANNEL_ID = "RitsuAvatarServiceChannel"
    private val NOTIFICATION_ID = 1
    
    override fun onCreate() {
        super.onCreate()
        
        // Crear canal de notificación para servicio en primer plano
        createNotificationChannel()
        
        // Iniciar servicio en primer plano
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Inicializar el gestor de ventanas
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // Inicializar la vista de overlay
        initOverlayView()
        
        // Inicializar el gestor de animaciones
        animationController = RitsuAnimationController()
        
        // Inicializar el gestor de física
        physicsEngine = RitsuPhysicsEngine()
        
        // Cargar el modelo 3D
        loadAvatarModel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Si el servicio se reinicia, mantener ejecutándose hasta que se detenga explícitamente
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Eliminar la vista de overlay
        if (::overlayView.isInitialized && overlayView.isAttachedToWindow) {
            windowManager.removeView(overlayView)
        }
        
        // Liberar recursos
        if (::sceneView.isInitialized) {
            sceneView.destroy()
        }
    }
    
    /**
     * Inicializa la vista de overlay
     */
    private fun initOverlayView() {
        // Inflar la vista
        binding = OverlayRitsu3dBinding.inflate(LayoutInflater.from(this))
        overlayView = binding.root
        
        // Configurar SceneView
        sceneView = binding.sceneView
        
        // Configurar parámetros de la ventana
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            else 
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        
        // Posición inicial
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 100
        layoutParams.y = 100
        
        // Configurar eventos de arrastre
        setupDragListener()
        
        // Configurar botones de control
        setupControlButtons()
        
        // Añadir la vista al gestor de ventanas
        windowManager.addView(overlayView, layoutParams)
    }
    
    /**
     * Configura el listener para arrastrar el avatar
     */
    private fun setupDragListener() {
        var initialX: Int = 0
        var initialY: Int = 0
        var initialTouchX: Float = 0f
        var initialTouchY: Float = 0f
        
        overlayView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Guardar posición inicial
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Calcular nueva posición
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    
                    // Actualizar posición
                    windowManager.updateViewLayout(overlayView, layoutParams)
                    true
                }
                else -> false
            }
        }
    }
    
    /**
     * Configura los botones de control del avatar
     */
    private fun setupControlButtons() {
        // Botón de configuración
        binding.avatarSettingsButton.setOnClickListener {
            // Abrir configuración del avatar
            Toast.makeText(this, "Configuración del avatar", Toast.LENGTH_SHORT).show()
        }
        
        // Botón para ocultar
        binding.avatarHideButton.setOnClickListener {
            // Ocultar temporalmente el avatar
            if (overlayView.visibility == View.VISIBLE) {
                overlayView.visibility = View.GONE
            } else {
                overlayView.visibility = View.VISIBLE
            }
        }
    }
    
    /**
     * Carga el modelo 3D del avatar
     */
    private fun loadAvatarModel() {
        serviceScope.launch {
            try {
                // Crear nodo para el modelo
                modelNode = ModelNode().apply {
                    // Posición inicial
                    position = Position(0.0f, 0.0f, -2.0f)
                    // Escala
                    scale = io.github.sceneview.math.Scale(0.5f)
                }
                
                // Cargar modelo (en un entorno real, cargaríamos un archivo .glb)
                // Para este ejemplo, usamos un modelo de prueba
                modelNode?.loadModelAsync(
                    context = applicationContext,
                    glbFileLocation = "models/ritsu_base.glb",
                    autoAnimate = true,
                    centerOrigin = io.github.sceneview.math.Position(x = 0.0f, y = 0.0f, z = 0.0f)
                )
                
                // Añadir el nodo a la escena
                sceneView.addChild(modelNode!!)
                
                // Iniciar animación de idle
                animationController.playAnimation(modelNode, "idle")
                
            } catch (e: Exception) {
                e.printStackTrace()
                // En caso de error, mostrar un modelo de respaldo o un mensaje
                Toast.makeText(
                    applicationContext,
                    "Error al cargar el modelo 3D",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    /**
     * Muestra un mensaje en la burbuja de diálogo
     */
    fun showSpeechBubble(message: String, showActions: Boolean = false) {
        binding.speechText.text = message
        binding.speechBubble.visibility = View.VISIBLE
        
        if (showActions) {
            binding.speechActions.visibility = View.VISIBLE
        } else {
            binding.speechActions.visibility = View.GONE
        }
        
        // Ocultar después de un tiempo
        serviceScope.launch {
            kotlinx.coroutines.delay(5000)
            binding.speechBubble.visibility = View.GONE
        }
    }
    
    /**
     * Crea el canal de notificación para Android 8.0+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ritsu Avatar Service"
            val descriptionText = "Servicio para mostrar el avatar de Ritsu"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Crea la notificación para el servicio en primer plano
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ritsu está activa")
            .setContentText("Ritsu está ejecutándose en segundo plano")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    companion object {
        // Identificador único para la sesión actual
        val SESSION_ID = UUID.randomUUID().toString()
    }
}

