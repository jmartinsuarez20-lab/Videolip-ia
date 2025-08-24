package com.ritsuai.launcher.viewmodels

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.service.notification.StatusBarNotification
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritsuai.launcher.models.AppInfo
import com.ritsuai.launcher.models.NotificationInfo
import com.ritsuai.launcher.models.WidgetInfo
import com.ritsuai.launcher.services.RitsuNotificationListenerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * ViewModel para la actividad principal del launcher.
 * Gestiona la lista de aplicaciones, widgets y el estado de Ritsu.
 */
class LauncherViewModel : ViewModel() {

    // LiveData para la lista de aplicaciones
    private val _appList = MutableLiveData<List<AppInfo>>()
    val appList: LiveData<List<AppInfo>> = _appList
    
    // LiveData para la lista de widgets
    private val _widgetList = MutableLiveData<List<WidgetInfo>>()
    val widgetList: LiveData<List<WidgetInfo>> = _widgetList
    
    // LiveData para el estado de ánimo de Ritsu
    private val _ritsuMood = MutableLiveData<String>()
    val ritsuMood: LiveData<String> = _ritsuMood
    
    // Lista completa de aplicaciones (para filtrado)
    private var fullAppList: List<AppInfo> = emptyList()
    
    // Preferencias del usuario
    private var userName: String = "Usuario"
    
    init {
        // Establecer estado de ánimo inicial según la hora del día
        updateMoodBasedOnTime()
    }
    
    /**
     * Carga la lista de aplicaciones instaladas en el dispositivo
     */
    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                val packageManager = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null)
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                
                val resolveInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(mainIntent, 0)
                
                // Filtrar aplicaciones favoritas (para mostrar en la pantalla principal)
                val favoritePackages = getFavoritePackages(context)
                
                resolveInfoList
                    .filter { resolveInfo ->
                        val packageName = resolveInfo.activityInfo.packageName
                        packageName in favoritePackages
                    }
                    .map { resolveInfo ->
                        val appName = resolveInfo.loadLabel(packageManager).toString()
                        val packageName = resolveInfo.activityInfo.packageName
                        val icon = resolveInfo.loadIcon(packageManager)
                        
                        AppInfo(appName, packageName, icon)
                    }
                    .sortedBy { it.appName }
            }
            
            fullAppList = apps
            _appList.value = apps
        }
    }
    
    /**
     * Carga los widgets disponibles
     */
    fun loadWidgets(context: Context) {
        viewModelScope.launch {
            val widgets = withContext(Dispatchers.IO) {
                // En una implementación real, se cargarían los widgets del sistema
                // Para este ejemplo, creamos widgets de ejemplo
                listOf(
                    WidgetInfo(
                        id = 1,
                        name = "Clima",
                        type = "weather",
                        icon = null
                    ),
                    WidgetInfo(
                        id = 2,
                        name = "Calendario",
                        type = "calendar",
                        icon = null
                    ),
                    WidgetInfo(
                        id = 3,
                        name = "Música",
                        type = "music",
                        icon = null
                    ),
                    WidgetInfo(
                        id = 4,
                        name = "Notas",
                        type = "notes",
                        icon = null
                    )
                )
            }
            
            _widgetList.value = widgets
        }
    }
    
    /**
     * Obtiene la lista de paquetes favoritos
     */
    private fun getFavoritePackages(context: Context): List<String> {
        // En una implementación real, se cargarían de las preferencias del usuario
        // Para este ejemplo, devolvemos una lista predefinida
        return listOf(
            "com.whatsapp",
            "com.google.android.youtube",
            "com.google.android.gm",
            "com.google.android.apps.maps",
            "com.google.android.apps.photos",
            "com.spotify.music",
            "com.instagram.android",
            "com.google.android.calendar"
        )
    }
    
    /**
     * Actualiza el estado de ánimo de Ritsu según la hora del día
     */
    private fun updateMoodBasedOnTime() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val mood = when {
            hour < 6 -> "sleepy"
            hour < 12 -> "happy"
            hour < 18 -> "neutral"
            hour < 22 -> "relaxed"
            else -> "sleepy"
        }
        
        _ritsuMood.value = mood
    }
    
    /**
     * Establece el estado de ánimo de Ritsu
     */
    fun setRitsuMood(mood: String) {
        _ritsuMood.value = mood
    }
    
    /**
     * Obtiene el nombre del usuario
     */
    suspend fun getUserName(): String {
        // En una implementación real, se cargaría de las preferencias del usuario
        return userName
    }
    
    /**
     * Establece el nombre del usuario
     */
    fun setUserName(name: String) {
        userName = name
    }
    
    /**
     * Obtiene las notificaciones pendientes
     */
    suspend fun getPendingNotifications(): List<NotificationInfo> {
        return withContext(Dispatchers.IO) {
            // Obtener notificaciones del servicio
            val notificationService = RitsuNotificationListenerService.getInstance()
            
            if (notificationService != null) {
                val activeNotifications = notificationService.getActiveNotifications()
                
                // Convertir a NotificationInfo
                activeNotifications.map { sbn ->
                    convertToNotificationInfo(sbn)
                }
            } else {
                // Servicio no disponible
                emptyList()
            }
        }
    }
    
    /**
     * Convierte una StatusBarNotification a NotificationInfo
     */
    private fun convertToNotificationInfo(sbn: StatusBarNotification): NotificationInfo {
        val packageName = sbn.packageName
        val notification = sbn.notification
        
        // Extraer información
        val title = notification.extras.getString("android.title") ?: ""
        val text = notification.extras.getCharSequence("android.text")?.toString() ?: ""
        
        return NotificationInfo(
            id = sbn.id,
            packageName = packageName,
            title = title,
            content = text,
            timestamp = sbn.postTime,
            isRead = false
        )
    }
}

