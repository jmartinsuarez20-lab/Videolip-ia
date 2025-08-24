package com.ritsuai.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.ritsuai.*
import kotlinx.coroutines.*

/**
 * Servicio de accesibilidad que permite a Ritsu controlar completamente el teléfono
 * Puede interactuar con cualquier aplicación y realizar acciones automáticas
 */
class RitsuAccessibilityService : AccessibilityService() {
    
    private lateinit var ritsuCore: RitsuAICore
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())
    
    companion object {
        var instance: RitsuAccessibilityService? = null
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        ritsuCore = RitsuAICore(this)
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        // Ritsu está lista para controlar el teléfono
        sendBroadcast(Intent("com.ritsuai.ACCESSIBILITY_CONNECTED"))
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let { processAccessibilityEvent(it) }
    }
    
    override fun onInterrupt() {
        // Manejar interrupciones del servicio
    }
    
    private fun processAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowChange(event)
            }
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                handleNotification(event)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                handleViewClick(event)
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                handleTextChange(event)
            }
        }
    }
    
    private fun handleWindowChange(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: return
        
        // Notificar a Ritsu sobre el cambio de aplicación
        scope.launch {
            val response = ritsuCore.processInput(
                "App cambiada a: $packageName",
                "system"
            )
            // Ritsu puede reaccionar al cambio de app
        }
    }
    
    private fun handleNotification(event: AccessibilityEvent) {
        val notificationText = event.text?.joinToString(" ") ?: return
        
        // Ritsu puede leer y responder notificaciones
        scope.launch {
            val response = ritsuCore.processInput(
                "Nueva notificación: $notificationText",
                "system"
            )
            
            if (response.phoneAction != null) {
                executePhoneAction(response.phoneAction)
            }
        }
    }
    
    private fun handleViewClick(event: AccessibilityEvent) {
        // Ritsu puede aprender de las interacciones del usuario
        val clickedText = event.text?.joinToString(" ") ?: ""
        if (clickedText.isNotEmpty()) {
            scope.launch {
                ritsuCore.processInput("Usuario hizo clic en: $clickedText", "learning")
            }
        }
    }
    
    private fun handleTextChange(event: AccessibilityEvent) {
        // Ritsu puede ayudar con la escritura
        val newText = event.text?.joinToString(" ") ?: ""
        if (newText.length > 50) { // Solo textos largos
            scope.launch {
                val response = ritsuCore.processInput(
                    "¿Puedes ayudarme a mejorar este texto: $newText",
                    "user"
                )
                // Ritsu podría sugerir mejoras
            }
        }
    }
    
    /**
     * Ejecuta acciones de teléfono solicitadas por Ritsu
     */
    private fun executePhoneAction(action: PhoneAction) {
        when (action.type) {
            PhoneActionType.CALL -> makeCall(action.target)
            PhoneActionType.MESSAGE -> sendMessage(action.target, action.content)
            PhoneActionType.WHATSAPP -> openWhatsApp(action.target, action.content)
            PhoneActionType.OPEN_APP -> openApp(action.target)
            else -> { /* Otras acciones */ }
        }
    }
    
    private fun makeCall(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = android.net.Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(callIntent)
    }
    
    private fun sendMessage(contact: String, message: String) {
        // Abrir app de mensajes y escribir automáticamente
        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("sms:$contact")
            putExtra("sms_body", message)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(smsIntent)
        
        // Después de un delay, hacer clic en enviar automáticamente
        handler.postDelayed({
            clickSendButton()
        }, 2000)
    }
    
    private fun openWhatsApp(contact: String, message: String) {
        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=$contact&text=${android.net.Uri.encode(message)}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(whatsappIntent)
        
        // Auto-enviar después de abrir WhatsApp
        handler.postDelayed({
            clickSendButton()
        }, 3000)
    }
    
    private fun openApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.let {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(it)
        }
    }
    
    private fun clickSendButton() {
        val rootNode = rootInActiveWindow ?: return
        
        // Buscar botón de enviar en diferentes apps
        val sendButtons = listOf("Enviar", "Send", "➤", "→")
        
        for (buttonText in sendButtons) {
            val sendButton = findNodeByText(rootNode, buttonText)
            if (sendButton != null && sendButton.isClickable) {
                sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                break
            }
        }
    }
    
    private fun findNodeByText(node: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            child?.let {
                val result = findNodeByText(it, text)
                if (result != null) return result
            }
        }
        
        return null
    }
    
    /**
     * Permite a Ritsu hacer clic en coordenadas específicas
     */
    fun performClick(x: Float, y: Float) {
        val path = Path().apply {
            moveTo(x, y)
        }
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        
        dispatchGesture(gesture, null, null)
    }
    
    /**
     * Permite a Ritsu escribir texto automáticamente
     */
    fun typeText(text: String) {
        val rootNode = rootInActiveWindow ?: return
        val editText = findEditableNode(rootNode)
        
        editText?.let { node ->
            val arguments = android.os.Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
    }
    
    private fun findEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            child?.let {
                val result = findEditableNode(it)
                if (result != null) return result
            }
        }
        
        return null
    }
    
    /**
     * Permite a Ritsu hacer scroll
     */
    fun performScroll(direction: ScrollDirection) {
        val rootNode = rootInActiveWindow ?: return
        
        val action = when (direction) {
            ScrollDirection.UP -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            ScrollDirection.DOWN -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            ScrollDirection.LEFT -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            ScrollDirection.RIGHT -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
        }
        
        rootNode.performAction(action)
    }
    
    /**
     * Obtiene el texto visible en pantalla
     */
    fun getScreenText(): String {
        val rootNode = rootInActiveWindow ?: return ""
        return extractTextFromNode(rootNode)
    }
    
    private fun extractTextFromNode(node: AccessibilityNodeInfo): String {
        val text = StringBuilder()
        
        node.text?.let { text.append(it).append(" ") }
        node.contentDescription?.let { text.append(it).append(" ") }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            child?.let {
                text.append(extractTextFromNode(it))
            }
        }
        
        return text.toString()
    }
    
    /**
     * Permite a Ritsu navegar hacia atrás
     */
    fun goBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }
    
    /**
     * Permite a Ritsu ir al home
     */
    fun goHome() {
        performGlobalAction(GLOBAL_ACTION_HOME)
    }
    
    /**
     * Permite a Ritsu abrir aplicaciones recientes
     */
    fun openRecents() {
        performGlobalAction(GLOBAL_ACTION_RECENTS)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        scope.cancel()
        ritsuCore.cleanup()
    }
}

enum class ScrollDirection {
    UP, DOWN, LEFT, RIGHT
}

