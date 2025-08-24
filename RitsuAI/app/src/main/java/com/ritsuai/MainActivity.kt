package com.ritsuai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ritsuai.databinding.ActivityMainBinding
import com.ritsuai.services.RitsuOverlayService

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private lateinit var ritsuAICore: RitsuAICore
    
    // Códigos de solicitud para permisos
    private val OVERLAY_PERMISSION_REQUEST_CODE = 1001
    private val ACCESSIBILITY_PERMISSION_REQUEST_CODE = 1002
    private val AUDIO_PERMISSION_REQUEST_CODE = 1003
    private val STORAGE_PERMISSION_REQUEST_CODE = 1004
    private val INSTALL_PACKAGES_PERMISSION_REQUEST_CODE = 1005
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar el núcleo de Ritsu
        ritsuAICore = RitsuAICore(applicationContext)
        
        // Configurar botones
        setupButtons()
        
        // Verificar permisos necesarios
        checkRequiredPermissions()
    }
    
    private fun setupButtons() {
        // Botón para iniciar el servicio de overlay
        binding.btnStartRitsu.setOnClickListener {
            if (checkOverlayPermission()) {
                startRitsuService()
            } else {
                requestOverlayPermission()
            }
        }
        
        // Botón para detener el servicio
        binding.btnStopRitsu.setOnClickListener {
            stopRitsuService()
        }
        
        // Botón para configuración
        binding.btnSettings.setOnClickListener {
            // Aquí iría la navegación a la pantalla de configuración
            Toast.makeText(this, "Configuración de Ritsu", Toast.LENGTH_SHORT).show()
        }
        
        // Botón para verificar actualizaciones
        binding.btnCheckUpdates.setOnClickListener {
            Toast.makeText(this, "Verificando actualizaciones...", Toast.LENGTH_SHORT).show()
            // Aquí iría la lógica para verificar actualizaciones
        }
    }
    
    private fun checkRequiredPermissions() {
        // Verificar permiso de overlay
        if (!checkOverlayPermission()) {
            requestOverlayPermission()
        }
        
        // Verificar permiso de audio
        if (!checkAudioPermission()) {
            requestAudioPermission()
        }
        
        // Verificar permiso de almacenamiento
        if (!checkStoragePermission()) {
            requestStoragePermission()
        }
        
        // Verificar permiso de instalación de paquetes
        if (!checkInstallPackagesPermission()) {
            requestInstallPackagesPermission()
        }
    }
    
    // Métodos para verificar permisos
    
    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }
    
    private fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // En Android 11+ usamos el nuevo método
            try {
                // Verificar si tenemos acceso a todos los archivos
                val uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3A")
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                true
            } catch (e: Exception) {
                false
            }
        } else {
            // En versiones anteriores usamos el permiso tradicional
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun checkInstallPackagesPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }
    
    // Métodos para solicitar permisos
    
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
        }
    }
    
    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            AUDIO_PERMISSION_REQUEST_CODE
        )
    }
    
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, STORAGE_PERMISSION_REQUEST_CODE)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent, STORAGE_PERMISSION_REQUEST_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }
    
    private fun requestInstallPackagesPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, INSTALL_PACKAGES_PERMISSION_REQUEST_CODE)
        }
    }
    
    // Métodos para iniciar/detener el servicio
    
    private fun startRitsuService() {
        if (checkOverlayPermission()) {
            val intent = Intent(this, RitsuOverlayService::class.java)
            startService(intent)
            binding.btnStartRitsu.isEnabled = false
            binding.btnStopRitsu.isEnabled = true
        } else {
            Toast.makeText(
                this,
                "Se requiere permiso para mostrar sobre otras aplicaciones",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    private fun stopRitsuService() {
        val intent = Intent(this, RitsuOverlayService::class.java)
        stopService(intent)
        binding.btnStartRitsu.isEnabled = true
        binding.btnStopRitsu.isEnabled = false
    }
    
    // Manejo de resultados de permisos
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (checkOverlayPermission()) {
                    Log.d(TAG, "Permiso de overlay concedido")
                    startRitsuService()
                } else {
                    Log.d(TAG, "Permiso de overlay denegado")
                    showPermissionExplanationDialog(
                        "Permiso necesario",
                        "Ritsu necesita permiso para aparecer sobre otras aplicaciones"
                    )
                }
            }
            ACCESSIBILITY_PERMISSION_REQUEST_CODE -> {
                Log.d(TAG, "Resultado de permiso de accesibilidad")
                // La verificación de este permiso es más compleja y se maneja de otra manera
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                if (checkStoragePermission()) {
                    Log.d(TAG, "Permiso de almacenamiento concedido")
                } else {
                    Log.d(TAG, "Permiso de almacenamiento denegado")
                    showPermissionExplanationDialog(
                        "Permiso necesario",
                        "Ritsu necesita acceso al almacenamiento para guardar datos"
                    )
                }
            }
            INSTALL_PACKAGES_PERMISSION_REQUEST_CODE -> {
                if (checkInstallPackagesPermission()) {
                    Log.d(TAG, "Permiso de instalación de paquetes concedido")
                } else {
                    Log.d(TAG, "Permiso de instalación de paquetes denegado")
                    showPermissionExplanationDialog(
                        "Permiso necesario",
                        "Ritsu necesita permiso para instalar actualizaciones"
                    )
                }
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            AUDIO_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permiso de audio concedido")
                } else {
                    Log.d(TAG, "Permiso de audio denegado")
                    showPermissionExplanationDialog(
                        "Permiso necesario",
                        "Ritsu necesita acceso al micrófono para escuchar tus comandos"
                    )
                }
            }
        }
    }
    
    private fun showPermissionExplanationDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Configuración") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        ritsuAICore.destroy()
    }
}

