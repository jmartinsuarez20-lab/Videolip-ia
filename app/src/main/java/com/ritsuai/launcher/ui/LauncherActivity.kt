package com.ritsuai.launcher.ui

import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.ritsuai.launcher.R
import com.ritsuai.launcher.adapters.AppGridAdapter
import com.ritsuai.launcher.adapters.WidgetAdapter
import com.ritsuai.launcher.databinding.ActivityLauncherBinding
import com.ritsuai.launcher.models.AppInfo
import com.ritsuai.launcher.models.WidgetInfo
import com.ritsuai.launcher.viewmodels.LauncherViewModel
import com.ritsuai.launcher.ai.RitsuAICore
import com.ritsuai.launcher.avatar3d.RitsuAvatarService
import com.ritsuai.launcher.speech.TextToSpeechManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Actividad principal del launcher personalizado de Ritsu.
 * Reemplaza el launcher por defecto de Android.
 */
class LauncherActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLauncherBinding
    private lateinit var viewModel: LauncherViewModel
    private lateinit var appAdapter: AppGridAdapter
    private lateinit var widgetAdapter: WidgetAdapter
    
    // Componentes de Ritsu
    private lateinit var aiCore: RitsuAICore
    private lateinit var ttsManager: TextToSpeechManager
    
    // Scope de corrutinas
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Estado de ánimo actual de Ritsu (para fondos dinámicos)
    private var currentMood = "neutral"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar binding
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this)[LauncherViewModel::class.java]
        
        // Inicializar componentes de Ritsu
        aiCore = RitsuAICore.getInstance(applicationContext)
        ttsManager = TextToSpeechManager.getInstance(applicationContext)
        
        // Configurar RecyclerView para aplicaciones
        setupAppGrid()
        
        // Configurar RecyclerView para widgets
        setupWidgets()
        
        // Configurar fondo dinámico
        setupDynamicWallpaper()
        
        // Configurar botón de voz
        binding.voiceButton.setOnClickListener {
            startVoiceCommand()
        }
        
        // Configurar botón de menú
        binding.menuButton.setOnClickListener {
            showAppDrawer()
        }
        
        // Observar cambios en la lista de aplicaciones
        viewModel.appList.observe(this) { apps ->
            appAdapter.submitList(apps)
        }
        
        // Observar cambios en la lista de widgets
        viewModel.widgetList.observe(this) { widgets ->
            widgetAdapter.submitList(widgets)
        }
        
        // Observar cambios en el estado de ánimo de Ritsu
        viewModel.ritsuMood.observe(this) { mood ->
            if (mood != currentMood) {
                currentMood = mood
                updateWallpaperForMood(mood)
            }
        }
        
        // Cargar aplicaciones y widgets
        viewModel.loadInstalledApps(this)
        viewModel.loadWidgets(this)
        
        // Iniciar servicio de avatar 3D
        startRitsuAvatarService()
        
        // Saludar al usuario
        greetUser()
    }
    
    override fun onResume() {
        super.onResume()
        
        // Actualizar lista de aplicaciones
        viewModel.loadInstalledApps(this)
        
        // Verificar notificaciones pendientes
        checkPendingNotifications()
    }
    
    /**
     * Configura el grid de aplicaciones
     */
    private fun setupAppGrid() {
        appAdapter = AppGridAdapter { app ->
            launchApp(app)
        }
        
        binding.appsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@LauncherActivity, 4)
            adapter = appAdapter
        }
    }
    
    /**
     * Configura los widgets
     */
    private fun setupWidgets() {
        widgetAdapter = WidgetAdapter { widget ->
            interactWithWidget(widget)
        }
        
        binding.widgetsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@LauncherActivity, 2)
            adapter = widgetAdapter
        }
    }
    
    /**
     * Configura el fondo dinámico
     */
    private fun setupDynamicWallpaper() {
        // Establecer fondo inicial según la hora del día
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val initialMood = when {
            hour < 6 -> "sleepy"
            hour < 12 -> "happy"
            hour < 18 -> "neutral"
            else -> "relaxed"
        }
        
        updateWallpaperForMood(initialMood)
    }
    
    /**
     * Actualiza el fondo según el estado de ánimo de Ritsu
     */
    private fun updateWallpaperForMood(mood: String) {
        val wallpaperDrawable: Drawable? = when (mood) {
            "happy" -> getDrawable(R.drawable.wallpaper_happy)
            "sad" -> getDrawable(R.drawable.wallpaper_sad)
            "angry" -> getDrawable(R.drawable.wallpaper_angry)
            "relaxed" -> getDrawable(R.drawable.wallpaper_relaxed)
            "sleepy" -> getDrawable(R.drawable.wallpaper_sleepy)
            else -> getDrawable(R.drawable.wallpaper_neutral)
        }
        
        // Establecer fondo
        binding.backgroundImage.setImageDrawable(wallpaperDrawable)
    }
    
    /**
     * Inicia la actividad de comandos de voz
     */
    private fun startVoiceCommand() {
        val intent = Intent(this, VoiceCommandActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * Muestra el cajón de aplicaciones completo
     */
    private fun showAppDrawer() {
        val intent = Intent(this, AppDrawerActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * Lanza una aplicación
     */
    private fun launchApp(app: AppInfo) {
        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, getString(R.string.error_app_not_found), Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Interactúa con un widget
     */
    private fun interactWithWidget(widget: WidgetInfo) {
        // Implementación específica para cada tipo de widget
        when (widget.type) {
            "weather" -> {
                // Abrir widget de clima
                Toast.makeText(this, "Abriendo widget de clima", Toast.LENGTH_SHORT).show()
            }
            "calendar" -> {
                // Abrir widget de calendario
                val calendarIntent = packageManager.getLaunchIntentForPackage("com.google.android.calendar")
                if (calendarIntent != null) {
                    startActivity(calendarIntent)
                }
            }
            "music" -> {
                // Abrir widget de música
                val musicIntent = Intent(this, MusicPlayerActivity::class.java)
                startActivity(musicIntent)
            }
            else -> {
                // Widget genérico
                Toast.makeText(this, "Interactuando con widget: ${widget.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Inicia el servicio de avatar 3D
     */
    private fun startRitsuAvatarService() {
        val intent = Intent(this, RitsuAvatarService::class.java)
        startService(intent)
    }
    
    /**
     * Saluda al usuario según la hora del día
     */
    private fun greetUser() {
        activityScope.launch {
            // Obtener nombre del usuario
            val userName = viewModel.getUserName()
            
            // Obtener saludo según la hora
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val greeting = when {
                hour < 12 -> getString(R.string.greeting_morning, userName)
                hour < 18 -> getString(R.string.greeting_afternoon, userName)
                else -> getString(R.string.greeting_evening, userName)
            }
            
            // Procesar con IA para personalizar
            val personalizedGreeting = withContext(Dispatchers.Default) {
                aiCore.processMessage(greeting, "greeting")
            }
            
            // Hablar saludo
            ttsManager.speak(personalizedGreeting)
        }
    }
    
    /**
     * Verifica notificaciones pendientes
     */
    private fun checkPendingNotifications() {
        activityScope.launch {
            val pendingNotifications = viewModel.getPendingNotifications()
            
            if (pendingNotifications.isNotEmpty()) {
                // Hay notificaciones pendientes
                val notificationSummary = if (pendingNotifications.size == 1) {
                    "Tienes 1 notificación pendiente"
                } else {
                    "Tienes ${pendingNotifications.size} notificaciones pendientes"
                }
                
                // Mostrar resumen
                binding.notificationBadge.visibility = View.VISIBLE
                binding.notificationBadge.text = pendingNotifications.size.toString()
                
                // Informar al usuario
                val response = withContext(Dispatchers.Default) {
                    aiCore.processMessage(notificationSummary, "notification")
                }
                
                // Hablar respuesta
                ttsManager.speak(response)
            } else {
                // No hay notificaciones
                binding.notificationBadge.visibility = View.GONE
            }
        }
    }
}

