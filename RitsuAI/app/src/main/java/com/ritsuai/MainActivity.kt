package com.ritsuai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ritsuai.services.RitsuAccessibilityService
import com.ritsuai.services.RitsuOverlayService
import kotlinx.coroutines.*

/**
 * Actividad principal de Ritsu AI
 * Maneja la configuración inicial, permisos y la interfaz de usuario
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var ritsuCore: RitsuAICore
    private lateinit var chatEditText: EditText
    private lateinit var chatScrollView: ScrollView
    private lateinit var chatContainer: LinearLayout
    private lateinit var sendButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var avatarImageView: ImageView
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1002
        private const val ACCESSIBILITY_PERMISSION_REQUEST_CODE = 1003
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initializeViews()
        initializeRitsu()
        checkAndRequestPermissions()
        setupChatInterface()
    }
    
    private fun initializeViews() {
        chatEditText = findViewById(R.id.chatEditText)
        chatScrollView = findViewById(R.id.chatScrollView)
        chatContainer = findViewById(R.id.chatContainer)
        sendButton = findViewById(R.id.sendButton)
        statusTextView = findViewById(R.id.statusTextView)
        avatarImageView = findViewById(R.id.avatarImageView)
    }
    
    private fun initializeRitsu() {
        ritsuCore = RitsuAICore(this)
        
        // Mostrar mensaje de bienvenida
        scope.launch {
            val welcomeResponse = ritsuCore.processInput("Hola Ritsu, preséntate", "user")
            addChatMessage("Ritsu", welcomeResponse.text, true)
            
            if (welcomeResponse.voiceEnabled) {
                ritsuCore.speak(welcomeResponse.text)
            }
        }
        
        updateAvatarDisplay()
    }
    
    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        
        // Permisos básicos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CALL_PHONE)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.SEND_SMS)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_CONTACTS)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            checkSpecialPermissions()
        }
    }
    
    private fun checkSpecialPermissions() {
        // Verificar permiso de overlay
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
                return
            }
        }
        
        // Verificar servicio de accesibilidad
        if (!isAccessibilityServiceEnabled()) {
            requestAccessibilityPermission()
            return
        }
        
        // Todos los permisos concedidos
        onAllPermissionsGranted()
    }
    
    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        
        Toast.makeText(this, "Por favor, permite que Ritsu aparezca sobre otras aplicaciones", Toast.LENGTH_LONG).show()
    }
    
    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivityForResult(intent, ACCESSIBILITY_PERMISSION_REQUEST_CODE)
        
        Toast.makeText(this, "Por favor, activa el servicio de accesibilidad de Ritsu", Toast.LENGTH_LONG).show()
    }
    
    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED,
            0
        )
        
        if (accessibilityEnabled == 1) {
            val services = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return services?.contains("${packageName}/${RitsuAccessibilityService::class.java.name}") == true
        }
        
        return false
    }
    
    private fun onAllPermissionsGranted() {
        statusTextView.text = "✅ Ritsu está completamente configurada y lista"
        
        // Iniciar servicios
        startRitsuServices()
        
        // Mostrar mensaje de confirmación
        scope.launch {
            val response = ritsuCore.processInput("Todos los permisos concedidos, estoy lista", "system")
            addChatMessage("Ritsu", response.text, true)
            ritsuCore.speak(response.text)
        }
    }
    
    private fun startRitsuServices() {
        // Iniciar servicio de overlay
        val overlayIntent = Intent(this, RitsuOverlayService::class.java)
        startForegroundService(overlayIntent)
        
        Toast.makeText(this, "Ritsu ahora vive en tu teléfono ✨", Toast.LENGTH_SHORT).show()
    }
    
    private fun setupChatInterface() {
        sendButton.setOnClickListener {
            val message = chatEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                chatEditText.text.clear()
            }
        }
        
        chatEditText.setOnEditorActionListener { _, _, _ ->
            val message = chatEditText.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                chatEditText.text.clear()
                true
            } else {
                false
            }
        }
    }
    
    private fun sendMessage(message: String) {
        // Agregar mensaje del usuario
        addChatMessage("Tú", message, false)
        
        // Procesar con Ritsu
        scope.launch {
            try {
                val response = ritsuCore.processInput(message, "user")
                
                // Agregar respuesta de Ritsu
                addChatMessage("Ritsu", response.text, true)
                
                // Actualizar avatar si hay cambios
                response.avatarChange?.let { outfit ->
                    RitsuOverlayService.instance?.changeClothing(outfit)
                    updateAvatarDisplay()
                }
                
                // Ejecutar acción de teléfono si es necesaria
                response.phoneAction?.let { action ->
                    executePhoneAction(action)
                }
                
                // Hablar si está habilitado
                if (response.voiceEnabled) {
                    ritsuCore.speak(response.text)
                }
                
            } catch (e: Exception) {
                addChatMessage("Sistema", "Error: ${e.message}", false)
            }
        }
    }
    
    private fun addChatMessage(sender: String, message: String, isRitsu: Boolean) {
        val messageView = TextView(this).apply {
            text = "$sender: $message"
            textSize = 16f
            setPadding(16, 8, 16, 8)
            
            if (isRitsu) {
                setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_blue_dark))
                setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.holo_blue_light))
            } else {
                setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
                setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.white))
            }
        }
        
        chatContainer.addView(messageView)
        
        // Scroll hacia abajo
        chatScrollView.post {
            chatScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
    
    private fun updateAvatarDisplay() {
        scope.launch(Dispatchers.IO) {
            val avatarManager = RitsuAvatarManager(this@MainActivity)
            val avatarBitmap = avatarManager.generateAvatarBitmap()
            
            withContext(Dispatchers.Main) {
                avatarImageView.setImageBitmap(avatarBitmap)
            }
        }
    }
    
    private suspend fun executePhoneAction(action: PhoneAction) {
        val phoneControlManager = PhoneControlManager(this)
        val result = phoneControlManager.executeAction(action)
        
        val resultMessage = if (result.success) {
            "✅ ${result.message}"
        } else {
            "❌ ${result.message}"
        }
        
        addChatMessage("Sistema", resultMessage, false)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                if (allGranted) {
                    checkSpecialPermissions()
                } else {
                    statusTextView.text = "⚠️ Algunos permisos son necesarios para el funcionamiento completo"
                    Toast.makeText(this, "Ritsu necesita todos los permisos para funcionar correctamente", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                    checkSpecialPermissions()
                } else {
                    statusTextView.text = "⚠️ Permiso de overlay requerido"
                }
            }
            
            ACCESSIBILITY_PERMISSION_REQUEST_CODE -> {
                if (isAccessibilityServiceEnabled()) {
                    checkSpecialPermissions()
                } else {
                    statusTextView.text = "⚠️ Servicio de accesibilidad requerido"
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Verificar estado de permisos
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this) && isAccessibilityServiceEnabled()) {
            statusTextView.text = "✅ Ritsu está completamente configurada"
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        ritsuCore.cleanup()
    }
}

