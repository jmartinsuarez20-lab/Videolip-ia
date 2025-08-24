package com.ritsuai.evolution

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.ritsuai.RitsuAICore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Sistema de actualización automática de Ritsu
 * Gestiona la descarga e instalación de actualizaciones
 */
class RitsuAutoUpdater(
    private val context: Context,
    private val ritsuAICore: RitsuAICore
) {
    private val TAG = "RitsuAutoUpdater"
    
    // Corrutinas
    private val updaterScope = CoroutineScope(Dispatchers.IO)
    
    // Estado
    private var isCheckingForUpdates = false
    private var isDownloadingUpdate = false
    private var downloadId: Long = -1
    
    // Configuración
    private val updateCheckInterval = 24 * 60 * 60 * 1000 // 24 horas en milisegundos
    private val githubRepoUrl = "https://api.github.com/repos/jmartinsuarez20-lab/Videolip-ia/releases/latest"
    
    // Receptor para descargas completadas
    private val downloadCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                Log.d(TAG, "Descarga de actualización completada")
                installUpdate()
            }
        }
    }
    
    /**
     * Inicializa el sistema de actualización automática
     */
    fun initialize() {
        Log.d(TAG, "Inicializando sistema de actualización automática")
        
        try {
            // Registrar receptor para descargas completadas
            context.registerReceiver(
                downloadCompleteReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
            
            // Verificar última actualización
            val lastUpdateCheck = context.getSharedPreferences("ritsu_settings", Context.MODE_PRIVATE)
                .getLong("last_update_check", 0)
            
            val currentTime = System.currentTimeMillis()
            
            // Si ha pasado suficiente tiempo, verificar actualizaciones
            if (currentTime - lastUpdateCheck > updateCheckInterval) {
                updaterScope.launch {
                    checkForUpdates()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar el sistema de actualización: ${e.message}")
        }
    }
    
    /**
     * Verifica si hay actualizaciones disponibles
     */
    suspend fun checkForUpdates(forceCheck: Boolean = false): Boolean {
        if (isCheckingForUpdates && !forceCheck) {
            Log.d(TAG, "Ya se está verificando actualizaciones")
            return false
        }
        
        isCheckingForUpdates = true
        
        try {
            // Guardar timestamp de verificación
            context.getSharedPreferences("ritsu_settings", Context.MODE_PRIVATE)
                .edit()
                .putLong("last_update_check", System.currentTimeMillis())
                .apply()
            
            // Obtener versión actual
            val currentVersion = getCurrentVersion()
            
            // Obtener última versión disponible
            val latestVersion = getLatestVersion()
            
            if (latestVersion != null && isNewerVersion(currentVersion, latestVersion.first)) {
                Log.d(TAG, "Nueva versión disponible: ${latestVersion.first}")
                
                // Notificar al usuario
                // En una implementación real, aquí se mostraría una notificación
                
                // Si la actualización automática está habilitada, descargar
                val autoUpdate = context.getSharedPreferences("ritsu_settings", Context.MODE_PRIVATE)
                    .getBoolean("auto_update", false)
                
                if (autoUpdate) {
                    downloadUpdate(latestVersion.second)
                }
                
                isCheckingForUpdates = false
                return true
            } else {
                Log.d(TAG, "No hay actualizaciones disponibles")
                isCheckingForUpdates = false
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar actualizaciones: ${e.message}")
            isCheckingForUpdates = false
            return false
        }
    }
    
    /**
     * Obtiene la versión actual de la aplicación
     */
    private fun getCurrentVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener versión actual: ${e.message}")
            "0.0.0"
        }
    }
    
    /**
     * Obtiene la última versión disponible desde GitHub
     * Retorna un par con la versión y la URL de descarga
     */
    private suspend fun getLatestVersion(): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(githubRepoUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(response)
                    
                    val tagName = jsonObject.getString("tag_name").removePrefix("v")
                    
                    // Buscar el asset de APK
                    val assets = jsonObject.getJSONArray("assets")
                    var apkUrl: String? = null
                    
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.getString("name")
                        
                        if (name.endsWith(".apk")) {
                            apkUrl = asset.getString("browser_download_url")
                            break
                        }
                    }
                    
                    if (apkUrl != null) {
                        Pair(tagName, apkUrl)
                    } else {
                        Log.e(TAG, "No se encontró APK en la release")
                        null
                    }
                } else {
                    Log.e(TAG, "Error al obtener última versión: $responseCode")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener última versión: ${e.message}")
                null
            }
        }
    }
    
    /**
     * Compara versiones para determinar si la nueva es más reciente
     */
    private fun isNewerVersion(currentVersion: String, latestVersion: String): Boolean {
        val current = parseVersion(currentVersion)
        val latest = parseVersion(latestVersion)
        
        // Comparar componentes de versión
        for (i in 0 until minOf(current.size, latest.size)) {
            if (latest[i] > current[i]) {
                return true
            } else if (latest[i] < current[i]) {
                return false
            }
        }
        
        // Si llegamos aquí y las versiones tienen diferente longitud
        return latest.size > current.size
    }
    
    /**
     * Convierte una cadena de versión en una lista de enteros
     */
    private fun parseVersion(version: String): List<Int> {
        return version.split(".")
            .mapNotNull { it.toIntOrNull() }
    }
    
    /**
     * Descarga una actualización
     */
    private fun downloadUpdate(downloadUrl: String) {
        if (isDownloadingUpdate) {
            Log.d(TAG, "Ya se está descargando una actualización")
            return
        }
        
        isDownloadingUpdate = true
        
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            
            val uri = Uri.parse(downloadUrl)
            val request = DownloadManager.Request(uri).apply {
                setTitle("Actualización de Ritsu")
                setDescription("Descargando nueva versión")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "ritsu_update.apk")
            }
            
            downloadId = downloadManager.enqueue(request)
            Log.d(TAG, "Descarga de actualización iniciada: $downloadId")
        } catch (e: Exception) {
            Log.e(TAG, "Error al descargar actualización: ${e.message}")
            isDownloadingUpdate = false
        }
    }
    
    /**
     * Instala una actualización descargada
     */
    private fun installUpdate() {
        updaterScope.launch {
            try {
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                
                if (cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(statusIndex)
                    
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        val uriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
                        val uriString = cursor.getString(uriIndex)
                        val uri = Uri.parse(uriString)
                        
                        // Instalar APK
                        installApk(uri)
                    } else {
                        Log.e(TAG, "La descarga no se completó correctamente: $status")
                    }
                }
                
                cursor.close()
                isDownloadingUpdate = false
            } catch (e: Exception) {
                Log.e(TAG, "Error al instalar actualización: ${e.message}")
                isDownloadingUpdate = false
            }
        }
    }
    
    /**
     * Instala un APK desde un URI
     */
    private fun installApk(uri: Uri) {
        try {
            val file = File(uri.path!!)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Para Android 7.0 y superior, usar FileProvider
                val contentUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    setDataAndType(contentUri, "application/vnd.android.package-archive")
                }
                
                context.startActivity(installIntent)
            } else {
                // Para versiones anteriores
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
                }
                
                context.startActivity(installIntent)
            }
            
            Log.d(TAG, "Instalación de actualización iniciada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al instalar APK: ${e.message}")
        }
    }
    
    /**
     * Instala un APK de forma silenciosa (requiere permisos especiales)
     */
    private fun installSilently(apkFile: File): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.e(TAG, "Instalación silenciosa no soportada en esta versión de Android")
            return false
        }
        
        try {
            val packageInstaller = context.packageManager.packageInstaller
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            val sessionId = packageInstaller.createSession(params)
            val session = packageInstaller.openSession(sessionId)
            
            val inputStream = FileInputStream(apkFile)
            val outputStream = session.openWrite("package", 0, apkFile.length())
            
            copyStream(inputStream, outputStream)
            
            session.fsync(outputStream)
            inputStream.close()
            outputStream.close()
            
            // Crear intent para la instalación
            val intent = Intent(context, context.javaClass)
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                android.app.PendingIntent.getActivity(
                    context, sessionId, intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_MUTABLE
                )
            } else {
                android.app.PendingIntent.getActivity(
                    context, sessionId, intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            
            session.commit(pendingIntent.intentSender)
            
            Log.d(TAG, "Instalación silenciosa iniciada")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error en instalación silenciosa: ${e.message}")
            return false
        }
    }
    
    /**
     * Copia datos de un stream a otro
     */
    @Throws(IOException::class)
    private fun copyStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            output.write(buffer, 0, read)
        }
    }
    
    /**
     * Libera recursos cuando se destruye la instancia
     */
    fun destroy() {
        Log.d(TAG, "Destruyendo sistema de actualización automática")
        
        try {
            // Desregistrar receptor
            context.unregisterReceiver(downloadCompleteReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error al desregistrar receptor: ${e.message}")
        }
    }
}

