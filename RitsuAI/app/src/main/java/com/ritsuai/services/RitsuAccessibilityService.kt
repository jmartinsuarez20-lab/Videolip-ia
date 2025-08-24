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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Servicio de accesibilidad que permite a Ritsu controlar otras aplicaciones
 */
class RitsuAccessibilityService : AccessibilityService() {

    private val TAG = "RitsuAccessibility"
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Procesar eventos de accesibilidad
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                Log.d(TAG, "Ventana cambiada: ${event.className}")
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // Contenido de la ventana cambiado
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                Log.d(TAG, "Vista clickeada: ${event.className}")
            }
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "Servicio de accesibilidad interrumpido")
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Servicio de accesibilidad conectado")
    }
    
    /**
     * Abre una aplicación por su paquete
     */
    fun openApp(packageName: String): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        return if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            true
        } else {
            Log.e(TAG, "No se pudo encontrar la aplicación: $packageName")
            false
        }
    }
    
    /**
     * Busca y hace clic en un elemento por su texto
     */
    fun clickOnElementWithText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        val node = findNodeByText(rootNode, text)
        return if (node != null) {
            performClickOnNode(node)
            true
        } else {
            Log.e(TAG, "No se encontró elemento con texto: $text")
            false
        }
    }
    
    /**
     * Busca y hace clic en un elemento por su ID
     */
    fun clickOnElementWithId(viewId: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        val node = findNodeById(rootNode, viewId)
        return if (node != null) {
            performClickOnNode(node)
            true
        } else {
            Log.e(TAG, "No se encontró elemento con ID: $viewId")
            false
        }
    }
    
    /**
     * Escribe texto en un campo de texto
     */
    fun typeText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        val editTextNode = findEditTextNode(rootNode)
        return if (editTextNode != null) {
            val bundle = android.os.Bundle()
            bundle.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
            editTextNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, bundle)
            true
        } else {
            Log.e(TAG, "No se encontró campo de texto editable")
            false
        }
    }
    
    /**
     * Realiza un gesto de desplazamiento
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun performScroll(direction: ScrollDirection): Boolean {
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val screenWidth = displayMetrics.widthPixels
        
        val path = Path()
        when (direction) {
            ScrollDirection.UP -> {
                path.moveTo(screenWidth / 2f, screenHeight * 0.7f)
                path.lineTo(screenWidth / 2f, screenHeight * 0.3f)
            }
            ScrollDirection.DOWN -> {
                path.moveTo(screenWidth / 2f, screenHeight * 0.3f)
                path.lineTo(screenWidth / 2f, screenHeight * 0.7f)
            }
            ScrollDirection.LEFT -> {
                path.moveTo(screenWidth * 0.7f, screenHeight / 2f)
                path.lineTo(screenWidth * 0.3f, screenHeight / 2f)
            }
            ScrollDirection.RIGHT -> {
                path.moveTo(screenWidth * 0.3f, screenHeight / 2f)
                path.lineTo(screenWidth * 0.7f, screenHeight / 2f)
            }
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 500))
            .build()
        
        return dispatchGesture(gesture, null, null)
    }
    
    /**
     * Realiza un toque en una posición específica de la pantalla
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun performTap(x: Float, y: Float): Boolean {
        val path = Path()
        path.moveTo(x, y)
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        
        return dispatchGesture(gesture, null, null)
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
     * Abre el menú de aplicaciones recientes
     */
    fun openRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }
    
    /**
     * Abre el panel de notificaciones
     */
    fun openNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }
    
    /**
     * Abre los ajustes rápidos
     */
    fun openQuickSettings(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)
    }
    
    /**
     * Toma una captura de pantalla (requiere Android 10+)
     */
    @RequiresApi(Build.VERSION_CODES.P)
    fun takeScreenshot() {
        serviceScope.launch {
            try {
                takeScreenshot(
                    AccessibilityService.TAKE_SCREENSHOT_DISPLAY,
                    Dispatchers.IO.asExecutor()
                ) { result ->
                    if (result != null) {
                        Log.d(TAG, "Captura de pantalla tomada con éxito")
                        // Aquí se procesaría la captura
                        result.bitmap.recycle()
                    } else {
                        Log.e(TAG, "Error al tomar captura de pantalla")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al tomar captura de pantalla: ${e.message}")
            }
        }
    }
    
    // Métodos auxiliares para buscar nodos
    
    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        // Buscar en el nodo actual
        if (node.text?.contains(text, ignoreCase = true) == true) {
            return node
        }
        
        // Buscar en los hijos
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeByText(child, text)
            if (result != null) {
                return result
            }
            child.recycle()
        }
        
        return null
    }
    
    private fun findNodeById(node: AccessibilityNodeInfo, viewId: String): AccessibilityNodeInfo? {
        // Buscar por ID
        val nodes = node.findAccessibilityNodeInfosByViewId(viewId)
        if (nodes.isNotEmpty()) {
            val result = nodes[0]
            for (i in 1 until nodes.size) {
                nodes[i].recycle()
            }
            return result
        }
        
        // Buscar en los hijos
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findNodeById(child, viewId)
            if (result != null) {
                return result
            }
            child.recycle()
        }
        
        return null
    }
    
    private fun findEditTextNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        // Verificar si el nodo actual es un campo de texto editable
        if (node.isEditable) {
            return node
        }
        
        // Buscar en los hijos
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findEditTextNode(child)
            if (result != null) {
                return result
            }
            child.recycle()
        }
        
        return null
    }
    
    private fun performClickOnNode(node: AccessibilityNodeInfo): Boolean {
        // Verificar si el nodo es clickeable
        if (node.isClickable) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        
        // Si no es clickeable, intentar con el padre
        val parent = node.parent
        return if (parent != null) {
            val result = performClickOnNode(parent)
            parent.recycle()
            result
        } else {
            false
        }
    }
    
    // Enumeración para direcciones de desplazamiento
    enum class ScrollDirection {
        UP, DOWN, LEFT, RIGHT
    }
}

