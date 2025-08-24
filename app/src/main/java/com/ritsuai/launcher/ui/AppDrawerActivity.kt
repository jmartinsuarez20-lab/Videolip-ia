package com.ritsuai.launcher.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.ritsuai.launcher.adapters.AppListAdapter
import com.ritsuai.launcher.databinding.ActivityAppDrawerBinding
import com.ritsuai.launcher.models.AppInfo
import com.ritsuai.launcher.viewmodels.AppDrawerViewModel
import com.ritsuai.launcher.speech.SpeechRecognitionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Actividad que muestra el cajón de aplicaciones completo.
 * Permite buscar y organizar todas las aplicaciones instaladas.
 */
class AppDrawerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppDrawerBinding
    private lateinit var viewModel: AppDrawerViewModel
    private lateinit var appAdapter: AppListAdapter
    
    // Reconocimiento de voz
    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    
    // Scope de corrutinas
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inicializar binding
        binding = ActivityAppDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this)[AppDrawerViewModel::class.java]
        
        // Inicializar reconocimiento de voz
        speechRecognitionManager = SpeechRecognitionManager.getInstance(this)
        
        // Configurar RecyclerView
        setupRecyclerView()
        
        // Configurar búsqueda
        setupSearch()
        
        // Configurar botón de voz
        binding.voiceSearchIcon.setOnClickListener {
            startVoiceSearch()
        }
        
        // Configurar botón de volver
        binding.backButton.setOnClickListener {
            finish()
        }
        
        // Observar cambios en la lista de aplicaciones
        viewModel.appList.observe(this) { apps ->
            appAdapter.submitList(apps)
            binding.loadingIndicator.visibility = View.GONE
            
            // Mostrar mensaje si no hay resultados
            if (apps.isEmpty() && binding.searchEditText.text.isNotEmpty()) {
                binding.noResultsText.visibility = View.VISIBLE
            } else {
                binding.noResultsText.visibility = View.GONE
            }
        }
        
        // Cargar aplicaciones
        viewModel.loadInstalledApps(this)
    }
    
    override fun onResume() {
        super.onResume()
        
        // Actualizar lista de aplicaciones
        viewModel.loadInstalledApps(this)
    }
    
    /**
     * Configura el RecyclerView
     */
    private fun setupRecyclerView() {
        appAdapter = AppListAdapter { app ->
            launchApp(app)
        }
        
        binding.appsRecyclerView.apply {
            layoutManager = GridLayoutManager(this@AppDrawerActivity, 4)
            adapter = appAdapter
        }
    }
    
    /**
     * Configura la búsqueda
     */
    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterApps(s.toString())
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    /**
     * Inicia la búsqueda por voz
     */
    private fun startVoiceSearch() {
        // Mostrar indicador de escucha
        binding.voiceSearchIcon.setImageResource(android.R.drawable.ic_btn_speak_now)
        binding.searchEditText.hint = "Escuchando..."
        
        // Iniciar reconocimiento
        speechRecognitionManager.initialize()
        speechRecognitionManager.startListening { result ->
            // Establecer texto de búsqueda
            binding.searchEditText.setText(result)
            
            // Restaurar icono
            binding.voiceSearchIcon.setImageResource(android.R.drawable.ic_btn_speak_now)
            binding.searchEditText.hint = "Buscar aplicaciones..."
        }
        
        // Observar estado del reconocimiento
        activityScope.launch {
            speechRecognitionManager.recognitionState.collect { state ->
                when (state) {
                    is SpeechRecognitionManager.RecognitionState.Error -> {
                        // Error en reconocimiento
                        Toast.makeText(this@AppDrawerActivity, state.message, Toast.LENGTH_SHORT).show()
                        
                        // Restaurar icono
                        binding.voiceSearchIcon.setImageResource(android.R.drawable.ic_btn_speak_now)
                        binding.searchEditText.hint = "Buscar aplicaciones..."
                    }
                    else -> {
                        // Otros estados
                    }
                }
            }
        }
    }
    
    /**
     * Lanza una aplicación
     */
    private fun launchApp(app: AppInfo) {
        val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
            finish() // Cerrar cajón de aplicaciones
        } else {
            Toast.makeText(this, "No se puede abrir la aplicación", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Liberar recursos
        speechRecognitionManager.destroy()
    }
}

