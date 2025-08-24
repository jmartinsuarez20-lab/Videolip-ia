package com.ritsuai.launcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.ritsuai.launcher.adapters.AppListAdapter
import com.ritsuai.launcher.avatar3d.RitsuAvatarService
import com.ritsuai.launcher.databinding.ActivityMainBinding
import com.ritsuai.launcher.models.AppInfo
import com.ritsuai.launcher.ui.VoiceCommandActivity
import com.ritsuai.launcher.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var appAdapter: AppListAdapter

    // Lista de permisos requeridos
    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS
    )
    
    // Código de solicitud para permisos
    private val PERMISSION_REQUEST_CODE = 100
    private val OVERLAY_PERMISSION_REQUEST_CODE = 101
    private val ACCESSIBILITY_PERMISSION_REQUEST_CODE = 102
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // Configurar RecyclerView
        setupRecyclerView()
        
        // Configurar búsqueda
        setupSearch()
        
        // Configurar botón de comandos de voz
        binding.voiceCommandFab.setOnClickListener {
            startVoiceCommand()
        }
        
        // Observar cambios en la lista de aplicaciones
        viewModel.appList.observe(this) { apps ->
            appAdapter.submitList(apps)
            binding.loadingIndicator.visibility = View.GONE
        }
        
        // Verificar y solicitar permisos
        checkAndRequestPermissions()
    }
    
    override fun onResume() {
        super.onResume()
        
        // Cargar lista de aplicaciones
        viewModel.loadInstalledApps(this)
        
        // Verificar si el servicio de overlay está activo
        if (Settings.canDrawOverlays(this)) {
            startRitsuAvatarService()
        }
    }
    
    /**
     * Configura el RecyclerView para mostrar la lista de aplicaciones
     */
    private fun setupRecyclerView() {
        appAdapter = AppListAdapter { app ->
            launchApp(app)
        }
        
        binding.appsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 4)
            adapter = appAdapter
        }
    }
    
    /**
     * Configura la funcionalidad de búsqueda
     */
    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterApps(s.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
        
        binding.voiceSearchIcon.setOnClickListener {
            startVoiceCommand()
        }
    }
    
    /**
     * Inicia la actividad de comandos de voz
     */
    private fun startVoiceCommand() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, VoiceCommandActivity::class.java)
            startActivity(intent)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_REQUEST_CODE)
        }
    }
    
    /**
     * Lanza la aplicación seleccionada
     */
    private fun launchApp(app: AppInfo) {
        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "No se puede abrir la aplicación", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Verifica y solicita los permisos necesarios
     */
    private fun checkAndRequestPermissions() {
        // Verificar permisos básicos
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSION_REQUEST_CODE)
        }
        
        // Verificar permiso de overlay
        if (!Settings.canDrawOverlays(this)) {
            showOverlayPermissionDialog()
        } else {
            startRitsuAvatarService()
        }
        
        // Verificar permiso de accesibilidad
        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityPermissionDialog()
        }
    }
    
    /**
     * Muestra un diálogo para solicitar permiso de overlay
     */
    private fun showOverlayPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.overlay_permission_required)
            .setPositiveButton(R.string.ok) { _, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    /**
     * Muestra un diálogo para solicitar permiso de accesibilidad
     */
    private fun showAccessibilityPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.accessibility_permission_required)
            .setPositiveButton(R.string.ok) { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivityForResult(intent, ACCESSIBILITY_PERMISSION_REQUEST_CODE)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    /**
     * Verifica si el servicio de accesibilidad está habilitado
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityEnabled = try {
            Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: Settings.SettingNotFoundException) {
            0
        }
        
        if (accessibilityEnabled == 1) {
            val services = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            return services?.contains("${packageName}/.services.RitsuAccessibilityService") == true
        }
        
        return false
    }
    
    /**
     * Inicia el servicio de avatar 3D
     */
    private fun startRitsuAvatarService() {
        if (Settings.canDrawOverlays(this)) {
            val intent = Intent(this, RitsuAvatarService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Todos los permisos concedidos
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show()
            } else {
                // Algunos permisos denegados
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                startRitsuAvatarService()
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == ACCESSIBILITY_PERMISSION_REQUEST_CODE) {
            if (isAccessibilityServiceEnabled()) {
                Toast.makeText(this, "Servicio de accesibilidad habilitado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

