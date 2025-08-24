package com.ritsuai.launcher.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.ritsuai.launcher.ai.RitsuAICore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Servicio de accesibilidad para Ritsu.
 * Permite a Ritsu interactuar con otras aplicaciones y controlar el dispositivo.
 */
class RitsuAccessibilityService : AccessibilityService() {

    // Tag para logs
    private val TAG = "RitsuAccessibility"
    
    // Scope de corrutinas
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Núcleo de IA
    private lateinit var aiCore: RitsuAICore
    
    // Estado actual
    private var currentPackage: String? = null
    private var currentActivity: String? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar núcleo de IA
        aiCore = RitsuAICore.getInstance(applicationContext)
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        
        Log.d(TAG, "Servicio de accesibilidad conectado")
        
        // Notificar al núcleo de IA
        serviceScope.launch {
            aiCore.processMessage("Servicio de accesibilidad conectado", "system")
        }
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Procesar evento de accesibilidad
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChanged(event)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                handleViewClicked(event)
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                handleTextChanged(event)
            }
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                handleNotificationChanged(event)
            }
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Servicio de accesibilidad interrumpido")
    }
    
    /**
     * Maneja cambios en el estado de la ventana
     */
    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        val className = event.className?.toString()
        
        if (packageName != null && className != null) {
            // Actualizar estado actual
            currentPackage = packageName
            currentActivity = className
            
            Log.d(TAG, "Ventana cambiada: $packageName / $className")
            
            // Notificar al núcleo de IA sobre aplicaciones específicas
            if (isAppOfInterest(packageName)) {
                serviceScope.launch {
                    aiCore.processMessage("Aplicación abierta: $packageName", "system")
                }
            }
        }
    }
    
    /**
     * Maneja clics en vistas
     */
    private fun handleViewClicked(event: AccessibilityEvent) {
        val nodeInfo = event.source ?: return
        
        // Obtener información del nodo
        val nodeText = nodeInfo.text?.toString() ?: ""
        val nodeDesc = nodeInfo.contentDescription?.toString() ?: ""
        
        Log.d(TAG, "Clic en vista: $nodeText / $nodeDesc")
        
        // Liberar recursos
        nodeInfo.recycle()
    }
    
    /**
     * Maneja cambios en texto
     */
    private fun handleTextChanged(event: AccessibilityEvent) {
        val nodeInfo = event.source ?: return
        
        // Obtener información del nodo
        val nodeText = nodeInfo.text?.toString() ?: ""
        
        // Verificar si es una aplicación de mensajería
        if (isMessagingApp(currentPackage)) {
            Log.d(TAG, "Texto cambiado en app de mensajería: $nodeText")
        }
        
        // Liberar recursos
        nodeInfo.recycle()
    }
    
    /**
     * Maneja cambios en notificaciones
     */
    private fun handleNotificationChanged(event: AccessibilityEvent) {
        // Procesar notificaciones si es necesario
    }
    
    /**
     * Verifica si una aplicación es de interés para Ritsu
     */
    private fun isAppOfInterest(packageName: String): Boolean {
        return packageName in listOf(
            "com.whatsapp",
            "com.facebook.orca",
            "com.google.android.apps.messaging",
            "com.android.dialer",
            "com.google.android.dialer",
            "com.android.contacts",
            "com.google.android.contacts"
        )
    }
    
    /**
     * Verifica si una aplicación es de mensajería
     */
    private fun isMessagingApp(packageName: String?): Boolean {
        return packageName in listOf(
            "com.whatsapp",
            "com.facebook.orca",
            "com.google.android.apps.messaging"
        )
    }
    
    /**
     * Busca un nodo por texto
     */
    fun findNodeByText(text: String, rootNode: AccessibilityNodeInfo? = null): AccessibilityNodeInfo? {
        val root = rootNode ?: rootInActiveWindow ?: return null
        
        // Buscar por texto exacto
        val nodes = root.findAccessibilityNodeInfosByText(text)
        if (nodes.isNotEmpty()) {
            return nodes[0]
        }
        
        return null
    }
    
    /**
     * Busca un nodo por ID
     */
    fun findNodeById(viewId: String, rootNode: AccessibilityNodeInfo? = null): AccessibilityNodeInfo? {
        val root = rootNode ?: rootInActiveWindow ?: return null
        
        // Buscar por ID
        val nodes = root.findAccessibilityNodeInfosByViewId(viewId)
        if (nodes.isNotEmpty()) {
            return nodes[0]
        }
        
        return null
    }
    
    /**
     * Hace clic en un nodo
     */
    fun clickNode(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        
        return if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            // Intentar con el padre
            val parent = node.parent
            val result = clickNode(parent)
            parent?.recycle()
            result
        }
    }
    
    /**
     * Escribe texto en un nodo
     */
    fun typeText(node: AccessibilityNodeInfo?, text: String): Boolean {
        if (node == null) return false
        
        if (node.isEditable) {
            // Limpiar texto existente
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, null)
            
            // Establecer nuevo texto
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        
        return false
    }
    
    /**
     * Realiza un gesto de deslizamiento
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 500): Boolean {
        val path = Path()
        path.moveTo(startX, startY)
        path.lineTo(endX, endY)
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
        
        return dispatchGesture(gesture, null, null)
    }
    
    /**
     * Realiza un gesto de toque
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun performTap(x: Float, y: Float): Boolean {
        val path = Path()
        path.moveTo(x, y)
        path.lineTo(x, y)
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        
        return dispatchGesture(gesture, null, null)
    }
    
    /**
     * Obtiene el texto de un nodo
     */
    fun getNodeText(node: AccessibilityNodeInfo?): String {
        if (node == null) return ""
        
        return node.text?.toString() ?: node.contentDescription?.toString() ?: ""
    }
    
    /**
     * Obtiene la posición de un nodo en la pantalla
     */
    fun getNodePosition(node: AccessibilityNodeInfo?): Rect? {
        if (node == null) return null
        
        val rect = Rect()
        node.getBoundsInScreen(rect)
        return rect
    }
    
    /**
     * Vuelve a la pantalla anterior
     */
    fun goBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }
    
    /**
     * Va a la pantalla de inicio
     */
    fun goHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }
    
    /**
     * Abre el panel de notificaciones
     */
    fun openNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }
    
    /**
     * Abre el selector de aplicaciones recientes
     */
    fun openRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }
    
    /**
     * Toma una captura de pantalla
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun takeScreenshot(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
    }
    
    companion object {
        // Instancia del servicio
        private var instance: RitsuAccessibilityService? = null
        
        /**
         * Obtiene la instancia del servicio
         */
        fun getInstance(): RitsuAccessibilityService? {
            return instance
        }
        
        /**
         * Inicia el servicio
         */
        fun start(context: android.content.Context) {
            val intent = Intent(context, RitsuAccessibilityService::class.java)
            context.startService(intent)
        }
    }
}

