package com.ritsuai.services

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
import com.ritsuai.RitsuAICore

/**
 * Servicio de accesibilidad para controlar el dispositivo
 */
class RitsuAccessibilityService : AccessibilityService() {

    private val TAG = "RitsuAccessibility"
    private var ritsuAICore: RitsuAICore? = null
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Servicio de accesibilidad creado")
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Servicio de accesibilidad conectado")
        
        // Inicializar el núcleo de Ritsu
        ritsuAICore = RitsuAICore(applicationContext)
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Procesar eventos de accesibilidad
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.d(TAG, "Cambio de ventana: ${event.className}")
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                Log.d(TAG, "Click en vista: ${event.className}")
            }
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                Log.d(TAG, "Notificación: ${event.text}")
            }
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Servicio de accesibilidad interrumpido")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Servicio de accesibilidad destruido")
        
        // Liberar recursos
        ritsuAICore?.destroy()
        ritsuAICore = null
    }
    
    /**
     * Abre una aplicación por su paquete
     */
    fun openApp(packageName: String): Boolean {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al abrir aplicación: ${e.message}")
        }
        return false
    }
    
    /**
     * Realiza un clic en una posición específica
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun performClick(x: Float, y: Float): Boolean {
        try {
            val path = Path()
            path.moveTo(x, y)
            
            val gestureBuilder = GestureDescription.Builder()
            val gesture = gestureBuilder
                .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
                .build()
            
            return dispatchGesture(gesture, null, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error al realizar clic: ${e.message}")
            return false
        }
    }
    
    /**
     * Realiza un deslizamiento
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long = 500): Boolean {
        try {
            val path = Path()
            path.moveTo(startX, startY)
            path.lineTo(endX, endY)
            
            val gestureBuilder = GestureDescription.Builder()
            val gesture = gestureBuilder
                .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
                .build()
            
            return dispatchGesture(gesture, null, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error al realizar deslizamiento: ${e.message}")
            return false
        }
    }
    
    /**
     * Busca un nodo por texto
     */
    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val rootNode = rootInActiveWindow ?: return null
        
        val nodes = ArrayList<AccessibilityNodeInfo>()
        findNodesByText(rootNode, text, nodes)
        
        return if (nodes.isNotEmpty()) nodes[0] else null
    }
    
    /**
     * Busca nodos por texto recursivamente
     */
    private fun findNodesByText(node: AccessibilityNodeInfo, text: String, result: ArrayList<AccessibilityNodeInfo>) {
        if (node.text != null && node.text.toString().contains(text, ignoreCase = true)) {
            result.add(node)
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                findNodesByText(child, text, result)
            }
        }
    }
    
    /**
     * Hace clic en un nodo
     */
    fun clickOnNode(node: AccessibilityNodeInfo): Boolean {
        try {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } catch (e: Exception) {
            Log.e(TAG, "Error al hacer clic en nodo: ${e.message}")
            
            // Intentar con coordenadas si falla el método directo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val rect = Rect()
                node.getBoundsInScreen(rect)
                
                val x = rect.centerX().toFloat()
                val y = rect.centerY().toFloat()
                
                return performClick(x, y)
            }
            
            return false
        }
    }
    
    /**
     * Escribe texto en un campo de texto
     */
    fun typeText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        // Buscar campo de texto enfocado
        val focusedNode = findFocusedEditText(rootNode)
        
        if (focusedNode != null) {
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            
            return focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        
        return false
    }
    
    /**
     * Busca un campo de texto enfocado
     */
    private fun findFocusedEditText(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable && node.isFocused) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val result = findFocusedEditText(child)
                if (result != null) {
                    return result
                }
            }
        }
        
        return null
    }
    
    /**
     * Presiona el botón Atrás
     */
    fun performBackButton(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }
    
    /**
     * Presiona el botón Home
     */
    fun performHomeButton(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }
    
    /**
     * Abre el selector de aplicaciones recientes
     */
    fun performRecentAppsButton(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }
    
    /**
     * Abre las notificaciones
     */
    fun performOpenNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }
    
    /**
     * Toma una captura de pantalla
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun takeScreenshot(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
    }
}

